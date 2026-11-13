package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.client.hud.AbilityHud;
import dev.aptitudes.logic.AbilityHudLayout;
import dev.aptitudes.logic.HudPreset;
import dev.aptitudes.logic.KScreenLayout;
import dev.aptitudes.logic.TreeScreenLayout;
import dev.aptitudes.network.RoleSyncPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Editor local del HUD (Mayús + O), con vista previa arrastrable y dos pestañas. */
public class HudConfigScreen extends Screen {
	private static final String HUD = "screen.aptitudes.hud.";
	private static final int TOP = 6;
	/** Con el kit de la pantalla K: la ventana baja 8 px y gana margen para su marco de 12 px. */
	private boolean kit;
	private static final int ROW = 21;
	private static final int BUTTON = 18;
	private static final int GAP = 4;

	private boolean indicators;
	private boolean dragging;
	private double dragX;
	private double dragY;
	private int panelWidth;
	private int panelX;
	private int panelBottom;

	public HudConfigScreen() {
		super(Text.translatable("screen.aptitudes.hud_config"));
	}

	@Override
	protected void init() {
		ClientConfig cfg = ClientConfig.get();
		kit = KUi.available();
		panelWidth = Math.min(364, width - (kit ? 32 : 12));
		panelX = (width - panelWidth) / 2;
		int half = (panelWidth - GAP) / 2;
		int right = panelX + half + GAP;
		int y = top() + 14;
		tab(panelX, y, half, "layout", false);
		tab(right, y, half, "indicators", true);
		y += ROW + (kit ? KScreenLayout.TAB_H - BUTTON + 2 : 0);

		if (indicators) {
			toggle(panelX, y, half, "minimal", () -> cfg.hudMinimal, v -> cfg.hudMinimal = v);
			toggle(right, y, half, "animations", () -> cfg.hudAnimations, v -> cfg.hudAnimations = v);
			y += ROW;
			toggle(panelX, y, half, "souls_bars", () -> cfg.soulsBars, v -> cfg.soulsBars = v);
			toggle(right, y, half, "mechanic", () -> cfg.showMechanic, v -> cfg.showMechanic = v);
			y += ROW;
			toggle(panelX, y, half, "damage_numbers", () -> cfg.damageNumbers, v -> cfg.damageNumbers = v);
			toggle(right, y, half, "screen_effects", () -> cfg.screenEffects, v -> cfg.screenEffects = v);
			y += ROW;
			toggle(panelX, y, half, "cast_animations", () -> cfg.castAnimations, v -> cfg.castAnimations = v);
			toggle(right, y, half, "eclipse_sky", () -> cfg.eclipseSkyEffects, v -> cfg.eclipseSkyEffects = v);
			y += ROW;
			toggle(panelX, y, half, "visible", () -> cfg.hudVisible, v -> cfg.hudVisible = v);
			toggle(right, y, half, "tutorial", () -> cfg.showTutorial, v -> cfg.showTutorial = v);
			y += ROW;
			button(panelX, y, panelWidth, ScreenTexts.DONE, b -> close());
			panelBottom = y + BUTTON + GAP + (kit ? 8 : 0);
			return;
		}

		int third = (panelWidth - 2 * GAP) / 3;
		int x = panelX;
		for (HudPreset preset : HudPreset.values()) {
			Text name = Text.translatable(HUD + "preset." + preset.id());
			boolean selected = cfg.preset() == preset;
			ButtonWidget presetButton = button(x, y, third, selected ? Text.literal("✓ ").append(name) : name,
					kit && selected ? "confirmar" : "cerrar", b -> {
						if (cfg.preset() == preset) return;
						cfg.hudPreset = preset.id();
						cfg.hudOffsetX = 0;
						cfg.hudOffsetY = 0;
						clearAndInit();
					});
			// Sin el kit, el preset elegido se muestra desactivado; con el kit se distingue por su pieza.
			if (!kit) presetButton.active = !selected;
			x += third + GAP;
		}
		y += ROW;
		button(width / 2 - 100, y, 20, Text.literal("−"), b -> scale(-1));
		button(width / 2 + 80, y, 20, Text.literal("+"), b -> scale(1));
		y += ROW;
		button(panelX, y, half, Text.translatable(HUD + "reset"), b -> {
			cfg.hudPreset = HudPreset.CLASSIC.id();
			cfg.hudOffsetX = 0;
			cfg.hudOffsetY = 0;
			cfg.hudScale = 1.0f;
			clearAndInit();
		});
		button(right, y, half, ScreenTexts.DONE, b -> close());
		panelBottom = y + ROW + 13 + (kit ? 8 : 0);
	}

	private int top() {
		return kit ? TOP + 8 : TOP;
	}

	private void tab(int x, int y, int w, String key, boolean next) {
		if (kit) {
			Text label = Text.translatable(HUD + key);
			addDrawableChild(new KTab(x, y, w, UiStyle.fit(textRenderer, label, w - 2 * KScreenLayout.TAB_PAD),
					() -> indicators == next, b -> {
						indicators = next;
						dragging = false;
						clearAndInit();
					}));
			return;
		}
		button(x, y, w, Text.translatable(HUD + key), b -> {
			indicators = next;
			dragging = false;
			clearAndInit();
		}).active = indicators != next;
	}

	private ButtonWidget button(int x, int y, int w, Text label, ButtonWidget.PressAction action) {
		return button(x, y, w, label, "cerrar", action);
	}

	/** Botón del kit (pieza {@code sprite}) o el vanilla de respaldo. */
	private ButtonWidget button(int x, int y, int w, Text label, String sprite, ButtonWidget.PressAction action) {
		Text fitted = UiStyle.fit(textRenderer, label, w - 10);
		ButtonWidget widget = kit ? new KButton(x, y, w, fitted, sprite, action)
				: ButtonWidget.builder(fitted, action).dimensions(x, y, w, BUTTON).build();
		widget.setTooltip(Tooltip.of(label));
		return addDrawableChild(widget);
	}

	/** Interruptor: con el kit, encendido usa la pieza «confirmar» (con brasa) y apagado la neutra «cerrar». */
	private void toggle(int x, int y, int w, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
		button(x, y, w, label(key, getter.getAsBoolean()), onOff(getter.getAsBoolean()), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			Text label = label(key, next);
			b.setMessage(UiStyle.fit(textRenderer, label, w - 10));
			b.setTooltip(Tooltip.of(label));
			if (b instanceof KButton k) k.setBase(onOff(next));
		});
	}

	private static String onOff(boolean on) {
		return on ? "confirmar" : "cerrar";
	}

	/** Pestaña con las piezas del kit de la pantalla K (activa, hover e inactiva), de {@link KScreenLayout#TAB_H} px. */
	private static final class KTab extends ButtonWidget {
		private final BooleanSupplier current;

		KTab(int x, int y, int w, Text label, BooleanSupplier current, PressAction action) {
			super(x, y, w, KScreenLayout.TAB_H, label, action, DEFAULT_NARRATION_SUPPLIER);
			this.current = current;
		}

		@Override
		protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
			boolean selected = current.getAsBoolean();
			KUi.tab(ctx, selected ? "activa" : isHovered() || isFocused() ? "hover" : "inactiva",
					new TreeScreenLayout.Rect(getX(), getY(), getWidth(), getHeight()));
			var tr = net.minecraft.client.MinecraftClient.getInstance().textRenderer;
			ctx.drawCenteredTextWithShadow(tr, getMessage(), getX() + getWidth() / 2, getY() + (selected ? 7 : 8),
					selected ? UiStyle.GOLD : isHovered() ? UiStyle.TEXT : UiStyle.MUTED);
		}
	}

	private static Text label(String key, boolean on) {
		return Text.translatable(HUD + key).append(" · ").append(on ? ScreenTexts.ON : ScreenTexts.OFF);
	}

	private void scale(int step) {
		ClientConfig cfg = ClientConfig.get();
		cfg.hudScale = Math.clamp(Math.round(cfg.hudScale * 10f) + step, 5, 20) / 10f;
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta);
		ClientConfig cfg = ClientConfig.get();
		if (kit) {
			KUi.mainPanel(ctx, false, new TreeScreenLayout.Rect(panelX - 12, 2, panelWidth + 24, panelBottom - 2));
		} else {
			UiStyle.window(ctx, panelX - GAP, TOP - GAP, panelWidth + 2 * GAP, panelBottom - TOP + GAP);
		}
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, top(), UiStyle.GOLD);
		if (!indicators) {
			Text scale = Text.translatable(HUD + "scale", Math.round(cfg.hudScale * 100));
			int tabsExtra = kit ? KScreenLayout.TAB_H - BUTTON + 2 : 0;
			ctx.drawCenteredTextWithShadow(textRenderer, scale, width / 2, top() + 14 + ROW * 2 + 5 + tabsExtra, UiStyle.TEXT);
			Text hint = UiStyle.fit(textRenderer, Text.translatable(HUD + "drag"), panelWidth);
			ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, panelBottom - 12 - (kit ? 8 : 0), UiStyle.MUTED);
		}
		if (indicators || panelBottom >= height) return;
		// El desenfoque de Minecraft ya se aplicó y la vista previa queda fuera de los controles.
		ctx.enableScissor(0, panelBottom, width, height);
		AbilityHudLayout preview = AbilityHud.barLayout(width, height);
		ctx.drawBorder(preview.left(), preview.top(), preview.right() - preview.left(), preview.bottom() - preview.top(),
				dragging ? UiStyle.GOLD : 0xA0FFFFFF);
		RoleSyncPayload data = ClientRoleState.data();
		if (data != null) AbilityHud.renderAbilityBar(ctx, textRenderer, data, width, height, client.player == null ? 0 : client.player.age);
		ctx.disableScissor();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		if (indicators || mouseY < panelBottom) return false;
		AbilityHudLayout preview = AbilityHud.barLayout(width, height);
		if (button != 0 || !preview.contains(mouseX, mouseY)) return false;
		dragging = true;
		dragX = mouseX - preview.x();
		dragY = mouseY - preview.y();
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (!dragging) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		ClientConfig cfg = ClientConfig.get();
		AbilityHudLayout anchor = AbilityHud.barLayout(width, height, 0, 0);
		cfg.hudOffsetX = (int) Math.clamp(Math.round(mouseX - dragX - anchor.x()), -4096, 4096);
		cfg.hudOffsetY = (int) Math.clamp(Math.round(mouseY - dragY - anchor.y()), -4096, 4096);
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void close() {
		ClientConfig.save();
		super.close();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
