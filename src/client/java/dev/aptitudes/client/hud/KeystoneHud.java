package dev.aptitudes.client.hud;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.network.KeystonePayload;
import dev.aptitudes.role.KeystoneRules;
import dev.aptitudes.role.NodeIcons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Util;

import java.util.List;

/**
 * Indicador de las piedras angulares (v46.1): un icono por piedra angular activa, encima del extremo izquierdo de
 * la barra de habilidades. Oscurecido de arriba abajo mientras está en espera; borde dorado que late cuando está
 * preparada (Espectro tras esquivar, Sobrecarga en la habilidad gratuita). Solo muestra datos del servidor.
 */
public final class KeystoneHud {
	private static List<KeystonePayload.Entry> entries = List.of();
	private static final int SIZE = 18;

	private KeystoneHud() {
	}

	public static void onPayload(KeystonePayload payload) {
		entries = payload.entries();
	}

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden || entries.isEmpty() || !ClientConfig.get().hudVisible) return;
		int[] origin = AbilityHud.barOrigin(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
		int x = origin[0];
		int y = Math.max(2, origin[1] - SIZE - 4);
		KeystoneRules.Template[] all = KeystoneRules.Template.values();
		long now = Util.getMeasuringTimeMs();
		for (KeystonePayload.Entry e : entries) {
			if (e.template() < 0 || e.template() >= all.length) continue;
			ctx.fill(x, y, x + SIZE, y + SIZE, 0xC0101018);
			ctx.drawItem(NodeIcons.keystone(all[e.template()]), x + 1, y + 1);
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
