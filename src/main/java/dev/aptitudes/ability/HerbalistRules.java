package dev.aptitudes.ability;

/**
 * v67 (F3): cifras de las 10 habilidades nuevas del Herbolista. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link HerbalistKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class HerbalistRules {
	// N2 Polinización
	public static final int POLLEN_SPEED_TICKS = 100;
	// N3 Semilla explosiva
	public static final double SEED_RANGE = 14;
	public static final int SEED_FUSE_TICKS = 40;
	public static final double SEED_RADIUS = 3;
	public static final int SEED_POISON_TICKS = 100;
	public static final int BUSH_SECONDS = 4;
	public static final float BUSH_HEAL = 1f;
	// N4 Raíz curativa
	public static final double ROOT_RADIUS = 5;
	public static final float ROOT_HEAL = 1f;
	// N5 Corteza viva
	public static final double BARK_BELOW = 0.35;
	public static final int BARK_TICKS = 120;
	public static final int BARK_COOLDOWN_TICKS = 900;
	// N6 Plaga de langostas
	public static final int LOCUST_SECONDS = 5;
	// N7 Antídoto universal
	public static final int IMMUNITY_TICKS = 100;
	// N8 Floración colosal
	public static final double BLOOM_RADIUS = 8;
	public static final int BLOOM_ABSORPTION_TICKS = 600;
	// N9 Raíz profunda
	public static final int STILL_TICKS = 40;
	// N10 Lluvia de semillas vivas
	public static final double RAIN_RADIUS = 12;
	public static final int RAIN_SECONDS = 12;
	public static final float RAIN_HEAL = 2f;
	public static final int RAIN_CLEANSE_EVERY = 3;

	private HerbalistRules() {
	}

	/** N1: multiplicador de toda tu curación (+10 / 20 / 30 %). */
	public static double healMultiplier(int rank) {
		return 1.0 + 0.10 * Math.max(0, rank);
	}

	/** N2: absorción que reciben los aliados curados (2 / 3 / 4 puntos). */
	public static float pollenAbsorption(int rank) {
		return rank <= 0 ? 0 : 1f + rank;
	}

	/** N3: daño de la semilla (5 / 6,5 / 8). */
	public static double seedDamage(int rank, double scale) {
		return (3.5 + 1.5 * rank) * scale;
	}

	/** N4: duración de la zona en ticks (10 / 12 / 14 s). */
	public static int rootTicks(int rank) {
		return rank <= 0 ? 0 : (8 + 2 * rank) * 20;
	}

	/** N5: amplificador de Regeneración (II en rango 1, III en rango 2 o más). */
	public static int barkAmplifier(int rank) {
		return Math.max(1, Math.min(2, rank));
	}

	/** N5: ¿hay que activar la corteza? (la vida tras el golpe queda por debajo del 35 % y está lista). */
	public static boolean barkTriggers(double healthAfter, double maxHealth, long now, long readyAt) {
		return maxHealth > 0 && healthAfter < maxHealth * BARK_BELOW && now >= readyAt;
	}

	/** N6: daño por segundo del enjambre (2 / 3). */
	public static double locustDamage(int rank, double scale) {
		return (1 + rank) * scale;
	}

	/** N8: fracción de vida máxima que recuperan los aliados (40 / 60 %). */
	public static double bloomFraction(int rank) {
		return rank <= 0 ? 0 : 0.2 + 0.2 * rank;
	}

	/** N9: reducción de daño estando quieto (−10 / 20 %). */
	public static double rootedReduction(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N9: daño de espinas (1 / 2). */
	public static float rootedThorns(int rank) {
		return Math.max(0, rank);
	}

	/** N10: ¿este segundo toca limpiar efectos negativos? (cada 3 s). */
	public static boolean rainCleanses(int second) {
		return second % RAIN_CLEANSE_EVERY == RAIN_CLEANSE_EVERY - 1;
	}
}
