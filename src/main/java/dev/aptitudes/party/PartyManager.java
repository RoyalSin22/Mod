package dev.aptitudes.party;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.network.PartySyncPayload;
import dev.aptitudes.network.PartySyncPayload.Member;
import dev.aptitudes.network.PartySyncPayload.OnlinePlayer;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Autoridad del servidor para grupos, invitaciones, XP y sincronización de vistas. */
public final class PartyManager {
	private static final long INVITE_TICKS = 60L * 20L;
	private static final Map<UUID, Invite> INVITES = new HashMap<>();
	private static final Map<UUID, Long> LAST_SYNC = new HashMap<>();

	private PartyManager() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(PartyManager::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> requestSync(handler.getPlayer()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			UUID id = handler.getPlayer().getUuid();
			LAST_SYNC.remove(id);
			INVITES.remove(id);
			INVITES.values().removeIf(invite -> invite.inviter().equals(id));
		});
	}

	private static void tick(MinecraftServer server) {
		long now = server.getTicks();
		INVITES.values().removeIf(invite -> now >= invite.expiresAt());
		if (now % 10 != 0) return;
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) syncIfDue(player, now);
	}

	private static PartyState state(MinecraftServer server) {
		return PartyState.get(server.getOverworld());
	}

	private static PartyState.Party findParty(MinecraftServer server, UUID player) {
		for (PartyState.Party party : state(server).parties.values()) {
			if (party.members.contains(player)) return party;
		}
		return null;
	}

	public static boolean inSameParty(ServerPlayerEntity first, ServerPlayerEntity second) {
		if (first == second) return true;
		if (first.getServer() == null || first.getServer() != second.getServer()) return false;
		PartyState.Party party = findParty(first.getServer(), first.getUuid());
		return party != null && party.members.contains(second.getUuid());
	}

	public static boolean hasParty(ServerPlayerEntity player) {
		return player.getServer() != null && findParty(player.getServer(), player.getUuid()) != null;
	}

	public static boolean isAlly(ServerPlayerEntity player, Entity other) {
		if (other == player) return true;
		ServerPlayerEntity otherPlayer = other instanceof ServerPlayerEntity sp ? sp : owner(other);
		return otherPlayer != null && inSameParty(player, otherPlayer);
	}

	private static ServerPlayerEntity owner(Entity entity) {
		ServerPlayerEntity owner = dev.aptitudes.ability.AbilityState.ownerOf(entity);
		if (owner != null) return owner;
		return entity instanceof TameableEntity tameable && tameable.getOwner() instanceof ServerPlayerEntity sp ? sp : null;
	}

	/** Bloquea daño de miembros y de sus invocaciones cuando el líder desactiva el fuego amigo. */
	public static boolean blocksFriendlyDamage(Entity attacker, ServerPlayerEntity victim) {
		ServerPlayerEntity owner = attacker instanceof ServerPlayerEntity sp ? sp : owner(attacker);
		if (owner == null || !inSameParty(owner, victim)) return false;
		PartyState.Party party = findParty(owner.getServer(), owner.getUuid());
		return party != null && !party.friendlyFire;
	}

	public static List<ServerPlayerEntity> onlineMembers(ServerPlayerEntity player) {
		if (player.getServer() == null) return List.of(player);
		PartyState.Party party = findParty(player.getServer(), player.getUuid());
		if (party == null) return List.of(player);
		List<ServerPlayerEntity> result = new ArrayList<>();
		for (UUID id : party.members) {
			ServerPlayerEntity online = player.getServer().getPlayerManager().getPlayer(id);
			if (online != null) result.add(online);
		}
		return result;
	}

	/** Da XP de combate más su extra al grupo y concede la parte compartida una sola vez. */
	public static void awardCombatXp(ServerPlayerEntity killer, dev.aptitudes.data.Skill skill, int baseAmount) {
		if (baseAmount <= 0) return;
		AptitudesConfig config = Aptitudes.config();
		if (killer.isSpectator() || (killer.isCreative() && !config.awardXpInCreative)) {
			dev.aptitudes.logic.ProgressManager.addXp(killer, skill, baseAmount);
			return;
		}
		if (dev.aptitudes.logic.ProgressManager.get(killer).getLevel(skill)
				>= dev.aptitudes.logic.ProgressManager.rules().maxLevel()) return;
		PartyState.Party party = killer.getServer() == null ? null : findParty(killer.getServer(), killer.getUuid());
		if (party == null) {
			dev.aptitudes.logic.ProgressManager.addXp(killer, skill, baseAmount);
			return;
		}
		List<ServerPlayerEntity> members = onlineMembers(killer);
		double bonus = PartyRules.bonus(members.size(), Aptitudes.config().partyXpBonusPerMember,
				Aptitudes.config().partyXpBonusMax);
		int bonusXp = PartyRules.bonusXp(baseAmount, bonus);
		for (ServerPlayerEntity member : members) {
			int amount = PartyRules.memberXp(baseAmount, party.sharePercent, member == killer, bonusXp);
			if (amount > 0) dev.aptitudes.logic.ProgressManager.addXp(member, skill, amount);
		}
	}

	/** Reparte también la XP que el atacante ya ganó para su rol de combate, sin volver a repartirla. */
	public static void awardCombatRoleXp(ServerPlayerEntity killer, Role role, int baseAmount) {
		if (baseAmount <= 0) return;
		PartyState.Party party = killer.getServer() == null ? null : findParty(killer.getServer(), killer.getUuid());
		if (RoleManager.get(killer).level(role) >= RoleManager.rules().maxLevel()) {
			RoleManager.addRoleXp(killer, role, baseAmount); // conserva la carga de recurso, pero no comparte XP ya capada
			return;
		}
		int earned = RoleManager.scaledRoleXp(killer, role, baseAmount, false);
		RoleManager.addRoleXp(killer, role, baseAmount);
		if (party == null || earned <= 0) return;
		List<ServerPlayerEntity> members = onlineMembers(killer);
		double bonus = PartyRules.bonus(members.size(), Aptitudes.config().partyXpBonusPerMember,
				Aptitudes.config().partyXpBonusMax);
		int bonusXp = PartyRules.bonusXp(earned, bonus);
		for (ServerPlayerEntity member : members) {
			int amount = PartyRules.memberXp(earned, party.sharePercent, member == killer, bonusXp);
			RoleManager.addSharedRoleXp(member, role, amount);
		}
	}

	public static boolean invite(ServerPlayerEntity inviter, ServerPlayerEntity target) {
		if (inviter == target || inviter.getServer() == null) return false;
		MinecraftServer server = inviter.getServer();
		PartyState state = state(server);
		PartyState.Party party = findParty(server, inviter.getUuid());
		if (party == null) {
			party = new PartyState.Party(inviter.getUuid());
			party.names.put(inviter.getUuid(), inviter.getGameProfile().getName());
			state.parties.put(party.leader, party);
			state.markDirty();
		}
		if (!party.leader.equals(inviter.getUuid()) || party.members.contains(target.getUuid())
				|| findParty(server, target.getUuid()) != null || party.members.size() >= Aptitudes.config().partyMaxSize) {
			inviter.sendMessage(Text.translatable("party.error.cannot_invite").formatted(Formatting.RED), false);
			return false;
		}
		INVITES.put(target.getUuid(), new Invite(inviter.getUuid(), server.getTicks() + INVITE_TICKS));
		Text accept = Text.translatable("party.invite.accept").formatted(Formatting.GREEN).styled(style ->
				style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept")));
		Text reject = Text.translatable("party.invite.reject").formatted(Formatting.RED).styled(style ->
				style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party reject")));
		target.sendMessage(Text.translatable("party.invite.message", inviter.getDisplayName(), accept, reject)
				.formatted(Formatting.YELLOW), false);
		inviter.sendMessage(Text.translatable("party.invite.sent", target.getDisplayName()).formatted(Formatting.GREEN), false);
		return true;
	}

	public static boolean accept(ServerPlayerEntity player) {
		if (player.getServer() == null) return false;
		Invite invite = INVITES.remove(player.getUuid());
		if (invite == null || player.getServer().getTicks() >= invite.expiresAt()) {
			player.sendMessage(Text.translatable("party.error.invite_expired").formatted(Formatting.RED), false);
			return false;
		}
		PartyState state = state(player.getServer());
		PartyState.Party party = findParty(player.getServer(), invite.inviter());
		if (party == null || findParty(player.getServer(), player.getUuid()) != null
				|| party.members.size() >= Aptitudes.config().partyMaxSize) {
			player.sendMessage(Text.translatable("party.error.party_full").formatted(Formatting.RED), false);
			return false;
		}
		party.members.add(player.getUuid());
		party.names.put(player.getUuid(), player.getGameProfile().getName());
		state.markDirty();
		player.getServer().getPlayerManager().getPlayer(invite.inviter()).sendMessage(
				Text.translatable("party.joined.other", player.getDisplayName()).formatted(Formatting.GREEN), false);
		player.sendMessage(Text.translatable("party.joined.self").formatted(Formatting.GREEN), false);
		return true;
	}

	public static void reject(ServerPlayerEntity player) {
		Invite invite = INVITES.remove(player.getUuid());
		if (invite == null) {
			player.sendMessage(Text.translatable("party.error.no_invite").formatted(Formatting.RED), false);
			return;
		}
		player.sendMessage(Text.translatable("party.invite.rejected").formatted(Formatting.GRAY), false);
		if (player.getServer() != null) {
			ServerPlayerEntity inviter = player.getServer().getPlayerManager().getPlayer(invite.inviter());
			if (inviter != null) inviter.sendMessage(Text.translatable("party.invite.rejected_by", player.getDisplayName()), false);
		}
	}

	public static boolean leave(ServerPlayerEntity player) {
		PartyState state = state(player.getServer());
		PartyState.Party party = findParty(player.getServer(), player.getUuid());
		if (party == null) return false;
		if (party.leader.equals(player.getUuid())) {
			for (UUID member : party.members) {
				ServerPlayerEntity online = player.getServer().getPlayerManager().getPlayer(member);
				if (online != null && online != player) online.sendMessage(Text.translatable("party.disbanded"), false);
			}
			state.parties.remove(party.leader);
		} else {
			party.members.remove(player.getUuid());
			party.names.remove(player.getUuid());
			for (ServerPlayerEntity member : onlineMembers(player)) {
				if (member != player) member.sendMessage(Text.translatable("party.left.other", player.getDisplayName()), false);
			}
		}
		state.markDirty();
		player.sendMessage(Text.translatable("party.left.self").formatted(Formatting.YELLOW), false);
		return true;
	}

	public static boolean kick(ServerPlayerEntity leader, UUID targetId) {
		if (leader.getServer() == null) return false;
		PartyState state = state(leader.getServer());
		PartyState.Party party = findParty(leader.getServer(), leader.getUuid());
		if (party == null || !party.leader.equals(leader.getUuid()) || targetId == null
				|| targetId.equals(leader.getUuid()) || !party.members.remove(targetId)) return false;
		String name = party.names.remove(targetId);
		state.markDirty();
		ServerPlayerEntity target = leader.getServer().getPlayerManager().getPlayer(targetId);
		if (target != null) target.sendMessage(Text.translatable("party.kicked.self").formatted(Formatting.YELLOW), false);
		leader.sendMessage(Text.translatable("party.kicked.other", name == null ? targetId.toString() : name)
				.formatted(Formatting.YELLOW), false);
		return true;
	}

	public static boolean toggleFriendlyFire(ServerPlayerEntity leader) {
		PartyState.Party party = leader.getServer() == null ? null : findParty(leader.getServer(), leader.getUuid());
		if (party == null || !party.leader.equals(leader.getUuid())) return false;
		party.friendlyFire = !party.friendlyFire;
		state(leader.getServer()).markDirty();
		leader.sendMessage(Text.translatable(party.friendlyFire ? "party.pvp.enabled" : "party.pvp.disabled"), false);
		return true;
	}

	public static boolean setShare(ServerPlayerEntity leader, int percent) {
		PartyState.Party party = leader.getServer() == null ? null : findParty(leader.getServer(), leader.getUuid());
		if (party == null || !party.leader.equals(leader.getUuid())) return false;
		int configuredMax = (int) Math.floor(Aptitudes.config().partyMaxXpShare * 100.0 / 5.0) * 5;
		party.sharePercent = Math.min(configuredMax, PartyRules.sharePercent(percent));
		state(leader.getServer()).markDirty();
		leader.sendMessage(Text.translatable("party.share.changed", party.sharePercent).formatted(Formatting.GREEN), false);
		return true;
	}

	public static void sendList(ServerPlayerEntity player) {
		PartyState.Party party = player.getServer() == null ? null : findParty(player.getServer(), player.getUuid());
		if (party == null) {
			player.sendMessage(Text.translatable("party.list.none").formatted(Formatting.GRAY), false);
			return;
		}
		player.sendMessage(Text.translatable("party.list.header", party.sharePercent,
				Text.translatable(party.friendlyFire ? "party.pvp.enabled" : "party.pvp.disabled")).formatted(Formatting.GOLD), false);
		for (UUID id : party.members) {
			String name = party.names.getOrDefault(id, id.toString());
			player.sendMessage(Text.literal(id.equals(party.leader) ? "★ " + name : "  " + name)
					.formatted(player.getServer().getPlayerManager().getPlayer(id) == null ? Formatting.GRAY : Formatting.WHITE), false);
		}
	}

	public static void requestSync(ServerPlayerEntity player) {
		if (player.getServer() != null) syncIfDue(player, player.getServer().getTicks());
	}

	private static void syncIfDue(ServerPlayerEntity player, long now) {
		if (!ServerPlayNetworking.canSend(player, PartySyncPayload.ID)) return;
		Long last = LAST_SYNC.get(player.getUuid());
		if (last != null && now - last < 10) return;
		PartyState.Party party = findParty(player.getServer(), player.getUuid());
		List<Member> members = new ArrayList<>();
		if (party != null) {
			for (UUID id : party.members) {
				ServerPlayerEntity online = player.getServer().getPlayerManager().getPlayer(id);
				String name = online == null ? party.names.getOrDefault(id, "?") : online.getGameProfile().getName();
				Role role = online == null ? null : RoleManager.get(online).primary();
				float health = online == null || online.getMaxHealth() <= 0 ? 0 : online.getHealth() / online.getMaxHealth();
				members.add(new Member(id, name, role == null ? -1 : role.ordinal(), health, online != null,
						online != null && online.getWorld() == player.getWorld(), id.equals(party.leader)));
			}
		}
		List<OnlinePlayer> onlinePlayers = new ArrayList<>();
		for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
			if (other == player) continue;
			boolean grouped = findParty(player.getServer(), other.getUuid()) != null;
			onlinePlayers.add(new OnlinePlayer(other.getUuid(), other.getGameProfile().getName(), grouped));
			if (onlinePlayers.size() >= 128) break;
		}
		if (party != null) {
			boolean namesChanged = false;
			for (Member member : members) {
				if (!member.name().equals(party.names.get(member.id()))) {
					party.names.put(member.id(), member.name());
					namesChanged = true;
				}
			}
			if (namesChanged) state(player.getServer()).markDirty(); // conserva nombres actualizados para desconexiones
		}
		ServerPlayNetworking.send(player, new PartySyncPayload(party != null,
				party != null && party.leader.equals(player.getUuid()), party == null ? null : party.leader,
				party != null && party.friendlyFire, party == null ? 0 : party.sharePercent, members, onlinePlayers));
		LAST_SYNC.put(player.getUuid(), now);
	}

	private record Invite(UUID inviter, long expiresAt) {}
}
