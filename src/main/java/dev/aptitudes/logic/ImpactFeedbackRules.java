package dev.aptitudes.logic;

/**
 * V95: reglas puras del lenguaje común de feedback de combate. Decide qué golpes merecen una señal visual y sonora propia (para no
 * saturar la pantalla), cuánta curación fue de verdad efectiva y cada cuánto se puede repetir una señal.
 */
public final class ImpactFeedbackRules {
	/** Un golpe es «pesado» si quita al menos esta parte de la vida máxima de la víctima... */
	public static final double HEAVY_RATIO = 0.20;
	/** ...o si causa al menos este daño absoluto (para víctimas con mucha vida). */
	public static final double HEAVY_MIN_DAMAGE = 8.0;
	/** Curación efectiva mínima para que valga una señal. */
	public static final double MIN_HEAL_SIGNAL = 0.5;
	/** Ticks mínimos entre dos señales de impacto del mismo atacante. */
	public static final int IMPACT_INTERVAL_TICKS = 2;
	/** Ticks mínimos entre dos señales de bloqueo, de curación o de recurso insuficiente. */
	public static final int SLOW_INTERVAL_TICKS = 10;

	private ImpactFeedbackRules() {
	}

	/** Tipo de señal de impacto; el orden es el que viaja por la red (no reordenar). */
	public enum Kind {
		NORMAL, HEAVY, CRITICAL;

		public static Kind byIndex(int index) {
			Kind[] all = values();
			return index < 0 || index >= all.length ? NORMAL : all[index];
		}
	}

	/**
	 * Clasifica un golpe. Devuelve {@code null} si no merece señal: un golpe vanilla normal ya tiene la suya y las habilidades pequeñas se
	 * ven mejor sin ruido añadido. Los críticos y los golpes pesados siempre la tienen; el resto, solo si lo dio una habilidad.
	 */
	public static Kind classify(double amount, double victimMaxHealth, boolean critical, boolean fromAbility) {
		if (!(amount > 0)) return null;
		if (critical) return Kind.CRITICAL;
		boolean heavy = amount >= HEAVY_MIN_DAMAGE || (victimMaxHealth > 0 && amount / victimMaxHealth >= HEAVY_RATIO);
		if (heavy) return Kind.HEAVY;
		return fromAbility ? Kind.NORMAL : null;
	}

	/** Parte de {@code amount} que de verdad sube la vida: lo que cabe hasta el máximo. */
	public static double effectiveHeal(double healthBefore, double maxHealth, double amount) {
		if (!(amount > 0) || !(maxHealth > 0)) return 0;
		return Math.max(0, Math.min(amount, maxHealth - healthBefore));
	}

	public static double overheal(double healthBefore, double maxHealth, double amount) {
		if (!(amount > 0)) return 0;
		return Math.max(0, amount - effectiveHeal(healthBefore, maxHealth, amount));
	}

	/** ¿Ha pasado el intervalo desde la última señal? {@code lastTick} negativo = nunca. */
	public static boolean due(long lastTick, long nowTick, int interval) {
		return lastTick < 0 || nowTick - lastTick >= interval || nowTick < lastTick;
	}

	/** Peso visual del golpe entre 0 y 1 (daño relativo a la vida de la víctima), para escalar el efecto. */
	public static float weight(double amount, double victimMaxHealth) {
		if (!(amount > 0) || !(victimMaxHealth > 0)) return 0f;
		return (float) Math.min(1.0, amount / victimMaxHealth);
	}
}
