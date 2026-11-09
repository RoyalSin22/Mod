package dev.aptitudes.boss;

import java.util.Set;

/**
 * Fase A del plan de avance: postura de jefes, élites y campeones. Puro (sin Minecraft); los tiempos van en ticks.
 *
 * <p>Al vaciar la postura, la entidad queda aturdida {@link #STUN_TICKS} ticks, con daño recibido aumentado, y
 * después {@link #IMMUNITY_TICKS} ticks sin poder volver a romperse. Esa inmunidad evita aturdimientos encadenados.
 * Los valores son provisionales: pendientes de calibrar con el autor.</p>
 */
public final class PostureRules {
	/** Duración del aturdimiento tras romper la postura (3 s). */
	public static final int STUN_TICKS = 60;
	/** Tras el aturdimiento, tiempo en el que no puede romperse (10 s). */
	public static final int IMMUNITY_TICKS = 200;
	/** Multiplicador del daño recibido mientras está aturdido. */
	public static final double VULNERABLE_MULTIPLIER = 1.5;

	/**
	 * Estado de postura. {@code stunnedUntil} e {@code immuneUntil} son instantes en ticks.
	 */
	public record Posture(double current, double max, long stunnedUntil, long immuneUntil) {
		public Posture {
			max = Math.max(1, max);
			current = Math.max(0, Math.min(max, current));
			stunnedUntil = Math.max(0, stunnedUntil);
			immuneUntil = Math.max(0, immuneUntil);
		}

		public static Posture fresh(double max) {
			return new Posture(max, max, 0, 0);
		}

		public boolean stunned(long now) {
			return now < stunnedUntil;
		}

		public boolean immune(long now) {
			return now < immuneUntil;
		}

		/** Multiplicador del daño recibido en este instante. */
		public double damageMultiplier(long now) {
			return stunned(now) ? VULNERABLE_MULTIPLIER : 1.0;
		}
	}

	/** Resultado de un golpe de postura: nuevo estado y si se rompió en este golpe. */
	public record Result(Posture state, boolean broke) {}

	private PostureRules() {}

	/**
	 * Drena la postura con un ataque. No tiene efecto si no hay daño, si está aturdido o si tiene inmunidad.
	 *
	 * @param amount daño de postura base del golpe (antes del multiplicador de etiquetas)
	 * @param tags   etiquetas del ataque
	 * @param now    tick actual
	 */
	public static Result drain(Posture p, double amount, Set<AttackTags.Tag> tags, long now) {
		if (amount <= 0 || p.stunned(now) || p.immune(now)) return new Result(p, false);
		double left = p.current() - amount * AttackTags.postureMultiplier(tags);
		if (left > 0) return new Result(new Posture(left, p.max(), p.stunnedUntil(), p.immuneUntil()), false);
		long stunEnd = now + STUN_TICKS;
		Posture broken = new Posture(p.max(), p.max(), stunEnd, stunEnd + IMMUNITY_TICKS);
		return new Result(broken, true);
	}

	/** Recupera postura con el tiempo; no recupera mientras está aturdido. */
	public static Posture recover(Posture p, double perTick, long now) {
		if (p.stunned(now) || perTick <= 0) return p;
		return new Posture(p.current() + perTick, p.max(), p.stunnedUntil(), p.immuneUntil());
	}
}
