package dev.aptitudes.ability;

/**
 * v67 (F6): cifras de las 10 habilidades nuevas del Cambiaformas. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link ShapeshifterKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class ShapeshifterRules {
	// N1 Metabolismo animal
	public static final int METABOLISM_INTERVAL_TICKS = 60;
	// N3 Mordisco salvaje
	public static final double BITE_RANGE = 4;
	// N5 Piel de repuesto
	public static final int SKIN_TICKS = 100;
	// N6 Zarpazo múltiple
	public static final int CLAW_SWIPES = 4;
	public static final int CLAW_INTERVAL_TICKS = 5;
	public static final double CLAW_RANGE = 5;
	public static final double CLAW_MIN_DOT = 0.6;
	// N7 Presa fácil
	public static final double EASY_PREY_BONUS = 0.40;
	public static final int EASY_PREY_WINDOW_TICKS = 600;
	// N8 Furia de la bestia
	public static final double FURY_DAMAGE = 0.30;
	public static final double FURY_ATTACK_SPEED = 0.20;
	// N10 Estampida primigenia
	public static final int STAMPEDE_PULSES = 10;
	public static final int STAMPEDE_INTERVAL_TICKS = 10;
	public static final double STAMPEDE_RADIUS = 4;
	public static final double STAMPEDE_DAMAGE = 8;

	private ShapeshifterRules() {
	}

	/** N1: vida que recuperas por intervalo (0,5 / 1 / 1,5). */
	public static float metabolismHeal(int rank) {
		return 0.5f * Math.max(0, rank);
	}

	/** N2: reducción de daño transformado (4 / 8 / 12 %). */
	public static double coldBlood(int rank) {
		return 0.04 * Math.max(0, rank);
	}

	/** N3: daño del mordisco (6 / 8 / 10). */
	public static double biteDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N3: vida que te cura el mordisco (2 / 3 / 4). */
	public static float biteHeal(int rank) {
		return 1f + rank;
	}

	/** N4: duración de la velocidad tras el cambio (3 / 4 / 5 s). */
	public static int swiftTicks(int rank) {
		return (2 + rank) * 20;
	}

	/** N4: índice de la forma siguiente. Devuelve {@code -1} para volver a humano tras la última. */
	public static int nextForm(int currentIndex, int formCount) {
		if (formCount <= 0) return -1;
		int next = currentIndex + 1;
		return next >= formCount ? -1 : next;
	}

	/** N5: absorción al volver a humano (4 / 6). */
	public static float skinAbsorption(int rank) {
		return rank <= 0 ? 0f : 2f + 2f * rank;
	}

	/** N6: daño de cada zarpazo (5 / 6). */
	public static double swipeDamage(int rank, double scale) {
		return (4 + rank) * scale;
	}

	/** N8: duración de la furia (8 / 10 s). */
	public static int furyTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: multiplicador del coste del siguiente poder de forma (−20 / −40 %). */
	public static double thirdChangeCost(int rank) {
		return 1.0 - 0.20 * Math.max(0, Math.min(2, rank));
	}

	/** N10: daño de cada embestida. */
	public static double stampedeDamage(double scale) {
		return STAMPEDE_DAMAGE * scale;
	}
}
