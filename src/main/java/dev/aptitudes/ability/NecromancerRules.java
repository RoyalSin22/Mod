package dev.aptitudes.ability;

/**
 * v67 (F4): cifras de las 10 habilidades nuevas del Nigromante. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link NecromancerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class NecromancerRules {
	// N3 Mano de huesos
	public static final double HAND_RANGE = 16;
	// N4 Sacrificio
	public static final double SACRIFICE_RADIUS = 4;
	public static final float SACRIFICE_HEAL = 4f;
	// N5 Aura putrefacta
	public static final double AURA_RADIUS = 4;
	public static final int AURA_WEAKNESS_TICKS = 40;
	// N6 Tierra maldita
	public static final double GROUND_RADIUS = 5;
	public static final int GROUND_PULSES = 8;
	public static final double GROUND_LIFESTEAL = 0.25;
	public static final double GROUND_RANGE = 12;
	// N7 Resurgir
	public static final int RESURGE_COOLDOWN_TICKS = 1200;
	public static final double RESURGE_HEALTH = 0.5;
	// N8 Frenesí necrótico
	public static final double FRENZY_RANGE = 16;
	// N10 Tormenta de almas
	public static final int STORM_PULSES = 10;
	public static final int STORM_INTERVAL_TICKS = 10;
	public static final double STORM_RADIUS = 10;
	public static final double STORM_DAMAGE = 3;
	public static final float STORM_HEAL = 0.5f;
	public static final float STORM_HEAL_MAX = 10f;

	private NecromancerRules() {
	}

	/** N1: radio en el que tus esbirros acuden al objetivo (12 / 16 / 20 bloques). */
	public static double orderRange(int rank) {
		return rank <= 0 ? 0 : 8 + 4 * rank;
	}

	/** N2: vida que recuperas cuando un esbirro mata (1 / 2 / 3). */
	public static float banquetHeal(int rank) {
		return Math.max(0, rank);
	}

	/** N2: maná que recuperas cuando un esbirro mata (4 / 6 / 8). */
	public static double banquetMana(int rank) {
		return rank <= 0 ? 0 : 2 + 2 * rank;
	}

	/** N3: daño de la mano (4 / 5 / 6). */
	public static double handDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N3: ticks que el enemigo queda inmóvil (2 / 3 / 4 s). */
	public static int rootTicks(int rank) {
		return 20 * (1 + Math.max(0, rank));
	}

	/** N4: daño del sacrificio (8 / 10 / 12). */
	public static double sacrificeDamage(int rank, double scale) {
		return (6 + 2 * rank) * scale;
	}

	/** N5: daño por segundo del aura (1 / 2). */
	public static double auraDamage(int rank, double scale) {
		return Math.max(0, rank) * scale;
	}

	/** N6: daño por segundo de la tierra maldita (3 / 4). */
	public static double groundDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N7: ¿el esbirro sobrevive? (iba a morir y Resurgir está lista). */
	public static boolean resurges(double health, double amount, boolean ready) {
		return ready && amount >= health;
	}

	/** N8: duración del frenesí en ticks (10 / 14 s). */
	public static int frenzyTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 4 * rank) * 20;
	}

	/** N9: parte del daño recibido que absorbe el esbirro (15 / 25 %). */
	public static double boneShare(int rank) {
		return rank <= 0 ? 0 : 0.05 + 0.10 * rank;
	}

	/** N10: curación de un golpe de la tormenta, sin pasar del total de 10. */
	public static float stormHeal(float alreadyHealed) {
		return Math.max(0f, Math.min(STORM_HEAL, STORM_HEAL_MAX - alreadyHealed));
	}
}
