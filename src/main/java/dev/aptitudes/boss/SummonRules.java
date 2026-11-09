package dev.aptitudes.boss;

/**
 * Fase C del plan de avance: reinvocación de jefes. Puro (sin Minecraft); los tiempos van en ticks.
 *
 * <p>Regla: un jefe puede reinvocarse cuando no hay otra instancia viva y han pasado {@link #COOLDOWN_TICKS} desde su
 * última derrota. Un jefe que nunca se derrotó no tiene espera. El cómo se invoca (objeto, altar o estructura) y su
 * coste todavía no están decididos: esta regla solo fija el tiempo de espera.</p>
 */
public final class SummonRules {
	/** Espera tras una derrota (20 minutos). Provisional: pendiente de validar con el autor. */
	public static final long COOLDOWN_TICKS = 24_000L;

	/** Sin derrota registrada. */
	public static final long NEVER = -1L;

	private SummonRules() {}

	/**
	 * ¿Puede reinvocarse ahora?
	 *
	 * @param alive          hay ya una instancia viva del jefe
	 * @param lastDefeatTick tick de la última derrota, o {@link #NEVER}
	 * @param now            tick actual
	 */
	public static boolean canSummon(boolean alive, long lastDefeatTick, long now) {
		return !alive && remainingTicks(lastDefeatTick, now) == 0;
	}

	/** Ticks que faltan para poder reinvocarlo (0 = ya se puede). */
	public static long remainingTicks(long lastDefeatTick, long now) {
		if (lastDefeatTick == NEVER) return 0;
		long elapsed = Math.max(0, now - lastDefeatTick);
		return Math.max(0, COOLDOWN_TICKS - elapsed);
	}
}
