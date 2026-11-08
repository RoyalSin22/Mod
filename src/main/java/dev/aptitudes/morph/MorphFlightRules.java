package dev.aptitudes.morph;

/** Impulsos de vuelo acotados para la activa del murciélago. */
final class MorphFlightRules {
	static final int PULSES = 10;
	static final int INTERVAL = 4;
	static final double SPEED = 0.38;

	private MorphFlightRules() {}

	static double verticalSpeed(double lookY) {
		if (!Double.isFinite(lookY)) return 0;
		return Math.clamp(0.16 + SPEED * Math.clamp(lookY, -1, 1), -0.15, 0.32);
	}
}
