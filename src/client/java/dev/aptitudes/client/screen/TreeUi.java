package dev.aptitudes.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import dev.aptitudes.role.RoleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Piezas de la pantalla del árbol de la dirección de arte (v66, generadas por {@code tools/textures/gen_tree_ui.py}).
 * Se dibujan a su tamaño real, sin escalar, para conservar el píxel lógico de 2×2. Las esquinas de los paneles no se
 * escalan y los tramos centrales se repiten (nunca se estiran ni se suavizan).
 */
final class TreeUi {
	private static final String BASE = "textures/gui/tree_ui/";
	/** Esquina del 9-slice: 12 px desde cada borde (según el kit). */
	private static final int CORNER = 12;
	static final int PANEL_MAIN_W = 192, PANEL_MAIN_H = 128;
	static final int PANEL_DETAIL_W = 112, PANEL_DETAIL_H = 160;

	private TreeUi() {
	}

	static Identifier id(String path) {
		return Aptitudes.id(BASE + path);
	}

	/** El paquete de recursos trae las piezas del árbol; si no, se usa la pantalla anterior. */
	static boolean available() {
		var manager = MinecraftClient.getInstance().getResourceManager();
		return manager.getResource(id("paneles/panel_principal_9slice.png")).isPresent()
				&& manager.getResource(id("paneles/panel_detalle_9slice.png")).isPresent()
				&& manager.getResource(id("botones/generico_normal.png")).isPresent()
				&& manager.getResource(id("node/marcial_comprado.png")).isPresent();
	}

	static String categoryKey(RoleCategory category) {
		return switch (category) {
			case MARTIAL -> "marcial";
			case ARCANE -> "arcano";
			case SHADOW -> "sombra";
			case GUARDIAN -> "guardian";
			case CRAFT -> "oficio";
		};
	}

	private static void blit(DrawContext ctx, Identifier texture, int x, int y, int u, int v, int w, int h, int texW,
							 int texH) {
		if (w > 0 && h > 0) ctx.drawTexture(texture, x, y, (float) u, (float) v, w, h, texW, texH);
	}

	/** Dibuja una pieza completa en (x, y) con su tamaño real. */
	static void sprite(DrawContext ctx, String path, int x, int y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			blit(ctx, id(path), x, y, 0, 0, w, h, w, h);
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Marco de un nodo (24×24) según la categoría del rol y su estado. */
	static void node(DrawContext ctx, RoleCategory category, String state, int x, int y) {
		sprite(ctx, "node/" + categoryKey(category) + "_" + state + ".png", x, y, 24, 24);
	}

	/**
	 * Panel de 9 partes: las cuatro esquinas de 12×12 px se copian sin escalar y los bordes y el centro se rellenan
	 * repitiendo el tramo central de la pieza.
	 */
	static void panel9(DrawContext ctx, String path, int texW, int texH, Rect r) {
		int c = CORNER;
		int w = r.w(), h = r.h(), x = r.x(), y = r.y();
		if (w < 2 * c || h < 2 * c) return;
		Identifier t = id(path);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			int midW = texW - 2 * c, midH = texH - 2 * c;
			// centro (repetido en las dos direcciones)
			for (int oy = 0; oy < h - 2 * c; oy += midH) {
				int ph = Math.min(midH, h - 2 * c - oy);
				for (int ox = 0; ox < w - 2 * c; ox += midW) {
					blit(ctx, t, x + c + ox, y + c + oy, c, c, Math.min(midW, w - 2 * c - ox), ph, texW, texH);
				}
			}
			// bordes superior e inferior (repetidos en horizontal) y laterales (en vertical)
			for (int ox = 0; ox < w - 2 * c; ox += midW) {
				int pw = Math.min(midW, w - 2 * c - ox);
				blit(ctx, t, x + c + ox, y, c, 0, pw, c, texW, texH);
				blit(ctx, t, x + c + ox, y + h - c, c, texH - c, pw, c, texW, texH);
			}
			for (int oy = 0; oy < h - 2 * c; oy += midH) {
				int ph = Math.min(midH, h - 2 * c - oy);
				blit(ctx, t, x, y + c + oy, 0, c, c, ph, texW, texH);
				blit(ctx, t, x + w - c, y + c + oy, texW - c, c, c, ph, texW, texH);
			}
			// esquinas
			blit(ctx, t, x, y, 0, 0, c, c, texW, texH);
			blit(ctx, t, x + w - c, y, texW - c, 0, c, c, texW, texH);
			blit(ctx, t, x, y + h - c, 0, texH - c, c, c, texW, texH);
			blit(ctx, t, x + w - c, y + h - c, texW - c, texH - c, c, c, texW, texH);
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Barra de maestría de 20 gemas (248×24): la vacía con las primeras {@code filled} gemas de la llena encima. */
	static void masteryBar(DrawContext ctx, int x, int y, int filled) {
		sprite(ctx, "maestria/maestria_20_vacias.png", x, y, 248, 24);
		int n = Math.max(0, Math.min(20, filled));
		if (n == 0) return;
		Identifier full = id("maestria/maestria_20_llenas.png");
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			for (int i = 0; i < n; i++) {
				int gx = 6 + 12 * i; // las gemas miden 6 px de ancho, cada 12 px, entre las filas 8 y 17
				blit(ctx, full, x + gx, y + 8, gx, 8, 6, 10, 248, 24);
			}
		} finally {
			RenderSystem.disableBlend();
		}
	}
}
