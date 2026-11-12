package dev.aptitudes.contract;

import dev.aptitudes.contract.ContractData.Active;
import dev.aptitudes.contract.ContractRules.Kind;
import dev.aptitudes.contract.ContractRules.Offer;
import dev.aptitudes.data.ModAttachments;
import dev.aptitudes.data.Skill;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.network.ContractViewPayload;
import dev.aptitudes.party.PartyManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * v30: contratos del gremio. Autoridad del servidor: aceptar, abandonar, entregar y cobrar se validan aquí;
 * el cliente solo envía el índice de la oferta (aceptar) o de la ranura activa (lo demás).
 */
public final class ContractManager {
	private ContractManager() {}

	private static ContractData data(ServerPlayerEntity p) {
		return p.getAttachedOrCreate(ModAttachments.CONTRACTS);
	}

	private static int evolution(ServerPlayerEntity p) {
		Role primary = RoleManager.get(p).primary();
		return primary == null ? 0 : RoleManager.get(p).tier(primary);
	}

	private static long day(ServerPlayerEntity p) {
		return Math.max(0L, p.getServer() == null ? 0L : p.getServer().getOverworld().getTimeOfDay() / 24_000L);
	}

	/** Ofertas de hoy (fijas por jugador y día; se generan con la reputación y la evolución del inicio del día). */
	public static List<Offer> offers(ServerPlayerEntity p) {
		ContractData data = data(p);
		long day = day(p);
		data.refreshDay(day, evolution(p));
		return ContractRules.dailyOffers(p.getUuid(), day, data.offerReputation(), data.offerEvolution());
	}

	// ------------------------------------------------------------------ Acciones (C2S)

	public static void accept(ServerPlayerEntity p, int offerIndex) {
		List<Offer> offers = offers(p);
		if (offerIndex < 0 || offerIndex >= offers.size()) return;
		if (data(p).accept(offerIndex, offers.get(offerIndex))) {
			p.sendMessage(Text.translatable("contract.aptitudes.accepted").formatted(Formatting.GREEN), true);
		} else {
			p.sendMessage(Text.translatable("contract.aptitudes.cannot_accept").formatted(Formatting.RED), true);
		}
	}

	public static void abandon(ServerPlayerEntity p, int slot) {
		if (data(p).abandon(slot)) p.sendMessage(Text.translatable("contract.aptitudes.abandoned").formatted(Formatting.GRAY), true);
	}

	/** Entrega: solo si lleva la cantidad completa; si falta algo no se quita nada. */
	public static void deliver(ServerPlayerEntity p, int slot) {
		ContractData data = data(p);
		List<Active> active = data.active();
		if (slot < 0 || slot >= active.size()) return;
		Active a = active.get(slot);
		if (a.offer().kind() != Kind.DELIVERY || a.complete()) return;
		int need = a.offer().goal() - a.progress();
		PlayerInventory inv = p.getInventory();
		int have = 0;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (ContractTargets.deliveryMatches(a.offer().target(), s)) have += s.getCount();
		}
		if (!ContractRules.canDeliver(have, need)) {
			p.sendMessage(Text.translatable("contract.aptitudes.delivery_missing", need - have,
					ContractTargets.name(Kind.DELIVERY, a.offer().target())).formatted(Formatting.RED), true);
			return;
		}
		int left = need;
		for (int i = 0; i < inv.size() && left > 0; i++) {
			ItemStack s = inv.getStack(i);
			if (!ContractTargets.deliveryMatches(a.offer().target(), s)) continue;
			int take = Math.min(left, s.getCount());
			s.decrement(take);
			left -= take;
		}
		inv.markDirty();
		data.progress(slot, need);
		p.sendMessage(Text.translatable("contract.aptitudes.delivered").formatted(Formatting.GREEN), true);
	}

	public static void claim(ServerPlayerEntity p, int slot) {
		Offer o = data(p).claim(slot);
		if (o == null) return;
		PlayerInventory inv = p.getInventory();
		if (o.emeralds() > 0) inv.offerOrDrop(new ItemStack(Items.EMERALD, Math.min(64, o.emeralds())));
		if (o.fragments() > 0) inv.offerOrDrop(new ItemStack(ModItems.SOUL_SHARD, Math.min(64, o.fragments())));
		Role primary = RoleManager.get(p).primary();
		if (primary != null && o.roleXp() > 0) RoleManager.addRoleXp(p, primary, o.roleXp());
		if (o.voucher()) inv.offerOrDrop(new ItemStack(ModItems.ARMORY_VOUCHER));
		if (o.emblem()) inv.offerOrDrop(new ItemStack(ModItems.MASTERY_EMBLEM));
		p.sendMessage(Text.translatable("contract.aptitudes.claimed", o.reputation()).formatted(Formatting.GOLD), true);
		dev.aptitudes.achievement.Achievements.checkContracts(p); // v31
	}

	// ------------------------------------------------------------------ Progreso (llamadas desde otros sistemas)

	/** Avanza los contratos activos de un tipo (y, si se indica, de un objetivo). Devuelve si alguno cambió. */
	private static boolean advance(ServerPlayerEntity p, Kind kind, int target, int amount) {
		if (amount <= 0) return false;
		ContractData data = data(p);
		List<Active> active = data.active();
		boolean changed = false;
		for (int slot = 0; slot < active.size(); slot++) {
			Offer o = active.get(slot).offer();
			if (!ContractRules.advances(o.kind(), o.target(), kind, target)) continue;
			boolean before = active.get(slot).complete();
			if (data.progress(slot, amount)) {
				changed = true;
				if (!before && data.active().get(slot).complete()) {
					p.sendMessage(Text.translatable("contract.aptitudes.ready").formatted(Formatting.GOLD), true);
				}
			}
		}
		return changed;
	}

	/** Caza: cuenta para el que mata y para los miembros conectados de su grupo (v29). */
	public static void onKill(ServerPlayerEntity killer, EntityType<?> victim) {
		// v65: Nether y criaturas nuevas cuentan para todo el grupo, como la caza.
		if (ContractTargets.NETHER.contains(victim) || ContractTargets.NEW_MOBS.contains(victim)) {
			Kind kind = ContractTargets.NETHER.contains(victim) ? Kind.NETHER : Kind.NEW_MOB;
			// Objetivo −1: estas ofertas no tienen objetivo y se generan con −1 (con 0 nunca avanzarían).
			for (ServerPlayerEntity member : PartyManager.onlineMembers(killer)) advance(member, kind, -1, 1);
		}
		int target = ContractTargets.huntIndex(victim);
		if (target < 0) return;
		for (ServerPlayerEntity member : PartyManager.onlineMembers(killer)) advance(member, Kind.HUNT, target, 1);
	}

	/** Oficio: XP real de una habilidad (no la de comandos de administrador). */
	public static void skillXp(ServerPlayerEntity p, Skill skill, int amount) {
		advance(p, Kind.PROFESSION, skill.ordinal(), amount);
	}

	public static void fish(ServerPlayerEntity p, int amount) { advance(p, Kind.FISHING, -1, amount); }

	public static void enchant(ServerPlayerEntity p, int amount) { advance(p, Kind.ENCHANTING, -1, amount); }

	public static void forge(ServerPlayerEntity p) { advance(p, Kind.FORGE, -1, 1); }

	/** v65: el jugador completó una expedición de Amenaza. */
	public static void expeditionDone(ServerPlayerEntity p) { advance(p, Kind.EXPEDITION, -1, 1); }

	public static void onEventSurvived(ServerPlayerEntity p, dev.aptitudes.event.WorldEventKind kind) {
		advance(p, Kind.EVENT, kind.ordinal(), 1);
		dev.aptitudes.event.Expeditions.onEventSurvived(p); // F5: el módulo de evento
	}

	public static void onBossKilled(ServerPlayerEntity p, dev.aptitudes.boss.BossKind kind) {
		for (ServerPlayerEntity member : PartyManager.onlineMembers(p)) advance(member, Kind.BOSS, kind.ordinal(), 1);
	}

	/** Contratos cobrados en total (para logros). */
	public static int completed(ServerPlayerEntity p) {
		return data(p).completed();
	}

	/** Reputación actual en el gremio (para logros). */
	public static int reputation(ServerPlayerEntity p) {
		return data(p).reputation();
	}

	// ------------------------------------------------------------------ Vista (S2C)

	public static void sync(ServerPlayerEntity p) {
		if (!ServerPlayNetworking.canSend(p, ContractViewPayload.ID)) return;
		List<Offer> offers = offers(p);
		ContractData data = data(p);
		List<ContractViewPayload.Row> offerRows = new ArrayList<>();
		for (int i = 0; i < offers.size(); i++) offerRows.add(row(i, offers.get(i), 0, data.isTaken(i)));
		List<ContractViewPayload.Row> activeRows = new ArrayList<>();
		List<Active> active = data.active();
		for (int slot = 0; slot < active.size(); slot++) activeRows.add(row(slot, active.get(slot).offer(), active.get(slot).progress(), false));
		int limit = Math.min(ContractRules.MAX_ACTIVE, ContractRules.activeLimit(data.reputation()));
		ServerPlayNetworking.send(p, new ContractViewPayload(ContractRules.rankIndex(data.reputation()), data.reputation(),
				limit, offerRows, activeRows));
	}

	private static ContractViewPayload.Row row(int index, Offer o, int progress, boolean taken) {
		return new ContractViewPayload.Row(index, o.kind().ordinal(), o.difficulty().ordinal(), o.target(), o.goal(), progress,
				o.emeralds(), o.fragments(), o.roleXp(), o.reputation(), o.voucher(), o.emblem(), taken);
	}
}
