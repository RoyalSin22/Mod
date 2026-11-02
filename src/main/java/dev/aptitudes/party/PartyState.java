package dev.aptitudes.party;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Grupos persistentes en el estado del Mundo Normal. */
public final class PartyState extends PersistentState {
	private static final String NAME = "aptitudes_parties";
	private static final Type<PartyState> TYPE = new Type<>(PartyState::new, PartyState::fromNbt, null);
	final Map<UUID, Party> parties = new LinkedHashMap<>();

	public static PartyState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Party party : parties.values()) {
			NbtCompound item = new NbtCompound();
			item.putUuid("leader", party.leader);
			item.putBoolean("friendly_fire", party.friendlyFire);
			item.putInt("share_percent", party.sharePercent);
			NbtList members = new NbtList();
			for (UUID member : party.members) {
				NbtCompound entry = new NbtCompound();
				entry.putUuid("uuid", member);
				entry.putString("name", party.names.getOrDefault(member, ""));
				members.add(entry);
			}
			item.put("members", members);
			list.add(item);
		}
		nbt.put("parties", list);
		return nbt;
	}

	private static PartyState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		PartyState state = new PartyState();
		Set<UUID> claimed = new HashSet<>();
		NbtList list = nbt.getList("parties", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < Math.min(list.size(), 10_000); i++) {
			NbtCompound item = list.getCompound(i);
			if (!item.containsUuid("leader")) continue;
			UUID leader = item.getUuid("leader");
			if (claimed.contains(leader)) continue;
			Party party = new Party(leader);
			party.friendlyFire = item.getBoolean("friendly_fire");
			party.sharePercent = PartyRules.sharePercent(item.getInt("share_percent"));
			NbtList members = item.getList("members", NbtElement.COMPOUND_TYPE);
			for (int j = 0; j < Math.min(members.size(), 5); j++) {
				NbtCompound entry = members.getCompound(j);
				if (!entry.containsUuid("uuid")) continue;
				UUID member = entry.getUuid("uuid");
				if (party.members.contains(member) || claimed.contains(member)) continue;
				party.members.add(member);
				party.names.put(member, entry.getString("name"));
			}
			if (!party.members.contains(leader)) {
				if (party.members.size() >= 5) party.members.remove(party.members.size() - 1);
				party.members.add(0, leader);
			}
			while (party.members.size() > 5) party.members.remove(party.members.size() - 1);
			if (!party.members.isEmpty()) {
				state.parties.put(leader, party);
				claimed.addAll(party.members);
			}
		}
		return state;
	}

	static final class Party {
		final UUID leader;
		final List<UUID> members = new ArrayList<>();
		final Map<UUID, String> names = new LinkedHashMap<>();
		boolean friendlyFire;
		int sharePercent;

		Party(UUID leader) {
			this.leader = leader;
			members.add(leader);
		}
	}
}
