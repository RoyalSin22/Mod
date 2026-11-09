package dev.aptitudes.boss;

import java.util.Set;

/**
 * Fase B3 del plan de avance: guardia y parry frente a los ataques de los jefes. Puro (sin Minecraft).
 *
 * <p>Decisión de diseño: un ataque {@link AttackTags.Tag#PARRYABLE} se desvía si el jugador está bloqueando en el
 * instante del impacto. Eso sustituye a una ventana medida desde que se levantó el escudo, que requeriría un seguimiento
 * de tiempo de uso que todavía no existe en el proyecto. Un ataque no desviable sigue el camino normal: el escudo de
 * Minecraft reduce el daño como ya lo hace.</p>
 *
 * <p>El parry no anula la postura del jefe: la reduce en {@link #PARRY_POSTURE_DAMAGE}, así que varios parrys seguidos
 * pueden romperla.</p>
 */
public final class GuardRules {
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

	/** Decide el resultado de un golpe según sus etiquetas y si el jugador está bloqueando en el impacto. */
	public static Outcome resolve(Set<AttackTags.Tag> tags, boolean blockingAtImpact) {
		return blockingAtImpact && AttackTags.parryable(tags) ? Outcome.PARRY : Outcome.HIT;
	}
}
