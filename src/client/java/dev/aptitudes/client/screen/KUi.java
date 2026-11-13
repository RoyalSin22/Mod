package dev.aptitudes.client.screen;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Piezas de la pantalla K (v67, generadas por {@code tools/textures/gen_k_ui.py}). Se dibujan a su tamaño real, sin
 * escalar, salvo los paneles (9-slice con tramos repetidos), para conservar el píxel lógico de 2×2.
 */
final class KUi {
	private static final String BASE = "textures/gui/k_ui/";
	static final int PANEL_MAIN_W = 192, PANEL_MAIN_H = 128;
	static final int PANEL_COLUMN = 64;
	static final int BAR_W = 160, BAR_H = 6;

	private KUi() {
	}

	static Identifier id(String path) {
		return Aptitudes.id(BASE + path);
	}

	static void sprite(DrawContext ctx, String path, int x, int y, int w, int h) {
		TreeUi.sprite(ctx, id(path), x, y, w, h);
	}

	static void panelMain(DrawContext ctx, boolean bonfire, Rect r) {
		TreeUi.panel9(ctx, id(bonfire ? "paneles/k_panel_hoguera_9slice.png" : "paneles/k_panel_principal_9slice.png"),
				PANEL_MAIN_W, PANEL_MAIN_H, r);
	}

	static void panelColumn(DrawContext ctx, Rect r) {
		TreeUi.panel9(ctx, id("paneles/k_panel_columna_9slice.png"), PANEL_COLUMN, PANEL_COLUMN, r);
	}

	static void panelDetail(DrawContext ctx, Rect r) {
		TreeUi.panel9(ctx, id("paneles/k_panel_detalle_9slice.png"), PANEL_COLUMN, PANEL_COLUMN, r);
	}

	/**
	 * Barra de XP de 160×6: la vacía con la parte llena encima. El ancho llenado se redondea a bloques de 2 px para
	 * respetar el píxel lógico.
	 */
	static void xpBar(DrawContext ctx, int x, int y, float fraction, boolean maxed) {
		sprite(ctx, "barras/xp_vacia.png", x, y, BAR_W, BAR_H);
		int filled = maxed ? BAR_W : Math.min(BAR_W, Math.round(BAR_W * Math.max(0f, fraction) / 2f) * 2);
		if (filled > 0) {
			TreeUi.spritePart(ctx, id(maxed ? "barras/xp_maxima.png" : "barras/xp_llena.png"), x, y, filled, BAR_H, BAR_W, BAR_H);
		}
	}
}
