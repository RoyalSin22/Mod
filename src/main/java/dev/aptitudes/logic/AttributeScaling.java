package dev.aptitudes.logic;

import java.util.Locale;

/**
 * Escalado de los puntos de atributo: convierte los puntos invertidos en "puntos efectivos"
 * (el valor final es {@code puntosEfectivos × valorPorPunto × afinidad}).
 *
 * <p>Java puro, sin dependencias de Minecraft, para poder probarlo con pruebas unitarias.</p>
 *
 * <table>
 *   <tr><th>Modo</th><th>Fórmula</th><th>Notas</th></tr>
 *   <tr><td>LINEAR (por defecto)</td><td>e(p) = p</td><td>Comportamiento original.</td></tr>
 *   <tr><td>SOFTCAP</td><td>p hasta S1; S1 + 0,5·(p−S1) hasta S2; después +0,2 por punto</td>
 *       <td>Idéntico al lineal hasta S1 (40): no cambia el juego temprano ni medio.</td></tr>
 *   <tr><td>SATURATION</td><td>e(p) = L·(1 − e^(−p/L))</td>
 *       <td>Misma pendiente inicial que el lineal; nunca supera L (límite asintótico).</td></tr>
 * </table>
 */
public final class AttributeScaling {
	public enum Mode {
		LINEAR, SOFTCAP, SATURATION;

		/** Interpreta el texto de la configuración; null si no es válido. */
		public static Mode parse(String text) {
			if (text == null) return null;
			try {
				return valueOf(text.trim().toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	/** Eficiencia de los puntos entre el primer y el segundo umbral, y a partir del segundo. */
	public static final double SECOND_SEGMENT_EFFICIENCY = 0.5;
	public static final double THIRD_SEGMENT_EFFICIENCY = 0.2;

	private AttributeScaling() {
	}

	/**
	 * @param points     puntos invertidos (≥ 0)
	 * @param softCap1   primer umbral del modo SOFTCAP
	 * @param softCap2   segundo umbral del modo SOFTCAP (≥ softCap1)
	 * @param saturation L del modo SATURATION (&gt; 0)
	 */
	public static double effectivePoints(Mode mode, int points, int softCap1, int softCap2, double saturation) {
		int p = Math.max(0, points);
		return switch (mode) {
			case LINEAR -> p;
			case SOFTCAP -> {
				int s1 = Math.max(0, softCap1);
				int s2 = Math.max(s1, softCap2);
				if (p <= s1) yield p;
				if (p <= s2) yield s1 + SECOND_SEGMENT_EFFICIENCY * (p - s1);
				yield s1 + SECOND_SEGMENT_EFFICIENCY * (s2 - s1) + THIRD_SEGMENT_EFFICIENCY * (p - s2);
			}
			case SATURATION -> {
				double l = Math.max(1.0, saturation);
				yield l * (1.0 - Math.exp(-p / l));
			}
		};
	}

	/** Valor marginal del siguiente punto (para la vista previa de la pantalla de atributos). */
	public static double marginal(Mode mode, int points, int softCap1, int softCap2, double saturation) {
		return effectivePoints(mode, points + 1, softCap1, softCap2, saturation)
				- effectivePoints(mode, points, softCap1, softCap2, saturation);
	}
}
