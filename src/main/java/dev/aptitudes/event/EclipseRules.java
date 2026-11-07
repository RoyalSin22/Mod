package dev.aptitudes.event;

/**
 * Reglas puras del Eclipse (v34): calendario, fases, intensidad del cielo, tamaño de las oleadas y escalado
 * del Heraldo. Sin dependencias de Minecraft, para poder probarlas con JUnit.
 *
 * <p>El tiempo del evento va de 0 a {@link #END} ticks: en un eclipse natural coincide con la hora del día
 * (0 = 6:00, 6000 = 12:00, 12 000 = 18:00); en uno iniciado por comando es el tiempo transcurrido.</p>
 */
public final class EclipseRules {
	/** Fases del evento. Solo se envían por red (no se guardan): el orden puede crecer al final. */
	public enum Phase { NONE, PENUMBRA, TOTALITY, RETREAT }

	public static final long DAY_TICKS = 24_000L;
	public static final long PENUMBRA_END = 3_000L;
	public static final long HERALD_TIME = 6_000L;
	public static final long TOTALITY_END = 9_000L;
	public static final long END = 12_000L;
	/** Hora del aviso: atardecer (18:00) del día anterior al eclipse. */
	public static final long ANNOUNCE_TIME = 12_000L;

	public static final int MAX_GROUP = 8;
	public static final double MAX_HERALD_HEALTH_MULTIPLIER = 4.0;

	private EclipseRules() {
	}

	/** Día de juego (0, 1, 2…) a partir del tiempo total del día. Valores negativos cuentan como día 0. */
	public static long dayOf(long timeOfDay) {
		return Math.max(0L, timeOfDay) / DAY_TICKS;
	}

	/** Tick dentro del día (0–23 999). */
	public static long timeInDay(long timeOfDay) {
		return Math.floorMod(timeOfDay, DAY_TICKS);
	}

	/** ¿Hay eclipse ese día? Cada {@code interval} días a partir de {@code firstDay} (días múltiplos del intervalo). */
	public static boolean isEclipseDay(long day, int interval, int firstDay) {
		int step = Math.max(1, interval);
		return day >= Math.max(1, firstDay) && day % step == 0;
	}

	/** Días que faltan hasta el próximo eclipse (0 si hoy hay uno que aún no ha terminado). */
	public static long daysUntilNext(long day, long timeInDay, int interval, int firstDay) {
		long d = Math.max(0L, day);
		if (isEclipseDay(d, interval, firstDay) && timeInDay < END) return 0L;
		int step = Math.max(1, interval);
		long candidate = Math.max(d + 1, Math.max(1, firstDay));
		long remainder = candidate % step;
		if (remainder != 0) candidate += step - remainder;
		return candidate - d;
	}

	public static Phase phase(long eventTime) {
		if (eventTime < 0 || eventTime >= END) return Phase.NONE;
		if (eventTime < PENUMBRA_END) return Phase.PENUMBRA;
		if (eventTime < TOTALITY_END) return Phase.TOTALITY;
		return Phase.RETREAT;
	}

	/** Intensidad visual 0–1: sube en la Penumbra, 1 en la Totalidad y baja en la Retirada. */
	public static float intensity(long eventTime) {
		return switch (phase(eventTime)) {
			case NONE -> 0f;
			case PENUMBRA -> clamp01(eventTime / (float) PENUMBRA_END);
			case TOTALITY -> 1f;
			case RETREAT -> clamp01(1f - (eventTime - TOTALITY_END) / (float) (END - TOTALITY_END));
		};
	}

	/** Tamaño histórico v34; el asalto v47 usa {@link EclipseAssaultRules#waveSize(Phase, int, int)}. */
	public static int waveSize(Phase phase) {
		return switch (phase) {
			case PENUMBRA -> 2;
			case TOTALITY -> 4;
			default -> 0;
		};
	}

	/** Cuántas criaturas pueden aparecer ahora sin superar el tope por jugador. Nunca negativo. */
	public static int allowedSpawns(int alive, int maxPerPlayer, int wave) {
		long free = (long) Math.max(0, maxPerPlayer) - Math.max(0, alive);
		return (int) Math.max(0L, Math.min(free, Math.max(0, wave)));
	}

	/** Multiplicador de vida del Heraldo según los jugadores de su grupo: +50 % por jugador extra, máximo ×4. */
	public static double heraldHealthMultiplier(int groupSize) {
		int n = Math.max(1, Math.min(MAX_GROUP, groupSize));
		return Math.min(MAX_HERALD_HEALTH_MULTIPLIER, 1.0 + 0.5 * (n - 1));
	}

	/** Fragmentos del eclipse que suelta una criatura: Heraldo 8, campeón 3, élite 1; la normal, 1 con probabilidad 10 %. */
	public static int fragments(boolean herald, boolean champion, boolean elite, double roll) {
		if (herald) return 8;
		if (champion) return 3;
		if (elite) return 1;
		return roll < 0.10 ? 1 : 0;
	}

	/** Limita un multiplicador de configuración a [min, max]; NaN o infinito → min. */
	public static double boundedMultiplier(double value, double min, double max) {
		if (!Double.isFinite(value)) return min;
		return Math.max(min, Math.min(max, value));
	}

	/** Intervalo entre oleadas en ticks, a partir de segundos (10–300 s). */
	public static long waveIntervalTicks(int seconds) {
		return Math.max(10, Math.min(300, seconds)) * 20L;
	}

	private static float clamp01(float v) {
		if (Float.isNaN(v)) return 0f;
		return Math.max(0f, Math.min(1f, v));
	}
}
