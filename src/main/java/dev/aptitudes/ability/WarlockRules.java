package dev.aptitudes.ability;

/**
 * v67 (F7): cifras de las 10 habilidades nuevas del Brujo abisal. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link WarlockKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class WarlockRules {
	// N3 Mal de ojo
	public static final double EVIL_EYE_RANGE = 24;
	public static final int EVIL_EYE_BLIND_TICKS = 40;
	public static final int EVIL_EYE_DELAY_TICKS = 60;
	// N4 Cadenas de sombra
	public static final double CHAINS_RANGE = 20;
	public static final double CHAINS_RADIUS = 4;
	public static final int CHAINS_WITHER_TICKS = 60;
	// N5 Ansia de alma
	public static final int CRAVING_TICKS = 120;
	// N6 Nube de pestilencia
	public static final double CLOUD_RANGE = 20;
	public static final double CLOUD_RADIUS = 5;
	public static final int CLOUD_PULSES = 6;
	public static final int CLOUD_INTERVAL_TICKS = 20;
	// N7 Desesperación
	public static final double DESPERATION_THRESHOLD = 0.30;
	public static final int DESPERATION_FREE_TICKS = 100;
	public static final int DESPERATION_COOLDOWN_TICKS = 1800;
	// N8 Forma demoníaca
	public static final double DEMON_DAMAGE = 0.25;
	public static final float DEMON_HEAL = 1f;
	// N9 Cosecha oscura
	public static final double HARVEST_RADIUS = 3;
	// N10 Fosa abisal
	public static final double PIT_RANGE = 20;
	public static final double PIT_RADIUS = 6;
	public static final int PIT_PULSES = 6;
	public static final int PIT_INTERVAL_TICKS = 20;
	public static final double PIT_DAMAGE = 6;
	public static final float PIT_HEAL = 1f;

	private WarlockRules() {
	}

	/** N1: bonificación contra enemigos con un efecto negativo (4 / 8 / 12 %). */
	public static double cursedBonus(int rank) {
		return 0.04 * Math.max(0, Math.min(3, rank));
	}

	/** N2: maná que recuperas cuando un hechizo te cuesta vida (3 / 5 / 7). */
	public static double bloodMana(int rank) {
		return rank <= 0 ? 0 : 1 + 2 * rank;
	}

	/** N3: daño del mal de ojo (8 / 10 / 12). */
	public static double evilEyeDamage(int rank, double scale) {
		return (6 + 2 * rank) * scale;
	}

	/** N4: duración de las cadenas (3 / 4 / 5 s). */
	public static int chainsTicks(int rank) {
		return (2 + rank) * 20;
	}

	/** N5: bonificación tras matar (20 / 30 %). */
	public static double cravingBonus(int rank) {
		return rank <= 0 ? 0 : 0.10 + 0.10 * rank;
	}

	/** N6: daño por segundo de la nube (3 / 4). */
	public static double cloudDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N7: ¿la desesperación se activa? Por debajo del umbral y lista. */
	public static boolean desperationTriggers(double health, double max, boolean ready) {
		return ready && max > 0 && health / max < DESPERATION_THRESHOLD;
	}

	/** N8: duración de la forma demoníaca (8 / 10 s). */
	public static int demonTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: daño de la cosecha (3 / 5). */
	public static double harvestDamage(int rank, double scale) {
		return rank <= 0 ? 0 : (1 + 2 * rank) * scale;
	}

	/** N10: daño por segundo de la fosa. */
	public static double pitDamage(double scale) {
		return PIT_DAMAGE * scale;
	}
}
