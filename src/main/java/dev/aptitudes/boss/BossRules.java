package dev.aptitudes.boss;

import java.util.List;

/** Reglas puras de fases y mejoras acumulables de armas de evolución. */
public final class BossRules {
	public static final int MAX_WEAPON_CORES = 3;
	private BossRules() {}

	public static int phase(double healthFraction) {
		if (!Double.isFinite(healthFraction)) return 1;
		if (healthFraction <= 1.0 / 3.0) return 3;
		if (healthFraction <= 2.0 / 3.0) return 2;
		return 1;
	}

	public static boolean canApplyCore(List<String> applied, String coreId) {
		if (applied == null || coreId == null || !isCore(coreId) || applied.size() >= MAX_WEAPON_CORES) return false;
		return !applied.contains(coreId);
	}

	public static boolean isCore(String id) {
		return "tower".equals(id) || "shrine".equals(id) || "camp".equals(id) || "eclipse".equals(id);
	}

	public static double damageBonus(int coreCount) {
		return Math.max(0, Math.min(MAX_WEAPON_CORES, coreCount)) * 0.5;
	}
}
