package dev.aptitudes.role;

import dev.aptitudes.config.AptitudesConfig;

/** Reglas de progresión de roles compartidas entre servidor y cliente (se sincronizan). */
public record RoleRules(int maxLevel, double xpBase, double xpExponent, int pointsEveryLevels) {

	public static RoleRules from(AptitudesConfig c) {
		return new RoleRules(c.maxRoleLevel, c.roleXpBase, c.roleXpExponent, c.rolePointsEveryLevels);
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
