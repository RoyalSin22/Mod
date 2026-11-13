package dev.aptitudes.event;

import java.util.UUID;

/** Reglas de origen y retirada de los Errantes de Niebla. */
public final class FogErranteRules {
	public static final int MAX_DELAY = 120;
	static final int MAX_REMOVALS_PER_TICK = 8;

	private FogErranteRules() {}

	static boolean belongsToSession(UUID origin, UUID active, boolean overworld) {
		return overworld && origin != null && origin.equals(active);
	}

	static boolean eventOwned(UUID origin, boolean legacyTag) {
		return origin != null || legacyTag;
	}

	static long removalAt(UUID entity, long closedAt) {
		return closedAt + 20 + Math.floorMod(entity.hashCode(), MAX_DELAY - 20 + 1);
	}

	public static boolean eligibleLoot(UUID origin, UUID active, boolean overworld, boolean retiring) {
		return !retiring && belongsToSession(origin, active, overworld);
	}
}
