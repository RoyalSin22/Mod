package dev.aptitudes.morph;

/**
 * Límites del tamaño de colisión de un jugador transformado (v26). Clase pura y probada.
 *
 * <ul>
 *   <li>Ancho entre {@link #MIN_WIDTH} y {@link #MAX_WIDTH}: un murciélago cabe por huecos pequeños, pero
 *       transformarse en un mob enorme no deja al jugador atascado en pasillos de 2 bloques.</li>
 *   <li>Alto entre {@link #MIN_HEIGHT} y {@link #MAX_HEIGHT}.</li>
 *   <li>Altura de los ojos dentro del cuerpo (como mínimo {@link #MIN_EYE}, como máximo alto − 0,05).</li>
 * </ul>
 */
public final class MorphSizeRules {
	public static final float MIN_WIDTH = 0.3f;
	public static final float MAX_WIDTH = 1.2f;
	public static final float MIN_HEIGHT = 0.35f;
	public static final float MAX_HEIGHT = 2.6f;
	public static final float MIN_EYE = 0.2f;

	private MorphSizeRules() {
	}

	/** @return {ancho, alto, altura de ojos} acotados; valores no finitos se tratan como el mínimo. */
	public static float[] clamp(float width, float height, float eyeHeight) {
		float w = bound(width, MIN_WIDTH, MAX_WIDTH);
		float h = bound(height, MIN_HEIGHT, MAX_HEIGHT);
		float eye = bound(eyeHeight, MIN_EYE, Math.max(MIN_EYE, h - 0.05f));
		return new float[]{w, h, eye};
	}

	private static float bound(float v, float min, float max) {
		if (!Float.isFinite(v)) return min;
		return Math.max(min, Math.min(max, v));
	}
}
