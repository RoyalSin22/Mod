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
}
