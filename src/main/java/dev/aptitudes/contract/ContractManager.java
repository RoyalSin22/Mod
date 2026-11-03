package dev.aptitudes.contract;

import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
	public static void sync(ServerPlayerEntity p) {
		var data=data(p); var offers=offers(p); var active=data.active();
		java.util.List<dev.aptitudes.network.ContractViewPayload.Row> all=new java.util.ArrayList<>(), running=new java.util.ArrayList<>();
		for(int i=0;i<offers.size();i++){var o=offers.get(i);all.add(row(i,o,0));}
		for(var a:active){var o=offers.get(a.offer());running.add(row(a.offer(),o,a.progress()));}
		net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new dev.aptitudes.network.ContractViewPayload(
				ContractRules.rank(data.reputation()),data.reputation(),all,running));
	}
	private static dev.aptitudes.network.ContractViewPayload.Row row(int i,ContractRules.Offer o,int progress){
		return new dev.aptitudes.network.ContractViewPayload.Row(i,o.kind().ordinal(),o.difficulty().ordinal(),o.target(),o.goal(),progress,
				o.emeralds(),o.fragments(),o.roleXp(),o.voucher(),o.emblem());
	}
	public static boolean accept(ServerPlayerEntity p, int offerIndex) {
		List<ContractRules.Offer> offers = offers(p);
		boolean accepted = offerIndex >= 0 && offerIndex < offers.size() && data(p).accept(offerIndex);
		if (accepted) p.sendMessage(Text.translatable("contract.accepted").formatted(Formatting.GREEN), true);
		return accepted;
	}
	public static void abandon(ServerPlayerEntity p, int offerIndex) { data(p).abandon(offerIndex); p.sendMessage(Text.translatable("contract.abandoned").formatted(Formatting.GRAY), true); }
	public static void professionXp(ServerPlayerEntity p, int amount) { progressKind(p, ContractRules.Kind.PROFESSION, amount); }
	public static void fish(ServerPlayerEntity p, int amount) { progressKind(p, ContractRules.Kind.FISHING, amount); }
	public static void enchant(ServerPlayerEntity p, int amount) { progressKind(p, ContractRules.Kind.ENCHANTING, amount); }
	public static void forge(ServerPlayerEntity p) { progressKind(p, ContractRules.Kind.FORGE, 1); }
	public static void onEventSurvived(ServerPlayerEntity p, dev.aptitudes.event.WorldEventKind kind) {
		progressTarget(p, ContractRules.Kind.EVENT, kind.ordinal());
	}
	public static void onBossKilled(ServerPlayerEntity p, dev.aptitudes.boss.BossKind kind) {
		for (ServerPlayerEntity member : PartyManager.onlineMembers(p)) progressTarget(member, ContractRules.Kind.BOSS, kind.ordinal());
	}
	private static void progressTarget(ServerPlayerEntity p, ContractRules.Kind kind, int target) {
		List<ContractRules.Offer> offers=offers(p); ContractData d=data(p);
		for (ContractData.Active a:d.active()) { var o=offers.get(a.offer()); if(o.kind()==kind && o.target()==target) d.progress(a.offer(),1,o.goal()); }
	}
	private static void progressKind(ServerPlayerEntity p, ContractRules.Kind kind, int amount) {
		List<ContractRules.Offer> offers=offers(p); ContractData d=data(p);
		for (ContractData.Active a:d.active()) { var o=offers.get(a.offer()); if(o.kind()==kind) d.progress(a.offer(),amount,o.goal()); }
	}
	public static void deliver(ServerPlayerEntity p, int index) {
		List<ContractRules.Offer> offers=offers(p); if(index>=offers.size()) return;
		var o=offers.get(index); if(o.kind()!=ContractRules.Kind.DELIVERY) return;
		if (data(p).active().stream().noneMatch(a -> a.offer() == index)) return;
		var inv=p.getInventory(); int need=o.goal();
		for(int slot=0;slot<inv.size()&&need>0;slot++) { ItemStack stack=inv.getStack(slot); if(matches(o.target(),stack)) { int take=Math.min(need,stack.getCount()); stack.decrement(take); need-=take; } }
		if(need==0) data(p).progress(index,o.goal(),o.goal());
		else p.sendMessage(Text.translatable("contract.delivery.missing",need).formatted(Formatting.RED),true);
	}
	private static boolean matches(int target, ItemStack stack) {
		return switch(target) { case 0 -> stack.isOf(Items.IRON_INGOT); case 1 -> stack.isOf(Items.WHEAT); case 2 -> stack.isOf(Items.CARROT); case 3 -> stack.isOf(Items.COD); case 4 -> stack.isOf(Items.SALMON); default -> stack.isOf(Items.POTATO); };
	}
	public static void claim(ServerPlayerEntity p,int index) {
		List<ContractRules.Offer> offers=offers(p); ContractData d=data(p); if(index>=offers.size()||!d.complete(index,offers.get(index).goal())) return;
		var o=offers.get(index);
		p.getInventory().offerOrDrop(new ItemStack(Items.EMERALD,o.emeralds()));
		if(o.fragments()>0) p.getInventory().offerOrDrop(new ItemStack(dev.aptitudes.item.ModItems.SOUL_SHARD,o.fragments()));
		var primary=RoleManager.get(p).primary(); if(primary!=null) RoleManager.addRoleXp(p,primary,o.roleXp());
		if(o.voucher()) p.getInventory().offerOrDrop(new ItemStack(dev.aptitudes.item.ModItems.ARMORY_VOUCHER));
		if(o.emblem()) p.getInventory().offerOrDrop(new ItemStack(dev.aptitudes.item.ModItems.MASTERY_EMBLEM));
		d.completeAndReward(index,o.reputation()); p.sendMessage(Text.translatable("contract.claimed",o.reputation()).formatted(Formatting.GOLD),true);
	}
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
