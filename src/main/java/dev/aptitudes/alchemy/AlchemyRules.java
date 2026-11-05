package dev.aptitudes.alchemy;

/** Límites de la alquimia; los amplificadores no aumentan la potencia de los efectos propios. */
public final class AlchemyRules {
	public static final int MANA_TICKS = 10 * 20;
	public static final int FRENZY_TICKS = 60 * 20;
	public static final int ECLIPSE_VEIL_TICKS = 90 * 20;
	public static final int FOREST_VIGOR_TICKS = 30 * 20;
	public static final double MANA_PER_SECOND = 4.0;
	public static final double FRENZY_ATTACK_SPEED = 0.15;
	public static final double ECLIPSE_VEIL_REDUCTION = 0.20;

	private AlchemyRules() {
	}

	/** No suma antes de acotar: ni un importe enorme puede desbordar la energía máxima. */
	public static double restoredEnergy(double current, double maximum, double requested) {
		if (!Double.isFinite(maximum) || maximum <= 0) return 0;
		double safe = Double.isFinite(current) ? Math.max(0, Math.min(maximum, current)) : 0;
		if (!Double.isFinite(requested) || requested <= 0) return safe;
		return safe + Math.min(requested, maximum - safe);
	}

	/** Solo reduce un golpe finito y positivo; nunca amplifica ni produce daño negativo. */
	public static float eventDamage(float amount, boolean eventCreature, boolean protectedByVeil) {
		if (!Float.isFinite(amount) || amount <= 0 || !eventCreature || !protectedByVeil) return amount;
		return (float) (amount * (1.0 - ECLIPSE_VEIL_REDUCTION));
	}
}
