package dev.aptitudes.contract;

/**
 * Reglas puras de los pedidos entre jugadores (F4). Sin Minecraft; las cantidades son unidades de objeto y los precios,
 * esmeraldas.
 *
 * <p>Decisiones de diseño (autor, v65): la moneda son esmeraldas, el pago es al entregar y el objeto se entrega al dueño
 * solo al completarse. El precio por unidad y el tope de pedidos abiertos son provisionales.</p>
 *
 * <p>Participantes: un pedido de nivel {@code T} necesita {@link OrderRules#requiredParticipants(int)} jugadores
 * distintos. Para que siempre pueda completarse, cada unidad que falta por entregar se reserva para un participante
 * que aún no ha entrado: un jugador que ya participa solo puede entregar lo que sobra tras esas reservas.</p>
 */
public final class OrderBookRules {
	/** Cantidad máxima de un pedido. */
	public static final int MAX_COUNT = 64;
	/** Pedidos abiertos por dueño a la vez. */
	public static final int MAX_OPEN_PER_OWNER = 3;

	private OrderBookRules() {}

	/** Esmeraldas por unidad según el nivel (1–4; se ajusta fuera de rango). Provisional: 2 × nivel. */
	public static long unitPrice(int tier) {
		return 2L * clampTier(tier);
	}

	/** Esmeraldas que el dueño deposita al publicar: el precio de todas las unidades. */
	public static long escrow(int count, int tier) {
		return (long) clampCount(count) * unitPrice(tier);
	}

	/** Precio bruto de {@code quantity} unidades. */
	public static long gross(int quantity, int tier) {
		return (long) Math.max(0, quantity) * unitPrice(tier);
	}

	/** Lo que cobra quien entrega: el bruto menos la comisión del gremio. */
	public static long net(long gross) {
		return Math.max(0, gross) - OrderRules.commission(gross);
	}

	/** ¿Puede publicarse un pedido? Debe tener al menos una unidad por participante exigido y no superar el tope. */
	public static boolean canPublish(int count, int tier, int openByOwner) {
		return count >= OrderRules.requiredParticipants(tier) && count <= MAX_COUNT
				&& openByOwner < MAX_OPEN_PER_OWNER;
	}

	/**
	 * Unidades que puede entregar un jugador ahora.
	 *
	 * @param remaining unidades que faltan
	 * @param participants jugadores que ya han entregado
	 * @param required participantes exigidos por el nivel
	 * @param alreadyParticipant si este jugador ya ha entregado antes
	 * @param requested unidades que quiere entregar
	 */
	public static int allowedQuantity(int remaining, int participants, int required, boolean alreadyParticipant,
			int requested) {
		if (remaining <= 0 || requested <= 0) return 0;
		int missing = Math.max(0, required - participants);
		// Un jugador nuevo ocupa uno de los cupos que faltan; uno que ya participa no.
		int reserved = alreadyParticipant ? missing : Math.max(0, missing - 1);
		int allowed = Math.max(0, remaining - reserved);
		return Math.min(allowed, requested);
	}

	/** Esmeraldas a devolver al dueño al cancelar: lo que quedó sin entregar. */
	public static long refund(int remaining, int tier) {
		return (long) Math.max(0, remaining) * unitPrice(tier);
	}

	private static int clampTier(int tier) {
		return Math.max(1, Math.min(4, tier));
	}

	private static int clampCount(int count) {
		return Math.max(0, Math.min(MAX_COUNT, count));
	}
}
