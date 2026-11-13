package dev.aptitudes.ability;

/**
 * v67 (F4): cifras de las 10 habilidades nuevas del Espadachín. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link SwordsmanKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class SwordsmanRules {
	// N1 Duelo
	public static final double DUEL_RANGE = 6;
	// N2 Filo penetrante
	public static final double PIERCE_CAP = 0.30;
	// N3 Tajo ascendente
	public static final double RISING_RANGE = 3;
	public static final int RISING_WEAKNESS_TICKS = 60;
	// N4 Molinete
	public static final int WINDMILL_PULSES = 4;
	public static final int WINDMILL_INTERVAL_TICKS = 10;
	public static final double WINDMILL_RADIUS = 3;
	// N5 Racha de victorias
	public static final int STREAK_MAX = 5;
	public static final int STREAK_WINDOW_TICKS = 160;
	// N6 Salto de espadachín
	public static final double LEAP_RANGE = 10;
	public static final int LEAP_SLOW_TICKS = 40;
	// N7 Voluntad de acero
	public static final int STEEL_COOLDOWN_TICKS = 2400;
	public static final int STEEL_RESISTANCE_TICKS = 60;
	public static final int STEEL_AMPLIFIER = 3;
	// N8 Estado de flujo
	public static final double FLOW_ATTACK_SPEED = 0.30;
	public static final int FLOW_COOLDOWN_CUT_TICKS = 10;
	// N9 Desenvainado
	public static final int SHEATHE_IDLE_TICKS = 60;
	// N10 Corte final
	public static final int FINAL_CHARGE_TICKS = 24;
	public static final double FINAL_RANGE = 10;
	public static final double FINAL_MIN_DOT = 0.5;
	public static final double FINAL_DAMAGE = 24;
	public static final double FINAL_HEALTH_FRACTION = 0.10;
	public static final double FINAL_HEALTH_CAP = 20;

	private SwordsmanRules() {
	}

	/** N1: bonificación de daño y reducción de daño recibido con un solo enemigo cerca (5 / 10 / 15 %). */
	public static double duel(int rank, int enemiesNearby) {
		return rank > 0 && enemiesNearby == 1 ? 0.05 * rank : 0;
	}

	/** N2: +1 / 2 / 3 % por punto de armadura del enemigo, hasta +30 %. */
	public static double pierceBonus(int rank, int armor) {
		return Math.min(PIERCE_CAP, 0.01 * Math.max(0, rank) * Math.max(0, armor));
	}

	/** N3: daño del tajo ascendente (4 / 5 / 6). */
	public static double risingDamage(int rank, double scale) {
		return (3 + rank) * scale;
	}

	/** N4: daño por pulso del molinete (3 / 4 / 5). */
	public static double windmillDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N5: bonificación total con {@code stacks} cargas (4 / 6 % cada una, máx. 5). */
	public static double streakBonus(int rank, int stacks) {
		return rank <= 0 ? 0 : (0.02 + 0.02 * rank) * Math.max(0, Math.min(STREAK_MAX, stacks));
	}

	/** N5: cargas tras una muerte en el tick {@code now}; si pasó la ventana, se empieza de cero. */
	public static int streakAfterKill(int stacks, long lastKillTick, long now) {
		int base = now - lastKillTick > STREAK_WINDOW_TICKS ? 0 : stacks;
		return Math.min(STREAK_MAX, base + 1);
	}

	/** N6: daño del salto (8 / 10). */
	public static double leapDamage(int rank, double scale) {
		return (6 + 2 * rank) * scale;
	}

	/**
	 * N7: daño final de un golpe: si fuera mortal y la habilidad está lista, deja 1 de vida.
	 *
	 * @param health vida más absorción actuales
	 */
	public static float surviveLethal(float health, float amount, boolean ready) {
		if (!ready || amount < health) return amount;
		return Math.max(0f, health - 1f);
	}

	/** N8: duración en ticks (8 / 10 s). */
	public static int flowTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: ¿lleva suficiente sin atacar? */
	public static boolean sheathed(long lastAttackTick, long now) {
		return now - lastAttackTick >= SHEATHE_IDLE_TICKS;
	}

	/** N9: bonificación del primer golpe (+30 / 50 %). */
	public static double sheatheBonus(int rank) {
		return rank <= 0 ? 0 : 0.1 + 0.2 * rank;
	}

	/** N10: daño del corte final contra un enemigo: 24 más el 10 % de su vida máxima (tope de 20). */
	public static double finalDamage(double maxHealth, double scale) {
		return (FINAL_DAMAGE + Math.min(FINAL_HEALTH_CAP, FINAL_HEALTH_FRACTION * Math.max(0, maxHealth))) * scale;
	}
}
