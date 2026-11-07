package dev.aptitudes.client.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Estilo visual común de las pantallas del mod: paneles oscuros translúcidos,
 * cabeceras doradas con rombo, filas alternas y marcos de habilidad cuya forma
 * indica el tipo (pasiva, activa, característica, definitiva, única).
 */
public final class UiStyle {
	public static final int BG = 0xE60D0F16;
	public static final int PANEL = 0xB0161A26;
	public static final int ROW = 0x40303850;
	public static final int ROW_ALT = 0x28303850;
	public static final int ROW_SELECTED = 0x60C9A54C;
	public static final int BORDER = 0xFF3A3F55;
	public static final int GOLD = 0xFFE6C36A;
	public static final int GOLD_DARK = 0xFF9C7A35;
	public static final int TEXT = 0xFFE8E8F0;
	public static final int MUTED = 0xFF8A8FA3;
	public static final int UP = 0xFF6FA8FF;      // valor que mejora (azul, como la referencia)
	public static final int DOWN = 0xFFFF6B6B;
	public static final int GOOD = 0xFF7CFC00;

	/** Forma del marco de una habilidad. */
	public enum Frame { PASSIVE, ACTIVE, SIGNATURE, ULTIMATE, CORE, UNIQUE }

	private UiStyle() {
	}

	public static void window(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, BG);
		ctx.drawBorder(x, y, w, h, BORDER);
		ctx.fill(x + 1, y + 1, x + w - 1, y + 2, GOLD_DARK);
	}

	public static void panel(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, PANEL);
		ctx.drawBorder(x, y, w, h, BORDER);
	}

	/**
	 * Recorta un texto para que quepa en {@code maxWidth} píxeles, añadiendo "…".
	 * Evita que etiquetas largas (o traducciones más largas) pisen los valores.
	 */
	public static Text fit(TextRenderer tr, Text text, int maxWidth) {
		if (maxWidth <= 0) return Text.empty();
		if (tr.getWidth(text) <= maxWidth) return text;
		String ellipsis = "…";
		String trimmed = tr.trimToWidth(text.getString(), Math.max(0, maxWidth - tr.getWidth(ellipsis)));
		return Text.literal(trimmed.stripTrailing() + ellipsis).setStyle(text.getStyle());
	}

	/** Dibuja {@code text} recortado a {@code maxWidth}. */
	public static void label(DrawContext ctx, TextRenderer tr, Text text, int x, int y, int maxWidth, int color) {
		ctx.drawText(tr, fit(tr, text, maxWidth), x, y, color, false);
	}

	/** Cabecera dorada con rombo y línea inferior. */
	public static void header(DrawContext ctx, TextRenderer tr, Text text, int x, int y, int width) {
		ctx.drawText(tr, Text.literal("◆ "), x, y, GOLD, false);
		ctx.drawTextWithShadow(tr, fit(tr, text, width - 12), x + 10, y, GOLD);
		ctx.fill(x, y + 11, x + width, y + 12, GOLD_DARK);
	}

	public static void row(DrawContext ctx, int x, int y, int w, int h, int index, boolean selected) {
		ctx.fill(x, y, x + w, y + h, selected ? ROW_SELECTED : index % 2 == 0 ? ROW : ROW_ALT);
		if (selected) ctx.drawBorder(x, y, w, h, GOLD);
	}

	/** "actual → nuevo" alineado a la derecha; el nuevo en azul si cambia. */
	public static void valueArrow(DrawContext ctx, TextRenderer tr, String current, String next, int right, int y,
								  boolean changed) {
		int nextW = tr.getWidth(next);
		ctx.drawTextWithShadow(tr, next, right - nextW, y, changed ? UP : TEXT);
		int arrowX = right - nextW - 14;
		ctx.drawText(tr, "→", arrowX, y, changed ? GOLD : MUTED, false);
		ctx.drawTextWithShadow(tr, current, arrowX - 6 - tr.getWidth(current), y, TEXT);
	}

	/** Marco de 24×24 de un nodo de habilidad; {@code lit} = aprendido/disponible. */
	public static void frame(DrawContext ctx, int x, int y, Frame type, boolean lit, boolean hovered) {
		int color = lit ? (hovered ? 0xFFFFFFFF : GOLD) : (hovered ? 0xFFB0B0C0 : 0xFF50546A);
		int fill = lit ? 0xF0262A3A : 0xF0151722;
		int s = 24;
		ctx.fill(x + 2, y + 2, x + s - 2, y + s - 2, fill);
		switch (type) {
			case PASSIVE -> { // cuadrado de esquinas marcadas
				ctx.drawBorder(x + 1, y + 1, s - 2, s - 2, color);
				corner(ctx, x, y, s, color);
			}
			case ACTIVE -> { // redondeado
				ctx.fill(x + 3, y, x + s - 3, y + 1, color);
				ctx.fill(x + 3, y + s - 1, x + s - 3, y + s, color);
				ctx.fill(x, y + 3, x + 1, y + s - 3, color);
				ctx.fill(x + s - 1, y + 3, x + s, y + s - 3, color);
				ctx.fill(x + 1, y + 1, x + 3, y + 3, color);
				ctx.fill(x + s - 3, y + 1, x + s - 1, y + 3, color);
				ctx.fill(x + 1, y + s - 3, x + 3, y + s - 1, color);
				ctx.fill(x + s - 3, y + s - 3, x + s - 1, y + s - 1, color);
			}
			case SIGNATURE -> { // doble borde
				ctx.drawBorder(x, y, s, s, color);
				ctx.drawBorder(x + 2, y + 2, s - 4, s - 4, color);
			}
			case ULTIMATE -> { // borde grueso dorado con remaches
				ctx.drawBorder(x, y, s, s, lit ? 0xFFFFC107 : color);
				ctx.drawBorder(x + 1, y + 1, s - 2, s - 2, lit ? 0xFFFFC107 : color);
				ctx.fill(x + s / 2 - 1, y - 2, x + s / 2 + 1, y, color);
				ctx.fill(x + s / 2 - 1, y + s, x + s / 2 + 1, y + s + 2, color);
			}
			case CORE -> {
				ctx.drawBorder(x - 2, y - 2, s + 4, s + 4, color);
				ctx.drawBorder(x, y, s, s, color);
			}
			case UNIQUE -> {
				ctx.drawBorder(x, y, s, s, 0xFFB388FF);
				corner(ctx, x, y, s, 0xFFB388FF);
			}
		}
	}

	private static void corner(DrawContext ctx, int x, int y, int s, int color) {
		ctx.fill(x, y, x + 4, y + 1, color);
		ctx.fill(x, y, x + 1, y + 4, color);
		ctx.fill(x + s - 4, y, x + s, y + 1, color);
		ctx.fill(x + s - 1, y, x + s, y + 4, color);
		ctx.fill(x, y + s - 1, x + 4, y + s, color);
		ctx.fill(x, y + s - 4, x + 1, y + s, color);
		ctx.fill(x + s - 4, y + s - 1, x + s, y + s, color);
		ctx.fill(x + s - 1, y + s - 4, x + s, y + s, color);
	}

	/** Candado de 8×10 px dibujado con rectángulos. */
	static void lockIcon(DrawContext ctx, int x, int y) {
		int c = 0xFFB0B4C0;
		ctx.fill(x + 2, y, x + 6, y + 1, c);
		ctx.fill(x + 1, y + 1, x + 2, y + 5, c);
		ctx.fill(x + 6, y + 1, x + 7, y + 5, c);
		ctx.fill(x, y + 5, x + 8, y + 11, c);
		ctx.fill(x + 3, y + 7, x + 5, y + 9, 0xFF303440);
	}

	/** Línea (horizontal/vertical/diagonal) de 2 px entre dos puntos, para el árbol. */
	public static void line(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
		int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
		for (int i = 0; i <= steps; i += 1) {
			int x = x1 + (x2 - x1) * i / Math.max(1, steps);
			int y = y1 + (y2 - y1) * i / Math.max(1, steps);
			ctx.fill(x, y, x + 2, y + 2, color);
		}
	}

	public static String fmt(double value) {
		if (Math.abs(value - Math.rint(value)) < 1e-6) return String.valueOf((long) Math.rint(value));
		if (Math.abs(value) < 1) return String.format(java.util.Locale.ROOT, "%.3f", value).replaceAll("0+$", "");
		return String.format(java.util.Locale.ROOT, "%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
	}
}
