package dev.aptitudes.ability;

/**
 * v67 (F3): cifras de las 10 habilidades nuevas del Asesino. Lógica pura (sin Minecraft), con pruebas; la usa
 * {@link AssassinKit}. {@code rank} es el rango efectivo. El daño se multiplica después por {@code extraSkillDamageScale}.
 */
public final class AssassinRules {
	// N3 Humo cegador
	public static final double SMOKE_RADIUS = 4;
	public static final int SMOKE_PULSES = 8;
	public static final int SMOKE_BLIND_TICKS = 40;
	// N4 Corte relámpago
	public static final double SLASH_RANGE = 8;
	// N5 Sombra persistente
	public static final int PERSISTENT_WINDOW_TICKS = 100;
	// N6 Cadena de sombras
	public static final int CHAIN_JUMPS = 3;
	public static final int CHAIN_INTERVAL_TICKS = 20;
	public static final double CHAIN_RANGE = 10;
	// N7 Veneno concentrado
	public static final int VENOM_MAX_TICKS = 200;
	public static final int VENOM_AMPLIFIER = 1;
	// N8 Velo de muerte
	public static final double VEIL_DAMAGE = 0.30;
	// N9 Cosecha silenciosa
	public static final float HARVEST_HEAL = 2f;
	// N10 Noche eterna
	public static final int NIGHT_TICKS = 120;
	public static final double NIGHT_RADIUS = 12;
	public static final double NIGHT_DAMAGE = 0.40;
	/** Bonificación de Puñalada por rango; con Noche eterna todo golpe cuenta como por la espalda. */
	public static final double BACKSTAB_PER_RANK = 0.25;

	private AssassinRules() {
	}

	/** N1: +5 / 10 / 15 % contra enemigos con algún efecto negativo. */
	public static double weakPointBonus(int rank, boolean debuffed) {
		return rank > 0 && debuffed ? 0.05 * rank : 0;
	}

	/** N2: fracción de la espera restante que se recorta (15 / 25 / 35 %). */
	public static double linkFraction(int rank) {
		return rank <= 0 ? 0 : 0.05 + 0.10 * rank;
	}

	/** N2: ticks que se quitan de una espera de {@code remaining} ticks. */
	public static long linkReduction(long remaining, int rank) {
		return remaining <= 0 ? 0 : (long) (remaining * linkFraction(rank));
	}

	/** N4: daño del corte (8 / 10 / 12). */
	public static double slashDamage(int rank, double scale) {
		return (6 + 2 * rank) * scale;
	}

	/** N5: bonificación del primer golpe tras la invisibilidad (+40 / 60 %). */
	public static double persistentBonus(int rank) {
		return rank <= 0 ? 0 : 0.2 + 0.2 * rank;
	}

	/** N6: daño por salto (5 / 6). */
	public static double chainDamage(int rank, double scale) {
		return (4 + rank) * scale;
	}

	/** N7: nueva duración del veneno: se duplica, hasta 10 s. */
	public static int venomTicks(int current) {
		return Math.min(VENOM_MAX_TICKS, Math.max(0, current) * 2);
	}

	/** N8: duración del velo en ticks (6 / 8 s). */
	public static int veilTicks(int rank) {
		return rank <= 0 ? 0 : (4 + 2 * rank) * 20;
	}

	/** N9: ticks de invisibilidad que se añaden al matar (2 / 3 s). */
	public static int harvestExtend(int rank) {
		return rank <= 0 ? 0 : (1 + rank) * 20;
	}

	/** N10: bonificación extra de «siempre por la espalda»: lo que daría Puñalada si no estás detrás. */
	public static double nightBackstab(int backstabRank, boolean alreadyBehind) {
		return alreadyBehind ? 0 : BACKSTAB_PER_RANK * Math.max(0, backstabRank);
	}
}
