package dev.aptitudes.event;

/** Límites de detección y memoria de una criatura del Eclipse. */
final class EclipsePursuitRules {
	private static final int INTERVAL_TICKS = 20;
	private static final int MEMORY_TICKS = 120;
	static final int RANGE = 48;
	private static final double ARRIVAL_SQUARED = 2.25;

	private EclipsePursuitRules() {
	}

	static boolean due(int key, long tick) {
		return Math.floorMod(key, INTERVAL_TICKS) == Math.floorMod(tick, INTERVAL_TICKS);
	}

	static boolean inRange(double squaredDistance) {
		return squaredDistance >= 0 && squaredDistance <= RANGE * RANGE;
	}

	static boolean canSearch(long now, long seenAt, double squaredDistance) {
		return seenAt >= 0 && now >= seenAt && now - seenAt < MEMORY_TICKS
				&& inRange(squaredDistance) && squaredDistance > ARRIVAL_SQUARED;
	}
}
