package dev.aptitudes.combat;

import dev.aptitudes.boss.GuardRules;

/**
 * C7 (S3): parada y golpe de gracia contra criaturas. Puro (sin Minecraft); los tiempos van en ticks del servidor.
 *
 * <p>Amplía la guardia perfecta de los jefes ({@link GuardRules}, fase B3) al resto de criaturas: un golpe cuerpo a
 * cuerpo que llega de frente mientras el escudo lleva como mucho {@link GuardRules#PERFECT_WINDOW_TICKS} ticks levantado
 * se desvía. Contra una criatura normal la aturde; contra un élite o campeón le quita postura (como a los jefes). En los
 * dos casos la criatura queda «expuesta»: el siguiente golpe cuerpo a cuerpo del mismo jugador es un golpe de gracia.</p>
 *
 * <p>Los jefes animados no pasan por aquí: ya tienen su parada propia en {@code AnimatedBossEntity}.</p>
 */
public final class ParryRules {
	/**
	 * Tras un aturdimiento, ticks en los que la misma criatura no puede volver a aturdirse (3 s). Evita que un grupo
	 * la encadene sin fin. Provisional: validar en partida.
	 */
	public static final int STUN_IMMUNITY_TICKS = 60;

	/** Qué tipo de criatura ataca. */
	public enum Attacker { NORMAL, ELITE, BOSS }

	/** Resultado de un golpe recibido con el escudo. */
	public enum Outcome {
		/** No hay parada: el golpe sigue su curso normal (vanilla lo bloquea o no). */
		NONE,
		/** Parada contra una criatura normal: sin daño, aturdimiento y golpe de gracia. */
		STUN,
		/** Parada contra un élite o campeón: sin daño, postura y golpe de gracia. */
		POSTURE
	}

	/**
	 * Golpe recibido.
	 *
	 * @param melee            el atacante golpea con su cuerpo (la fuente directa es el propio atacante)
	 * @param bypassesShield   el tipo de daño ignora los escudos (etiqueta vanilla {@code bypasses_shield})
	 * @param fromFront        llega por delante del jugador (ver {@link #fromFront})
	 * @param ticksSinceRaised ticks desde que el escudo se levantó, o {@link GuardRules#NOT_BLOCKING}
	 */
	public record Hit(Attacker attacker, boolean melee, boolean bypassesShield, boolean fromFront, int ticksSinceRaised) {}

	private ParryRules() {}

	/** ¿El escudo está dentro de la ventana de guardia perfecta? Misma ventana que contra los jefes. */
	public static boolean inWindow(int ticksSinceRaised) {
		return ticksSinceRaised >= 0 && ticksSinceRaised <= GuardRules.PERFECT_WINDOW_TICKS;
	}

	public static Outcome resolve(boolean enabled, Hit hit) {
		if (!enabled || hit.attacker() == Attacker.BOSS) return Outcome.NONE;
		if (!hit.melee() || hit.bypassesShield() || !hit.fromFront() || !inWindow(hit.ticksSinceRaised())) {
			return Outcome.NONE;
		}
		return hit.attacker() == Attacker.ELITE ? Outcome.POSTURE : Outcome.STUN;
	}

	/**
	 * ¿El atacante está delante del jugador? Misma regla que el escudo vanilla ({@code LivingEntity.blockedByShield}):
	 * vector horizontal atacante → jugador normalizado, producto escalar con la mirada (cabeza, sin inclinación)
	 * negativo. Si ocupan el mismo punto no se considera de frente.
	 */
	public static boolean fromFront(double attackerX, double attackerZ, double playerX, double playerZ, float headYaw) {
		double dx = playerX - attackerX;
		double dz = playerZ - attackerZ;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-4) return false;
		double yaw = Math.toRadians(headYaw);
		double lookX = -Math.sin(yaw);
		double lookZ = Math.cos(yaw);
		return (dx / length) * lookX + (dz / length) * lookZ < 0.0;
	}

	/** ¿Se puede aturdir ahora? No si sigue aturdida o dentro de la inmunidad posterior. */
	public static boolean canStun(long now, long immuneUntil) {
		return now >= immuneUntil;
	}

	/** Fin de la inmunidad de un aturdimiento que empieza en {@code now} y dura {@code stunTicks}. */
	public static long immuneUntil(long now, int stunTicks) {
		return now + Math.max(0, stunTicks) + STUN_IMMUNITY_TICKS;
	}

	/** ¿Sigue aturdida? {@code stunnedUntil} es el primer tick sin aturdimiento. */
	public static boolean stunned(long now, long stunnedUntil) {
		return now < stunnedUntil;
	}

	/**
	 * Multiplicador del golpe de gracia: se aplica si quien golpea es quien hizo la parada y aún no ha caducado
	 * ({@code exposedUntil} incluido); si no, 1.
	 */
	public static double graceMultiplier(boolean sameAttacker, long now, long exposedUntil, double configured) {
		if (!sameAttacker || now > exposedUntil) return 1.0;
		return Math.max(1.0, configured);
	}
}
