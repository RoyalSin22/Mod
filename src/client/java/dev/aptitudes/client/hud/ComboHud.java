package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.network.ComboPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

/**
 * Contador de combo junto a la mira (v28): «×N» que crece al subir y cambia de color (blanco, oro al tener
 * remate listo); «¡REMATE!» al rematar. Se oculta solo si no hay golpes en la ventana de combo.
 */
public final class ComboHud {
	private static int count;
	private static long updatedAt;
	private static long finisherAt;
	/** Ventana visible (ms): algo más que la ventana de combo por defecto. */
	private static final long VISIBLE_MS = 2600;

	private ComboHud() {
	}

	public static void onCombo(ComboPayload payload) {
		long now = Util.getMeasuringTimeMs();
		count = payload.count();
		updatedAt = now;
		if (payload.finisher()) finisherAt = now;
	}

	public static void clear() {
		count = 0;
		updatedAt = finisherAt = -VISIBLE_MS;
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientConfig cfg = ClientConfig.get();
		if (client.player == null || client.options.hudHidden || cfg.hudMinimal) return;
		long now = Util.getMeasuringTimeMs();
		int cx = ctx.getScaledWindowWidth() / 2;
		int cy = ctx.getScaledWindowHeight() / 2;
		long sinceFinisher = now - finisherAt;
		if (sinceFinisher < 700) {
			float t = sinceFinisher / 700f;
			float scale = cfg.hudAnimations ? 1.6f - 0.4f * t : 1f;
			int alpha = cfg.hudAnimations ? (int) (255 * (1f - t)) : 255;
			Text text = Text.translatable("hud.aptitudes.combo.finisher").formatted(Formatting.BOLD);
			draw(ctx, client, text, cx, cy - 26, scale, (Math.max(8, alpha) << 24) | 0xFF5252);
		}
		if (count < 2 || now - updatedAt > VISIBLE_MS) return;
		long since = now - updatedAt;
		float pop = cfg.hudAnimations && since < 150 ? 1f + 0.5f * (1f - since / 150f) : 1f; // crece al subir
		boolean ready = count >= 5;
		int color = ready ? 0xFFE6C36A : 0xFFFFFFFF;
		Text text = Text.literal("×" + count).formatted(Formatting.BOLD);
		draw(ctx, client, text, cx + 22, cy + 4, pop, color);
		if (ready) {
			Text hint = Text.translatable("hud.aptitudes.combo.ready");
			draw(ctx, client, hint, cx + 22, cy + 15, 0.7f, 0xC0E6C36A);
		}
	}

	private static void draw(DrawContext ctx, MinecraftClient client, Text text, int x, int y, float scale, int color) {
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y, 0);
		ctx.getMatrices().scale(scale, scale, 1f);
		int w = client.textRenderer.getWidth(text);
		ctx.drawTextWithShadow(client.textRenderer, text, -w / 2, -4, color);
		ctx.getMatrices().pop();
	}
}
