package dev.aptitudes.logic;

/** Curva de XP acumulada y bonificación de maestría, sin registros ni estado del juego. */
public final class MasteryRules {
	public static final long MAX_XP = 1_000_000_000_000L;
	public static final int MAX_POINTS = 1000;
	public static final double PER_POINT = 0.005;

	private MasteryRules() {
	}

	public static boolean canEarn(boolean gameplay, boolean enabled, boolean alive, boolean creative, boolean spectator) {
		return gameplay && enabled && alive && !creative && !spectator;
	}
	public static boolean atCap(int level, int configuredMaximum, int minimum) {
		return level >= Math.max(Math.max(1, configuredMaximum), minimum);
	}

	public static long sanitize(long xp) { return Math.max(0, Math.min(MAX_XP, xp)); }
	public static long add(long current, long earned) {
		long safe = sanitize(current);
		return safe + Math.min(Math.max(0, earned), MAX_XP - safe);
	}

	/** XP total para n puntos: base*n + escalón*n*(n-1)/2, siempre en long. */
	public static long required(int points, int base, int step) {
		long n = Math.max(0, Math.min(MAX_POINTS, points));
		return n * Math.max(1, Math.min(1_000_000, base))
				+ (long) Math.max(0, Math.min(1_000_000, step)) * n * (n - 1) / 2;
	}

	/** Búsqueda acotada, incluso con XP corrupta o una concesión masiva. */
	public static int points(long xp, int base, int step) {
		long safe = sanitize(xp);
		int low = 0, high = MAX_POINTS;
		while (low < high) {
			int mid = (low + high + 1) / 2;
			if (required(mid, base, step) <= safe) low = mid;
			else high = mid - 1;
		}
		return low;
	}

	public static double bonus(int points, double cap) {
		double safe = Double.isFinite(cap) ? Math.max(0, Math.min(0.25, cap)) : 0;
		return Math.min(safe, Math.max(0, Math.min(MAX_POINTS, points)) * PER_POINT);
	}

	/** Nunca convierte XP vieja de comandos o de cambios de curva al cruzar el máximo. */
	public static long surplus(long remaining, int newlyEarned, boolean eligible) {
		return eligible ? Math.min(Math.max(0, remaining), Math.max(0, newlyEarned)) : 0;
	}

	public static double energyCost(int original, double bonus) {
		double safe = Double.isFinite(bonus) ? Math.max(0, Math.min(0.25, bonus)) : 0;
		return Math.max(0, original) / (1 + safe);
	}
}
