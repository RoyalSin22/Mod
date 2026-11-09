package dev.aptitudes.client.hud;

import dev.aptitudes.client.screen.NodeIconRenderer;
import dev.aptitudes.network.KeystonePayload;
import dev.aptitudes.role.KeystoneRules;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.List;

/**
 * Fila de piedras angulares encima de las mecánicas de rol. Icono oscurecido mientras está en espera;
 * borde dorado que late cuando está
 * preparada (Espectro tras esquivar, Sobrecarga en la habilidad gratuita). Solo muestra datos del servidor.
 */
public final class KeystoneHud {
	private static List<KeystonePayload.Entry> entries = List.of();
	private static final int SIZE = 18;
	private static final int ROW_Y = -47;

	private KeystoneHud() {
	}

	public static void onPayload(KeystonePayload payload) {
		entries = payload.entries();
	}

	static void clear() {
		entries = List.of();
	}

	static int occupiedAbove() {
		return entries.isEmpty() ? 0 : -ROW_Y;
	}

	static void draw(DrawContext ctx) {
		int x = 0;
		int y = ROW_Y;
		KeystoneRules.Template[] all = KeystoneRules.Template.values();
		long now = Util.getMeasuringTimeMs();
		for (KeystonePayload.Entry e : entries) {
			if (e.template() < 0 || e.template() >= all.length) continue;
			ctx.fill(x, y, x + SIZE, y + SIZE, 0xC0101018);
			NodeIconRenderer.drawKeystoneTemplate(ctx, all[e.template()], x + 1, y + 1);
			// drawItem dibuja más arriba en Z: la sombra y el borde van por encima del icono.
			ctx.getMatrices().push();
			ctx.getMatrices().translate(0, 0, 200);
			float progress = Math.max(0f, Math.min(1f, e.progress()));
			if (progress < 1f) {
				int shade = Math.round((SIZE - 2) * (1f - progress));
				ctx.fill(x + 1, y + 1, x + SIZE - 1, y + 1 + shade, 0xA0000000);
			}
			int border = 0xFF7E57C2;
			if (e.armed()) {
				int alpha = 150 + (int) (105 * (0.5 + 0.5 * Math.sin(now / 150.0)));
				border = (alpha << 24) | 0xE6C36A;
			}
			ctx.drawBorder(x, y, SIZE, SIZE, border);
			ctx.getMatrices().pop();
			x += SIZE + 2;
		}
	}
}
