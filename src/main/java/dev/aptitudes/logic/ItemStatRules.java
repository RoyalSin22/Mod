package dev.aptitudes.logic;

import java.util.Locale;

/**
 * v62 (A6): cifras de los tooltips de armas. Puro (sin Minecraft) para poder probarlo.
 *
 * <p>Daño y velocidad siguen las reglas de vanilla: el jugador parte de 1 de daño y 4 golpes por segundo, y los
 * modificadores «sumar valor» de la mano principal se añaden a esa base.</p>
 */
public final class ItemStatRules {
	public static final double PLAYER_BASE_DAMAGE = 1.0;
	public static final double PLAYER_BASE_SPEED = 4.0;

	private ItemStatRules() {}

	/** Una cifra decimal como mucho, sin «.0» final: 7 → «7», 1.6 → «1.6», NaN/∞ → «0». */
	public static String fmt(double value) {
		if (!Double.isFinite(value)) return "0";
		double rounded = Math.round(value * 10.0) / 10.0;
		if (rounded == Math.rint(rounded)) return Long.toString((long) rounded);
		return String.format(Locale.ROOT, "%.1f", rounded);
	}

	/** Ticks de espera en segundos legibles (20 ticks = 1 s); negativos se tratan como 0. */
	public static String seconds(int ticks) {
		return fmt(Math.max(0, ticks) / 20.0);
	}

	/** Daño por segundo con los modificadores de la mano principal (velocidad mínima 0). */
	public static double dps(double damageBonus, double speedBonus) {
		double damage = Math.max(0, PLAYER_BASE_DAMAGE + damageBonus);
		double speed = Math.max(0, PLAYER_BASE_SPEED + speedBonus);
		return damage * speed;
	}
}
