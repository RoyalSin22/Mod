package dev.aptitudes.client;

import dev.aptitudes.network.PartySyncPayload;

/** Última vista sincronizada por el servidor para el menú y el HUD del grupo. */
public final class PartyClientState {
	private static PartySyncPayload current;

	private PartyClientState() {
	}

	public static PartySyncPayload get() {
		return current;
	}

	public static void onSync(PartySyncPayload payload) {
		current = payload;
	}

	public static void clear() {
		current = null;
	}
}
