package dev.aptitudes.logic;

/**
 * Tácticas de las criaturas según su anillo de Amenaza (V83): llamada de auxilio, esquiva de proyectiles, furia
 * al quedar heridas y resistencia al retroceso. Lógica pura, sin Minecraft, para poder probarla.
 */
public final class ThreatTactics {
	/** Esperas y topes fijos. */
	public static final int CALL_COOLDOWN_TICKS = 60;
	public static final int CALL_MAX_ALLIES = 6;
	public static final float ENRAGE_HEALTH = 0.35f;
	public static final double MAX_DODGE = 0.25;
	public static final double MAX_KNOCKBACK_RESISTANCE = 0.4;

	private ThreatTactics() {
	}

	/** Radio en bloques en el que una criatura herida avisa a las demás; 0 en la zona inicial. */
	public static int callRadius(int ring) {
		int r = ThreatRules.boundedRing(ring);
		return r < 1 ? 0 : 8 + 2 * r;
	}

	/** Probabilidad de esquivar un proyectil: desde el anillo II, {@code (anillo − 1) × porAnillo}, con tope 25 %. */
	public static double dodgeChance(int ring, double perRing) {
		int r = ThreatRules.boundedRing(ring);
		if (r < 2) return 0;
		return Math.min(MAX_DODGE, (r - 1) * ThreatRules.bounded(perRing, 0, 0.15));
	}

	/** La furia se activa una sola vez, desde el anillo III, al bajar del 35 % de vida. */
	public static boolean enrages(int ring, float healthFraction, boolean alreadyEnraged) {
		return !alreadyEnraged && ThreatRules.boundedRing(ring) >= 3 && Float.isFinite(healthFraction)
				&& healthFraction > 0 && healthFraction <= ENRAGE_HEALTH;
	}

	/** Duración de la furia en ticks: 5 s en el anillo III, hasta 9 s en el V. */
	public static int enrageTicks(int ring) {
		return 100 + 40 * Math.max(0, Math.min(2, ThreatRules.boundedRing(ring) - 3));
	}

	/** Amplificador de Velocidad y Fuerza durante la furia: I hasta el anillo IV, II en el V. */
	public static int enrageAmplifier(int ring) {
		return ThreatRules.boundedRing(ring) >= 5 ? 1 : 0;
	}

	/** Resistencia al retroceso: 8 % por anillo, tope 40 %. */
	public static double knockbackResistance(int ring) {
		return Math.min(MAX_KNOCKBACK_RESISTANCE, 0.08 * ThreatRules.boundedRing(ring));
	}
}
