package dev.aptitudes.elite;

import java.util.List;

/**
 * Marcas de campeón (enemigos, paso 1): forma geométrica de cada afijo, dibujada con partículas. Puro (sin Minecraft).
 * Los ids son los nombres de los afijos en minúsculas; cada marca ocupa una de tres posiciones (base, pecho, cabeza).
 */
public final class GlyphRules {
	/** Un punto de la marca, en unidades relativas (≈0,22 bloques por unidad). */
	public record Point(double x, double y) {}

	/** Número máximo de marcas visibles por enemigo (los campeones tienen tres afijos). */
	public static final int MAX_MARKS = 3;
	/** Altura de cada posición de marca sobre los pies del mob, en bloques. */
	private static final double[] SLOT_HEIGHT = {0.1, 1.0, 1.7};

	private GlyphRules() {}

	/** Puntos de la marca del afijo. Vacía si el afijo no existe. */
	public static List<Point> shape(String affix) {
		if (affix == null) return List.of();
		return switch (affix) {
			case "fast" -> List.of(new Point(-1, -0.5), new Point(0, 0.5), new Point(1, -0.5), new Point(-1, -1.5), new Point(0, -0.5), new Point(1, -1.5));
			case "armored" -> List.of(new Point(0, 1), new Point(0.87, 0.5), new Point(0.87, -0.5), new Point(0, -1), new Point(-0.87, -0.5), new Point(-0.87, 0.5));
			case "vampiric" -> List.of(new Point(-1, 0), new Point(-0.7, -0.7), new Point(0, -1), new Point(0.7, -0.7), new Point(1, 0));
			case "brutal" -> List.of(new Point(-1, 1), new Point(0, 0), new Point(1, 1), new Point(-1, 0.2), new Point(0, -0.8), new Point(1, 0.2));
			case "regenerating" -> List.of(new Point(0, 1), new Point(1, 0), new Point(0, -1), new Point(-1, 0));
			case "explosive" -> List.of(new Point(-1, 1), new Point(1, 1), new Point(-1, -1), new Point(1, -1));
			case "frost" -> List.of(new Point(0, 0), new Point(0, 1), new Point(0, -1), new Point(0.87, 0.5), new Point(0.87, -0.5), new Point(-0.87, 0.5), new Point(-0.87, -0.5));
			case "blinking" -> List.of(new Point(-1.2, 1), new Point(-1.2, 0), new Point(-1.2, -1), new Point(1.2, 1), new Point(1.2, 0), new Point(1.2, -1));
			case "giant" -> List.of(new Point(0, 1), new Point(0.7, 0.7), new Point(1, 0), new Point(0.7, -0.7), new Point(0, -1), new Point(-0.7, -0.7), new Point(-1, 0), new Point(-0.7, 0.7));
			default -> List.of();
		};
	}

	/** Altura de la posición de marca {@code slot} (0 base, 1 pecho, 2 cabeza). */
	public static double slotHeight(int slot) {
		return SLOT_HEIGHT[Math.max(0, Math.min(SLOT_HEIGHT.length - 1, slot))];
	}
}
