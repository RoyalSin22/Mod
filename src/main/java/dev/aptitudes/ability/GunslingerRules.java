package dev.aptitudes.ability;

/**
 * v67 (F6): cifras de las 10 habilidades nuevas del Mago pistolero. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link GunslingerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class GunslingerRules {
	// N1 Manos firmes
	public static final double STEADY_DISTANCE = 15;
	// N2 Cabeza fría
	public static final double COOL_CHANCE = 0.25;
	// N3 Bala aturdidora
	public static final double STUN_RANGE = 32;
	public static final int STUN_TICKS = 40;
	// N4 Granada de pólvora
	public static final double GRENADE_RANGE = 16;
	public static final int GRENADE_FUSE_TICKS = 40;
	public static final double GRENADE_RADIUS = 4;
	// N5 Presión
	public static final int PRESSURE_TICKS = 40;
	// N6 Disparo a través
	public static final double THROUGH_RANGE = 24;
	public static final double THROUGH_WIDTH = 1.2;
	// N7 Sin remordimientos
	public static final long REMORSE_TICKS = 40;
	// N8 Doble pistola
	public static final double DOUBLE_FRACTION = 0.5;
	public static final int DOUBLE_DELAY_TICKS = 2;
	// N9 Pulso de tirador
	public static final int PULSE_IDLE_TICKS = 60;
	// N10 Ejecución
	public static final int EXECUTE_CHARGE_TICKS = 20;
	public static final double EXECUTE_RANGE = 24;
	public static final int EXECUTE_TARGETS = 3;
	public static final double EXECUTE_THRESHOLD = 0.25;
	public static final double EXECUTE_DAMAGE = 25;

	private GunslingerRules() {
	}

	/** N1: bonificación a larga distancia (5 / 10 / 15 %). */
	public static double steadyBonus(int rank, double distance) {
		return distance >= STEADY_DISTANCE ? 0.05 * Math.max(0, rank) : 0;
	}

	/** N2: maná que devuelve un impacto afortunado (2 / 3 / 4). */
	public static double coolMana(int rank) {
		return rank <= 0 ? 0 : 1 + rank;
	}

	/** N3: daño de la bala aturdidora (6 / 8 / 10). */
	public static double stunDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: daño de la granada (8 / 10 / 12). */
	public static double grenadeDamage(int rank, double scale) {
		return (6 + 2 * rank) * scale;
	}

	/** N5: probabilidad de ralentizar (10 / 20 %). */
	public static double pressureChance(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N6: daño del disparo a través (12 / 16). */
	public static double throughDamage(int rank, double scale) {
		return (8 + 4 * rank) * scale;
	}

	/** N8: duración de la doble pistola (8 / 10 s). */
	public static int doubleTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: bonificación del primer disparo tras la pausa (40 / 60 %). */
	public static double pulseBonus(int rank) {
		return rank <= 0 ? 0 : 0.2 + 0.2 * rank;
	}

	/** N9: ¿ha pasado el tiempo sin disparar? */
	public static boolean pulseReady(long now, long lastShot) {
		return now - lastShot >= PULSE_IDLE_TICKS;
	}

	/** N10: ¿la ejecución mata al objetivo? (menos del 25 % de vida y no es un jefe). */
	public static boolean executes(double health, double maxHealth, boolean boss) {
		return !boss && maxHealth > 0 && health / maxHealth < EXECUTE_THRESHOLD;
	}

	/** N10: daño al que no muere. */
	public static double executeDamage(double scale) {
		return EXECUTE_DAMAGE * scale;
	}
}
