package dev.aptitudes.sync;

import dev.aptitudes.network.SyncProgressPayload;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Última copia de los datos recibida por el cliente.
 *
 * <p>Vive en el source set común (no usa clases de cliente) porque el mixin de
 * velocidad de minado, que es común, necesita leerla cuando se ejecuta en el
 * lado cliente. En un servidor dedicado simplemente permanece vacía.</p>
 */
public final class ClientCache {
	private static volatile SyncProgressPayload data;
	private static volatile UUID owner;

	private ClientCache() {
	}

	public static void update(UUID playerId, SyncProgressPayload payload) {
		owner = playerId;
		data = payload;
	}

	public static void clear() {
		ClientMastery.clear();
		owner = null;
		data = null;
	}

	@Nullable
	public static SyncProgressPayload get() {
		return data;
	}

	@Nullable
	public static UUID owner() {
		return owner;
	}
}
