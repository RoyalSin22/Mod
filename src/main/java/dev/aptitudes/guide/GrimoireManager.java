package dev.aptitudes.guide;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.achievement.Achievement;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.network.GrimoirePayload;
import dev.aptitudes.network.GrimoireRequestPayload;
import dev.aptitudes.party.PartyManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Grimorio v61 en el servidor: bestiario por jugador (G1) y datos de la página de progreso (G3).
 * Las entradas se desbloquean al vencer a la criatura: el que la remata y su grupo cercano.
 */
public final class GrimoireManager {
	/** Distancia máxima (en bloques) para que un miembro del grupo comparta la entrada. */
	private static final double PARTY_RANGE = 64;
	/** Peticiones de refresco: una por segundo como mucho. */
	private static final long REQUEST_COOLDOWN_TICKS = 20;
	private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();

	private GrimoireManager() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof PlayerEntity || !(source.getAttacker() instanceof ServerPlayerEntity killer)) return;
			onKill(killer, entity);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> LAST_REQUEST.remove(handler.player.getUuid()));
		ServerPlayNetworking.registerGlobalReceiver(GrimoireRequestPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			long now = player.getServer().getTicks();
			Long last = LAST_REQUEST.get(player.getUuid());
			if (last != null && now - last >= 0 && now - last < REQUEST_COOLDOWN_TICKS) return;
			LAST_REQUEST.put(player.getUuid(), now);
			sync(player);
		});
	}

	public static Map<String, Integer> get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.BESTIARY);
	}

	private static void onKill(ServerPlayerEntity killer, LivingEntity victim) {
		String bossKind = null;
		for (String tag : victim.getCommandTags()) {
			BossKind kind = BossKind.fromTag(tag);
			if (kind != null) {
				bossKind = kind.id();
				break;
			}
		}
		List<String> ids = BestiaryRules.unlockedBy(EntityType.getId(victim.getType()).toString(), bossKind,
				Elites.isElite(victim), Elites.isChampion(victim));
		if (ids.isEmpty()) return;
		for (ServerPlayerEntity member : PartyManager.onlineMembers(killer)) {
			if (member != killer && (member.getWorld() != killer.getWorld()
					|| member.squaredDistanceTo(victim) > PARTY_RANGE * PARTY_RANGE)) continue;
			record(member, ids);
		}
		// onlineMembers devuelve al jugador solo si no tiene grupo; si lo tiene, ya está incluido.
	}

	private static void record(ServerPlayerEntity player, List<String> ids) {
		Map<String, Integer> next = new TreeMap<>(get(player));
		BestiaryRules.Entry unlocked = null;
		for (String id : ids) {
			int before = next.getOrDefault(id, 0);
			next.put(id, BestiaryRules.increment(before));
			if (before <= 0 && unlocked == null) unlocked = BestiaryRules.byId(id);
		}
		player.setAttached(ModAttachments.BESTIARY, next);
		if (unlocked == null) return;
		player.sendMessage(Text.translatable("bestiary.aptitudes.unlocked",
				Text.translatable(BestiaryRules.nameKey(unlocked))).formatted(Formatting.GOLD), true);
		player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 0.8f, 1.2f);
		sync(player);
	}

	/** Envía el bestiario y el recuento de logros de Aptitudes (sin la raíz). */
	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, GrimoirePayload.ID)) return;
		int done = 0;
		int total = 0;
		MinecraftServer server = player.getServer();
		if (server != null) {
			for (Achievement achievement : Achievement.values()) {
				if (achievement == Achievement.ROOT) continue;
				AdvancementEntry entry = server.getAdvancementLoader().get(Aptitudes.id(achievement.path()));
				if (entry == null) continue; // un datapack puede quitarlo
				total++;
				if (player.getAdvancementTracker().getProgress(entry).isDone()) done++;
			}
		}
		ServerPlayNetworking.send(player, new GrimoirePayload(get(player), done, total));
	}
}
