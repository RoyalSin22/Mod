package dev.aptitudes.ability;

/**
 * v67 (F7): cifras de las 10 habilidades nuevas del Bardo. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link BardKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class BardRules {
	// N1 Armonía de grupo
	public static final double HARMONY_RADIUS = 8;
	// N3 Nota cortante
	public static final double NOTE_RANGE = 14;
	public static final double NOTE_WIDTH = 1.2;
	public static final int NOTE_WEAKNESS_TICKS = 60;
	// N4 Arrullo
	public static final double LULL_RADIUS = 8;
	// N5 Coro
	public static final int CHOIR_INTERVAL_TICKS = 100;
	public static final double CHOIR_RADIUS = 10;
	public static final int CHOIR_MAX_ALLIES = 3;
	// N6 Estruendo
	public static final int RUMBLE_PULSES = 4;
	public static final int RUMBLE_INTERVAL_TICKS = 15;
	public static final double RUMBLE_RADIUS = 8;
	// N7 Pulso del compás
	public static final double BEAT_RADIUS = 8;
	public static final float BEAT_HEAL = 1f;
	// N8 Marcha de victoria
	public static final double MARCH_RADIUS = 10;
	// N9 Contrapunto
	public static final int COUNTER_WINDOW_TICKS = 200;
	public static final int COUNTER_SONGS = 3;
	public static final int COUNTER_TICKS = 120;
	// N10 Gran final
	public static final int FINALE_PULSES = 6;
	public static final int FINALE_INTERVAL_TICKS = 20;
	public static final double FINALE_RADIUS = 12;
	public static final double FINALE_DAMAGE = 4;
	public static final float FINALE_HEAL = 2f;

	private BardRules() {
	}

	/** N1: reducción de daño de grupo (3 / 6 / 9 %). */
	public static double harmonyReduction(int rank) {
		return 0.03 * Math.max(0, Math.min(3, rank));
	}

	/** N2: multiplicador del coste de la siguiente habilidad (−10 / 20 / 30 %). */
	public static double refrainCost(int rank) {
		return 1.0 - 0.10 * Math.max(0, Math.min(3, rank));
	}

	/** N3: daño de la nota cortante (5 / 7 / 9). */
	public static double noteDamage(int rank, double scale) {
		return (3 + 2 * rank) * scale;
	}

	/** N4: duración del sueño (3 / 4 / 5 s). */
	public static int lullTicks(int rank) {
		return (2 + rank) * 20;
	}

	/** N5: maná por aliado cercano (1 / 2). */
	public static double choirMana(int rank, int allies) {
		return Math.max(0, rank) * Math.max(0, Math.min(CHOIR_MAX_ALLIES, allies));
	}

	/** N6: daño de cada pulso del estruendo (3 / 4). */
	public static double rumbleDamage(int rank, double scale) {
		return (2 + rank) * scale;
	}

	/** N8: duración de la marcha (8 / 10 s). */
	public static int marchTicks(int rank) {
		return rank <= 0 ? 0 : (6 + 2 * rank) * 20;
	}

	/** N9: bonificación del contrapunto (10 / 20 %). */
	public static double counterBonus(int rank) {
		return 0.10 * Math.max(0, rank);
	}

	/** N9: ¿se han lanzado suficientes canciones distintas? */
	public static boolean counterReached(int distinctSongs) {
		return distinctSongs >= COUNTER_SONGS;
	}

	/** N10: daño por segundo del gran final. */
	public static double finaleDamage(double scale) {
		return FINALE_DAMAGE * scale;
	}
}
