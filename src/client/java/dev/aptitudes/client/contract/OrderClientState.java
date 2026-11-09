package dev.aptitudes.client.contract;

import dev.aptitudes.network.OrderViewPayload;

/** Última lista de pedidos recibida del servidor. La pantalla del tablón la muestra; no decide nada. */
public final class OrderClientState {
	private static OrderViewPayload view;

	private OrderClientState() {}

	public static void set(OrderViewPayload payload) {
		view = payload;
	}

	public static OrderViewPayload get() {
		return view;
	}

	public static void clear() {
		view = null;
	}
}
