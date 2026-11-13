package dev.aptitudes.ability;

/**
 * v67 (F4): cifras de las 10 habilidades nuevas del Lancero. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link LancerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class LancerRules {
	// N1 Mantener la distancia
	public static final double DISTANCE_BLOCKS = 3;
	// N2 Punta clavada
	public static final int MARK_TICKS = 100;
	public static final int MARK_SCAN_TICKS = 20;
	// N3 Jabalina
	public static final double JAVELIN_RANGE = 20;
	public static final double JAVELIN_WIDTH = 1.2;
	public static final int JAVELIN_SLOW_TICKS = 40;
	// N4 Muro de picas
	public static final int WALL_PULSES = 8;
	public static final int WALL_INTERVAL_TICKS = 10;
	public static final double WALL_RADIUS = 3;
	// N5 Ensartar
	public static final double CLEAVE_RANGE = 2;
	// N6 Abanico de picas
	public static final int FAN_THRUSTS = 5;
	public static final int FAN_INTERVAL_TICKS = 4;
	public static final double FAN_RANGE = 5;
	public static final double FAN_MIN_DOT = 0.7;
	// N7 Primer contacto
	public static final double FIRST_CONTACT_BONUS = 0.30;
	public static final int FIRST_CONTACT_RESET_TICKS = 200;
	// N8 Lanza ancestral
	public static final double ANCESTRAL_REACH = 2;
	public static final double ANCESTRAL_DAMAGE = 0.20;
	public static final double ANCESTRAL_CLEAVE = 0.5;
	// N9 Doble estocada
	public static final double DOUBLE_FRACTION = 0.6;
	public static final int DOUBLE_DELAY_TICKS = 3;
	// N10 Ráfaga de jabalinas
	public static final int BARRAGE_TARGETS = 8;
	public static final double BARRAGE_RANGE = 12;
	public static final double BARRAGE_DAMAGE = 12;
	public static final int BARRAGE_INTERVAL_TICKS = 2;

	private LancerRules() {
	}

	/** N1: +5 / 10 / 15 % a 3 bloques o más. */
	public static double distanceBonus(int rank, double distance) {
		return rank > 0 && distance >= DISTANCE_BLOCKS ? 0.05 * rank : 0;
	}

	/** N2: bonificación contra un enemigo marcado (+4 / 8 / 12 %). */
	public static double markBonus(int rank) {
		return 0.04 * Math.max(0, rank);
	}

	/** N3: daño de la jabalina (6 / 8 / 10). */
	public static double javelinDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: daño por pulso del muro de picas (3 / 4 / 5). */
	public static double wallDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N5: fracción del daño que recibe el enemigo de detrás (40 / 60 %). */
	public static double cleaveFraction(int rank) {
		return rank <= 0 ? 0 : 0.2 + 0.2 * rank;
	}

	/** N6: daño de cada estocada del abanico (4 / 5). */
	public static double fanDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N7: ¿es un primer contacto? (nunca lo golpeaste, o hace más de 10 s). */
	public static boolean isFirstContact(Long lastHitTick, long now) {
		return lastHitTick == null || now - lastHitTick > FIRST_CONTACT_RESET_TICKS;
	}

	/** N8: duración en ticks (8 / 10 s). */
	public static int ancestralTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: probabilidad de repetir el golpe (10 / 20 %). */
	public static double doubleChance(int rank) {
		return 0.10 * Math.max(0, rank);
	}
}
