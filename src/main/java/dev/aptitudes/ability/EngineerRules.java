package dev.aptitudes.ability;

import java.util.List;

/**
 * v67 (F3): cifras de las 10 habilidades nuevas del Ingeniero. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link EngineerKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class EngineerRules {
	// N3 Mina de proximidad
	public static final int MINE_MAX = 3;
	public static final int MINE_TICKS = 600;
	public static final double MINE_TRIGGER = 2;
	public static final double MINE_BLAST = 3;
	// N4 Dron de apoyo
	public static final int DRONE_INTERVAL_TICKS = 10;
	public static final double DRONE_DAMAGE = 2;
	public static final double DRONE_RANGE = 14;
	// N5 Calor residual
	public static final double HEAT_RADIUS = 4;
	// N6 Domo protector
	public static final double DOME_RADIUS = 4;
	public static final int DOME_TICKS = 100;
	// N7 Modo ahorro
	public static final double THRIFT_COST = 0.8;
	public static final double THRIFT_DURATION = 1.3;
	// N8 Exotraje
	public static final double EXO_ARMOR = 4;
	public static final float EXO_ABSORPTION = 8f;
	// N10 Torre Tesla
	public static final int TESLA_TICKS = 240;
	public static final double TESLA_RADIUS = 10;
	public static final int TESLA_TARGETS = 3;
	public static final double TESLA_DAMAGE = 3;
	/** Reciclaje: los enemigos muertos por tus artilugios marcados cuentan durante 2 s. */
	public static final int GADGET_KILL_TICKS = 40;

	/** Artilugios afectados por Modo ahorro: torreta, minas, dron, domo y torre Tesla. */
	public static final List<String> GADGETS = List.of("turret", "engineer_n3", "engineer_n4", "engineer_n6", "engineer_n10");
	/** Artilugios cuya espera recorta Cadena de montaje. */
	public static final List<String> ASSEMBLY = List.of("turret", "thrusters", "grapple", "pulse_grenade", "engineer_n3",
			"engineer_n4", "engineer_n6", "engineer_n10");

	private EngineerRules() {
	}

	/** N1: reducción del daño de explosiones, fuego y rayos (−10 / 20 / 30 %). */
	public static double insulation(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N2: maná devuelto por un enemigo muerto por un artilugio (4 / 6 / 8). */
	public static double recycleMana(int rank) {
		return rank <= 0 ? 0 : 2 + 2 * rank;
	}

	/** N3: daño de la mina (6 / 8 / 10). */
	public static double mineDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N4: duración del dron en ticks (15 / 20 / 25 s). */
	public static int droneTicks(int rank) {
		return rank <= 0 ? 0 : (10 + 5 * rank) * 20;
	}

	/** N5: daño de Calor residual (6 / 8). */
	public static double heatDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N6: reducción de daño dentro del domo (−40 / 50 %). */
	public static double domeReduction(int rank) {
		return 0.30 + 0.10 * Math.max(0, rank);
	}

	/** N7: ¿es la activa un artilugio? */
	public static boolean isGadget(String nodeId) {
		return GADGETS.contains(nodeId);
	}

	/** N7: coste de maná de un artilugio ({@code thrift} = el jugador tiene Modo ahorro). */
	public static double costMultiplier(String nodeId, boolean thrift) {
		return thrift && isGadget(nodeId) ? THRIFT_COST : 1.0;
	}

	/** N7: duración de un artilugio. */
	public static int duration(int ticks, boolean thrift) {
		return thrift ? (int) Math.round(ticks * THRIFT_DURATION) : ticks;
	}

	/** N8: duración del exotraje en ticks (10 / 14 s). */
	public static int exoTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 4 * rank) * 20;
	}

	/** N9: ticks que se quitan de la espera de cada artilugio (1 / 2 s). */
	public static int assemblyTicks(int rank) {
		return rank <= 0 ? 0 : 20 * rank;
	}
}
