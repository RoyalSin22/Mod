package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.client.hud.AbilityHud;
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

	public HudConfigScreen() {
		super(Text.translatable("screen.aptitudes.hud_config"));
	}

	@Override
	protected void init() {
		ClientConfig cfg = ClientConfig.get();
		int cx = width / 2;
		int y = height / 2 - 80;
		addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> scale(-0.1f)).dimensions(cx - 100, y, 20, 18).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> scale(0.1f)).dimensions(cx + 80, y, 20, 18).build());
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.souls_bars", () -> cfg.soulsBars, v -> cfg.soulsBars = v));
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.mechanic", () -> cfg.showMechanic, v -> cfg.showMechanic = v));
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.damage_numbers", () -> cfg.damageNumbers, v -> cfg.damageNumbers = v));
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.screen_effects", () -> cfg.screenEffects, v -> cfg.screenEffects = v));
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.eclipse_sky", () -> cfg.eclipseSkyEffects, v -> cfg.eclipseSkyEffects = v));
		y += 22;
		addDrawableChild(toggle(cx - 100, y, "screen.aptitudes.hud.visible", () -> cfg.hudVisible, v -> cfg.hudVisible = v));
		y += 26;
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
		super.render(ctx, mouseX, mouseY, delta);
		ClientConfig cfg = ClientConfig.get();
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 92, UiStyle.GOLD);
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.hud.scale",
				Math.round(cfg.hudScale * 100)), width / 2, height / 2 - 65, UiStyle.TEXT);
		ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.hud.drag"),
				width / 2, height / 2 + 82, UiStyle.MUTED);
		// Vista previa de la barra con su marco de arrastre.
		RoleSyncPayload data = ClientRoleState.data();
		int[] o = AbilityHud.barOrigin(width, height);
		int w = (int) (AbilityHud.barWidth() * cfg.hudScale);
		int h = (int) (24 * cfg.hudScale);
		ctx.drawBorder(o[0] - 3, o[1] - (int) (32 * cfg.hudScale), w + 6, h + (int) (32 * cfg.hudScale),
				dragging ? UiStyle.GOLD : 0xA0FFFFFF);
		if (data != null) AbilityHud.renderAbilityBar(ctx, textRenderer, data, width, height, client.player == null ? 0 : client.player.age);
	}

	private boolean overBar(double mx, double my) {
		ClientConfig cfg = ClientConfig.get();
		int[] o = AbilityHud.barOrigin(width, height);
		int w = (int) (AbilityHud.barWidth() * cfg.hudScale);
		return mx >= o[0] - 3 && mx < o[0] + w + 3 && my >= o[1] - 32 * cfg.hudScale && my < o[1] + 24 * cfg.hudScale;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		if (overBar(mouseX, mouseY)) {
			dragging = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging) {
			ClientConfig cfg = ClientConfig.get();
			cfg.hudOffsetX += (int) Math.round(deltaX);
			cfg.hudOffsetY += (int) Math.round(deltaY);
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
