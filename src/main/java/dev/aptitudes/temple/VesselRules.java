package dev.aptitudes.temple;

/**
 * Vasos de vida (v64, K6): +1 corazón permanente por vaso, con tope por jugador. Puro (JUnit).
 *
 * <p>Cada vaso lleva un id único. Usarlo lo registra en el mundo, así que una copia del mismo vaso (por un fallo
 * de duplicación o un objeto copiado en creativo) se rechaza aunque la use otro jugador.</p>
 */
public final class VesselRules {
	public static final int DEFAULT_MAX = 5;
	public static final int MAX_LIMIT = 20;
	/** Puntos de vida por vaso (2 = un corazón). */
	public static final double HEALTH_PER_VESSEL = 2.0;

	public enum Result { OK, AT_LIMIT, ALREADY_USED, DISABLED }

	private VesselRules() {}

	public static int limit(int configured) {
		return Math.max(0, Math.min(MAX_LIMIT, configured));
	}

	public static Result use(int used, int configuredMax, boolean vesselAlreadyConsumed) {
		int max = limit(configuredMax);
		if (max == 0) return Result.DISABLED;
		if (vesselAlreadyConsumed) return Result.ALREADY_USED;
		if (used >= max) return Result.AT_LIMIT;
		return Result.OK;
	}

	/** Vida extra aplicada: los vasos por encima de un tope rebajado se conservan, pero no cuentan. */
	public static double bonus(int used, int configuredMax) {
		return HEALTH_PER_VESSEL * Math.max(0, Math.min(used, limit(configuredMax)));
	}
}
