package dev.aptitudes.ability;

/**
 * v67 (F6): cifras de las 10 habilidades nuevas del Invocador de armas. Lógica pura (sin Minecraft), con pruebas; la
 * usa {@link WeaponSummonerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class WeaponSummonerRules {
	// N2 Equilibrio
	public static final int BALANCE_MIN_BLADES = 3;
	// N3 Espada boomerang
	public static final double BOOMERANG_RANGE = 16;
	public static final double BOOMERANG_WIDTH = 1.3;
	public static final int BOOMERANG_RETURN_TICKS = 10;
	// N4 Muro de espadas
	public static final double WALL_DISTANCE = 3;
	public static final double WALL_HALF_WIDTH = 3;
	public static final double WALL_HALF_THICKNESS = 1.2;
	public static final int WALL_PULSES = 4;
	public static final int WALL_INTERVAL_TICKS = 20;
	// N5 Hoja de repuesto
	public static final double SPARE_THRESHOLD = 0.30;
	public static final int SPARE_COOLDOWN_TICKS = 1200;
	// N6 Danza de acero
	public static final int DANCE_PULSES = 10;
	public static final int DANCE_INTERVAL_TICKS = 10;
	public static final double DANCE_RADIUS = 4;
	// N7 Filo perfecto
	public static final int PERFECT_EVERY = 10;
	public static final double PERFECT_BONUS = 1.0;
	// N8 Armería viva
	public static final int LIVING_EXTRA_CAP = 2;
	// N10 Juicio de acero
	public static final double JUDGMENT_RANGE = 24;
	public static final int JUDGMENT_EXTRA_SWORDS = 5;
	public static final int JUDGMENT_INTERVAL_TICKS = 2;
	public static final double JUDGMENT_DAMAGE = 6;
	public static final double JUDGMENT_SPLASH = 3;
	public static final double JUDGMENT_RADIUS = 4;

	private WeaponSummonerRules() {
	}

	/** N1: bonificación al daño de las espadas (10 / 20 / 30 %). */
	public static double sharpBonus(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N2: bonificación cuerpo a cuerpo con espadas suficientes (4 / 8 / 12 %). */
	public static double balanceBonus(int rank, int blades) {
		return blades >= BALANCE_MIN_BLADES ? 0.04 * Math.max(0, rank) : 0;
	}

	/** N3: daño de la espada boomerang (6 / 8 / 10). */
	public static double boomerangDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: daño por segundo del muro (3 / 4 / 5). */
	public static double wallDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N5: ¿se invocan espadas de repuesto? Cae por debajo del umbral sin morir y está lista. */
	public static boolean spareTriggers(double health, double amount, double max, boolean ready) {
		if (!ready || max <= 0 || amount <= 0) return false;
		double after = health - amount;
		return after > 0 && after / max < SPARE_THRESHOLD;
	}

	/** N5: espadas que se invocan (3 / 5). */
	public static int spareBlades(int rank) {
		return rank <= 0 ? 0 : 1 + 2 * rank;
	}

	/** N6: daño de cada pulso de la danza (3 / 4). */
	public static double danceDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N7: ¿este golpe es el décimo? Devuelve la nueva cuenta (0 tras el golpe perfecto). */
	public static int perfectCount(int current) {
		return current + 1 >= PERFECT_EVERY ? 0 : current + 1;
	}

	/** N7: ¿el golpe con la cuenta {@code current} (antes de contarlo) es el décimo? */
	public static boolean perfectHit(int current) {
		return current + 1 >= PERFECT_EVERY;
	}

	/** N8: duración de la armería viva (8 / 10 s). */
	public static int livingTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: probabilidad de que la espada vuelva (30 / 50 %). */
	public static double echoChance(int rank) {
		return rank <= 0 ? 0 : 0.1 + 0.2 * rank;
	}

	/** N10: espadas que caen. */
	public static int judgmentSwords(int blades) {
		return Math.max(0, blades) + JUDGMENT_EXTRA_SWORDS;
	}

	public static double judgmentDamage(double scale) {
		return JUDGMENT_DAMAGE * scale;
	}

	public static double judgmentSplash(double scale) {
		return JUDGMENT_SPLASH * scale;
	}
}
