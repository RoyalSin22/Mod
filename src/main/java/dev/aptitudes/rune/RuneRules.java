package dev.aptitudes.rune;

import java.util.List;

/** Reglas puras de ranuras y potencia de runas. */
public final class RuneRules {
	public static final int SOCKETS = 2;
	private RuneRules() {}
	public static boolean canSocket(List<String> existing, String runeId) {
		if (existing == null || runeId == null || RuneKind.byId(runeId) == null || existing.size() >= SOCKETS) return false;
		return !existing.contains(runeId);
	}
	public static float damageBonus(int count) { return count > 0 ? 1.0f : 0.0f; }
	public static double attackSpeedBonus(int count) { return count > 0 ? 0.10 : 0.0; }
	public static float leechAmount() { return 1.0f; }
	public static int leechCooldownTicks() { return 80; }
	public static int flameSeconds() { return 3; }
	/** Runa del Eclipse (v34): +15 % de daño contra élites, campeones y jefes. */
	public static float eclipseBonus() { return 0.15f; }

	// ---------------------------------------------------------------- v35: valores configurables y nuevas runas

	/** Segundos de configuración → ticks, limitados a [min, max] segundos. NaN o infinito → min. */
	public static long secondsToTicks(double seconds, double min, double max) {
		double s = Double.isFinite(seconds) ? Math.max(min, Math.min(max, seconds)) : min;
		return Math.round(s * 20.0);
	}

	/** ¿Ha pasado la espera? Sin uso previo, o si el reloj retrocedió (mundo restaurado), está lista. */
	public static boolean ready(long now, Long last, long cooldownTicks) {
		if (last == null || now < last) return true;
		return now - last >= Math.max(0L, cooldownTicks);
	}

	/** Absorción tras la Runa de Guardia: suma hasta el tope, pero nunca reduce una absorción mayor ya existente. */
	public static float absorptionAfter(float current, float add, float cap) {
		float c = Float.isFinite(current) ? Math.max(0f, current) : 0f;
		float a = Float.isFinite(add) ? Math.max(0f, add) : 0f;
		float limit = Float.isFinite(cap) ? Math.max(0f, cap) : 0f;
		return Math.max(c, Math.min(limit, c + a));
	}

	/** Tirada de probabilidad: {@code random} en [0, 1). La probabilidad se limita a [0, 1]. */
	public static boolean roll(double chance, double random) {
		double p = Double.isFinite(chance) ? Math.max(0.0, Math.min(1.0, chance)) : 0.0;
		return random < p;
	}

	/** Objetivos extra de la descarga de la Runa del Trueno (0–5). */
	public static int thunderTargets(int configured) {
		return Math.max(0, Math.min(5, configured));
	}

	/** Esmeraldas que cuesta quitar una runa (0–64); gratis en creativo. */
	public static int removalCost(int configured, boolean creative) {
		return creative ? 0 : Math.max(0, Math.min(64, configured));
	}

	/** Runas que quedan tras quitar la última engarzada (la que se pierde). Lista vacía si no había. */
	public static List<String> afterRemoval(List<String> existing) {
		if (existing == null || existing.isEmpty()) return List.of();
		return List.copyOf(existing.subList(0, existing.size() - 1));
	}
}
