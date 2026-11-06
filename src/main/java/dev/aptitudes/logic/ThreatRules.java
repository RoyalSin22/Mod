package dev.aptitudes.logic;

/** Anillos horizontales y bonificaciones acotadas; no depende de registros del juego. */
public final class ThreatRules {
	public static final int MAX_RING = 5;

	private ThreatRules() {
	}

	public static int ring(double x, double z, double spawnX, double spawnZ, int size, int maximum) {
		if (!Double.isFinite(x) || !Double.isFinite(z) || !Double.isFinite(spawnX) || !Double.isFinite(spawnZ)) return 0;
		double distance = Math.hypot(x - spawnX, z - spawnZ);
		int limit = Math.max(1, Math.min(MAX_RING, maximum));
		int width = Math.max(100, Math.min(1_000_000, size));
		return (int) Math.min(limit, Math.floor(distance / width));
	}

	public static boolean eligible(boolean overworld, boolean hostile, boolean boss, boolean summon,
			boolean event, boolean named) {
		return overworld && hostile && !boss && !summon && !event && !named;
	}

	public static int boundedRing(int ring) { return Math.max(0, Math.min(MAX_RING, ring)); }

	public static double bounded(double value, double min, double max) {
		return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
	}

	public static double healthMultiplier(int ring, double perRing) {
		return 1 + boundedRing(ring) * bounded(perRing, 0, 0.4);
	}

	public static double damageMultiplier(int ring, double perRing, double globalCap) {
		double multiplier = 1 + boundedRing(ring) * bounded(perRing, 0, 0.2);
		return globalCap > 0 && Double.isFinite(globalCap) ? Math.min(multiplier, globalCap) : multiplier;
	}

	/** Una única tirada adicional de la tabla: ×(1 + probabilidad) de botín esperado, sin XP ni equipo. */
	public static double lootChance(int ring, double perRing) {
		return boundedRing(ring) * bounded(perRing, 0, 0.2);
	}

	public static double eliteChance(double base, int ring, double perRing) {
		return Math.min(1, bounded(base, 0, 1) * (1 + boundedRing(ring) * bounded(perRing, 0, 1)));
	}

	public static boolean bonusLoot(boolean enabled, boolean eligible, boolean claimed, double chance, double roll) {
		return enabled && eligible && !claimed && Double.isFinite(roll) && roll >= 0 && roll < 1
				&& roll < bounded(chance, 0, 1);
	}
}
