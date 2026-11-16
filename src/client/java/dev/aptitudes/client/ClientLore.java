package dev.aptitudes.client;

import dev.aptitudes.guide.LoreState;
import dev.aptitudes.network.LorePayload;

/**
 * v67 (T7a): copia visual del estado de lore del Grimorio. Solo se lee; el servidor manda sincronización en eventos.
 */
public final class ClientLore {
	private static LoreState state = new LoreState();
	private static boolean received;

	private ClientLore() {
	}

	public static void onPayload(LorePayload payload) {
		state = payload.loreState();
		received = true;
	}

	/** Retorna True si un lore está desbloqueado. */
	public static boolean isUnlocked(String loreId) {
		return state.isUnlocked(loreId);
	}

	public static LoreState state() { return state; }
	public static boolean received() { return received; }

	public static void clear() {
		state = new LoreState();
		received = false;
	}
}
