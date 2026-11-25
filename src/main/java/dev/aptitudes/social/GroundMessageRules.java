package dev.aptitudes.social;

import java.util.Collection;

/**
 * C7 (S5): reglas de las notas en el suelo (colocar, caducar, valorar, borrar y recompensar). Puro (sin Minecraft) y
 * probado; los tiempos van en ticks del mundo.
 */
public final class GroundMessageRules {
	/** Ticks de un día de juego. */
	public static final long DAY_TICKS = 24_000L;
	/** Distancia máxima (bloques) para escribir en un bloque: el alcance normal de la mano. */
	public static final double WRITE_REACH = 5.0;
	/** Distancia máxima (bloques) para valorar o borrar una nota. */
	public static final double ACT_REACH = 6.0;
	/** Distancia (bloques) a la que se leen las notas en el HUD. */
	public static final double READ_REACH = 4.0;
	/** Radio (bloques) de las notas que se envían a cada cliente. */
	public static final double SYNC_RADIUS = 48.0;
	/** Edad mínima (ticks) de una nota para que su valoración dé recompensa: 1 minuto. */
	public static final long MIN_REWARD_AGE_TICKS = 1_200L;
	/** Máximo de valoraciones guardadas por nota (el resto solo suma al contador). */
	public static final int MAX_RATERS_STORED = 256;

	/** Posición de una nota ya colocada. */
	public record Spot(int x, int y, int z) {}

	/** Por qué no se puede colocar una nota. */
	public enum PlaceResult { OK, DISABLED, TOO_CLOSE, CHUNK_FULL }

	private GroundMessageRules() {}

	/**
	 * ¿Se puede colocar en {@code (x, y, z)}? {@code nearby} son las notas cercanas (al menos las de radio
	 * {@code minSpacing}); {@code inChunk}, cuántas hay ya en el chunk.
	 */
	public static PlaceResult canPlace(boolean enabled, int x, int y, int z, Collection<Spot> nearby, int minSpacing,
									   int inChunk, int perChunk) {
		if (!enabled) return PlaceResult.DISABLED;
		long spacingSq = (long) Math.max(0, minSpacing) * Math.max(0, minSpacing);
		for (Spot s : nearby) {
			long dx = s.x() - x, dy = s.y() - y, dz = s.z() - z;
			long d = dx * dx + dy * dy + dz * dz;
			if (d == 0 || d < spacingSq) return PlaceResult.TOO_CLOSE;
		}
		return inChunk >= Math.max(1, perChunk) ? PlaceResult.CHUNK_FULL : PlaceResult.OK;
	}

	/** Cuántas notas antiguas del autor hay que borrar antes de añadir una nueva (0 si cabe). */
	public static int evictBeforeAdding(int ownCount, int perPlayer) {
		return Math.max(0, ownCount + 1 - Math.max(1, perPlayer));
	}

	/** ¿Ha caducado? {@code lifetimeDays} 0 = nunca. Un reloj que va hacia atrás no la caduca. */
	public static boolean expired(long created, long now, int lifetimeDays) {
		if (lifetimeDays <= 0) return false;
		return now - created >= lifetimeDays * DAY_TICKS;
	}

	/** Día de juego de un instante (para el tope diario de recompensas). */
	public static long day(long time) {
		return Math.floorDiv(time, DAY_TICKS);
	}

	/** ¿Puede valorar? Nunca la propia ni dos veces; tiene que estar cerca. */
	public static boolean canRate(boolean isAuthor, boolean alreadyRated, double distanceSq) {
		return !isAuthor && !alreadyRated && distanceSq <= ACT_REACH * ACT_REACH;
	}

	/** ¿Puede borrar? El autor o un operador, cerca de la nota. */
	public static boolean canDelete(boolean isAuthor, boolean isOperator, double distanceSq) {
		return (isAuthor || isOperator) && distanceSq <= ACT_REACH * ACT_REACH;
	}

	/**
	 * ¿Esta valoración da recompensa al autor? Freno contra granjas (diseño §7): tope por autor y día, una por
	 * persona que valora y día, nada entre miembros del mismo grupo ni por notas de menos de un minuto.
	 *
	 * @param rewardsToday         recompensas que ya recibió el autor hoy
	 * @param raterRewardedToday   esta persona ya le dio una recompensa hoy
	 * @param ageTicks             edad de la nota
	 * @param perDay               tope diario (0 = sin recompensas)
	 */
	public static boolean rewards(int rewardsToday, boolean raterRewardedToday, boolean sameParty, long ageTicks,
								  int perDay) {
		return perDay > 0 && rewardsToday < perDay && !raterRewardedToday && !sameParty
				&& ageTicks >= MIN_REWARD_AGE_TICKS;
	}

	/** Brillo del glifo (0–1) según las valoraciones: crece rápido al principio y se satura en 10. */
	public static float glow(int ratings) {
		int r = Math.max(0, Math.min(10, ratings));
		return 0.35f + 0.65f * (float) Math.sqrt(r / 10.0);
	}
}
