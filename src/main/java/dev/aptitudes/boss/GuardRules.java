package dev.aptitudes.boss;

import java.util.Set;

/**
 * Fase B3 del plan de avance: guardia perfecta y parry frente a los ataques de los jefes. Puro (sin Minecraft).
 *
 * <p>Guardia perfecta: el jugador tiene el escudo levantado desde hace como mucho {@link #PERFECT_WINDOW_TICKS} ticks
 * en el momento del impacto. Un ataque {@link AttackTags.Tag#PARRYABLE} se desvía solo entonces. Si el escudo lleva más
 * tiempo levantado, el golpe llega y el escudo de Minecraft reduce el daño como siempre. Así el parry premia el momento
 * exacto, no mantener el escudo en alto.</p>
 *
 * <p>El parry no anula la postura del jefe: la reduce en {@link #PARRY_POSTURE_DAMAGE}, así que varios parrys
 * seguidos pueden romperla.</p>
 */
public final class GuardRules {
	/** Ventana de guardia perfecta: 4 ticks (200 ms) desde que se levantó el escudo. Provisional. */
	public static final int PERFECT_WINDOW_TICKS = 4;
	/** Valor de {@code ticksSinceRaised} cuando el jugador no está bloqueando. */
	public static final int NOT_BLOCKING = -1;
	/** Daño de postura que causa un parry exitoso (provisional: validar en partida). */
	public static final double PARRY_POSTURE_DAMAGE = 25;

	/** Resultado de un golpe de jefe frente a un jugador. */
	public enum Outcome {
		/** El golpe llega con normalidad. */
		HIT,
		/** El golpe se desvía: sin daño y con daño de postura para el jefe. */
		PARRY
	}

	private GuardRules() {}

	/**
	 * Decide el resultado de un golpe.
	 *
	 * @param tags           etiquetas del ataque
	 * @param ticksSinceRaised ticks desde que el jugador levantó el escudo, o {@link #NOT_BLOCKING}
	 */
	public static Outcome resolve(Set<AttackTags.Tag> tags, int ticksSinceRaised) {
		boolean perfect = ticksSinceRaised >= 0 && ticksSinceRaised <= PERFECT_WINDOW_TICKS;
		return perfect && AttackTags.parryable(tags) ? Outcome.PARRY : Outcome.HIT;
	}
}
