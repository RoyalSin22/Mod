package dev.aptitudes.client.screen;

import net.minecraft.client.gui.DrawContext;

/**
 * v67 (G3): piezas de pergamino del grimorio, dibujadas con rellenos (sin texturas). Son el respaldo provisional: el
 * arte definitivo del libro (ver {@code docs/PROMPT_ARTE_GRIMORIO.md}) sustituirá estas piezas sin cambiar quién las
 * llama. La paleta es un pergamino oscuro (sepia) para que el texto claro del libro siga leyéndose.
 */
final class GuideUi {
	static final int WINDOW = 0xFF1E1711;
	static final int PARCHMENT = 0xFF3A2D1F;
	static final int PARCHMENT_LIGHT = 0xFF46372A;
	static final int FRAME_OUTER = 0xFF5A4220;
	static final int FRAME_GOLD = 0xFFC9A24A;
	static final int FRAME_GOLD_DARK = 0xFF8A6A2A;
	static final int RIBBON = 0xFF5A1E1E;
	static final int RIBBON_DARK = 0xFF3A1212;
	static final int FIBER_LIGHT = 0x10FFFFFF;
	static final int FIBER_DARK = 0x14000000;

	private GuideUi() {
	}

	/** Ventana del libro: fondo oscuro, doble marco dorado y esquinas con remate. */
	static void window(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, WINDOW);
		ctx.drawBorder(x, y, w, h, FRAME_OUTER);
		ctx.drawBorder(x + 2, y + 2, w - 4, h - 4, FRAME_GOLD_DARK);
		corners(ctx, x + 1, y + 1, w - 2, h - 2);
	}

	/** Hoja de pergamino con fibras suaves y filete dorado. */
	static void panel(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, PARCHMENT);
		for (int yy = y + 2; yy < y + h - 1; yy += 3) {
			ctx.fill(x + 1, yy, x + w - 1, yy + 1, ((yy - y) / 3) % 2 == 0 ? FIBER_LIGHT : FIBER_DARK);
		}
		ctx.fill(x + 1, y + 1, x + w - 1, y + 2, PARCHMENT_LIGHT);
		ctx.drawBorder(x, y, w, h, FRAME_GOLD_DARK);
	}

	/** Marco del retrato de una criatura: interior oscuro y doble filete dorado. */
	static void portrait(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, 0xFF16110C);
		ctx.drawBorder(x, y, w, h, FRAME_GOLD);
		ctx.drawBorder(x + 2, y + 2, w - 4, h - 4, FRAME_GOLD_DARK);
		corners(ctx, x, y, w, h);
	}

	/** Cinta con el nombre: banda granate con remates dorados. */
	static void ribbon(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, RIBBON);
		ctx.fill(x, y + h - 2, x + w, y + h, RIBBON_DARK);
		ctx.fill(x, y, x + w, y + 1, FRAME_GOLD);
		ctx.fill(x, y + h - 1, x + w, y + h, FRAME_GOLD);
		ctx.fill(x, y, x + 2, y + h, FRAME_GOLD);
		ctx.fill(x + w - 2, y, x + w, y + h, FRAME_GOLD);
	}

	/** Filete con un rombo en el centro. */
	static void divider(DrawContext ctx, int x, int y, int w) {
		int mid = x + w / 2;
		ctx.fill(x, y, mid - 4, y + 1, FRAME_GOLD_DARK);
		ctx.fill(mid + 4, y, x + w, y + 1, FRAME_GOLD_DARK);
		ctx.fill(mid - 1, y - 1, mid + 1, y + 2, FRAME_GOLD);
		ctx.fill(mid - 2, y, mid + 2, y + 1, FRAME_GOLD);
	}

	/** Remates de esquina de 3×3 píxeles. */
	private static void corners(DrawContext ctx, int x, int y, int w, int h) {
		int s = 3;
		for (int cx : new int[]{x, x + w - s}) {
			for (int cy : new int[]{y, y + h - s}) ctx.fill(cx, cy, cx + s, cy + s, FRAME_GOLD);
		}
	}
}
