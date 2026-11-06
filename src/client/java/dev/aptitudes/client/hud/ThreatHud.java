package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientTutorial;
import dev.aptitudes.client.screen.UiStyle;
import dev.aptitudes.network.ThreatPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/** Aviso breve al cambiar de anillo, fuera de la barra de acción y de la franja del Eclipse. */
public final class ThreatHud {
	private static int ring = -1;
	private static long until;

	private ThreatHud() {
	}

	public static void onPayload(ThreatPayload payload) {
		ring = payload.ring();
		until = ring < 0 ? 0 : Util.getMeasuringTimeMs() + 4000;
	}

	public static void clear() { ring = -1; until = 0; }

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientConfig cfg = ClientConfig.get();
		if (ring < 0 || Util.getMeasuringTimeMs() >= until || !cfg.hudVisible || client.player == null
				|| client.options.hudHidden || client.currentScreen != null || !client.player.isAlive()
				|| client.player.isCreative() || client.player.isSpectator()
				|| client.world == null || client.world.getRegistryKey() != net.minecraft.world.World.OVERWORLD) return;
		Text text = Text.translatable("threat.aptitudes.hud", Text.translatable("threat.aptitudes.ring." + ring));
		int available = Math.min(180, ctx.getScaledWindowWidth() / 2 - 60);
		if (available < 60 || ctx.getScaledWindowHeight() < 220) return;
		int width = Math.min(available, Math.max(60, client.textRenderer.getWidth(text) + 10));
		int x = ctx.getScaledWindowWidth() - width - 8, y = ctx.getScaledWindowHeight() - 126;
		var tutorial = ClientTutorial.view();
		if (cfg.showTutorial && tutorial != null && tutorial.visible()) {
			float scale = Float.isFinite(cfg.hudScale) ? Math.max(0.5f, Math.min(2, cfg.hudScale)) : 1;
			int tutorialY = Math.min(ctx.getScaledWindowHeight() - 36,
					cfg.soulsBars ? Math.max(58, 6 + (int) Math.ceil(46 * scale)) : 58);
			if (y < tutorialY + 40 && y + 18 > tutorialY - 4) y = tutorialY - 22;
		}
		ctx.fill(x, y, x + width, y + 18, 0xA0181212);
		ctx.drawTextWithShadow(client.textRenderer, UiStyle.fit(client.textRenderer, text, width - 10), x + 5, y + 4, 0xFFE3A66C);
	}
}
