package dev.aptitudes.temple;

/**
 * V69: reglas puras de la Cripta de la Niebla, la etapa interna opcional del Templo del Custodio (ver
 * {@code docs/V69_CRIPTA_DE_LA_NIEBLA.md}). Sin Minecraft: se prueban con JUnit ({@code CryptRulesTest}).
 *
 * <p>La cripta tiene su propio estado, independiente de {@link TempleFlowRules.Flow}: se desbloquea cuando el Custodio
 * cae, se abre con una Llave del Velo (que se consume), y se supera con tres oleadas y un clímax.</p>
 */
public final class CryptRules {
	/** Fases de la cripta. Se guardan como número: solo se añaden valores al final. */
	public enum Stage { LOCKED, READY, OPEN, WAVE_1, WAVE_2, WAVE_3, CLIMAX, DONE }

	/** Resultado de usar la trampilla de la cripta. */
	public enum HatchUse {
		/** La llave abre la trampilla (se consume). */
		OPEN,
		/** Todavía no toca: el Custodio sigue en pie. */
		NOT_READY,
		/** Está lista pero falta la Llave del Velo. */
		NEEDS_KEY,
		/** Ya se abrió (o la etapa está en marcha o terminada). */
		ALREADY_OPEN,
		/** Este templo no tiene cripta (se generó antes de la v69). */
		NO_CRYPT
	}

	/** Variantes de las criaturas de las oleadas. */
	public enum Variant { ERRANTE, OSARIO, VANILLA }

	/** Oleadas antes del clímax. */
	public static final int WAVES = 3;
	/** Criaturas de cada oleada para un jugador, sin escalar. */
	private static final int[] BASE_SIZES = {5, 7, 9};
	/** V70: criaturas de cada oleada de la sala de pruebas del Piso 2 (más suaves que las de la Cripta, que es el final opcional). */
	private static final int[] TRIAL_BASE_SIZES = {4, 6};
	/** Criaturas extra por cada jugador adicional dentro de la sala. */
	public static final int EXTRA_PER_PLAYER = 2;
	/** Jugadores que cuentan para el tamaño de la oleada. */
	public static final int MAX_PLAYERS_COUNTED = 6;
	/** Bonificación multiplicativa por anillo de Amenaza (la misma del Autómata y el Custodio del templo). */
	public static final double RING_BONUS = 0.25;
	/** Tope de criaturas vivas a la vez. */
	public static final int MAX_ALIVE = 12;
	/** Tope absoluto del tamaño de una oleada, hagan lo que hagan la configuración y los jugadores. */
	public static final int MAX_WAVE_SIZE = 40;
	/** Escoltas del clímax. */
	public static final int ESCORTS = 2;
	/** Límites del porcentaje de tamaño y del tiempo de inactividad, en segundos. */
	public static final int MIN_SIZE_PERCENT = 25, MAX_SIZE_PERCENT = 300;
	public static final int MIN_IDLE_SECONDS = 5, MAX_IDLE_SECONDS = 300;
	/** Reparto de variantes (tirada 0–99): 30 % Errante, 25 % Osario y el resto no muertos vanilla. */
	public static final int ERRANTE_PERCENT = 30, OSARIO_PERCENT = 25;

	private CryptRules() {
	}

	// ------------------------------------------------------------------------------------------------- Flujo

	/** Fase guardada → fase válida (un valor desconocido es «bloqueada», nunca lanza). */
	public static Stage stageFromOrdinal(int ordinal) {
		Stage[] all = Stage.values();
		return ordinal < 0 || ordinal >= all.length ? Stage.LOCKED : all[ordinal];
	}

	/** Cae el Custodio: la cripta pasa a estar lista. En cualquier otra fase no cambia nada. */
	public static Stage afterCustodianCleared(Stage stage) {
		return stage == Stage.LOCKED ? Stage.READY : stage;
	}

	/** Se usa la Llave del Velo: la trampilla se abre. */
	public static Stage afterKeyUsed(Stage stage) {
		return stage == Stage.READY ? Stage.OPEN : stage;
	}

	/** Un jugador baja a la sala: empieza la primera oleada. */
	public static Stage afterPlayerEntered(Stage stage) {
		return stage == Stage.OPEN ? Stage.WAVE_1 : stage;
	}

	/** Se vence la oleada en curso: la siguiente o el clímax. */
	public static Stage afterWaveCleared(Stage stage) {
		return switch (stage) {
			case WAVE_1 -> Stage.WAVE_2;
			case WAVE_2 -> Stage.WAVE_3;
			case WAVE_3 -> Stage.CLIMAX;
			default -> stage;
		};
	}

	/** Cae el campeón del clímax: la cripta queda superada. */
	public static Stage afterClimaxDown(Stage stage) {
		return stage == Stage.CLIMAX ? Stage.DONE : stage;
	}

	/** ¿Hay combate en curso? */
	public static boolean isFighting(Stage stage) {
		return stage == Stage.WAVE_1 || stage == Stage.WAVE_2 || stage == Stage.WAVE_3 || stage == Stage.CLIMAX;
	}

	/** Número de oleada (1–3) de la fase, o 0 si no es una oleada. */
	public static int waveIndex(Stage stage) {
		return switch (stage) {
			case WAVE_1 -> 1;
			case WAVE_2 -> 2;
			case WAVE_3 -> 3;
			default -> 0;
		};
	}

	/** Qué pasa al usar la trampilla. */
	public static HatchUse useHatch(Stage stage, boolean holdsKey, boolean templeHasCrypt) {
		if (!templeHasCrypt) return HatchUse.NO_CRYPT;
		return switch (stage) {
			case LOCKED -> HatchUse.NOT_READY;
			case READY -> holdsKey ? HatchUse.OPEN : HatchUse.NEEDS_KEY;
			default -> HatchUse.ALREADY_OPEN;
		};
	}

	// ----------------------------------------------------------------------------------------- Tamaño de oleada

	/**
	 * Criaturas de una oleada: base (4 / 6 / 8) + 2 por jugador adicional, por (1 + 0,25 × anillo) y por el porcentaje de la
	 * configuración. Mínimo 1 y tope {@link #MAX_WAVE_SIZE}. Todas las entradas se acotan: nunca lanza.
	 */
	public static int waveSize(int wave, int players, int ring, int sizePercent) {
		int w = Math.max(1, Math.min(WAVES, wave));
		int p = Math.max(1, Math.min(MAX_PLAYERS_COUNTED, players));
		int pct = Math.max(MIN_SIZE_PERCENT, Math.min(MAX_SIZE_PERCENT, sizePercent));
		double raw = (BASE_SIZES[w - 1] + (double) EXTRA_PER_PLAYER * (p - 1))
				* (1.0 + RING_BONUS * TempleRules.clampRing(ring)) * pct / 100.0;
		return Math.max(1, Math.min(MAX_WAVE_SIZE, (int) Math.round(raw)));
	}

	/** V70: tamaño de una oleada de la sala de pruebas (2 oleadas, mismas reglas de jugadores, anillo y porcentaje). */
	public static int trialWaveSize(int wave, int players, int ring, int sizePercent) {
		int w = Math.max(1, Math.min(TRIAL_BASE_SIZES.length, wave));
		int p = Math.max(1, Math.min(MAX_PLAYERS_COUNTED, players));
		int pct = Math.max(MIN_SIZE_PERCENT, Math.min(MAX_SIZE_PERCENT, sizePercent));
		double raw = (TRIAL_BASE_SIZES[w - 1] + (double) EXTRA_PER_PLAYER * (p - 1))
				* (1.0 + RING_BONUS * TempleRules.clampRing(ring)) * pct / 100.0;
		return Math.max(1, Math.min(MAX_WAVE_SIZE, (int) Math.round(raw)));
	}

	/** Bonificación multiplicativa de vida del campeón del clímax (+25 % por anillo). */
	public static double climaxHealthBonus(int ring) {
		return RING_BONUS * TempleRules.clampRing(ring);
	}

	// ------------------------------------------------------------------------------------- Oleada en curso

	/** Cuántas criaturas aparecen ahora: las que faltan por salir sin pasar de {@link #MAX_ALIVE} vivas a la vez. */
	public static int toSpawn(int remainingToSpawn, int alive) {
		if (remainingToSpawn <= 0) return 0;
		return Math.min(remainingToSpawn, Math.max(0, MAX_ALIVE - Math.max(0, alive)));
	}

	/** La oleada termina cuando ya no queda nada por salir ni nada vivo. */
	public static boolean waveCleared(int remainingToSpawn, int alive) {
		return remainingToSpawn <= 0 && alive <= 0;
	}

	/** Variante de una criatura de oleada para una tirada de 0 a 99 (acotada). */
	public static Variant choose(int roll) {
		int r = Math.max(0, Math.min(99, roll));
		if (r < ERRANTE_PERCENT) return Variant.ERRANTE;
		return r < ERRANTE_PERCENT + OSARIO_PERCENT ? Variant.OSARIO : Variant.VANILLA;
	}

	// ------------------------------------------------------------------------------- Inactividad y reinicio

	/** ¿Cuentan los ticks sin jugadores vivos en la sala? Solo durante un combate. */
	public static boolean countsIdle(Stage stage) {
		return isFighting(stage);
	}

	/** ¿Hay que reiniciar la oleada en curso tras {@code idleTicks} sin jugadores vivos en la sala? */
	public static boolean shouldResetWave(long idleTicks, int idleSeconds) {
		if (idleTicks <= 0) return false;
		long limit = 20L * Math.max(MIN_IDLE_SECONDS, Math.min(MAX_IDLE_SECONDS, idleSeconds));
		return idleTicks >= limit;
	}
}
