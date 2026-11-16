package dev.aptitudes.guide;

import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.network.LorePayload;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Gestor de desbloqueos de lore del Grimorio (v67, T7a).
 *
 * <p>Desbloquea entradas de lore en eventos: encuentros con NPCs, derrotas de jefes, finales de eventos
 * mundiales. Valida en el servidor antes de escribir.
 */
public final class LoreManager {
	private LoreManager() {
	}

	/** Registra los hooks de sincronización de lore. */
	public static void register() {
		// Al conectarse, sincronizar el estado de lore del jugador.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
	}

	/**
	 * Desbloquea una entrada de lore para un jugador.
	 *
	 * @param player jugador del servidor
	 * @param loreId id de la entrada de lore
	 */
	public static void unlock(ServerPlayerEntity player, String loreId) {
		if (!LoreRules.isValidLoreId(loreId)) return;
		LoreState state = player.getAttachedOrCreate(ModAttachments.LORE);
		player.setAttached(ModAttachments.LORE, state.unlock(loreId));
	}

	/**
	 * Desbloquea múltiples entradas de lore para un jugador.
	 *
	 * @param player jugador del servidor
	 * @param loreIds ids de las entradas de lore
	 */
	public static void unlockAll(ServerPlayerEntity player, Set<String> loreIds) {
		LoreState state = player.getAttachedOrCreate(ModAttachments.LORE);
		LoreState updated = state.unlockAll(loreIds);
		if (updated != state) {
			player.setAttached(ModAttachments.LORE, updated);
		}
	}

	/**
	 * Desbloquea lore al hablar con el aldeano del gremio.
	 *
	 * @param player jugador del servidor
	 * @param whichEvent cuál evento se activó ("fog" o "rain")
	 */
	public static void unlocksOnGuildmasterTalk(ServerPlayerEntity player, String whichEvent) {
		Set<String> toUnlock = LoreRules.unlocksOnGuildmasterTalk(whichEvent);
		if (!toUnlock.isEmpty()) {
			unlockAll(player, toUnlock);
		}
	}

	/** Desbloquea lore al encontrar/derrotar al Custodio. */
	public static void unlocksOnCustodianEncounter(ServerPlayerEntity player) {
		unlockAll(player, LoreRules.unlocksOnCustodianEncounter());
	}

	/** Desbloquea lore al completar la cámara de diamante. */
	public static void unlocksOnDiamondChamberComplete(ServerPlayerEntity player) {
		unlockAll(player, LoreRules.unlocksOnDiamondChamberComplete());
	}

	/** Desbloquea lore al sacrificar el Behelit en la Hoguera. */
	public static void unlocksOnBeheritSacrifice(ServerPlayerEntity player) {
		unlockAll(player, LoreRules.unlocksOnBeheritSacrifice());
	}

	/** Desbloquea lore al finalizar el Eclipse (evento global). */
	public static void unlocksOnEclipseEnd(ServerPlayerEntity player) {
		unlockAll(player, LoreRules.unlocksOnEclipseEnd());
	}

	/** Retorna True si un lore está desbloqueado para el jugador. */
	public static boolean isUnlocked(ServerPlayerEntity player, String loreId) {
		LoreState state = player.getAttachedOrCreate(ModAttachments.LORE);
		return state.isUnlocked(loreId);
	}

	/** Envía el estado de lore del jugador al cliente. */
	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, LorePayload.ID)) return;
		LoreState state = player.getAttachedOrCreate(ModAttachments.LORE);
		ServerPlayNetworking.send(player, new LorePayload(state));
	}
}
