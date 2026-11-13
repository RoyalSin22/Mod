package dev.aptitudes.ability;

/**
 * v67 (F4): cifras de las 10 habilidades nuevas del Paladín. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link PaladinKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class PaladinRules {
	// N2 Triunfo compartido
	public static final double TRIUMPH_RADIUS = 8;
	// N3 Rayo de luz
	public static final double RAY_RANGE = 24;
	public static final double UNDEAD_MULTIPLIER = 2.0;
	// N4 Golpe de escudo
	public static final double BASH_RANGE = 4;
	// N5 Oración
	public static final int PRAYER_STILL_TICKS = 40;
	public static final double PRAYER_RADIUS = 6;
	// N6 Muro de luz
	public static final double WALL_WIDTH = 8;
	public static final double WALL_THICKNESS = 1.0;
	public static final int WALL_TICKS = 120;
	// N7 Vínculo de escudo
	public static final double BOND_REDUCTION = 0.10;
	public static final double BOND_RADIUS = 4;
	// N8 Estandarte sagrado
	public static final double BANNER_RADIUS = 8;
	// N9 Ira justa
	public static final double WRATH_PER_HEALTH = 2.0;
	public static final double WRATH_CHARGE_CAP = 1.0;
	// N10 Columna de luz
	public static final double PILLAR_RADIUS = 5;
	public static final int PILLAR_PULSES = 6;
	public static final double PILLAR_DAMAGE = 6;
	public static final float PILLAR_HEAL = 3f;

	private PaladinRules() {
	}

	/** N1: reducción del daño de los no-muertos (8 / 16 / 24 %). */
	public static double undeadReduction(int rank) {
		return 0.08 * Math.max(0, rank);
	}

	/** N2: curación de los aliados al matar (2 / 3 / 4). */
	public static float triumphHeal(int rank) {
		return rank <= 0 ? 0 : 1f + rank;
	}

	/** N3: curación del rayo a un aliado (4 / 6 / 8). */
	public static float rayHeal(int rank) {
		return 2f + 2f * rank;
	}

	/** N3: daño del rayo a un enemigo (6 / 8 / 10), el doble contra no-muertos. */
	public static double rayDamage(int rank, boolean undead, double scale) {
		return (4 + 2 * rank) * (undead ? UNDEAD_MULTIPLIER : 1.0) * scale;
	}

	/** N4: daño del golpe de escudo (5 / 6 / 7). */
	public static double bashDamage(int rank, double scale) {
		return (4 + rank) * scale;
	}

	/** N4: aturdimiento en ticks (1 / 1,5 / 2 s). */
	public static int bashStunTicks(int rank) {
		return 10 + 10 * Math.max(0, rank);
	}

	/** N5: vida que se recupera por segundo (1 / 2). */
	public static float prayerHeal(int rank) {
		return Math.max(0, rank);
	}

	/** N6: daño por segundo del muro (3 / 4). */
	public static double wallDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N8: duración del estandarte en ticks (10 / 14 s). */
	public static int bannerTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 4 * rank) * 20;
	}

	/**
	 * N9: carga que añade un golpe recibido: por cada 10 % de vida máxima perdido, +20 %. Se acumula hasta +100 %.
	 */
	public static double wrathCharge(double current, double taken, double maxHealth) {
		if (taken <= 0 || maxHealth <= 0) return current;
		return Math.min(WRATH_CHARGE_CAP, current + taken / maxHealth * WRATH_PER_HEALTH);
	}

	/** N9: bonificación del siguiente golpe: la carga, hasta +30 / 50 %. */
	public static double wrathBonus(double charge, int rank) {
		return rank <= 0 ? 0 : Math.min(0.1 + 0.2 * rank, Math.max(0, charge));
	}

	/** N10: daño de la columna a un enemigo (6, el doble contra no-muertos). */
	public static double pillarDamage(boolean undead, double scale) {
		return PILLAR_DAMAGE * (undead ? UNDEAD_MULTIPLIER : 1.0) * scale;
	}
}
