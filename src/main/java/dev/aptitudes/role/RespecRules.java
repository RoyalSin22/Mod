package dev.aptitudes.role;

/**
 * Reinicio del árbol de un rol pagando niveles de experiencia (v66). Lógica pura: el coste y la decisión se calculan
 * aquí y los usa el servidor (que valida) y la pantalla del árbol (que solo avisa del coste).
 *
 * <p>Coste en niveles de experiencia = {@code base + porPunto × puntos gastados}, redondeado hacia arriba y acotado a
 * {@code máximo}. Sin puntos gastados no hay nada que reiniciar y no cuesta nada. Un jugador en Creativo no paga.</p>
 */
public final class RespecRules {
	private RespecRules() {
	}

	public enum Result { OK, DISABLED, UNAVAILABLE, NOTHING_TO_RESET, NOT_ENOUGH_LEVELS }

	public static int cost(int spentPoints, int base, double perPoint, int max) {
		if (spentPoints <= 0) return 0;
		double raw = Math.max(0, base) + Math.max(0.0, perPoint) * spentPoints;
		int cap = Math.max(0, max);
		if (!Double.isFinite(raw)) return cap;
		long cost = (long) Math.ceil(raw);
		return (int) Math.max(0L, Math.min((long) cap, cost));
	}

	/**
	 * @param enabled          el servidor permite el reinicio de pago
	 * @param playerAvailable  el jugador está vivo y no es espectador
	 * @param spentPoints      puntos gastados en el rol
	 * @param experienceLevel  niveles de experiencia del jugador
	 * @param cost             coste calculado con {@link #cost}
	 * @param creative         en Creativo no se paga
	 */
	public static Result check(boolean enabled, boolean playerAvailable, int spentPoints, int experienceLevel, int cost,
							   boolean creative) {
		if (!playerAvailable) return Result.UNAVAILABLE;
		if (!enabled) return Result.DISABLED;
		if (spentPoints <= 0) return Result.NOTHING_TO_RESET;
		if (!creative && experienceLevel < cost) return Result.NOT_ENOUGH_LEVELS;
		return Result.OK;
	}
}
