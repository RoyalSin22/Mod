package dev.aptitudes.role;

import dev.aptitudes.config.AptitudesConfig;

/** Reglas de progresión de roles compartidas entre servidor y cliente (se sincronizan). */
public record RoleRules(int maxLevel, double xpBase, double xpExponent, int pointsEveryLevels,
						int masterBonusPoints, int ultimateBonusPoints) {

	/** Sin puntos extra por evolución (compatibilidad). */
	public RoleRules(int maxLevel, double xpBase, double xpExponent, int pointsEveryLevels) {
		this(maxLevel, xpBase, xpExponent, pointsEveryLevels, 0, 0);
	}

	public static RoleRules from(AptitudesConfig c) {
		return new RoleRules(c.maxRoleLevel, c.roleXpBase, c.roleXpExponent, c.rolePointsEveryLevels,
				c.masterBonusPoints, c.ultimateBonusPoints);
	}

	/**
	 * Puntos de árbol con un nivel y una evolución (v23): los de nivel más los extra de Maestro
	 * (tier ≥ 1) y de Definitivo (tier ≥ 2), para las filas nuevas del árbol.
	 */
	public int earnedPoints(int level, int tier) {
		long total = (long) earnedPoints(level)
				+ (tier >= 1 ? Math.max(0, masterBonusPoints) : 0)
				+ (tier >= 2 ? Math.max(0, ultimateBonusPoints) : 0);
		return (int) Math.min(Integer.MAX_VALUE, total);
	}

	public int xpToNext(int level) {
		double value = xpBase * Math.pow(level + 1, xpExponent);
		return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.round(value)));
	}

	/**
	 * Puntos de árbol ganados con un nivel de rol: 1 al nivel 1 y 3 cada N niveles
	 * (con N = 3, 31 puntos al nivel 30 para 69 rangos posibles en 23 nodos: hay que elegir).
	 */
	public int earnedPoints(int level) {
		return level <= 0 ? 0 : 1 + (level * 3) / Math.max(1, pointsEveryLevels);
	}
}
