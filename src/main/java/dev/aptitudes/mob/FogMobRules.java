package dev.aptitudes.mob;

/**
 * Reparto de la Niebla de almas (v65): Osario Quebrado (10 %), Errante Velado (10 %) y el resto son los no muertos
 * vanilla de siempre. Puro (sin Minecraft); la tirada va de 0 a 99.
 */
public final class FogMobRules {
	/** Variantes nuevas de la niebla. */
	public enum Variant { OSARIO, ERRANTE, VANILLA }

	/** Porcentaje del Errante Velado (justo después del Osario). Provisional. */
	public static final int ERRANTE_PERCENT = 10;

	private FogMobRules() {}

	public static Variant choose(int roll) {
		if (OsarioRules.spawnsOsario(roll)) return Variant.OSARIO;
		int r = Math.max(0, Math.min(99, roll));
		return r < OsarioRules.PERCENT + ERRANTE_PERCENT ? Variant.ERRANTE : Variant.VANILLA;
	}
}
