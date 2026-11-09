package dev.aptitudes.temple;

import java.util.Arrays;

/**
 * Reglas puras de los Templos sellados (v64, K6): tamaño del puzle por anillo de Amenaza, comprobación de las
 * palancas, avance de la secuencia de placas y fuerza del guardián. Sin Minecraft: se prueba con JUnit.
 */
public final class TempleRules {
	/** Etapas guardadas como número en el sello: solo se añaden al final. */
	public enum Stage { LEVERS, PLATES, AWAKENED, CLEARED }

	public static final int MIN_LEVERS = 3;
	public static final int MAX_LEVERS = 4;
	public static final int MAX_PLATES = 4;

	private TempleRules() {}

	public static int leverCount(int ring) {
		return clampRing(ring) >= 2 ? MAX_LEVERS : MIN_LEVERS;
	}

	/** Anillos 0–I: sin placas; II–III: tres; IV–V: cuatro. */
	public static int plateCount(int ring) {
		int r = clampRing(ring);
		return r <= 1 ? 0 : r <= 3 ? 3 : MAX_PLATES;
	}

	/**
	 * Patrón objetivo a partir de bits aleatorios: nunca todas encendidas ni todas apagadas (si no, el puzle se
	 * resolvería solo o tirando de todas).
	 */
	public static boolean[] pattern(int bits, int count) {
		int n = Math.max(1, Math.min(MAX_LEVERS, count));
		boolean[] out = new boolean[n];
		for (int i = 0; i < n; i++) out[i] = ((bits >>> i) & 1) == 1;
		int on = 0;
		for (boolean b : out) if (b) on++;
		if (on == 0) out[0] = true;
		if (on == n && n > 1) out[n - 1] = false;
		return out;
	}

	public static boolean leversSolved(boolean[] current, boolean[] target) {
		return current != null && target != null && current.length == target.length && Arrays.equals(current, target);
	}

	/**
	 * Avance de la secuencia de placas. {@code pressed[i]} indica si hay un jugador sobre la placa {@code i}
	 * (en el orden correcto). Pisar la siguiente avanza; pisar una que aún no toca reinicia; quedarse en las ya
	 * superadas no cambia nada. Devuelve el nuevo progreso (0..pressed.length).
	 */
	public static int plateProgress(int progress, boolean[] pressed) {
		int p = Math.max(0, Math.min(pressed.length, progress));
		if (p >= pressed.length) return p;
		for (int i = p + 1; i < pressed.length; i++) if (pressed[i]) return 0;
		return pressed[p] ? p + 1 : p;
	}

	/** Bonificación multiplicativa de vida del guardián (+25 % por anillo). */
	public static double guardianHealthBonus(int ring) {
		return 0.25 * clampRing(ring);
	}

	/** Tipo de guardián: 0 = vindicador (I–II), 1 = esqueleto wither (II–III), 2 = invocador (IV–V). */
	public static int guardianKind(int ring) {
		int r = clampRing(ring);
		return r <= 1 ? 0 : r <= 3 ? 1 : 2;
	}

	public static int clampRing(int ring) {
		return Math.max(0, Math.min(5, ring));
	}
}
