package dev.aptitudes.boss;

/**
 * V68: reglas puras del Gran Osario Quebrado, el jefe que levanta la Niebla de almas (ver {@code docs/V68_JEFE_OSARIO.md}).
 * Sin Minecraft: aparición, disolución, reparto del trofeo, escudo del pacto de almas y curas. Se prueban con JUnit.
 * Los porcentajes que vienen de la configuración se acotan aquí, de modo que un valor absurdo nunca rompe una regla.
 */
public final class OsarioBossRules {
	/** Acompañantes (Errantes invocados) vivos como máximo. */
	public static final int MAX_ADDS = 4;
	/** El lamento solo se usa si hay menos acompañantes vivos que esto. */
	public static final int WAIL_BELOW = 2;
	/** Errantes que invoca cada lamento (limitado por {@link #MAX_ADDS}). */
	public static final int WAIL_ADDS = 2;
	/** Daño que recibe el jefe mientras hay acompañantes vivos (pacto de almas): 35 % menos. */
	public static final float PACT_MULTIPLIER = 0.65f;
	/** Banda de aparición alrededor del jugador, en bloques. */
	public static final double SPAWN_MIN = 24, SPAWN_MAX = 36;
	/** Fragmentos de alma por jugador elegible. */
	public static final int SHARD_MIN = 6, SHARD_MAX = 10;
	/** Curas: fracción de la vida máxima por jugador alcanzado (latigazo) o por acompañante cosechado, y su tope por golpe. */
	private static final double LASH_PER_HIT = 0.015, LASH_CAP = 0.03;
	private static final double HARVEST_PER_ADD = 0.02, HARVEST_CAP = 0.06;
	private static final int MIN_PERCENT = 1, MAX_PERCENT = 100, MIN_FOG_PERCENT = 10;

	private OsarioBossRules() {
	}

	// ------------------------------------------------------------------------------------------- Aparición

	/** Tick del evento a partir del cual aparece: el porcentaje (10–100) de su duración. */
	public static long spawnThreshold(long duration, int percent) {
		if (duration <= 0) return 0;
		int p = Math.max(MIN_FOG_PERCENT, Math.min(MAX_PERCENT, percent));
		return duration * p / 100;
	}

	/** ¿Debe aparecer ahora? Una vez por sesión, con el jefe activado, sin Pacífico y con alguien en el Overworld. */
	public static boolean shouldSpawn(long eventTime, long duration, int percent, boolean alreadySpawned, boolean enabled,
			boolean peaceful, int overworldPlayers) {
		return enabled && !alreadySpawned && !peaceful && overworldPlayers > 0
				&& eventTime >= spawnThreshold(duration, percent);
	}

	/** Distancia al jugador elegido para {@code roll} en [0, 1] (acotada; NaN = la mínima). */
	public static double spawnDistance(double roll) {
		if (Double.isNaN(roll)) return SPAWN_MIN;
		double r = Math.max(0, Math.min(1, roll));
		return SPAWN_MIN + r * (SPAWN_MAX - SPAWN_MIN);
	}

	// ----------------------------------------------------------------------------------------- Disolución

	/**
	 * ¿Se disuelve el jefe? Solo si la Niebla ya cerró ({@code closeTick} ≥ 0) y han pasado {@code lingerTicks}.
	 * Con la Niebla aún abierta ({@code closeTick} &lt; 0) nunca se disuelve.
	 */
	public static boolean shouldDissolve(long now, long closeTick, int lingerTicks) {
		if (closeTick < 0) return false;
		return now - closeTick >= Math.max(0, lingerTicks);
	}

	// ---------------------------------------------------------------------------------- Reparto del trofeo

	/** ¿Recibe trofeo quien hizo {@code damage} a un jefe de {@code maxHealth}, con un mínimo de {@code minPercent} %? */
	public static boolean eligible(float damage, float maxHealth, int minPercent) {
		if (Float.isNaN(damage) || Float.isNaN(maxHealth) || damage <= 0 || maxHealth <= 0) return false;
		int p = Math.max(MIN_PERCENT, Math.min(MAX_PERCENT, minPercent));
		return damage >= (double) maxHealth * p / 100.0;
	}

	/** Fragmentos de alma para una tirada {@code roll} (0 … SHARD_MAX − SHARD_MIN), acotada. */
	public static int shards(int roll) {
		return SHARD_MIN + Math.max(0, Math.min(SHARD_MAX - SHARD_MIN, roll));
	}

	// -------------------------------------------------------------------------------- Escudo y acompañantes

	/** Multiplicador del daño que recibe el jefe según los acompañantes vivos (pacto de almas). */
	public static float damageMultiplier(int livingAdds) {
		return livingAdds > 0 ? PACT_MULTIPLIER : 1.0f;
	}

	public static boolean canWail(int livingAdds) {
		return livingAdds < WAIL_BELOW;
	}

	/** Cuántos Errantes invoca un lamento sin pasar de {@link #MAX_ADDS} vivos. */
	public static int addsToSummon(int livingAdds) {
		return Math.max(0, Math.min(WAIL_ADDS, MAX_ADDS - Math.max(0, livingAdds)));
	}

	// ---------------------------------------------------------------------------------------------- Curas

	/** Cura del latigazo: 1,5 % de la vida máxima por jugador alcanzado, con tope del 3 % por golpe. */
	public static float lashHeal(int playersHit, float maxHealth) {
		if (playersHit <= 0 || maxHealth <= 0) return 0f;
		return (float) (Math.min(playersHit * LASH_PER_HIT, LASH_CAP) * maxHealth);
	}

	/** Cura de la cosecha: 2 % de la vida máxima por acompañante consumido, con tope del 6 %. */
	public static float harvestHeal(int consumedAdds, float maxHealth) {
		if (consumedAdds <= 0 || maxHealth <= 0) return 0f;
		return (float) (Math.min(consumedAdds * HARVEST_PER_ADD, HARVEST_CAP) * maxHealth);
	}

	/** Vida tras curar {@code amount}, sin pasar de {@code max}. Una cura no positiva no cambia nada. */
	public static float healedTo(float current, float amount, float max) {
		if (amount <= 0) return current;
		return Math.min(max, current + amount);
	}
}
