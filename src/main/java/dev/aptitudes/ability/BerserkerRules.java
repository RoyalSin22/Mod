package dev.aptitudes.ability;

/**
 * v67 (F2): cifras de las 10 habilidades nuevas del Bersérker. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link BerserkerKit}. {@code rank} es el rango efectivo que recibe la habilidad (1–3 aprendidos, más las
 * bonificaciones de evolución, Maestría y recurso), así que las fórmulas aceptan rangos mayores que los del árbol.
 * El daño se multiplica después por la opción {@code extraSkillDamageScale}.
 */
public final class BerserkerRules {
	// N1 Cicatrices
	public static final double SCAR_TRIGGER = 0.15;
	public static final int SCAR_TICKS = 400;
	public static final double SCAR_REDUCTION = 0.03;
	// N2 Sangre hirviente
	public static final double BOILING_HEALTH = 0.5;
	// N3 Hacha arrojadiza
	public static final double AXE_RANGE = 16;
	public static final int AXE_BLEED_SECONDS = 4;
	public static final double AXE_BLEED_PER_SECOND = 1.0;
	// N4 Cadena de sangre
	public static final double CHAIN_RANGE = 12;
	public static final int CHAIN_WINDOW_TICKS = 60;
	// N5 Rugido intimidante
	public static final double ROAR_RADIUS = 4;
	public static final int ROAR_TICKS = 60;
	public static final int ROAR_PER_ENEMY_TICKS = 100;
	// N6 Desgarro
	public static final int REND_SLASHES = 3;
	public static final double REND_RANGE = 3.5;
	public static final int REND_BLEED_SECONDS = 5;
	public static final int MAX_BLEED_STACKS = 3;
	// N7 Juramento de sangre
	public static final double OATH_HEALTH_COST = 0.10;
	public static final double OATH_DURATION = 1.5;
	public static final double OATH_LIFESTEAL = 0.15;
	// N8 Avatar de la guerra
	public static final double AVATAR_SCALE = 0.20;
	public static final double AVATAR_DAMAGE = 0.30;
	public static final int AVATAR_WEAKNESS_TICKS = 100;
	// N9 Eco de batalla
	public static final double ECHO_RANGE = 4;
	public static final int ECHO_WINDOW_TICKS = 60;
	// N10 Ragnarök
	public static final double RAGNAROK_RANGE = 16;
	public static final int RAGNAROK_FISSURE = 10;
	public static final double RAGNAROK_DAMAGE = 14;
	public static final int RAGNAROK_FIRE_TICKS = 80;

	private BerserkerRules() {
	}

	/** N1: ¿el golpe es lo bastante fuerte para dejar una cicatriz (más del 15 % de la vida máxima)? */
	public static boolean scarTriggers(double damage, double maxHealth) {
		return maxHealth > 0 && damage > maxHealth * SCAR_TRIGGER;
	}

	/** N1: cicatrices máximas a la vez (3 / 4 / 5). */
	public static int maxScars(int rank) {
		return rank <= 0 ? 0 : 2 + rank;
	}

	/** N1: fracción de daño que se resta con {@code scars} cicatrices. */
	public static double scarReduction(int scars) {
		return SCAR_REDUCTION * Math.max(0, scars);
	}

	/** N2: ¿está por debajo de la mitad de la vida? */
	public static boolean boiling(double health, double maxHealth) {
		return maxHealth > 0 && health < maxHealth * BOILING_HEALTH;
	}

	/** N2: segundos de fuego (2 / 3 / 4). */
	public static int boilingFireSeconds(int rank) {
		return rank <= 0 ? 0 : 1 + rank;
	}

	/** N3: daño del hacha (5 / 6,5 / 8). */
	public static double axeDamage(int rank, double scale) {
		return (3.5 + 1.5 * rank) * scale;
	}

	/** N4: bonificación del siguiente golpe tras la cadena (+50 / 75 / 100 %). */
	public static double chainBonus(int rank) {
		return rank <= 0 ? 0 : 0.25 + 0.25 * rank;
	}

	/** N5: amplificador de Debilidad (I / II → 0 / 1). */
	public static int roarAmplifier(int rank) {
		return Math.max(0, Math.min(1, rank - 1));
	}

	/** N6: daño de cada tajo. */
	public static double rendSlashDamage(int rank, double scale) {
		return (2.0 + 0.5 * rank) * scale;
	}

	/** N6: daño por segundo de cada carga de sangrado (1,5 / 2). */
	public static double rendBleedPerStack(int rank) {
		return 1.0 + 0.5 * rank;
	}

	/** Sangrado: cargas tras añadir una (máximo {@link #MAX_BLEED_STACKS}). */
	public static int addBleedStack(int stacks) {
		return Math.min(MAX_BLEED_STACKS, Math.max(0, stacks) + 1);
	}

	/** Sangrado: daño de un segundo. */
	public static double bleedTick(int stacks, double perStack, double scale) {
		return Math.max(0, stacks) * perStack * scale;
	}

	/** N7: vida que cuesta el juramento (10 % de la vida actual). */
	public static double oathCost(double health) {
		return Math.max(0, health) * OATH_HEALTH_COST;
	}

	/** N7: duración de Furia alargada un 50 %. */
	public static int oathDuration(int ticks) {
		return (int) Math.round(Math.max(0, ticks) * OATH_DURATION);
	}

	/** N8: duración de Avatar en ticks (10 / 14 s). */
	public static int avatarTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 4 * rank) * 20;
	}

	/** N9: daño de la onda (4 / 6). */
	public static double echoDamage(int rank, double scale) {
		return (2.0 + 2.0 * rank) * scale;
	}

	/** N10: daño de la fisura. */
	public static double ragnarokDamage(double scale) {
		return RAGNAROK_DAMAGE * scale;
	}

	/** ¿Sigue abierta una ventana (Cadena, Eco) que se cierra en el tick {@code until}? */
	public static boolean windowOpen(long now, long until) {
		return now < until;
	}
}
