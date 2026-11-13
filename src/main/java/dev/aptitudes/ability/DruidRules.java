package dev.aptitudes.ability;

/**
 * v67 (F5): cifras de las 10 habilidades nuevas del Druida. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link DruidKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class DruidRules {
	// N2 Zarpas desgarradoras
	public static final int CLAW_PULSES = 3;
	public static final int CLAW_INTERVAL_TICKS = 20;
	// N3 Látigo de lianas
	public static final double WHIP_RANGE = 16;
	public static final int WHIP_SLOW_TICKS = 40;
	// N4 Rebrote
	public static final int REGROW_PULSES = 5;
	public static final int REGROW_INTERVAL_TICKS = 20;
	// N5 Letargo
	public static final double LETHARGY_THRESHOLD = 0.30;
	public static final int LETHARGY_COOLDOWN_TICKS = 1800;
	// N6 Rugido del oso
	public static final double ROAR_RADIUS = 8;
	public static final int ROAR_WEAKNESS_TICKS = 80;
	// N7 Roble firme
	public static final double OAK_REDUCTION = 0.10;
	// N8 Forma de huargo
	public static final double DIRE_DAMAGE = 0.30;
	public static final float DIRE_HEAL = 1f;
	// N9 Flujo estacional
	public static final int SEASON_QUIET_TICKS = 400;
	// N10 Despertar del bosque
	public static final int AWAKEN_PULSES = 6;
	public static final int AWAKEN_INTERVAL_TICKS = 20;
	public static final double AWAKEN_RADIUS = 10;
	public static final double AWAKEN_DAMAGE = 4;
	public static final float AWAKEN_HEAL = 2f;
	public static final int AWAKEN_ROOT_TICKS = 30;

	private DruidRules() {
	}

	/** N1: reducción del daño sobre hierba y tierra (4 / 8 / 12 %). */
	public static double grassReduction(int rank) {
		return 0.04 * Math.max(0, rank);
	}

	/** N2: daño por segundo del desgarro (1 / 2 / 3). */
	public static double clawDamage(int rank, double scale) {
		return Math.max(0, rank) * scale;
	}

	/** N3: daño del látigo (4 / 5 / 6). */
	public static double whipDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N4: parte de la vida máxima que recuperas en total (25 / 35 / 45 %). */
	public static double regrowFraction(int rank) {
		return rank <= 0 ? 0 : 0.15 + 0.10 * rank;
	}

	/** N5: ¿el golpe te deja por debajo del umbral sin matarte, y Letargo está lista? */
	public static boolean lethargyTriggers(double health, double amount, double max, boolean ready) {
		if (!ready || max <= 0 || amount <= 0) return false;
		double after = health - amount;
		return after > 0 && after / max < LETHARGY_THRESHOLD;
	}

	/** N5: parte de la vida máxima que te cura (20 / 30 %). */
	public static double lethargyHeal(int rank) {
		return rank <= 0 ? 0 : 0.10 + 0.10 * rank;
	}

	/** N6: daño del rugido (4 / 5). */
	public static double roarDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N8: duración en ticks (8 / 10 s). */
	public static int direTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: curación del golpe tras la calma (3 / 5). */
	public static float seasonHeal(int rank) {
		return rank <= 0 ? 0f : 1f + 2f * rank;
	}

	/** N9: ¿han pasado 20 s desde el último daño o el último efecto? */
	public static boolean seasonReady(long now, long lastHurt, long lastProc) {
		return now - Math.max(lastHurt, lastProc) >= SEASON_QUIET_TICKS;
	}

	/** N10: daño por segundo del despertar. */
	public static double awakenDamage(double scale) {
		return AWAKEN_DAMAGE * scale;
	}
}
