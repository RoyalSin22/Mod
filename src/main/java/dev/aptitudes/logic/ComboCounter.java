package dev.aptitudes.logic;

/**
 * Contador de combo de un jugador (v28). Clase pura (sin Minecraft) y probada.
 *
 * <p>Cada golpe dentro de la ventana suma 1; si pasa más tiempo que la ventana, el combo vuelve a 0. Al llegar
 * al umbral, el SIGUIENTE golpe es un remate (con su propia espera) y el contador se reinicia.</p>
 */
public final class ComboCounter {
	/** Máximo que se cuenta (solo para mostrar; la bonificación tiene su propio tope). */
	public static final int MAX_COUNT = 99;

	private int count;
	private long lastHit = Long.MIN_VALUE / 2;
	private long lastFinisher = Long.MIN_VALUE / 2;

	/** Combo vigente en {@code now} (0 si caducó). */
	public int count(long now, long window) {
		if (now - lastHit > window) count = 0;
		return count;
	}

	/** Registra un golpe y devuelve el combo nuevo. */
	public int hit(long now, long window) {
		if (now - lastHit > window) count = 0;
		count = Math.min(MAX_COUNT, count + 1);
		lastHit = now;
		return count;
	}

	/**
	 * Si el combo alcanzó {@code threshold} y pasó la espera de remate, consume el combo (vuelve a 0) y devuelve
	 * el combo que tenía; si no, devuelve 0.
	 */
	public int tryFinisher(long now, long window, int threshold, long cooldown) {
		int current = count(now, window);
		if (current < Math.max(1, threshold) || now - lastFinisher < cooldown) return 0;
		lastFinisher = now;
		count = 0;
		return current;
	}

	public void reset() {
		count = 0;
	}

	/** Bonificación de daño (fracción) para un combo: {@code count × perHit}, entre 0 y {@code max}. */
	public static double bonus(int count, double perHit, double max) {
		if (count <= 0 || !(perHit > 0) || !(max > 0)) return 0;
		return Math.min(max, count * perHit);
	}
}
