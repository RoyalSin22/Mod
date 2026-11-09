package dev.aptitudes.ability;

/**
 * Fase F1 del plan de avance: provocación (agro) de élites y campeones. Puro (sin Minecraft); los tiempos van en ticks.
 *
 * <p>Regla: solo élites y campeones pueden ser provocados, nunca jefes. Una provocación dura {@link #DURATION_TICKS} y
 * atrae al mob al jugador que la aplica mientras esté dentro de {@link #RADIUS} bloques. Después hay una espera de
 * {@link #COOLDOWN_TICKS} antes de poder volver a provocar. Valores provisionales, pendientes de validar en partida.</p>
 */
public final class TauntRules {
	/** Radio en bloques en el que el mob responde a la provocación. */
	public static final double RADIUS = 8.0;
	/** Duración de la provocación (6 s). */
	public static final int DURATION_TICKS = 120;
	/** Espera tras provocar (20 s). */
	public static final int COOLDOWN_TICKS = 400;
	/** Sin provocación activa. */
	public static final long NONE = -1L;

	private TauntRules() {}

	/** ¿Puede este mob ser provocado? Solo élites y campeones; nunca jefes. */
	public static boolean eligible(boolean elite, boolean boss) {
		return elite && !boss;
	}

	/** ¿Puede el jugador provocar ahora? */
	public static boolean canTaunt(long readyAt, long now) {
		return now >= readyAt;
	}

	/** Tick en el que el jugador puede volver a provocar tras haber provocado en {@code now}. */
	public static long readyAfter(long now) {
		return now + COOLDOWN_TICKS;
	}

	/** ¿Sigue activa una provocación iniciada en {@code startTick}? */
	public static boolean isTaunting(long startTick, long now) {
		return startTick != NONE && now >= startTick && now - startTick < DURATION_TICKS;
	}

	/** ¿Debe el mob cambiar su objetivo al provocador? Requiere provocación activa y estar dentro del radio. */
	public static boolean shouldRetarget(boolean taunting, double distance) {
		return taunting && distance <= RADIUS;
	}
}
