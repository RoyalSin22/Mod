package dev.aptitudes.ability;

/**
 * v67 (F5): cifras de las 10 habilidades nuevas del Monje. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link MonkKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class MonkRules {
	// N2 Combinación
	public static final int COMBO_WINDOW_TICKS = 80;
	public static final int COMBO_HIT = 3;
	// N3 Patada giratoria
	public static final double KICK_RADIUS = 3;
	// N4 Onda de chi
	public static final double WAVE_RANGE = 12;
	public static final double WAVE_WIDTH = 1.2;
	public static final int WAVE_SLOW_TICKS = 40;
	// N5 Cuerpo sereno
	public static final int SERENE_QUIET_TICKS = 100;
	// N6 Palma explosiva
	public static final double PALM_RANGE = 16;
	public static final int PALM_DELAY_TICKS = 60;
	public static final double PALM_RADIUS = 4;
	// N7 Punto de presión
	public static final int PRESSURE_TICKS = 60;
	// N8 Postura del tigre
	public static final double TIGER_DAMAGE = 0.30;
	public static final int TIGER_MAX_ARMOR = 5;
	// N9 Eco de puño
	public static final int ECHO_EVERY = 4;
	public static final double ECHO_RADIUS = 5;
	// N10 Palma del vacío
	public static final int VOID_CHARGE_TICKS = 20;
	public static final double VOID_RANGE = 5;
	public static final double VOID_BASE = 30;
	public static final double VOID_HEALTH_FRACTION = 0.15;
	public static final double VOID_EXTRA_CAP = 20;

	private MonkRules() {
	}

	/** N1: bonificación con las manos vacías (4 / 8 / 12 %). */
	public static double bareHands(int rank) {
		return 0.04 * Math.max(0, rank);
	}

	/** N2: bonificación del tercer golpe (10 / 20 / 30 %). */
	public static double comboBonus(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N2: número de golpe de la combinación tras un golpe en {@code now}. */
	public static int comboCount(int previous, long lastHit, long now) {
		return previous > 0 && now - lastHit <= COMBO_WINDOW_TICKS ? previous + 1 : 1;
	}

	/** N3: daño de la patada (5 / 6 / 7). */
	public static double kickDamage(int rank, double scale) {
		return (4 + rank) * scale;
	}

	/** N4: daño de la onda (6 / 8 / 10). */
	public static double waveDamage(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N5: velocidad de ataque extra (15 / 25 %). */
	public static double sereneSpeed(int rank) {
		return rank <= 0 ? 0 : 0.05 + 0.10 * rank;
	}

	/** N5: ¿han pasado 5 s desde el último golpe recibido? */
	public static boolean sereneActive(long now, long lastHurt) {
		return now - lastHurt >= SERENE_QUIET_TICKS;
	}

	/** N6: daño de la palma al objetivo (12 / 16). */
	public static double palmDamage(int rank, double scale) {
		return (8 + 4 * rank) * scale;
	}

	/** N6: daño de la palma a los de alrededor (6 / 8). */
	public static double palmSplash(int rank, double scale) {
		return (4 + 2 * rank) * scale;
	}

	/** N8: duración de la postura (8 / 10 s). */
	public static int tigerTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N8: armadura quitada tras otro golpe, con tope. */
	public static int tigerArmor(int current) {
		return Math.min(TIGER_MAX_ARMOR, current + 1);
	}

	/** N9: parte del daño de la onda (40 / 60 %). */
	public static double echoFraction(int rank) {
		return rank <= 0 ? 0 : 0.2 + 0.2 * rank;
	}

	/** N10: daño de la palma del vacío: base más el 15 % de la vida máxima del objetivo, con tope de 20 extra. */
	public static double voidDamage(double targetMaxHealth, double scale) {
		return (VOID_BASE + Math.min(VOID_EXTRA_CAP, VOID_HEALTH_FRACTION * Math.max(0, targetMaxHealth))) * scale;
	}
}
