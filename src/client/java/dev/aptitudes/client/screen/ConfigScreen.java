package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientServerConfig;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.config.ConfigTooltipRules;
import dev.aptitudes.config.ConfigFieldRules;
import dev.aptitudes.network.ConfigEditPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * v40: pantalla de configuración. Lista todas las opciones del servidor (con buscador y desplazamiento) y da
 * acceso a las opciones del cliente (HUD y efectos). Un operador puede cambiar valores: el cambio se envía al
 * servidor, que lo valida, lo guarda y reenvía la configuración a todos. Sin permiso, se ve en solo lectura.
 * Se abre con {@code /aptitudes config}, desde la pantalla K o desde Mod Menu.
 */
public class ConfigScreen extends Screen {
	private static final int ROW_H = 20;
	private final Screen parent;
	private final List<Field> allFields = ConfigFieldRules.editableFields(AptitudesConfig.class);
	/** Textos sin enviar de cada campo (se conservan al desplazar o filtrar). */
	private final Map<String, String> drafts = new HashMap<>();
	private TextFieldWidget search;
	private String query = "";
	private int scroll;
	/** Valores por defecto, para mostrarlos en el tooltip. */
	private static final AptitudesConfig DEFAULTS = new AptitudesConfig();
	private int seenRevision;
	private int left, top, w, h;

	public ConfigScreen(Screen parent) {
		super(Text.translatable("screen.aptitudes.config"));
		this.parent = parent;
	}

	private List<Field> filtered() {
		String q = query.trim().toLowerCase(Locale.ROOT);
		if (q.isEmpty()) return allFields;
		List<Field> out = new ArrayList<>();
		// v67: también se busca en la descripción de cada opción (en el idioma actual).
		for (Field f : allFields) {
			if (ConfigTooltipRules.matches(q, f.getName(), I18n.translate(ConfigTooltipRules.descriptionKey(f.getName())))) out.add(f);
		}
		return out;
	}

	private int visibleRows() {
		return Math.max(1, (h - 78) / ROW_H);
	}

	@Override
	protected void init() {
		seenRevision = ClientServerConfig.revision();
		w = MathHelper.clamp(width - 20, 300, 440);
		h = MathHelper.clamp(height - 16, 200, 320);
		left = (width - w) / 2;
		top = (height - h) / 2;

		search = new TextFieldWidget(textRenderer, left + 10, top + 24, w - 130, 16, Text.translatable("screen.aptitudes.config.search"));
		search.setText(query);
		search.setPlaceholder(Text.translatable("screen.aptitudes.config.search").formatted(Formatting.DARK_GRAY));
		search.setChangedListener(s -> {
			if (!s.equals(query)) {
				query = s;
				scroll = 0;
				clearAndInit();
				setFocused(search);
				search.setFocused(true);
			}
		});
		addDrawableChild(search);
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.config.client"),
				b -> client.setScreen(new HudConfigScreen())).dimensions(left + w - 114, top + 23, 104, 18).build());

		List<Field> fields = filtered();
		scroll = MathHelper.clamp(scroll, 0, Math.max(0, fields.size() - visibleRows()));
		AptitudesConfig cfg = ClientServerConfig.get();
		boolean editable = ClientServerConfig.canEdit();
		int y = top + 48;
		int valueX = left + w / 2 + 10;
		int valueW = w / 2 - 50;
		for (int i = scroll; i < Math.min(fields.size(), scroll + visibleRows()); i++) {
			Field f = fields.get(i);
			Object value = read(f, cfg);
			if (f.getType() == boolean.class) {
				boolean on = Boolean.TRUE.equals(value);
				ButtonWidget toggle = ButtonWidget.builder(on ? ScreenTexts.ON : ScreenTexts.OFF,
						b -> send(f.getName(), Boolean.toString(!on))).dimensions(valueX, y, valueW + 34, 18).build();
				toggle.active = editable;
				addDrawableChild(toggle);
			} else {
				TextFieldWidget field = new TextFieldWidget(textRenderer, valueX, y + 1, valueW, 16, Text.literal(f.getName()));
				field.setMaxLength(ConfigFieldRules.MAX_STRING);
				field.setText(drafts.getOrDefault(f.getName(), ConfigFieldRules.format(value)));
				field.setEditable(editable);
				field.setChangedListener(s -> drafts.put(f.getName(), s));
				addDrawableChild(field);
				ButtonWidget apply = ButtonWidget.builder(Text.literal("✔"), b -> {
					String text = drafts.getOrDefault(f.getName(), field.getText());
					send(f.getName(), text);
				}).dimensions(valueX + valueW + 4, y, 30, 18).build();
				apply.active = editable;
				addDrawableChild(apply);
			}
			y += ROW_H;
		}
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
				.dimensions(width / 2 - 50, top + h - 24, 100, 18).build());
	}

	private static Object read(Field f, AptitudesConfig cfg) {
		try {
			return f.get(cfg);
		} catch (IllegalAccessException e) {
			return "?";
		}
	}

	private void send(String field, String value) {
		if (!ClientServerConfig.canEdit() || !ClientPlayNetworking.canSend(ConfigEditPayload.ID)) return;
		try {
			ConfigFieldRules.parse(fieldType(field), value); // aviso inmediato en el cliente; el servidor vuelve a validar
		} catch (IllegalArgumentException e) {
			if (client.player != null) {
				client.player.sendMessage(Text.translatable("aptitudes.config.invalid", field, value).formatted(Formatting.RED), true);
			}
			return;
		}
		drafts.remove(field);
		ClientPlayNetworking.send(new ConfigEditPayload(field, value));
	}

	private Class<?> fieldType(String name) {
		for (Field f : allFields) if (f.getName().equals(name)) return f.getType();
		return Void.class;
	}

	@Override
	public void tick() {
		super.tick();
		// Llegó una configuración nueva del servidor: refresca los valores mostrados.
		if (seenRevision != ClientServerConfig.revision()) {
			boolean searching = search != null && search.isFocused();
			clearAndInit();
			if (searching) {
				setFocused(search);
				search.setFocused(true);
			}
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int max = Math.max(0, filtered().size() - visibleRows());
		int target = MathHelper.clamp(scroll + (verticalAmount > 0 ? -1 : 1) * 3, 0, max);
		if (verticalAmount != 0 && target != scroll) {
			scroll = target;
			clearAndInit();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// Enter en un campo de texto: envía ese valor.
		if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && getFocused() instanceof TextFieldWidget field
				&& field != search) {
			String name = field.getMessage().getString();
			send(name, field.getText());
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, w, h);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawTextWithShadow(textRenderer, title.copy().formatted(Formatting.BOLD), left + 12, top + 8, UiStyle.GOLD);
		Text mode = !ClientServerConfig.isSynced() ? Text.translatable("screen.aptitudes.config.local")
				: ClientServerConfig.canEdit() ? Text.translatable("screen.aptitudes.config.editable")
				: Text.translatable("screen.aptitudes.config.readonly");
		ctx.drawText(textRenderer, mode, left + w - 12 - textRenderer.getWidth(mode), top + 8, UiStyle.MUTED, false);

		List<Field> fields = filtered();
		int y = top + 48;
		for (int i = scroll; i < Math.min(fields.size(), scroll + visibleRows()); i++) {
			Field f = fields.get(i);
			if ((i & 1) == 0) ctx.fill(left + 8, y - 1, left + w - 8, y + ROW_H - 1, UiStyle.ROW_ALT);
			UiStyle.label(ctx, textRenderer, Text.literal(f.getName()), left + 12, y + 5, w / 2 - 6, UiStyle.TEXT);
			y += ROW_H;
		}
		// v67: descripción y valor por defecto al pasar el ratón por el nombre de una opción.
		int labelRight = left + w / 2 + 4;
		if (mouseX >= left + 8 && mouseX < labelRight && mouseY >= top + 48) {
			int row = (mouseY - (top + 48)) / ROW_H;
			int index = scroll + row;
			if (row < visibleRows() && index < fields.size()) renderFieldTooltip(ctx, fields.get(index), mouseX, mouseY);
		}
		if (fields.isEmpty()) {
			ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.config.none"), width / 2, top + 60, UiStyle.MUTED);
		}
		int max = Math.max(0, fields.size() - visibleRows());
		if (max > 0) { // barra de desplazamiento de la lista
			int trackY = top + 48, trackH = visibleRows() * ROW_H;
			int thumbH = Math.max(12, trackH * visibleRows() / fields.size());
			int thumbY = trackY + (trackH - thumbH) * scroll / max;
			ctx.fill(left + w - 6, trackY, left + w - 4, trackY + trackH, 0x60000000);
			ctx.fill(left + w - 6, thumbY, left + w - 4, thumbY + thumbH, UiStyle.GOLD);
		}
		ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.config.hint"), left + 10, top + h - 38, UiStyle.MUTED, false);
	}

	private void renderFieldTooltip(DrawContext ctx, Field f, int mouseX, int mouseY) {
		List<OrderedText> lines = new ArrayList<>();
		lines.add(Text.literal(f.getName()).formatted(Formatting.GOLD, Formatting.BOLD).asOrderedText());
		Text description = Text.translatable(ConfigTooltipRules.descriptionKey(f.getName()));
		lines.addAll(textRenderer.wrapLines(description, ConfigTooltipRules.MAX_TOOLTIP_WIDTH));
		lines.add(Text.translatable("screen.aptitudes.config.default", ConfigFieldRules.format(read(f, DEFAULTS)))
				.formatted(Formatting.GRAY).asOrderedText());
		ctx.drawOrderedTooltip(textRenderer, lines, mouseX, mouseY);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}
}
