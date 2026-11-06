package dev.aptitudes.sync;

import dev.aptitudes.network.MasteryPayload;
import java.util.UUID;

/** Copia del servidor también accesible desde la predicción común de rotura de bloques. */
public final class ClientMastery {
	private static MasteryPayload data;
	private static UUID owner;
	private ClientMastery() { }
	public static void update(UUID player, MasteryPayload payload) { owner = player; data = payload; }
	public static MasteryPayload get() { return data; }
	public static UUID owner() { return owner; }
	public static void clear() { owner = null; data = null; }
}
