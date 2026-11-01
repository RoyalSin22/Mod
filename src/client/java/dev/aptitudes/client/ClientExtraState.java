package dev.aptitudes.client;

import dev.aptitudes.network.FormSyncPayload;
import dev.aptitudes.network.UniqueSyncPayload;
import org.jetbrains.annotations.Nullable;

/** Copias en el cliente del estado de formas (Cambiaformas) y de habilidades únicas. */
public final class ClientExtraState {
	private static FormSyncPayload forms;
	private static UniqueSyncPayload uniques;
	private static dev.aptitudes.network.StatsPayload stats;

	private ClientExtraState() {
	}

	static void onForms(FormSyncPayload payload) { forms = payload; }
	static void onUniques(UniqueSyncPayload payload) { uniques = payload; }
	static void onStats(dev.aptitudes.network.StatsPayload payload) { stats = payload; }
	@Nullable public static dev.aptitudes.network.StatsPayload stats() { return stats; }

	static void clear() {
		forms = null;
		uniques = null;
		stats = null;
	}

	@Nullable public static FormSyncPayload forms() { return forms; }
	@Nullable public static UniqueSyncPayload uniques() { return uniques; }
}
