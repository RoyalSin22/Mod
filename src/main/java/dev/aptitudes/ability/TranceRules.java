package dev.aptitudes.ability;

/**
 * Fase F2 del plan de avance: trance de combate (sintonía). Puro (sin Minecraft); los tiempos van en ticks.
 *
 * <p>La sintonía se llena con golpes críticos y habilidades. Al llegar a {@link #MAX_GAUGE} el jugador entra en trance
 * durante {@link #DURATION_TICKS} ticks (10 s) y la sintonía vuelve a cero. Valores elegidos por el diseño (criterio del
 * autor delegado en el diseño), pendientes de validar en partida.</p>
 */
public final class TranceRules {
	/** Sintonía máxima. */
	public static final int MAX_GAUGE = 100;
	/** Duración del trance: 10 s. */
	public static final int DURATION_TICKS = 200;
	/** Sintonía que da un golpe crítico. */
	public static final int GAIN_CRITICAL = 10;
	/** Sintonía que da usar una habilidad. */
	public static final int GAIN_SKILL = 15;
	/** Sin trance activo. */
	public static final long NONE = -1L;

	private TranceRules() {}

	/** Suma sintonía sin pasar del máximo ni bajar de cero. */
	public static int gain(int gauge, int amount) {
		int current = Math.max(0, Math.min(MAX_GAUGE, gauge));
		return Math.min(MAX_GAUGE, current + Math.max(0, amount));
	}

	/** ¿Está llena la sintonía? */
	public static boolean full(int gauge) {
		return gauge >= MAX_GAUGE;
	}

	/** ¿Sigue activo el trance iniciado en {@code startTick}? */
	public static boolean active(long startTick, long now) {
		return startTick != NONE && now >= startTick && now - startTick < DURATION_TICKS;
	}
}
