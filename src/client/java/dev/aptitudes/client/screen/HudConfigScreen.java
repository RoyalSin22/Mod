package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.client.hud.AbilityHud;
import dev.aptitudes.logic.AbilityHudLayout;
import dev.aptitudes.network.RoleSyncPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * Configuración del HUD (Mayús + O): arrastra la barra de habilidades para moverla,
 * cambia la escala y elige qué se muestra. Se guarda en {@code config/aptitudes-client.json}.
 */
public class HudConfigScreen extends Screen {
	private boolean dragging;
	private double dragX;
	private double dragY;

	public HudConfigScreen() {
		super(Text.translatable("screen.aptitudes.hud_config"));
	}

	/** Alto de una fila (botón de 18 + 2 de separación). */
	private static final int ROW = 20;
	/** Título + fila de escala + 8 interruptores + ayuda + Restablecer/Hecho. */
	private static final int CONTENT_HEIGHT = 12 + ROW + 8 * ROW + 6 + 12 + 18;
	private int titleY;
	private int scaleY;
	private int hintY;

	@Override
	protected void init() {
		ClientConfig cfg = ClientConfig.get();
		int cx = width / 2;
		// v41.1: la distribución se calcula por filas (antes eran posiciones fijas desde el centro y, al añadir
		// «Cielo del Eclipse», el texto de escala y la ayuda quedaron encima de los botones).
		int y = Math.max(4, (height - CONTENT_HEIGHT) / 2);
		titleY = y;
		y += 12;
		// Fila de escala: − [texto] +, todo en la misma fila.
		addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> scale(-0.1f)).dimensions(cx - 100, y, 20, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> scale(0.1f)).dimensions(cx + 80, y, 20, 18).build());
		scaleY = y + 5;
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.souls_bars", () -> cfg.soulsBars, v -> cfg.soulsBars = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.mechanic", () -> cfg.showMechanic, v -> cfg.showMechanic = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.damage_numbers", () -> cfg.damageNumbers, v -> cfg.damageNumbers = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.screen_effects", () -> cfg.screenEffects, v -> cfg.screenEffects = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.cast_animations", () -> cfg.castAnimations, v -> cfg.castAnimations = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.eclipse_sky", () -> cfg.eclipseSkyEffects, v -> cfg.eclipseSkyEffects = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.visible", () -> cfg.hudVisible, v -> cfg.hudVisible = v));
		y += ROW;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.tutorial", () -> cfg.showTutorial, v -> cfg.showTutorial = v));
		y += ROW + 6;
		hintY = y;
		y += 12;
		addDrawableChild(ButtonWidget.builder(Text.translatable("screen.aptitudes.hud.reset"), b -> {
			cfg.hudOffsetX = 0;
			cfg.hudOffsetY = 0;
			cfg.hudScale = 1.0f;
		}).dimensions(cx - 100, y, 98, 18).build());
		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).dimensions(cx + 2, y, 98, 18).build());
	}

	private interface Getter { boolean get(); }
	private interface Setter { void set(boolean value); }

	private ButtonWidget toggle(int x, int y, String key, Getter getter, Setter setter) {
		return ButtonWidget.builder(label(key, getter.get()), b -> {
			setter.set(!getter.get());
			b.setMessage(label(key, getter.get()));
		}).dimensions(x, y, 200, 18).build();
	}

	private static Text label(String key, boolean on) {
		return Text.translatable(key).append(": ").append(on ? ScreenTexts.ON : ScreenTexts.OFF);
	}

	private void scale(float delta) {
		ClientConfig cfg = ClientConfig.get();
		cfg.hudScale = MathHelper.clamp(Math.round((cfg.hudScale + delta) * 10f) / 10f, 0.5f, 2.0f);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		ClientConfig cfg = ClientConfig.get();
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, titleY, UiStyle.GOLD);
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.hud.scale",
				Math.round(cfg.hudScale * 100)), width / 2, scaleY, UiStyle.TEXT);
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.hud.drag"),
				width / 2, hintY, UiStyle.MUTED);
		// Vista previa de la barra con su marco de arrastre.
		RoleSyncPayload data = ClientRoleState.data();
		AbilityHudLayout preview = AbilityHud.barLayout(width, height);
		ctx.drawBorder(preview.left(), preview.top(), preview.right() - preview.left(), preview.bottom() - preview.top(),
				dragging ? UiStyle.GOLD : 0xA0FFFFFF);
		if (data != null) AbilityHud.renderAbilityBar(ctx, textRenderer, data, width, height, client.player == null ? 0 : client.player.age);
		// La previsualización arrastrable no debe tapar los botones en la altura mínima.
		super.render(ctx, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		AbilityHudLayout preview = AbilityHud.barLayout(width, height);
		if (button == 0 && preview.contains(mouseX, mouseY)) {
			dragging = true;
			dragX = mouseX - preview.x();
			dragY = mouseY - preview.y();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging) {
			ClientConfig cfg = ClientConfig.get();
			AbilityHudLayout anchor = AbilityHudLayout.place(width, height, AbilityHud.barWidth(), cfg.hudScale, 0, 0);
			cfg.hudOffsetX = (int) Math.clamp(Math.round(mouseX - dragX - anchor.x()), -4096, 4096);
			cfg.hudOffsetY = (int) Math.clamp(Math.round(mouseY - dragY - anchor.y()), -4096, 4096);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
