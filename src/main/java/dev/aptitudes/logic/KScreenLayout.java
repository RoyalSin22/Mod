package dev.aptitudes.logic;

import dev.aptitudes.logic.TreeScreenLayout.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometría de la pantalla K (Atributos y Habilidades) con las piezas de la dirección de arte. Lógica pura, sin
 * Minecraft: la usa {@code AptitudesScreen} y la prueban {@code KScreenLayoutTest}.
 *
 * <p>Todas las coordenadas son absolutas a partir de la esquina superior izquierda de la ventana ({@code left},
 * {@code top}). La ventana conserva 430×262 y las 10 tarjetas en 5 filas × 2.</p>
 */
public final class KScreenLayout {
	public static final int W = 430;
	public static final int H = 262;

	public static final int CARD_W = 200;
	public static final int CARD_H = 38;
	public static final int CARD_STEP_X = 210;
	public static final int CARD_STEP_Y = 40;
	public static final int CARD_X = 10;
	public static final int CARD_Y = 44;

	/** Casilla del icono dentro de la tarjeta (marco de 18×18 dibujado en la pieza) y objeto de 16×16 centrado. */
	public static final int ICON_X = 5, ICON_Y = 11;
	/** Canal de la barra de XP dentro de la tarjeta y tamaño de la pieza de la barra. */
	public static final int BAR_X = 32, BAR_Y = 14, BAR_W = 160, BAR_H = 6;
	/** Texto del nombre (arriba) y de las pasivas (abajo), a la derecha del icono y alineado con la barra. */
	public static final int TEXT_X = BAR_X, NAME_Y = 5, PASSIVES_Y = 23;

	public static final int ATTR_X = 10, ATTR_Y = 66, ATTR_ROW_W = 140, ATTR_ROW_H = 16, ATTR_ROW_STEP = 17;

	public static final int TAB_H = 22;
	public static final int TAB_CAP = 10;
	public static final int TAB_PAD = 10;
	public static final int TAB_GAP = 2;

	private KScreenLayout() {
	}

	/** Tarjeta de la habilidad {@code index} (0–9): dos columnas, de izquierda a derecha y de arriba abajo. */
	public static Rect card(int left, int top, int index) {
		return new Rect(left + CARD_X + (index % 2) * CARD_STEP_X, top + CARD_Y + (index / 2) * CARD_STEP_Y, CARD_W, CARD_H);
	}

	/** Fila del atributo {@code index} en la columna de atributos. */
	public static Rect attributeRow(int left, int top, int index) {
		return new Rect(left + ATTR_X, top + ATTR_Y + index * ATTR_ROW_STEP, ATTR_ROW_W, ATTR_ROW_H);
	}

	/** Panel hundido de la columna de atributos, alrededor de las {@code rows} filas. */
	public static Rect attributeColumn(int left, int top, int rows) {
		return new Rect(left + ATTR_X - 4, top + ATTR_Y - 4, ATTR_ROW_W + 8, (rows - 1) * ATTR_ROW_STEP + ATTR_ROW_H + 8);
	}

	/** Panel de la columna de estadísticas base ({@code rows} filas de {@code rowStep} px desde {@code top + 46}). */
	public static Rect statsColumn(int left, int top, int rows, int rowStep) {
		return new Rect(left + 156, top + 40, 132, rows * rowStep + 12);
	}

	/** Ficha del atributo seleccionado. */
	public static Rect detailPanel(int left, int top) {
		return new Rect(left + 294, top + 46, 130, 120);
	}

	/**
	 * Pestañas alineadas a la derecha de la cabecera, colocadas de derecha a izquierda en el orden recibido. Cada una
	 * mide el ancho de su texto más {@code TAB_PAD} a cada lado, y nunca menos que dos esquinas.
	 */
	public static List<Rect> tabs(int left, int top, int[] textWidths) {
		List<Rect> result = new ArrayList<>(textWidths.length);
		int x = left + W - 8;
		for (int textW : textWidths) {
			int w = Math.max(2 * TAB_CAP + 2, Math.max(0, textW) + 2 * TAB_PAD);
			x -= w;
			result.add(new Rect(x, top + 3, w, TAB_H));
			x -= TAB_GAP;
		}
		return result;
	}

	/**
	 * Tramo de una pieza de 3 partes en horizontal: {@code dest} es el desplazamiento en pantalla, {@code src} el de la
	 * textura y {@code len} su longitud.
	 */
	public record Tile(int dest, int src, int len) {
	}

	/**
	 * Reparte {@code length} px usando una pieza de {@code texLen} px con dos extremos fijos ({@code capStart} y
	 * {@code capEnd}) y el tramo central repetido (nunca estirado). Si no caben los dos extremos, se recortan por la
	 * mitad. Los tramos devueltos cubren exactamente {@code [0, length)} sin solaparse.
	 */
	public static List<Tile> tiles(int length, int capStart, int capEnd, int texLen) {
		List<Tile> result = new ArrayList<>();
		if (length <= 0 || texLen <= 0) return result;
		int start = Math.max(0, capStart), end = Math.max(0, capEnd);
		if (start + end >= texLen) return List.of(new Tile(0, 0, Math.min(length, texLen)));
		if (length <= start + end) {
			int a = Math.min(start, (length + 1) / 2);
			int b = length - a;
			if (a > 0) result.add(new Tile(0, 0, a));
			if (b > 0) result.add(new Tile(a, texLen - b, b));
			return result;
		}
		result.add(new Tile(0, 0, start));
		int mid = texLen - start - end;
		for (int x = start; x < length - end; x += mid) {
			result.add(new Tile(x, start, Math.min(mid, length - end - x)));
		}
		result.add(new Tile(length - end, texLen - end, end));
		return result;
	}

	/** Píxeles rellenos de una barra de {@code width} px con un progreso de {@code fraction} (0–1, acotado). */
	public static int filled(int width, double fraction) {
		if (width <= 0 || !(fraction > 0)) return 0;
		return (int) Math.round(width * Math.min(1.0, fraction));
	}
}
