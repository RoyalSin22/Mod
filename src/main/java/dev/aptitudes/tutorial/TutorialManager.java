package dev.aptitudes.tutorial;

import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Eventos confirmados en el servidor y entrega individual de recompensas, sin compartir XP con grupos. */
public final class TutorialManager {
	/** Solo durante la entrega síncrona; se vacía en finally, no almacena progreso. */
	private static final Set<UUID> PAYING = new HashSet<>();

	private TutorialManager() {
	}

	public static void register() {
		// Después de RoleManager: el grimorio de bienvenida ya se entregó al inventario.
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			data(handler.getPlayer());
			tick(handler.getPlayer());
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> tick(newPlayer));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 != 0) return;
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) tick(player);
		});
	}

	/** La migración ocurre una sola vez; los siguientes accesos conservan el historial del attachment. */
	public static TutorialData data(ServerPlayerEntity player) {
		TutorialData data = player.getAttached(ModAttachments.TUTORIAL);
		if (data != null) return data;
		// El estado previo no conserva el modo en que se obtuvo. Se reconoce incluso si entra en creativo;
		// los eventos nuevos y la entrega de objetos siguen exigiendo un jugador vivo fuera de esos modos.
		data = new TutorialData(TutorialRules.existingProgress(TutorialRules.empty(), hasGuide(player),
				ProgressManager.get(player).getSoulLevel(), RoleManager.get(player).primary() != null));
		player.setAttached(ModAttachments.TUTORIAL, data);
		return data;
	}

	/** Lo llaman los ganchos tras su validación y éxito, nunca un payload de progreso del cliente. */
	public static void onStep(ServerPlayerEntity player, TutorialRules.Step step) {
		if (!eligible(player)) return;
		TutorialData before = data(player);
		TutorialData after = before.observe(step);
		if (!after.equals(before)) player.setAttached(ModAttachments.TUTORIAL, after);
		payPending(player);
	}

	public static void onCombo(ServerPlayerEntity player, int count) {
		if (TutorialRules.comboReached(count)) onStep(player, TutorialRules.Step.COMBO);
	}

	private static void tick(ServerPlayerEntity player) {
		if (!eligible(player)) return;
		TutorialData data = data(player);
		if (data.state().skipped()) return;
		if (!data.state().observed().contains(TutorialRules.Step.GUIDE) && hasGuide(player)) {
			player.setAttached(ModAttachments.TUTORIAL, data.observe(TutorialRules.Step.GUIDE));
		}
		payPending(player);
	}

	private static boolean eligible(ServerPlayerEntity player) {
		return player.isAlive() && !player.isCreative() && !player.isSpectator();
	}

	private static boolean hasGuide(ServerPlayerEntity player) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isOf(ModItems.GUIDE_BOOK)) return true;
		}
		return false;
	}

	private static void payPending(ServerPlayerEntity player) {
		if (!PAYING.add(player.getUuid())) return;
		try {
			// Como máximo seis iteraciones; todas las mutaciones ocurren en el hilo del servidor.
			for (int i = 0; i < TutorialRules.ORDER.size(); i++) {
				TutorialRules.Step step = TutorialRules.pendingReward(data(player).state());
				if (step == null) return;
				ItemStack reward = new ItemStack(Items.EMERALD, TutorialRules.emeralds(step));
				// Una sola unidad: si no cabe no se insertó nada, y el cobro sigue pendiente.
				if (!player.getInventory().insertStack(reward)) return;
				// La guarda bloquea entregas anidadas. Se conserva cualquier observación hecha al insertar.
				player.setAttached(ModAttachments.TUTORIAL, data(player).claim(step));
			}
		} finally {
			PAYING.remove(player.getUuid());
		}
	}
}
