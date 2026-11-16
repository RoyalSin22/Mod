package dev.aptitudes.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.aptitudes.Aptitudes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Piezas de pergamino del grimorio. v67 (G5): se dibujan con las texturas de {@code textures/gui/guide_ui/} (generadas
 * por {@code tools/textures/gen_guide_ui.py} a partir del arte de referencia, ver {@code docs/PROMPT_ARTE_GRIMORIO.md});
 * si el paquete de recursos no las trae, se usa el dibujo con rellenos de la v67 (G3). Las piezas se dibujan a su
 * tamaño real; los marcos repiten su tramo de borde en lugar de estirarlo. La paleta es oscura para que el texto claro
 * del libro siga leyéndose.
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

	private static final String BASE = "textures/gui/guide_ui/";
	/** Grosor del marco del retrato: el modelo de la criatura se dibuja dentro, a esta distancia del borde. */
	static final int PORTRAIT_INSET = 7;
	private static final int WINDOW_C = 14, PANEL_C = 8, PORTRAIT_C = 14;
	private static final int RIBBON_CAP = 19, RIBBON_TEX_W = 48, RIBBON_H = 14;
	private static final int DIV_TEX_W = 56, DIV_H = 7, DIV_CAP = 4, DIV_CENTER_X = 20, DIV_CENTER_W = 16, DIV_LINE = 4;
	private static final int EYE_W = 25, EYE_H = 8, BOOK_W = 21, BOOK_H = 9;

	private static Identifier id(String name) {
		return Aptitudes.id(BASE + name);
	}

	/** Comprobación barata (consulta el gestor de recursos): solo se dibujan las texturas si el paquete las trae. */
	private static boolean textured() {
		return MinecraftClient.getInstance().getResourceManager().getResource(id("window.png")).isPresent();
	}

	private static void blit(DrawContext ctx, Identifier t, int x, int y, int u, int v, int w, int h, int texW, int texH) {
		if (w > 0 && h > 0) ctx.drawTexture(t, x, y, (float) u, (float) v, w, h, texW, texH);
	}

	/** Marco de 9 partes: esquinas fijas; bordes y centro repetidos (nunca estirados). */
	private static boolean nine(DrawContext ctx, String name, int texSize, int c, int x, int y, int w, int h) {
		if (w < 2 * c || h < 2 * c) return false;
		Identifier t = id(name);
		int mid = texSize - 2 * c;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			for (int oy = 0; oy < h - 2 * c; oy += mid) {
				int ph = Math.min(mid, h - 2 * c - oy);
				for (int ox = 0; ox < w - 2 * c; ox += mid) {
					blit(ctx, t, x + c + ox, y + c + oy, c, c, Math.min(mid, w - 2 * c - ox), ph, texSize, texSize);
				}
				blit(ctx, t, x, y + c + oy, 0, c, c, ph, texSize, texSize);
				blit(ctx, t, x + w - c, y + c + oy, texSize - c, c, c, ph, texSize, texSize);
			}
			for (int ox = 0; ox < w - 2 * c; ox += mid) {
				int pw = Math.min(mid, w - 2 * c - ox);
				blit(ctx, t, x + c + ox, y, c, 0, pw, c, texSize, texSize);
				blit(ctx, t, x + c + ox, y + h - c, c, texSize - c, pw, c, texSize, texSize);
			}
			blit(ctx, t, x, y, 0, 0, c, c, texSize, texSize);
			blit(ctx, t, x + w - c, y, texSize - c, 0, c, c, texSize, texSize);
			blit(ctx, t, x, y + h - c, 0, texSize - c, c, c, texSize, texSize);
			blit(ctx, t, x + w - c, y + h - c, texSize - c, texSize - c, c, c, texSize, texSize);
		} finally {
			RenderSystem.disableBlend();
		}
		return true;
	}

	private static void sprite(DrawContext ctx, String name, int x, int y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			blit(ctx, id(name), x, y, 0, 0, w, h, w, h);
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Ventana del libro: marco de pergamino con el ojo arriba y el libro abajo, en el centro. */
	static void window(DrawContext ctx, int x, int y, int w, int h) {
		if (!textured() || !nine(ctx, "window.png", 34, WINDOW_C, x, y, w, h)) {
			windowFallback(ctx, x, y, w, h);
			return;
		}
		if (w >= 2 * WINDOW_C + EYE_W && h >= 2 * WINDOW_C + BOOK_H) {
			sprite(ctx, "emblema_ojo.png", x + (w - EYE_W) / 2, y, EYE_W, EYE_H);
			sprite(ctx, "emblema_libro.png", x + (w - BOOK_W) / 2, y + h - BOOK_H, BOOK_W, BOOK_H);
		}
	}

	/** Hoja de pergamino de la página. */
	static void panel(DrawContext ctx, int x, int y, int w, int h) {
		if (!textured() || !nine(ctx, "panel.png", 20, PANEL_C, x, y, w, h)) panelFallback(ctx, x, y, w, h);
	}

	/** Marco del retrato de una criatura; el contenido va a {@link #PORTRAIT_INSET} px del borde. */
	static void portrait(DrawContext ctx, int x, int y, int w, int h) {
		if (!textured() || !nine(ctx, "portrait.png", 34, PORTRAIT_C, x, y, w, h)) portraitFallback(ctx, x, y, w, h);
	}

	/** Cinta con el nombre: remates con calavera y dragón y tramo central repetido; alto fijo de 14 px. */
	static void ribbon(DrawContext ctx, int x, int y, int w, int h) {
		if (!textured() || w < 2 * RIBBON_CAP) {
			ribbonFallback(ctx, x, y, w, h);
			return;
		}
		Identifier t = id("ribbon.png");
		int mid = RIBBON_TEX_W - 2 * RIBBON_CAP;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			blit(ctx, t, x, y, 0, 0, RIBBON_CAP, RIBBON_H, RIBBON_TEX_W, RIBBON_H);
			for (int ox = RIBBON_CAP; ox < w - RIBBON_CAP; ox += mid) {
				blit(ctx, t, x + ox, y, RIBBON_CAP, 0, Math.min(mid, w - RIBBON_CAP - ox), RIBBON_H, RIBBON_TEX_W, RIBBON_H);
			}
			blit(ctx, t, x + w - RIBBON_CAP, y, RIBBON_TEX_W - RIBBON_CAP, 0, RIBBON_CAP, RIBBON_H, RIBBON_TEX_W, RIBBON_H);
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Filete con el rombo en el centro; {@code y} es la línea del filete (la pieza ocupa de y−3 a y+3). */
	static void divider(DrawContext ctx, int x, int y, int w) {
		if (!textured() || w < DIV_TEX_W) {
			dividerFallback(ctx, x, y, w);
			return;
		}
		Identifier t = id("divider.png");
		int top = y - DIV_H / 2;
		int cx = x + (w - DIV_CENTER_W) / 2;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try {
			blit(ctx, t, x, top, 0, 0, DIV_CAP, DIV_H, DIV_TEX_W, DIV_H);
			blit(ctx, t, x + w - DIV_CAP, top, DIV_TEX_W - DIV_CAP, 0, DIV_CAP, DIV_H, DIV_TEX_W, DIV_H);
			blit(ctx, t, cx, top, DIV_CENTER_X, 0, DIV_CENTER_W, DIV_H, DIV_TEX_W, DIV_H);
			for (int lx = x + DIV_CAP; lx < cx; lx += DIV_LINE) {
				blit(ctx, t, lx, top, DIV_CAP, 0, Math.min(DIV_LINE, cx - lx), DIV_H, DIV_TEX_W, DIV_H);
			}
			for (int lx = cx + DIV_CENTER_W; lx < x + w - DIV_CAP; lx += DIV_LINE) {
				blit(ctx, t, lx, top, DIV_CAP, 0, Math.min(DIV_LINE, x + w - DIV_CAP - lx), DIV_H, DIV_TEX_W, DIV_H);
			}
		} finally {
			RenderSystem.disableBlend();
		}
	}

	/** Remate dorado de esquina (arriba a la izquierda) de 12×13 px; no hay respaldo: sin textura no se dibuja. */
	static void corner(DrawContext ctx, int x, int y) {
		if (textured()) sprite(ctx, "corner_tl.png", x, y, 12, 13);
	}

	/** Ventana del libro: fondo oscuro, doble marco dorado y esquinas con remate. */
	private static void windowFallback(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, WINDOW);
		ctx.drawBorder(x, y, w, h, FRAME_OUTER);
		ctx.drawBorder(x + 2, y + 2, w - 4, h - 4, FRAME_GOLD_DARK);
		corners(ctx, x + 1, y + 1, w - 2, h - 2);
	}

	/** Hoja de pergamino con fibras suaves y filete dorado. */
	private static void panelFallback(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, PARCHMENT);
		for (int yy = y + 2; yy < y + h - 1; yy += 3) {
			ctx.fill(x + 1, yy, x + w - 1, yy + 1, ((yy - y) / 3) % 2 == 0 ? FIBER_LIGHT : FIBER_DARK);
		}
		ctx.fill(x + 1, y + 1, x + w - 1, y + 2, PARCHMENT_LIGHT);
		ctx.drawBorder(x, y, w, h, FRAME_GOLD_DARK);
	}

	/** Marco del retrato de una criatura: interior oscuro y doble filete dorado. */
	private static void portraitFallback(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, 0xFF16110C);
		ctx.drawBorder(x, y, w, h, FRAME_GOLD);
		ctx.drawBorder(x + 2, y + 2, w - 4, h - 4, FRAME_GOLD_DARK);
		corners(ctx, x, y, w, h);
	}

	/** Cinta con el nombre: banda granate con remates dorados. */
	private static void ribbonFallback(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, RIBBON);
		ctx.fill(x, y + h - 2, x + w, y + h, RIBBON_DARK);
		ctx.fill(x, y, x + w, y + 1, FRAME_GOLD);
		ctx.fill(x, y + h - 1, x + w, y + h, FRAME_GOLD);
		ctx.fill(x, y, x + 2, y + h, FRAME_GOLD);
		ctx.fill(x + w - 2, y, x + w, y + h, FRAME_GOLD);
	}

	/** Filete con un rombo en el centro. */
	private static void dividerFallback(DrawContext ctx, int x, int y, int w) {
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
