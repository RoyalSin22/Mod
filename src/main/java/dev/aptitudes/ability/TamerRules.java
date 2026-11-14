package dev.aptitudes.ability;

/**
 * v67 (F7): cifras de las 10 habilidades nuevas del Domador. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link TamerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class TamerRules {
	// N1 Pelaje denso
	public static final double FUR_RADIUS = 16;
	public static final int FUR_INTERVAL_TICKS = 200;
	// N2 Frenesí de caza
	public static final double FRENZY_RADIUS = 16;
	public static final int FRENZY_TICKS = 60;
	public static final int FRENZY_COOLDOWN_TICKS = 40;
	public static final int FRENZY_MAX_PETS = 3;
	// N3 Convocar a la manada
	public static final double GATHER_RADIUS = 32;
	// N4 Zarpazos coordinados
	public static final double STRIKE_RANGE = 24;
	public static final double STRIKE_PET_RADIUS = 32;
	public static final int STRIKE_MAX_PETS = 3;
	public static final int STRIKE_INTERVAL_TICKS = 3;
	// N5 Cuidado veterano
	public static final int CARE_INTERVAL_TICKS = 20;
	public static final double CARE_RADIUS = 8;
	public static final double CARE_THRESHOLD = 0.5;
	// N6 Manto protector
	public static final double MANTLE_RADIUS = 12;
	public static final int MANTLE_TICKS = 120;
	// N7 Olfato de la manada
	public static final int SCENT_INTERVAL_TICKS = 200;
	public static final double SCENT_RADIUS = 16;
	public static final int SCENT_TICKS = 60;
	// N8 Furia animal
	public static final double RAGE_RADIUS = 24;
	// N9 Vínculo de sangre
	public static final double BLOOD_RADIUS = 16;
	// N10 Llamada del rey
	public static final double KING_RADIUS = 24;
	public static final int KING_TICKS = 160;

	private TamerRules() {
	}

	/** N1: absorción de las mascotas (2 / 4 / 6). */
	public static float furAbsorption(int rank) {
		return 2f * Math.max(0, Math.min(3, rank));
	}

	/** N3: duración de la resistencia (3 / 4 / 5 s). */
	public static int gatherTicks(int rank) {
		return (2 + rank) * 20;
	}

	/** N4: daño de cada mascota (4 / 5 / 6). */
	public static double strikeDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N5: vida por segundo que recupera una mascota herida (1 / 2). */
	public static float careHeal(int rank) {
		return Math.max(0, rank);
	}

	/** N5: ¿está herida (por debajo de la mitad)? */
	public static boolean wounded(double health, double max) {
		return max > 0 && health / max < CARE_THRESHOLD;
	}

	/** N6: absorción del manto (4 / 6). */
	public static float mantleAbsorption(int rank) {
		return rank <= 0 ? 0f : 2f + 2f * rank;
	}

	/** N8: duración de la furia (8 / 10 s). */
	public static int rageTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: parte del daño que cura a la mascota (10 / 20 %). */
	public static float bloodShare(int rank) {
		return 0.10f * Math.max(0, rank);
	}
}
