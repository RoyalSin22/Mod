package dev.aptitudes.party;

/** Reglas puras de límites y reparto de experiencia de los grupos. */
public final class PartyRules {
	private PartyRules() {
	}

	/** Porcentaje permitido, redondeado hacia abajo a pasos de cinco. */
	public static int sharePercent(int requested) {
		return Math.max(0, Math.min(50, requested / 5 * 5));
	}

	/** XP compartida; el cálculo intermedio no puede desbordarse aunque la entrada sea grande. */
	public static int sharedXp(int amount, int percent) {
		if (amount <= 0) return 0;
		long bounded = Math.max(0, Math.min(50, percent));
		return (int) Math.min(Integer.MAX_VALUE, (long) amount * bounded / 100L);
	}

	/** XP individual: el autor conserva el importe íntegro y los demás reciben una copia compartida. */
	public static int memberXp(int amount, int sharedPercent, boolean source, int bonusXp) {
		if (amount <= 0) return 0;
		long personal = source ? amount : sharedXp(amount, sharedPercent);
		return (int) Math.min(Integer.MAX_VALUE, personal + Math.max(0, bonusXp));
	}

	/** Suma XP base y bonificación para el origen, sin contar dos veces la XP base. */
	public static int sourceXp(int amount, int bonusXp) {
		if (amount <= 0) return 0;
		return (int) Math.min(Integer.MAX_VALUE, (long) amount + Math.max(0, bonusXp));
	}

	/** Fracción extra por cada miembro conectado además del líder, limitada por la configuración. */
	public static double bonus(int connectedMembers, double perMember, double maximum) {
		if (!Double.isFinite(perMember) || perMember < 0) perMember = 0;
		if (!Double.isFinite(maximum) || maximum < 0) maximum = 0;
		int contributors = Math.max(0, Math.min(5, connectedMembers) - 1);
		return Math.min(maximum, contributors * perMember);
	}

	/** Convierte una fracción en XP extra sin desbordar ni conceder valores negativos. */
	public static int bonusXp(int amount, double bonus) {
		if (amount <= 0 || !Double.isFinite(bonus) || bonus <= 0) return 0;
		return (int) Math.min(Integer.MAX_VALUE, Math.floor((double) amount * bonus));
	}
}
