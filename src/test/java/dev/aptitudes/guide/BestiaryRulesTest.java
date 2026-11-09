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
	@Test public void catalogHasUniqueIdsAndTheFourOwnBosses() {
		Set<String> ids = new HashSet<>();
		for (Entry e : BestiaryRules.entries()) assertTrue("duplicada: " + e.id(), ids.add(e.id()));
		for (String boss : List.of("tower_sentinel", "shrine_oracle", "camp_champion", "eclipse_herald")) {
			Entry e = BestiaryRules.byId("boss_" + boss);
			assertNotNull(boss, e);
			assertEquals(Kind.BOSS, e.kind());
			assertEquals("boss.aptitudes." + boss, BestiaryRules.nameKey(e));
		}
		assertEquals(7, BestiaryRules.total(Kind.BOSS)); // v63: + Coloso Sin Rostro
		assertEquals(2, BestiaryRules.total(Kind.ELITE));
		assertEquals(BestiaryRules.entries().size(), BestiaryRules.total(null));
		assertTrue(BestiaryRules.entries().size() <= BestiaryRules.MAX_ENTRIES);
	}

	@Test public void namesUseTheGameKeysForCreatures() {
		assertEquals("entity.minecraft.zombie", BestiaryRules.nameKey(BestiaryRules.byId("zombie")));
		assertEquals("entity.minecraft.wither_skeleton", BestiaryRules.nameKey(BestiaryRules.byId("wither_skeleton")));
		assertEquals("bestiary.aptitudes.elite.name", BestiaryRules.nameKey(BestiaryRules.byId("elite")));
		assertEquals("bestiary.aptitudes.zombie.tip", BestiaryRules.byId("zombie").tipKey());
		assertEquals("minecraft:zombie_spawn_egg", BestiaryRules.byId("zombie").icon());
	}

	@Test public void ordinaryKillsUnlockOnlyTheirCreature() {
		assertEquals(List.of("zombie"), BestiaryRules.unlockedBy("minecraft:zombie", null, false, false));
		assertEquals(List.of(), BestiaryRules.unlockedBy("minecraft:cow", null, false, false));
		assertEquals(List.of(), BestiaryRules.unlockedBy(null, null, false, false));
	}

	@Test public void elitesAndChampionsAddTheirPages() {
		assertEquals(List.of("spider", "elite"), BestiaryRules.unlockedBy("minecraft:spider", null, true, false));
		assertEquals(List.of("spider", "elite", "champion"), BestiaryRules.unlockedBy("minecraft:spider", null, true, true));
		// Una criatura fuera del catálogo también enseña la página de élite.
		assertEquals(List.of("elite"), BestiaryRules.unlockedBy("minecraft:polar_bear", null, true, false));
	}

	@Test public void ownBossesUnlockTheirPageAndTheBaseCreatureButNotTheEliteOnes() {
		assertEquals(List.of("boss_tower_sentinel", "wither_skeleton"),
				BestiaryRules.unlockedBy("minecraft:wither_skeleton", "tower_sentinel", true, true));
		assertEquals(List.of("boss_shrine_oracle", "evoker"),
				BestiaryRules.unlockedBy("minecraft:evoker", "shrine_oracle", true, true));
		// Un jefe desconocido (datos de otra versión) no rompe nada.
		assertEquals(List.of("zombie"), BestiaryRules.unlockedBy("minecraft:zombie", "future_boss", true, true));
	}

	@Test public void animatedBossesUnlockByEntityType() {
		assertEquals(List.of("faceless_colossus"), BestiaryRules.unlockedBy("aptitudes:faceless_colossus", null, false, false));
		assertEquals("entity.aptitudes.faceless_colossus", BestiaryRules.nameKey(BestiaryRules.byId("faceless_colossus")));
	}

	@Test public void vanillaBossesAreCreaturePages() {
		assertEquals(List.of("wither"), BestiaryRules.unlockedBy("minecraft:wither", null, false, false));
		assertEquals(Kind.BOSS, BestiaryRules.byId("ender_dragon").kind());
	}

	@Test public void countsSaturate() {
		assertEquals(1, BestiaryRules.increment(0));
		assertEquals(1, BestiaryRules.increment(-5));
		assertEquals(BestiaryRules.MAX_COUNT, BestiaryRules.increment(BestiaryRules.MAX_COUNT));
		assertEquals(BestiaryRules.MAX_COUNT, BestiaryRules.increment(Integer.MAX_VALUE));
	}

	@Test public void sanitizeDropsUnknownAndInvalidCounts() {
		Map<String, Integer> raw = new HashMap<>();
		raw.put("zombie", 3);
		raw.put("unknown_mob", 9);
		raw.put("creeper", 0);
		raw.put("spider", -2);
		raw.put("ghast", null);
		raw.put("blaze", Integer.MAX_VALUE);
		Map<String, Integer> clean = BestiaryRules.sanitize(raw);
		assertEquals(Map.of("zombie", 3, "blaze", BestiaryRules.MAX_COUNT), clean);
		assertTrue(BestiaryRules.sanitize(null).isEmpty());
		assertFalse(BestiaryRules.known("unknown_mob"));
		assertNull(BestiaryRules.byId(null));
	}

	@Test public void unlockedCountsByKind() {
		Map<String, Integer> kills = Map.of("zombie", 2, "boss_tower_sentinel", 1, "wither", 1, "elite", 4);
		assertEquals(4, BestiaryRules.unlocked(kills, null));
		assertEquals(2, BestiaryRules.unlocked(kills, Kind.BOSS));
		assertEquals(1, BestiaryRules.unlocked(kills, Kind.CREATURE));
		assertEquals(1, BestiaryRules.unlocked(kills, Kind.ELITE));
	}
}
