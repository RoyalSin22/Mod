package dev.aptitudes.party;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Guardado NBT de grupos sin servidor ni inicialización de registros de Minecraft. */
class PartyStateNbtTest {
	@Test
	void roundTripPreservesSeveralGroupsLeadersOptionsAndAllNames() throws Exception {
		PartyState original = new PartyState();
		PartyState.Party first = new PartyState.Party(id(1));
		first.members.addAll(List.of(id(2), id(3)));
		first.names.put(id(1), "Líder_Águila");
		first.names.put(id(2), "Minero");
		first.names.put(id(3), "Arquera");
		first.friendlyFire = true;
		first.sharePercent = 35;
		PartyState.Party second = new PartyState.Party(id(4));
		second.members.add(id(5));
		second.names.put(id(4), "Guía");
		second.names.put(id(5), "Bardo");
		second.sharePercent = 50;
		original.parties.put(first.leader, first);
		original.parties.put(second.leader, second);

		NbtCompound saved = original.writeNbt(new NbtCompound(), null);
		PartyState loaded = load(saved.copy());
		assertEquals(List.of(id(1), id(4)), List.copyOf(loaded.parties.keySet()));
		for (PartyState.Party expected : original.parties.values()) {
			PartyState.Party actual = loaded.parties.get(expected.leader);
			assertEquals(expected.leader, actual.leader);
			assertEquals(expected.members, actual.members);
			assertEquals(expected.names, actual.names);
			assertEquals(expected.friendlyFire, actual.friendlyFire);
			assertEquals(expected.sharePercent, actual.sharePercent);
		}
		assertEquals(saved, loaded.writeNbt(new NbtCompound(), null));
	}

	@Test
	void duplicateLeaderEntriesKeepFirstNameAndSingleMembership() throws Exception {
		NbtCompound group = group(id(1));
		NbtList members = new NbtList();
		members.add(member(id(1), "Líder"));
		members.add(member(id(1), "Duplicado"));
		members.add(member(id(2), "Compañero"));
		members.add(member(id(2), "Otro duplicado"));
		group.put("members", members);
		PartyState.Party loaded = load(rootWith(group)).parties.get(id(1));
		assertEquals(List.of(id(1), id(2)), loaded.members);
		assertEquals("Líder", loaded.names.get(id(1)));
		assertEquals("Compañero", loaded.names.get(id(2)));
	}

	@Test
	void emptyOrWrongTypeRootLoadsNoGroups() throws Exception {
		assertTrue(load(new NbtCompound()).parties.isEmpty());
		NbtCompound root = new NbtCompound();
		root.putString("parties", "lista ausente");
		assertTrue(load(root).parties.isEmpty());
		assertEquals(rootWith(), load(new NbtCompound()).writeNbt(new NbtCompound(), null));
	}

	@Test
	void missingLeaderIsSkippedAndMissingOptionsAndMembersUseDefaults() throws Exception {
		NbtCompound leaderOnly = group(id(1));
		PartyState loaded = load(rootWith(new NbtCompound(), leaderOnly));
		assertEquals(1, loaded.parties.size());
		PartyState.Party party = loaded.parties.get(id(1));
		assertEquals(List.of(id(1)), party.members);
		assertTrue(party.names.isEmpty());
		assertFalse(party.friendlyFire);
		assertEquals(0, party.sharePercent);
	}

	@Test
	void missingMemberUuidIsSkippedAndMissingNameIsEmpty() throws Exception {
		NbtCompound group = group(id(1));
		NbtList members = new NbtList();
		members.add(new NbtCompound());
		NbtCompound noUuid = new NbtCompound();
		noUuid.putString("uuid", "no es un UUID");
		members.add(noUuid);
		NbtCompound member = new NbtCompound();
		member.putUuid("uuid", id(2));
		members.add(member);
		group.put("members", members);
		PartyState.Party loaded = load(rootWith(group)).parties.get(id(1));
		assertEquals(List.of(id(1), id(2)), loaded.members);
		assertEquals("", loaded.names.get(id(2)));
	}

	@Test
	void sharePercentIsClampedAndRoundedOnLoad() throws Exception {
		NbtCompound low = group(id(1));
		low.putInt("share_percent", Integer.MIN_VALUE);
		NbtCompound high = group(id(2));
		high.putInt("share_percent", Integer.MAX_VALUE);
		NbtCompound rounded = group(id(3));
		rounded.putInt("share_percent", 34);
		PartyState loaded = load(rootWith(low, high, rounded));
		assertEquals(0, loaded.parties.get(id(1)).sharePercent);
		assertEquals(50, loaded.parties.get(id(2)).sharePercent);
		assertEquals(30, loaded.parties.get(id(3)).sharePercent);
	}

	@Test
	void duplicateMembershipAndOverlongListsCannotDuplicatePlayersOrDropLeader() throws Exception {
		NbtCompound first = group(id(1));
		NbtList firstMembers = new NbtList();
		for (int i = 2; i <= 9; i++) firstMembers.add(member(id(i), "Miembro" + i));
		first.put("members", firstMembers);
		NbtCompound second = group(id(10));
		NbtList secondMembers = new NbtList();
		secondMembers.add(member(id(2), "Repetido"));
		secondMembers.add(member(id(11), "Nuevo"));
		secondMembers.add(member(id(11), "Duplicado"));
		second.put("members", secondMembers);
		PartyState loaded = load(rootWith(first, group(id(1)), group(id(2)), second));
		assertEquals(2, loaded.parties.size());
		assertEquals(List.of(id(1), id(2), id(3), id(4), id(5)), loaded.parties.get(id(1)).members);
		assertEquals(List.of(id(10), id(11)), loaded.parties.get(id(10)).members);
		HashSet<UUID> seen = new HashSet<>();
		for (PartyState.Party party : loaded.parties.values()) {
			assertTrue(party.members.size() <= 5);
			assertTrue(party.members.contains(party.leader));
			for (UUID member : party.members) assertTrue(seen.add(member));
		}
	}

	private static UUID id(long value) {
		return new UUID(0, value);
	}

	private static NbtCompound group(UUID leader) {
		NbtCompound group = new NbtCompound();
		group.putUuid("leader", leader);
		return group;
	}

	private static NbtCompound member(UUID uuid, String name) {
		NbtCompound member = new NbtCompound();
		member.putUuid("uuid", uuid);
		member.putString("name", name);
		return member;
	}

	private static NbtCompound rootWith(NbtCompound... groups) {
		NbtList list = new NbtList();
		for (NbtCompound group : groups) list.add(group);
		NbtCompound root = new NbtCompound();
		root.put("parties", list);
		return root;
	}

	private static PartyState load(NbtCompound nbt) throws Exception {
		// La lectura es privada; se ejercita sin añadir una API de producción solo para las pruebas.
		Method method = PartyState.class.getDeclaredMethod("fromNbt", NbtCompound.class, RegistryWrapper.WrapperLookup.class);
		method.setAccessible(true);
		// El lector no usa registros: null evita arrancar Minecraft para leer UUID, nombres y opciones.
		return (PartyState) method.invoke(null, nbt, null);
	}
}
