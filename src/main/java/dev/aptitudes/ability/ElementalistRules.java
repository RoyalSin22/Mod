package dev.aptitudes.ability;

/**
 * v67 (F5): cifras de las 10 habilidades nuevas del Chamán (rol {@code elementalist}). Lógica pura (sin Minecraft),
 * con pruebas; la usa {@link ElementalistKit}. {@code rank} es el rango efectivo. El daño se multiplica después por
 * {@code extraSkillDamageScale}.
 */
public final class ElementalistRules {
	// N1 Cambio de marea
	public static final int TIDE_TICKS = 100;
	// N3 Lanza elemental
	public static final double LANCE_RANGE = 20;
	public static final double LANCE_WIDTH = 1.2;
	// N4 Muro elemental
	public static final double WALL_DISTANCE = 4;
	public static final double WALL_HALF_WIDTH = 4;
	public static final double WALL_HALF_THICKNESS = 1.2;
	public static final int WALL_PULSES = 4;
	public static final int WALL_INTERVAL_TICKS = 20;
	// N6 Tótem elemental
	public static final double TOTEM_RANGE = 10;
	public static final double TOTEM_RADIUS = 10;
	public static final int TOTEM_PULSES = 8;
	public static final int TOTEM_INTERVAL_TICKS = 20;
	// N7 Fusión estable
	public static final int STEADY_TICKS = 120;
	public static final int STEADY_ABSORPTION_AMPLIFIER = 1;
	// N9 Armonía
	public static final int HARMONY_WINDOW_TICKS = 200;
	public static final int HARMONY_ELEMENTS = 3;
	public static final int HARMONY_TICKS = 120;
	// N10 Colapso de elementos
	public static final double COLLAPSE_RADIUS = 8;
	public static final double COLLAPSE_DAMAGE = 25;

	private ElementalistRules() {
	}

	/** N1: bonificación de daño tras cambiar de elemento (10 / 15 / 20 %). */
	public static double tideBonus(int rank) {
		return rank <= 0 ? 0 : 0.05 + 0.05 * rank;
	}

	/** N2: probabilidad de repetir el efecto del elemento (8 / 12 / 16 %). */
	public static double echoChance(int rank) {
		return rank <= 0 ? 0 : 0.04 + 0.04 * rank;
	}

	/** N3: daño de la lanza (6 / 8 / 10). */
	public static double lanceDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: daño por segundo del muro (3 / 4 / 5). */
	public static double wallDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N5: parte del maná del Estallido que se devuelve si activa una fusión (25 / 40 %). */
	public static double chainRefund(int rank) {
		return rank <= 0 ? 0 : 0.10 + 0.15 * rank;
	}

	/** N6: daño por segundo del tótem (4 / 5). */
	public static double totemDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N8: duración de la forma elemental (8 / 10 s). */
	public static int formTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: bonificación de la armonía (10 / 20 %). */
	public static double harmonyBonus(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N9: ¿se ha usado un número suficiente de elementos distintos? */
	public static boolean harmonyReached(int distinctElements) {
		return distinctElements >= HARMONY_ELEMENTS;
	}

	/** N10: daño del colapso. */
	public static double collapseDamage(double scale) {
		return COLLAPSE_DAMAGE * scale;
	}
}
