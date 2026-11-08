package dev.aptitudes.boss;

import java.util.List;

/** Reglas puras de fases y mejoras acumulables de armas de evolución. */
public final class BossRules {
	public static final int MAX_WEAPON_CORES = 3;
	public static final int STRIKE_WARNING_TICKS = 20;
	public static final double STRIKE_HEIGHT = 2.0;
	/** La misma geometría delimita el aviso y el daño. La dirección se mide desde el eje X. */
	enum StrikeShape {
		CIRCLE(4.5, 0, Math.PI),
		RING(6, 2, Math.PI),
		CONE(6, 0, Math.PI / 4);

		final double radius;
		final double safeRadius;
		final double halfAngle;

		StrikeShape(double radius, double safeRadius, double halfAngle) {
			this.radius = radius;
			this.safeRadius = safeRadius;
			this.halfAngle = halfAngle;
		}

		boolean contains(double dx, double dy, double dz, double heading) {
			if (!inStrike(dx, dy, dz, radius)) return false;
			double distanceSq = dx * dx + dz * dz;
			if (distanceSq < safeRadius * safeRadius) return false;
			if (this != CONE) return true;
			double projection = dx * Math.cos(heading) + dz * Math.sin(heading);
			// La tolerancia incluye la línea dibujada aunque el coseno introduzca redondeo.
			return projection >= Math.sqrt(distanceSq) * Math.cos(halfAngle) - 1e-9;
		}
	}

	private BossRules() {}

	public static int phase(double healthFraction) {
		if (!Double.isFinite(healthFraction)) return 1;
		if (healthFraction <= 1.0 / 3.0) return 3;
		if (healthFraction <= 2.0 / 3.0) return 2;
		return 1;
	}

	public static boolean inStrike(double dx, double dy, double dz, double radius) {
		return Math.abs(dy) <= STRIKE_HEIGHT && dx * dx + dz * dz <= radius * radius;
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
