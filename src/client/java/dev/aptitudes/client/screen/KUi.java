package dev.aptitudes.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.KScreenLayout;
import dev.aptitudes.logic.KScreenLayout.Tile;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Piezas de la pantalla K de la dirección de arte (generadas por {@code tools/textures/gen_k_ui.py}). Se dibujan a su
 * tamaño real; los paneles, pestañas y botones se ajustan repitiendo su tramo central, nunca estirándolo, para
 * conservar el píxel lógico de 2×2. Si el paquete de recursos no trae las piezas, la pantalla usa el dibujo anterior.
 */
final class KUi {
	private static final String BASE = "textures/gui/k_ui/";
	private static final int CORNER = 12;
	/** Columnas fijas a izquierda y derecha de los botones de 80×18 (el de confirmar lleva una brasa a la izquierda). */
	private static final int BUTTON_CAP_START = 12, BUTTON_CAP_END = 6;

	private KUi() {
	}

	static Identifier id(String path) {
		return Aptitudes.id(BASE + path);
	}

	/** Comprobación en cada apertura de la pantalla (barata: solo consulta el gestor de recursos). */
	static boolean available() {
		var manager = MinecraftClient.getInstance().getResourceManager();
		return manager.getResource(id("paneles/k_panel_principal_9slice.png")).isPresent()
				&& manager.getResource(id("tarjetas/tarjeta_normal.png")).isPresent()
				&& manager.getResource(id("pestanas/pestana_activa.png")).isPresent()
				&& manager.getResource(id("botones/confirmar_normal.png")).isPresent();
	}

	private static void blit(DrawContext ctx, Identifier t, int x, int y, int u, int v, int w, int h, int texW, int texH) {
		if (w > 0 && h > 0) ctx.drawTexture(t, x, y, (float) u, (float) v, w, h, texW, texH);
	}

	private static void begin() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}

	private static void end() {
		RenderSystem.disableBlend();
	}

	/** Pieza completa a su tamaño real. */
	static void sprite(DrawContext ctx, String path, int x, int y, int w, int h) {
		begin();
		try {
			blit(ctx, id(path), x, y, 0, 0, w, h, w, h);
		} finally {
			end();
		}
	}

	/** Los primeros {@code visibleW} px de una pieza de {@code texW}×{@code h} (barras de XP y cabeceras recortadas). */
	static void spriteLeft(DrawContext ctx, String path, int x, int y, int visibleW, int texW, int h) {
		begin();
		try {
			blit(ctx, id(path), x, y, 0, 0, Math.min(visibleW, texW), h, texW, h);
		} finally {
			end();
		}
	}

	/**
	 * Pieza dibujada a la mitad de su tamaño: con la rejilla de 2×2, cada bloque pasa a ser un píxel de interfaz y el
	 * dibujo se conserva exacto (se usa para el emblema de la Hoguera en la cabecera).
	 */
	static void spriteHalf(DrawContext ctx, String path, int x, int y, int texW, int texH) {
		begin();
		try {
			ctx.drawTexture(id(path), x, y, texW / 2, texH / 2, 0f, 0f, texW, texH, texW, texH);
		} finally {
			end();
		}
	}

	/** Pieza de altura fija ajustada a {@code w} px repitiendo su tramo central entre dos extremos fijos. */
	static void slice3(DrawContext ctx, String path, int x, int y, int w, int capStart, int capEnd, int texW, int texH) {
		Identifier t = id(path);
		begin();
		try {
			for (Tile tile : KScreenLayout.tiles(w, capStart, capEnd, texW)) {
				blit(ctx, t, x + tile.dest(), y, tile.src(), 0, tile.len(), texH, texW, texH);
			}
		} finally {
			end();
		}
	}

	static void button(DrawContext ctx, String base, String state, int x, int y, int w) {
		slice3(ctx, "botones/" + base + "_" + state + ".png", x, y, w, BUTTON_CAP_START, BUTTON_CAP_END, 80, 18);
	}

	static void tab(DrawContext ctx, String state, Rect r) {
		slice3(ctx, "pestanas/pestana_" + state + ".png", r.x(), r.y(), r.w(), KScreenLayout.TAB_CAP,
				KScreenLayout.TAB_CAP, 96, KScreenLayout.TAB_H);
	}

	/** Panel de 9 partes: esquinas de 12×12 sin escalar; bordes y centro repetidos. */
	static void panel9(DrawContext ctx, String path, int texW, int texH, Rect r) {
		int c = CORNER;
		int w = r.w(), h = r.h(), x = r.x(), y = r.y();
		if (w < 2 * c || h < 2 * c) return;
		Identifier t = id(path);
		begin();
		try {
			int midW = texW - 2 * c, midH = texH - 2 * c;
			for (int oy = 0; oy < h - 2 * c; oy += midH) {
				int ph = Math.min(midH, h - 2 * c - oy);
				for (int ox = 0; ox < w - 2 * c; ox += midW) {
					blit(ctx, t, x + c + ox, y + c + oy, c, c, Math.min(midW, w - 2 * c - ox), ph, texW, texH);
				}
				blit(ctx, t, x, y + c + oy, 0, c, c, ph, texW, texH);
				blit(ctx, t, x + w - c, y + c + oy, texW - c, c, c, ph, texW, texH);
			}
			for (int ox = 0; ox < w - 2 * c; ox += midW) {
				int pw = Math.min(midW, w - 2 * c - ox);
				blit(ctx, t, x + c + ox, y, c, 0, pw, c, texW, texH);
				blit(ctx, t, x + c + ox, y + h - c, c, texH - c, pw, c, texW, texH);
			}
			blit(ctx, t, x, y, 0, 0, c, c, texW, texH);
			blit(ctx, t, x + w - c, y, texW - c, 0, c, c, texW, texH);
			blit(ctx, t, x, y + h - c, 0, texH - c, c, c, texW, texH);
			blit(ctx, t, x + w - c, y + h - c, texW - c, texH - c, c, c, texW, texH);
		} finally {
			end();
		}
	}

	static void mainPanel(DrawContext ctx, boolean bonfire, Rect r) {
		panel9(ctx, bonfire ? "paneles/k_panel_hoguera_9slice.png" : "paneles/k_panel_principal_9slice.png", 192, 128, r);
	}

	static void columnPanel(DrawContext ctx, Rect r) {
		panel9(ctx, "paneles/k_panel_columna_9slice.png", 64, 64, r);
	}

	static void detailPanel(DrawContext ctx, Rect r) {
		panel9(ctx, "paneles/k_panel_detalle_9slice.png", 64, 64, r);
	}

	/** Cabecera de sección: rombo y filete de la pieza (recortada a {@code width}); el título va en x + 10. */
	static void sectionHeader(DrawContext ctx, int x, int y, int width) {
		spriteLeft(ctx, "cabeceras/cabecera_seccion.png", x, y - 1, width, 140, 12);
	}

	/** Barra de XP: el canal vacío y, encima, los primeros {@code filled} px de la llena (verde) o la máxima (dorada). */
	static void xpBar(DrawContext ctx, int x, int y, int filled, boolean maxed) {
		sprite(ctx, "barras/xp_vacia.png", x, y, KScreenLayout.BAR_W, KScreenLayout.BAR_H);
		if (filled > 0) {
			spriteLeft(ctx, maxed ? "barras/xp_maxima.png" : "barras/xp_llena.png", x, y, filled, KScreenLayout.BAR_W,
					KScreenLayout.BAR_H);
		}
	}
}
