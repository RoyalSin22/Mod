package dev.aptitudes.contract;

import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

/** Progreso de Caza individual y sincronizado con los miembros conectados del grupo v29. */
public final class ContractManager {
	private ContractManager() {}
	private static ContractData data(ServerPlayerEntity p) { return p.getAttachedOrCreate(ModAttachments.CONTRACTS); }
	private static int evolution(ServerPlayerEntity p) {
		var primary = RoleManager.get(p).primary();
		return primary == null ? 0 : RoleManager.get(p).tier(primary);
	}
	public static List<ContractRules.Offer> offers(ServerPlayerEntity p) {
		long day = p.getServerWorld().getTimeOfDay() / 24000L;
		ContractData data = data(p); data.refreshDay(day);
		return ContractRules.dailyOffers(p.getUuid(), day, data.reputation(), evolution(p));
	}
	public static boolean accept(ServerPlayerEntity p, int offerIndex) {
		List<ContractRules.Offer> offers = offers(p);
		return offerIndex >= 0 && offerIndex < offers.size() && data(p).accept(offerIndex);
	}
	public static void abandon(ServerPlayerEntity p, int offerIndex) { data(p).abandon(offerIndex); }
	public static boolean huntProgress(ServerPlayerEntity player, EntityType<?> victim) {
		List<ContractRules.Offer> offers = offers(player);
		ContractData data = data(player); boolean changed = false;
		for (ContractData.Active active : data.active()) {
			ContractRules.Offer o = offers.get(active.offer());
			if (o.kind() != ContractRules.Kind.HUNT || o.target() != target(victim)) continue;
			data.progress(active.offer(), 1, o.goal()); changed = true;
		}
		return changed;
	}
	private static int target(EntityType<?> type) {
		if (type == EntityType.ZOMBIE) return 0;
		if (type == EntityType.SKELETON) return 1;
		if (type == EntityType.CREEPER) return 2;
		if (type == EntityType.SPIDER) return 3;
		return 4;
	}
	public static void onKill(ServerPlayerEntity killer, EntityType<?> victim) {
		for (ServerPlayerEntity member : PartyManager.onlineMembers(killer)) {
			if (huntProgress(member, victim)) member.sendMessage(Text.translatable("contract.progress.hunt").formatted(Formatting.GREEN), true);
		}
	}
}
