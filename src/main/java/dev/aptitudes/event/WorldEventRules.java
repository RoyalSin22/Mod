package dev.aptitudes.event;

/**
 * v41: reglas puras de la rotación de eventos: qué evento toca en cada fecha (al azar, ponderado, sin repetir el
 * anterior y reproducible a partir de la semilla del mundo) y la intensidad de los eventos con subida y bajada.
 * Sin Minecraft, con pruebas.
 */
public final class WorldEventRules {
	private WorldEventRules() {
	}

	/** Número de evento de un día de evento (0, 1, 2…), o −1 si ese día no hay evento. */
	public static long eventIndex(long day, int interval, int firstDay) {
		if (!EclipseRules.isEclipseDay(day, interval, firstDay)) return -1;
		return day / Math.max(1, interval);
	}

	/**
	 * Elige el evento número {@code index}: ponderado por {@code weights} (uno por tipo; 0 = desactivado) y sin
	 * repetir {@code previous} (índice del anterior, −1 si no hay) mientras haya otra opción. Devuelve −1 si todos
	 * los pesos son 0. Es determinista: la misma semilla e índice dan el mismo resultado.
	 */
	public static int choose(long seed, long index, int[] weights, int previous) {
		if (weights == null || weights.length == 0) return -1;
		long total = 0;
		int available = 0;
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] > 0) {
				available++;
				if (i != previous) total += weights[i];
			}
		}
		if (available == 0) return -1;
		boolean excludePrevious = available > 1 && previous >= 0 && previous < weights.length && weights[previous] > 0;
		if (!excludePrevious) {
			total = 0;
			for (int w : weights) total += Math.max(0, w);
		}
		long roll = Math.floorMod(mix(seed ^ (index * 0x9E3779B97F4A7C15L)), total);
		for (int i = 0; i < weights.length; i++) {
			if (weights[i] <= 0 || (excludePrevious && i == previous)) continue;
			roll -= weights[i];
			if (roll < 0) return i;
		}
		return -1;
	}

	/** Intensidad 0–1 de un evento de {@code duration} ticks que sube y baja en {@code ramp} ticks. */
	public static float ramp(long t, long duration, long ramp) {
		if (t < 0 || t >= duration || duration <= 0) return 0f;
		long r = Math.max(1, Math.min(ramp, duration / 2));
		if (t < r) return t / (float) r;
		if (t > duration - r) return (duration - t) / (float) r;
		return 1f;
	}

	/** Mezclador SplitMix64: distribuye bien semillas e índices consecutivos. */
	static long mix(long z) {
		z += 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}
}
