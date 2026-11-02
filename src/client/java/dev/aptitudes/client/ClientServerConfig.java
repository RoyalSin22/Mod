package dev.aptitudes.client;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.network.ConfigSyncPayload;

/**
 * v40: configuración del servidor vista desde el cliente (sincronizada al entrar y tras cada cambio). Sirve para
 * que la pantalla de configuración y los tooltips (p. ej., runas) muestren los valores reales del servidor.
 * Sin sincronizar (servidor sin el mod o antes de recibirla) se usan los valores locales.
 */
public final class ClientServerConfig {
	private static final Gson GSON = new Gson();
	private static AptitudesConfig synced;
	private static boolean canEdit;
	private static int revision;

	private ClientServerConfig() {
	}

	public static void onSync(ConfigSyncPayload payload) {
		try {
			AptitudesConfig parsed = GSON.fromJson(payload.json(), AptitudesConfig.class);
			if (parsed != null) {
				synced = parsed;
				canEdit = payload.canEdit();
				revision++;
			}
		} catch (JsonParseException e) {
			Aptitudes.LOGGER.warn("[Aptitudes] Configuración del servidor ilegible", e);
		}
	}

	public static void clear() {
		synced = null;
		canEdit = false;
		revision++;
	}

	/** Configuración efectiva: la del servidor si se recibió; si no, la local. */
	public static AptitudesConfig get() {
		return synced != null ? synced : Aptitudes.config();
	}

	public static boolean isSynced() {
		return synced != null;
	}

	/** ¿El jugador puede editar la configuración del servidor (operador)? */
	public static boolean canEdit() {
		return canEdit;
	}

	/** Cambia en cada sincronización (la pantalla lo usa para refrescar los valores). */
	public static int revision() {
		return revision;
	}
}
