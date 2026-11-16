package dev.aptitudes.ability;

/**
 * v65 (C5 Y1): Contexto temporal de bonificadores de sinergia durante la ejecución de una habilidad.
 *
 * <p>Se establece en AbilityManager.execute() antes de invocar la habilidad y se limpia después.</p>
 */
public record AbilitySynergyContext(float radiusBonus, float damageBonus, float energyCostBonus) {
	/** Sin bonificadores: multiplicadores = 1.0. */
	public static AbilitySynergyContext NONE = new AbilitySynergyContext(1.0f, 1.0f, 1.0f);

	/**
	 * Aplica bonificador de radio: {@code originalRadius * radiusBonus}.
	 */
	public float applyRadiusBonus(float originalRadius) {
		return originalRadius * radiusBonus;
	}

	/**
	 * Aplica bonificador de daño: {@code originalDamage * damageBonus}.
	 */
	public float applyDamageBonus(float originalDamage) {
		return originalDamage * damageBonus;
	}

	/**
	 * Aplica bonificador de coste energético: {@code originalCost * energyCostBonus}.
	 */
	public int applyEnergyCostBonus(int originalCost) {
		return Math.max(1, (int) Math.ceil(originalCost * energyCostBonus));
	}
}
