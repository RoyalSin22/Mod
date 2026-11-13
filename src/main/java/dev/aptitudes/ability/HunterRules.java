package dev.aptitudes.ability;

/**
 * v67 (F6): cifras de las 10 habilidades nuevas del Cazador. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link HunterKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class HunterRules {
	// N1 Mira certera
	public static final double STEADY_DISTANCE = 12;
	// N2 Presa herida
	public static final double WOUNDED_THRESHOLD = 0.5;
	// N3 Red de caza
	public static final double NET_RANGE = 20;
	public static final double NET_RADIUS = 3;
	// N4 Flecha explosiva
	public static final double ARROW_RANGE = 32;
	public static final double ARROW_RADIUS = 3;
	// N6 Disparo en abanico
	public static final int FAN_VOLLEYS = 5;
	public static final int FAN_INTERVAL_TICKS = 3;
	public static final double FAN_RANGE = 8;
	public static final double FAN_MIN_DOT = 0.7;
	// N7 Cazador paciente
	public static final int PATIENT_TICKS = 60;
	public static final double PATIENT_BONUS = 0.50;
	// N8 Vista de halcón
	public static final double FALCON_SIGHT_BONUS = 0.25;
	// N9 Botín de caza
	public static final int LOOT_XP_PER_RANK = 4;
	// N10 Cacería final
	public static final double FINAL_RADIUS = 20;
	public static final int FINAL_MARK_TICKS = 120;
	public static final int FINAL_MARK_PERCENT = 15;
	public static final int FINAL_TARGETS = 10;
	public static final int FINAL_INTERVAL_TICKS = 4;
	public static final double FINAL_DAMAGE = 8;

	private HunterRules() {
	}

	/** N1: bonificación a larga distancia (6 / 12 / 18 %). */
	public static double steadyBonus(int rank, double distance) {
		return distance >= STEADY_DISTANCE ? 0.06 * Math.max(0, rank) : 0;
	}

	/** N2: bonificación contra enemigos con menos de la mitad de vida (4 / 8 / 12 %). */
	public static double woundedBonus(int rank, double health, double maxHealth) {
		return maxHealth > 0 && health / maxHealth < WOUNDED_THRESHOLD ? 0.04 * Math.max(0, rank) : 0;
	}

	/** N3: duración de la inmovilidad (2 / 3 / 4 s). */
	public static int netTicks(int rank) {
		return (1 + rank) * 20;
	}

	/** N4: daño de la flecha explosiva (6 / 8 / 10). */
	public static double arrowDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: parte del daño que reciben los de alrededor. */
	public static double arrowSplashFraction() {
		return 0.5;
	}

	/** N5: multiplicador de la duración de las trampas (×1,5 / ×2). */
	public static double trapFactor(int rank) {
		return rank <= 0 ? 1.0 : 1.0 + 0.5 * Math.min(2, rank);
	}

	/** N6: daño de cada flecha del abanico (5 / 6). */
	public static double fanDamage(int rank, double scale) {
		return (4 + rank) * scale;
	}

	/** N8: duración de la vista de halcón (8 / 10 s). */
	public static int sightTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: experiencia extra al matar a una presa marcada (4 / 8). */
	public static int lootExperience(int rank) {
		return LOOT_XP_PER_RANK * Math.max(0, rank);
	}

	/** N10: daño de cada ataque de la cacería final. */
	public static double finalDamage(double scale) {
		return FINAL_DAMAGE * scale;
	}
}
