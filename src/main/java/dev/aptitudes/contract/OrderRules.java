package dev.aptitudes.contract;

/**
 * Fase F4 del plan de avance: pedidos del gremio entre jugadores. Puro (sin Minecraft).
 *
 * <p>Un jugador publica un pedido y otro lo completa a cambio de comisión. El servidor es dueño del estado. Los pedidos
 * de nivel alto exigen participación de varios jugadores: nivel 1 → 1 jugador, 2 → 2, 3 → 4 y 4 → 5. La comisión es
 * el 10 % del precio. El reparto usa división entera y el resto va a los primeros participantes.</p>
 */
public final class OrderRules {
	/** Comisión del artesano en porcentaje del precio. */
	public static final int COMMISSION_PERCENT = 10;
	/** Participantes máximos de un pedido. */
	public static final int MAX_PARTICIPANTS = 5;

	private static final int[] PARTICIPANTS_BY_TIER = {1, 2, 4, 5};

	private OrderRules() {}

	/** Participantes que exige un pedido de este nivel (1–4; se ajusta fuera de rango). */
	public static int requiredParticipants(int tier) {
		int clamped = Math.max(1, Math.min(PARTICIPANTS_BY_TIER.length, tier));
		return PARTICIPANTS_BY_TIER[clamped - 1];
	}

	/** Comisión sobre el precio. Nunca negativa. */
	public static long commission(long price) {
		return Math.max(0, price) / 100 * COMMISSION_PERCENT + Math.max(0, price) % 100 * COMMISSION_PERCENT / 100;
	}

	/**
	 * Reparto del total entre participantes. La suma de las partes es exactamente {@code total}.
	 */
	public static long[] split(long total, int participants) {
		int n = Math.max(1, Math.min(MAX_PARTICIPANTS, participants));
		long safeTotal = Math.max(0, total);
		long base = safeTotal / n;
		long remainder = safeTotal % n;
		long[] shares = new long[n];
		for (int i = 0; i < n; i++) shares[i] = base + (i < remainder ? 1 : 0);
		return shares;
	}
}
