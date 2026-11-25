package dev.aptitudes.logic;

import dev.aptitudes.boss.GuardRules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * C7 · S3 Parada y contraataque: reglas puras (sin Minecraft, con JUnit en {@code ParryRulesTest}).
 *
 * <p>Una <b>parada</b> es un golpe cuerpo a cuerpo de una criatura que el jugador bloquea con el escudo dentro de la
 * ventana de {@link GuardRules#PERFECT_WINDOW_TICKS} ticks desde que lo levantó (la misma «guardia perfecta» que usan los
 * jefes: una sola definición). Aturde al atacante y abre un <b>golpe de gracia</b>: el siguiente golpe directo del mismo
 * jugador a esa criatura, dentro de un plazo, hace daño extra. Solo afecta a criaturas: nunca a jugadores (sin PvP) ni a
 * jefes y campeones, que ya tienen su propio sistema de postura.</p>
 *
 * <p>Tiempos en ticks del servidor; los cálculos con valores de la configuración van en {@code long}.</p>
 */
public final class ParryRules {
	public static final int MAX_STUN_TICKS = 200;
	public static final int MIN_FINISHER_TICKS = 10;
	public static final int MAX_FINISHER_TICKS = 200;
	public static final double MIN_MULTIPLIER = 1.0;
	public static final double MAX_MULTIPLIER = 4.0;

	private ParryRules() {}

	/** ¿Cuenta como parada el bloqueo hecho {@code ticksSinceRaised} ticks después de levantar el escudo? */
	public static boolean inWindow(int ticksSinceRaised) {
		return ticksSinceRaised >= 0 && ticksSinceRaised <= GuardRules.PERFECT_WINDOW_TICKS;
	}

	/** ¿Puede esta criatura ser parada? Un golpe a distancia, un jugador o un jefe/campeón no abren parada. */
	public static boolean canParry(boolean attackerIsPlayer, boolean directMelee, boolean boss, boolean champion) {
		return !attackerIsPlayer && directMelee && !boss && !champion;
	}

	/** Daño del golpe de gracia: el multiplicador se acota a [1, 4] por seguridad ante una configuración absurda. */
	public static float finisherDamage(float amount, double multiplier) {
		if (Double.isNaN(multiplier)) return amount; // Math.min/max propagan NaN y el daño sería NaN
		double m = Math.max(MIN_MULTIPLIER, Math.min(MAX_MULTIPLIER, multiplier));
		return (float) (amount * m);
	}

	/** Tick en el que caduca el golpe de gracia (long: la duración viene de la configuración). */
	public static long expiresAt(long now, int finisherTicks) {
		return now + Math.max(MIN_FINISHER_TICKS, Math.min(MAX_FINISHER_TICKS, (long) finisherTicks));
	}

	/** Golpes de gracia abiertos: criatura → jugador que puede asestarlo y tick de caducidad. */
	public static final class Book {
		private record Mark(UUID player, long expiresAt) {}

		private final Map<UUID, Mark> marks = new HashMap<>();

		/** Abre (o renueva) el golpe de gracia sobre {@code target} para {@code player}. */
		public void open(UUID target, UUID player, long expiresAt) {
			marks.put(target, new Mark(player, expiresAt));
		}

		/** ¿Está abierto para este jugador y esta criatura? */
		public boolean isOpen(UUID target, UUID player, long now) {
			Mark m = marks.get(target);
			return m != null && m.player().equals(player) && now < m.expiresAt();
		}

		/** Gasta el golpe de gracia: devuelve {@code true} una sola vez por parada. */
		public boolean consume(UUID target, UUID player, long now) {
			if (!isOpen(target, player, now)) return false;
			marks.remove(target);
			return true;
		}

		public void forget(UUID target) {
			marks.remove(target);
		}

		/** Borra los caducados; se llama de vez en cuando para que el mapa no crezca. */
		public void prune(long now) {
			marks.values().removeIf(m -> now >= m.expiresAt());
		}

		public void clear() {
			marks.clear();
		}

		public int size() {
			return marks.size();
		}
	}
}
