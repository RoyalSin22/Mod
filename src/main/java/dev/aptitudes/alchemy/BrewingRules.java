package dev.aptitudes.alchemy;

import dev.aptitudes.logic.XpDiminishing;

/** Cálculos del destilado, independientes del mundo y de sus registros. */
public final class BrewingRules {
	public static final int MAX_DURATION = 24_000;

	private BrewingRules() {
	}

	public static double safeBonus(double value) {
		return Double.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
	}

	/** Las duraciones especiales (instantánea o infinita) no se prolongan. */
	public static int duration(int original, double bonus) {
		if (original <= 1) return original;
		return (int) Math.min(MAX_DURATION, Math.round(Math.min(original, MAX_DURATION) * (1 + safeBonus(bonus))));
	}

	/** Fracción de tick extra; nunca se acumula más de un tick de deuda por soporte. */
	public static double speedCredit(double previous, double bonus) {
		double fraction = Double.isFinite(previous) && previous >= 0 ? previous % 1 : 0;
		return fraction + safeBonus(bonus);
	}

	/** Sin suelo ni redondeo al alza: las elaboraciones muy repetidas terminan dando cero XP. */
	public static int xp(int base, int recentBrews, int threshold) {
		return (int) Math.floor(Math.max(0, Math.min(20, base))
				* XpDiminishing.multiplier(Math.max(1, recentBrews), Math.max(1, Math.min(20, threshold)), 0));
	}
}
