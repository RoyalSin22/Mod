package dev.aptitudes.mob;

/**
 * Reparto del Osario Quebrado dentro de la Niebla de almas: un porcentaje de las criaturas de la niebla. Puro (sin
 * Minecraft); la tirada va de 0 a 99.
 */
public final class OsarioRules {
	/** Porcentaje de las apariciones de la Niebla que son Osarios. Provisional. */
	public static final int PERCENT = 10;

	private OsarioRules() {}

	/** ¿Esta aparición es un Osario? */
	public static boolean spawnsOsario(int roll) {
		return roll >= 0 && roll < PERCENT;
	}
}
