package dev.aptitudes.guide;

import dev.aptitudes.guide.BestiaryRules.Entry;
import dev.aptitudes.guide.BestiaryRules.Kind;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BestiaryRulesTest {
	@Test public void catalogHasUniqueIdsAndOnlyModEntries() {
		Set<String> ids = new HashSet<>();
		for (Entry e : BestiaryRules.entries()) {
			assertTrue("duplicada: " + e.id(), ids.add(e.id()));
			if (e.entity() != null && e.bossKind() == null) {
				assertTrue("criatura de Minecraft en el bestiario: " + e.id(), e.entity().startsWith("aptitudes:"));
			}
		}
		for (String boss : List.of("tower_sentinel", "shrine_oracle", "camp_champion", "eclipse_herald")) {
			Entry e = BestiaryRules.byId("boss_" + boss);
			assertNotNull(boss, e);
			assertEquals(Kind.BOSS, e.kind());
			assertEquals("boss.aptitudes." + boss, BestiaryRules.nameKey(e));
		}
		assertEquals(7, BestiaryRules.total(Kind.BOSS)); // 4 propios + Coloso, Guardián y Autómata
		assertEquals(2, BestiaryRules.total(Kind.ELITE));
		assertEquals(4, BestiaryRules.total(Kind.CREATURE)); // Osario, Errante, Escorificador y Custodio
		assertEquals(13, BestiaryRules.total(null));
		assertEquals(BestiaryRules.entries().size(), BestiaryRules.total(null));
		assertTrue(BestiaryRules.entries().size() <= BestiaryRules.MAX_ENTRIES);
	}

	@Test public void minecraftCreaturesAreNoLongerInTheCatalog() {
		for (String id : List.of("zombie", "skeleton", "creeper", "wither", "ender_dragon", "warden")) assertFalse(id, BestiaryRules.known(id));
	}

	@Test public void namesAndKeys() {
		assertEquals("entity.aptitudes.osario_quebrado", BestiaryRules.nameKey(BestiaryRules.byId("osario_quebrado")));
		assertEquals("entity.aptitudes.custodio_cuatro_llaves", BestiaryRules.nameKey(BestiaryRules.byId("custodio")));
		assertEquals("bestiary.aptitudes.elite.name", BestiaryRules.nameKey(BestiaryRules.byId("elite")));
		assertEquals("bestiary.aptitudes.osario_quebrado.tip", BestiaryRules.byId("osario_quebrado").tipKey());
		assertEquals("bestiary.aptitudes.osario_quebrado.lore", BestiaryRules.byId("osario_quebrado").loreKey());
	}

	@Test public void ordinaryKillsUnlockOnlyTheirCreature() {
		assertEquals(List.of("osario_quebrado"), BestiaryRules.unlockedBy("aptitudes:osario_quebrado", null, false, false));
		assertEquals(List.of("custodio"), BestiaryRules.unlockedBy("aptitudes:custodio_cuatro_llaves", null, false, false));
		assertEquals(List.of(), BestiaryRules.unlockedBy("minecraft:cow", null, false, false));
		assertEquals(List.of(), BestiaryRules.unlockedBy("minecraft:zombie", null, false, false));
		assertEquals(List.of(), BestiaryRules.unlockedBy(null, null, false, false));
	}

	@Test public void elitesAndChampionsAddTheirPages() {
		assertEquals(List.of("osario_quebrado", "elite"), BestiaryRules.unlockedBy("aptitudes:osario_quebrado", null, true, false));
		assertEquals(List.of("osario_quebrado", "elite", "champion"), BestiaryRules.unlockedBy("aptitudes:osario_quebrado", null, true, true));
		// Una criatura de Minecraft también enseña la página de élite.
		assertEquals(List.of("elite"), BestiaryRules.unlockedBy("minecraft:zombie", null, true, false));
	}

	@Test public void ownBossesUnlockOnlyTheirPage() {
		assertEquals(List.of("boss_tower_sentinel"), BestiaryRules.unlockedBy("minecraft:wither_skeleton", "tower_sentinel", true, true));
		assertEquals(List.of("boss_shrine_oracle"), BestiaryRules.unlockedBy("minecraft:evoker", "shrine_oracle", true, true));
		// Un jefe desconocido (datos de otra versión) no rompe nada.
		assertEquals(List.of(), BestiaryRules.unlockedBy("minecraft:zombie", "future_boss", false, false));
	}

	@Test public void animatedBossesUnlockByEntityType() {
		assertEquals(List.of("faceless_colossus"), BestiaryRules.unlockedBy("aptitudes:faceless_colossus", null, false, false));
		assertEquals("entity.aptitudes.faceless_colossus", BestiaryRules.nameKey(BestiaryRules.byId("faceless_colossus")));
		assertEquals(List.of("dismantled_guardian"), BestiaryRules.unlockedBy("aptitudes:dismantled_guardian", null, false, false));
		assertEquals(List.of("ancient_automaton"), BestiaryRules.unlockedBy("aptitudes:ancient_automaton", null, false, false));
	}

	@Test public void countsSaturate() {
		assertEquals(1, BestiaryRules.increment(0));
		assertEquals(1, BestiaryRules.increment(-5));
		assertEquals(BestiaryRules.MAX_COUNT, BestiaryRules.increment(BestiaryRules.MAX_COUNT));
		assertEquals(BestiaryRules.MAX_COUNT, BestiaryRules.increment(Integer.MAX_VALUE));
	}

	@Test public void sanitizeDropsUnknownAndInvalidCounts() {
		Map<String, Integer> raw = new HashMap<>();
		raw.put("osario_quebrado", 3);
		raw.put("unknown_mob", 9);
		raw.put("zombie", 7); // criatura de Minecraft retirada: se descarta
		raw.put("errante_velado", 0);
		raw.put("escorificador_hierro", -2);
		raw.put("custodio", null);
		raw.put("elite", Integer.MAX_VALUE);
		Map<String, Integer> clean = BestiaryRules.sanitize(raw);
		assertEquals(Map.of("osario_quebrado", 3, "elite", BestiaryRules.MAX_COUNT), clean);
		assertTrue(BestiaryRules.sanitize(null).isEmpty());
		assertFalse(BestiaryRules.known("unknown_mob"));
		assertNull(BestiaryRules.byId(null));
	}

	@Test public void unlockedCountsByKind() {
		Map<String, Integer> kills = Map.of("osario_quebrado", 2, "boss_tower_sentinel", 1, "faceless_colossus", 1, "elite", 4);
		assertEquals(4, BestiaryRules.unlocked(kills, null));
		assertEquals(2, BestiaryRules.unlocked(kills, Kind.BOSS));
		assertEquals(1, BestiaryRules.unlocked(kills, Kind.CREATURE));
		assertEquals(1, BestiaryRules.unlocked(kills, Kind.ELITE));
	}
}
