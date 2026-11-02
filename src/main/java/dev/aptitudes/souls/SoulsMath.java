package dev.aptitudes.souls;

/**
 * Fórmulas de la Hoguera en Java puro (sin Minecraft), para poder probarlas con
 * pruebas unitarias. {@link SoulsManager} las usa con los valores de la configuración.
 */
public final class SoulsMath {
	private SoulsMath() {
	}

	/** Niveles de alma a comprar desde {@code soulLevel} para ganar {@code points} puntos (1 cada {@code per}). */
	public static int levelsForPoints(int soulLevel, int points, int per) {
		if (points <= 0) return 0;
		int p = Math.max(1, per);
		int target = p * (soulLevel / p + points);
		return target - soulLevel;
	}

	/** Coste en niveles de XP de pasar del nivel de alma {@code level} al siguiente. */
	public static int levelCost(long level, int base, int divisor, int max) {
		// Aritmética en long: un nivel enorme (o negativo por datos corruptos) no desborda ni da coste negativo.
		long cost = (long) base + Math.max(0L, level) / Math.max(1, divisor);
		return (int) Math.max(0L, Math.min((long) max, cost));
	}

	/**
	 * Valida una petición de subida de nivel del cliente.
	 *
	 * @param requested puntos pedidos por atributo
	 * @param allocated puntos ya invertidos por atributo
	 * @param max       máximo de puntos por atributo
	 * @return total de puntos pedidos (1..1000) o -1 si la petición es inválida. Las sumas se hacen
	 *         con long: un cliente modificado podría usar valores cercanos a Integer.MAX_VALUE para
	 *         desbordar "asignado + n" a negativo y pasar el control.
	 */
	public static long validRequestTotal(int[] requested, int[] allocated, int max) {
		if (requested == null || allocated == null || requested.length != allocated.length) return -1;
		long total = 0;
		for (int i = 0; i < requested.length; i++) {
			int n = requested[i];
			if (n < 0 || n > max || (long) allocated[i] + n > max) return -1;
			total += n;
		}
		return total <= 0 || total > 1000 ? -1 : total;
	}
}
