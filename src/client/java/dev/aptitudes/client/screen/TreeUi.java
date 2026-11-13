package dev.aptitudes.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.KScreenLayout.Tile;
import dev.aptitudes.logic.TreeScreenLayout;
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
	static final int PANEL_MAIN_W = 426, PANEL_MAIN_H = 240;
	static final int PANEL_DETAIL_W = 128, PANEL_DETAIL_H = 176;
	/**
	 * Distancia al borde hasta la que llegan los adornos de cada panel (grietas, remaches, filete interior): el relleno
	 * al estirar se toma siempre más hacia dentro. Medido en el kit: las grietas llegan a 38 px en horizontal y 30 px en
	 * vertical en los dos paneles.
	 */
	static final int PANEL_MAIN_ORNAMENT = 40, PANEL_DETAIL_ORNAMENT = 40;

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
				&& manager.getResource(id("botones/equipar_normal.png")).isPresent()
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
	 * Panel del kit (piedra con grietas y remaches cerca de los bordes) ajustado a {@code r}: en cada eje, las dos mitades
	 * de la pieza quedan ancladas a sus bordes y el hueco se rellena con la franja anterior al centro, nunca estirada
	 * (ver {@link TreeScreenLayout#anchoredTiles}). Así las grietas de las esquinas no se repiten en medio del panel.
	 */
	static void panel9(DrawContext ctx, String path, int texW, int texH, int ornament, Rect r) {
		if (r.w() <= 0 || r.h() <= 0) return;
		Identifier t = id(path);
		var cols = TreeScreenLayout.anchoredTiles(r.w(), texW, ornament);
		var rows = TreeScreenLayout.anchoredTiles(r.h(), texH, ornament);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			for (Tile row : rows) {
				for (Tile col : cols) {
					blit(ctx, t, r.x() + col.dest(), r.y() + row.dest(), col.src(), row.src(), col.len(), row.len(), texW, texH);
				}
			}
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Barra de maestría de 20 casillas (320×18): la vacía con las primeras {@code filled} casillas de la llena encima. */
	static void masteryBar(DrawContext ctx, int x, int y, int filled) {
		int w = TreeScreenLayout.MASTERY_W, h = TreeScreenLayout.MASTERY_H;
		sprite(ctx, "maestria/maestria_20_vacias.png", x, y, w, h);
		int n = Math.max(0, Math.min(20, filled));
		if (n == 0) return;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			blit(ctx, id("maestria/maestria_20_llenas.png"), x, y, 0, 0, n * (w / 20), h, w, h); // casillas de 16 px
		} finally {
			RenderSystem.disableBlend();
		}
	}
}
