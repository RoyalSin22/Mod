package dev.aptitudes.ability;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * v67 (F3): cifras de las 10 habilidades nuevas del Mago. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link MageKit}. {@code rank} es el rango efectivo (puede superar el del árbol con evolución, Maestría y recurso).
 * El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class MageRules {
	// N1 Plenitud arcana
	public static final double PLENITUDE_MANA = 0.80;
	// N3 Rayo encadenado
	public static final double CHAIN_RANGE = 6;
	public static final double CHAIN_START_RANGE = 24;
	// N4 Escudo especular
	public static final int MIRROR_TICKS = 60;
	// N5 Concatenación
	public static final int CONCAT_NEEDED = 3;
	public static final int CONCAT_WINDOW_TICKS = 160;
	// N6 Tormenta de granizo
	public static final int HAIL_PULSES = 6;
	public static final double HAIL_RADIUS = 5;
	// N7 Reserva de emergencia
	public static final double EMERGENCY_BELOW = 0.10;
	public static final double EMERGENCY_RESTORE = 0.40;
	public static final int EMERGENCY_COOLDOWN_TICKS = 1200;
	// N8 Forma de arcano puro
	public static final double PURE_COST = 0.5;
	public static final double PURE_DAMAGE = 0.20;
	public static final double PURE_END_ENERGY = 0.20;
	// N9 Maná cristalizado
	public static final double CRYSTAL_PER_CHARGE = 100;
	public static final int CRYSTAL_MAX_CHARGES = 3;
	public static final double CRYSTAL_RADIUS = 3;
	// N10 Nova estelar
	public static final int NOVA_CHARGE_TICKS = 30;
	public static final double NOVA_RADIUS = 10;
	public static final double NOVA_DAMAGE = 18;
	public static final double NOVA_REFUND_PER_ENEMY = 5;
	public static final double NOVA_REFUND_MAX = 40;

	private MageRules() {
	}

	/** N1: bonificación de daño mágico (+4 / 8 / 12 %) con el maná al 80 % o más. */
	public static double plenitudeBonus(int rank, double energy, double maxEnergy) {
		if (rank <= 0 || maxEnergy <= 0 || energy < maxEnergy * PLENITUDE_MANA) return 0;
		return 0.04 * rank;
	}

	/** N2: ticks de Lentitud (2 / 3 / 4 s). */
	public static int frostTicks(int rank) {
		return rank <= 0 ? 0 : 20 * (rank + 1);
	}

	/** N3: enemigos que alcanza el rayo (3 / 4 / 5). */
	public static int chainTargets(int rank) {
		return rank <= 0 ? 0 : rank + 2;
	}

	/** N3: daño por salto (4 / 5 / 6). */
	public static double chainDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N4: multiplicador del golpe devuelto (×1,5 / 2 / 2,5). */
	public static double mirrorMultiplier(int rank) {
		return 1.0 + 0.5 * rank;
	}

	/** N6: daño por pulso (3 / 4). */
	public static double hailDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N7: ¿hay que reponer maná? (por debajo del 10 % y fuera de espera). */
	public static boolean emergency(double energy, double maxEnergy, long now, long readyAt) {
		return maxEnergy > 0 && energy < maxEnergy * EMERGENCY_BELOW && now >= readyAt;
	}

	/** N8: duración de la forma en ticks (8 / 10 s). */
	public static int pureTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N8: maná que queda al terminar (el 20 % del máximo, o menos si ya tenías menos). */
	public static double pureEndEnergy(double energy, double maxEnergy) {
		return Math.min(energy, maxEnergy * PURE_END_ENERGY);
	}

	/** N9: daño de la explosión (6 / 9). */
	public static double crystalDamage(int rank, double scale) {
		return (3 + 3 * rank) * scale;
	}

	/** N10: maná devuelto por los enemigos alcanzados (5 cada uno, máx. 40). */
	public static double novaRefund(int enemies) {
		return Math.min(NOVA_REFUND_MAX, NOVA_REFUND_PER_ENEMY * Math.max(0, enemies));
	}

	/**
	 * N5: cuenta activas distintas lanzadas en una ventana de 8 s. Con 3 distintas, la siguiente sale gratis
	 * ({@link #take()}). Un mismo hechizo repetido no cuenta dos veces.
	 */
	public static final class ConcatTracker {
		private record Cast(String key, long at) {}

		private final Deque<Cast> recent = new ArrayDeque<>();
		private boolean ready;

		/** Registra una activa lanzada en el tick {@code now}. */
		public void record(String key, long now) {
			if (ready) return; // ya hay una gratis pendiente
			recent.removeIf(c -> now - c.at() > CONCAT_WINDOW_TICKS);
			recent.addLast(new Cast(key, now));
			Set<String> distinct = new HashSet<>();
			for (Cast c : recent) distinct.add(c.key());
			if (distinct.size() >= CONCAT_NEEDED) {
				ready = true;
				recent.clear();
			}
		}

		public boolean ready() {
			return ready;
		}

		/** Gasta la activa gratis. */
		public void take() {
			ready = false;
			recent.clear();
		}

		public void clear() {
			ready = false;
			recent.clear();
		}
	}

	/** N9: maná gastado acumulado; cada 100 da una carga (máx. 3). */
	public static final class CrystalMeter {
		private double carry;
		private int charges;

		public void add(double spent) {
			if (spent <= 0) return;
			carry += spent;
			while (carry >= CRYSTAL_PER_CHARGE) {
				carry -= CRYSTAL_PER_CHARGE;
				charges = Math.min(CRYSTAL_MAX_CHARGES, charges + 1);
			}
		}

		public int charges() {
			return charges;
		}

		/** Gasta una carga; false si no había. */
		public boolean take() {
			if (charges <= 0) return false;
			charges--;
			return true;
		}
	}
}
