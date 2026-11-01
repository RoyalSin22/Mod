package dev.aptitudes.data;

import dev.aptitudes.config.AptitudesConfig;

/**
 * Subconjunto de la configuración que el cliente necesita conocer. El servidor
 * la envía en cada sincronización para que la GUI y la predicción de velocidad
 * de minado usen exactamente los mismos valores que el servidor, aunque el
 * archivo de configuración local del cliente sea distinto.
 */
public record ProgressRules(int maxLevel, double xpBase, double xpExponent,
							int maxPointsPerAttribute, double breakSpeedPerLevel) {

	public static ProgressRules from(AptitudesConfig c) {
		return new ProgressRules(c.maxSkillLevel, c.xpBase, c.xpExponent,
				c.maxPointsPerAttribute, c.breakSpeedPerLevel);
	}

	/** XP necesaria para pasar de {@code level} a {@code level + 1}. */
	public int xpToNext(int level) {
		double value = xpBase * Math.pow(level + 1, xpExponent);
		return (int) Math.max(1, Math.min(Integer.MAX_VALUE, Math.round(value)));
	}
}
