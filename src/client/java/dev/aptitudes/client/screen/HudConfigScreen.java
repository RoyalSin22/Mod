package dev.aptitudes.client.screen;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.client.hud.AbilityHud;
import dev.aptitudes.logic.AbilityHudLayout;
import dev.aptitudes.logic.HudPreset;
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
		panelWidth = Math.min(364, width - 12);
		panelX = (width - panelWidth) / 2;
		int half = (panelWidth - GAP) / 2;
		int right = panelX + half + GAP;
		int y = TOP + 14;
		tab(panelX, y, half, "layout", false);
		tab(right, y, half, "indicators", true);
		y += ROW;

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
			panelBottom = y + BUTTON + GAP;
			return;
		}

		int third = (panelWidth - 2 * GAP) / 3;
		int x = panelX;
		for (HudPreset preset : HudPreset.values()) {
			Text name = Text.translatable(HUD + "preset." + preset.id());
			boolean selected = cfg.preset() == preset;
			button(x, y, third, selected ? Text.literal("✓ ").append(name) : name, b -> {
				cfg.hudPreset = preset.id();
				cfg.hudOffsetX = 0;
				cfg.hudOffsetY = 0;
				clearAndInit();
			}).active = !selected;
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
		panelBottom = y + ROW + 13;
	}

	private void tab(int x, int y, int w, String key, boolean next) {
		button(x, y, w, Text.translatable(HUD + key), b -> {
			indicators = next;
			dragging = false;
			clearAndInit();
		}).active = indicators != next;
	}

	private ButtonWidget button(int x, int y, int w, Text label, ButtonWidget.PressAction action) {
		return addDrawableChild(ButtonWidget.builder(UiStyle.fit(textRenderer, label, w - 10), action)
				.dimensions(x, y, w, BUTTON).tooltip(Tooltip.of(label)).build());
	}

	private void toggle(int x, int y, int w, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
		button(x, y, w, label(key, getter.getAsBoolean()), b -> {
			boolean next = !getter.getAsBoolean();
			setter.accept(next);
			Text label = label(key, next);
			b.setMessage(UiStyle.fit(textRenderer, label, w - 10));
			b.setTooltip(Tooltip.of(label));
		});
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
		UiStyle.window(ctx, panelX - GAP, TOP - GAP, panelWidth + 2 * GAP, panelBottom - TOP + GAP);
		ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, TOP, UiStyle.GOLD);
		if (!indicators) {
			Text scale = Text.translatable(HUD + "scale", Math.round(cfg.hudScale * 100));
			ctx.drawCenteredTextWithShadow(textRenderer, scale, width / 2, TOP + 14 + ROW * 2 + 5, UiStyle.TEXT);
			Text hint = UiStyle.fit(textRenderer, Text.translatable(HUD + "drag"), panelWidth);
			ctx.drawCenteredTextWithShadow(textRenderer, hint, width / 2, panelBottom - 12, UiStyle.MUTED);
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
