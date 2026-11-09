package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientTutorial;
import dev.aptitudes.client.screen.UiStyle;
import dev.aptitudes.logic.HudMotion;
import dev.aptitudes.network.ThreatPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/** Aviso breve al cambiar de anillo, fuera de la barra de acción y de la franja del Eclipse. */
public final class ThreatHud {
	private static int ring = -1;
	private static final long VISIBLE_MS = 4000;
	private static long started;

	private ThreatHud() {
	}

	public static void onPayload(ThreatPayload payload) {
		ring = payload.ring();
		started = Util.getMeasuringTimeMs();
	}

	public static void clear() { ring = -1; started = 0; }

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientConfig cfg = ClientConfig.get();
		if (ring < 0 || !cfg.hudVisible || client.player == null
				|| client.options.hudHidden || client.currentScreen != null || !client.player.isAlive()
				|| client.player.isCreative() || client.player.isSpectator()
				|| client.world == null || client.world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) return;
		float opacity = HudMotion.notice(Util.getMeasuringTimeMs() - started, VISIBLE_MS, cfg.hudAnimations);
		int alpha = Math.round(255 * opacity);
		if (alpha < 8) return;
		Text text = Text.translatable("threat.aptitudes.hud", Text.translatable("threat.aptitudes.ring." + ring));
		int available = Math.min(180, ctx.getScaledWindowWidth() / 2 - 60);
		if (available < 60 || ctx.getScaledWindowHeight() < 220) return;
		int width = Math.min(available, Math.max(60, client.textRenderer.getWidth(text) + 10));
		int x = ctx.getScaledWindowWidth() - width - 8, y = ctx.getScaledWindowHeight() - 126;
		var tutorial = ClientTutorial.view();
		if (!cfg.hudMinimal && cfg.showTutorial && tutorial != null && tutorial.visible()) {
			int tutorialY = TutorialHud.top(cfg, ctx.getScaledWindowHeight());
			if (y < tutorialY + 40 && y + 18 > tutorialY - 4) y = tutorialY - 22;
		}
		ctx.fill(x, y, x + width, y + 18, (Math.round(160 * opacity) << 24) | 0x181212);
		ctx.drawTextWithShadow(client.textRenderer, UiStyle.fit(client.textRenderer, text, width - 10),
				x + 5, y + 4, (alpha << 24) | 0xE3A66C);
	}
}
