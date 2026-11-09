package dev.aptitudes.client;

import dev.aptitudes.network.GrimoirePayload;
import dev.aptitudes.network.GrimoireRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.Map;

/** v61: copia visual del bestiario y del recuento de logros. Solo se lee; el servidor manda. */
public final class ClientGrimoire {
	private static Map<String, Integer> kills = Map.of();
	private static int achievementsDone;
	private static int achievementsTotal;
	private static boolean received;

	private ClientGrimoire() {
	}

	public static void onPayload(GrimoirePayload payload) {
		kills = payload.kills();
		achievementsDone = payload.achievementsDone();
		achievementsTotal = payload.achievementsTotal();
		received = true;
	}

	/** Pide datos frescos (el servidor limita la frecuencia). */
	public static void request() {
		if (ClientPlayNetworking.canSend(GrimoireRequestPayload.ID)) ClientPlayNetworking.send(new GrimoireRequestPayload());
	}

	public static Map<String, Integer> kills() { return kills; }
	public static int kills(String id) { return kills.getOrDefault(id, 0); }
	public static boolean unlocked(String id) { return kills(id) > 0; }
	public static int achievementsDone() { return achievementsDone; }
	public static int achievementsTotal() { return achievementsTotal; }
	public static boolean received() { return received; }

	public static void clear() {
		kills = Map.of();
		achievementsDone = 0;
		achievementsTotal = 0;
		received = false;
	}
}
