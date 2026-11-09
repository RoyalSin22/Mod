package dev.aptitudes.guide;

import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.PathRules.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GuideRulesTest {
	@Test public void normalizeIgnoresCaseAccentsAndSpaces() {
		assertEquals("eter", GuideRules.normalize("Éter"));
		assertEquals("hoguera de almas", GuideRules.normalize("  Hoguera   de ALMAS "));
		assertEquals("fogo", GuideRules.normalize("Fogó"));
		assertEquals("strasse", GuideRules.normalize("Straße"));
		assertEquals("", GuideRules.normalize(null));
	}

	@Test public void termsAreSplitAndCapped() {
		assertEquals(List.of("frasco", "estus"), GuideRules.terms("Frasco  Estus"));
		assertTrue(GuideRules.terms("   ").isEmpty());
		String longQuery = "a".repeat(100);
		assertEquals(GuideRules.MAX_QUERY, GuideRules.terms(longQuery).get(0).length());
	}

	@Test public void scoreNeedsEveryTermAndPrefersTitles() {
		List<String> q = GuideRules.terms("fras estus");
		assertEquals(0, GuideRules.score(q, "frasco de eter", "cura mana"));
		int title = GuideRules.score(q, "frasco de estus", "");
		int body = GuideRules.score(q, "hoguera", "mejora el frasco de estus");
		assertTrue(title > body);
		assertTrue(body > 0);
		assertEquals(0, GuideRules.score(List.of(), "x", "y"));
		// Empezar por la consulta suma.
		assertTrue(GuideRules.score(List.of("zom"), "zombi", "") > GuideRules.score(List.of("zom"), "aldeano zombi", ""));
	}

	@Test public void bookmarksAreValidatedDedupedAndCapped() {
		assertTrue(GuideRules.validBookmark("chapter:bonfire"));
		assertTrue(GuideRules.validBookmark("item:aptitudes:estus_flask"));
		assertTrue(GuideRules.validBookmark("beast:boss_tower_sentinel"));
		assertTrue(GuideRules.validBookmark("role:berserker"));
		assertFalse(GuideRules.validBookmark("chapter:"));
		assertFalse(GuideRules.validBookmark("evil:x"));
		assertFalse(GuideRules.validBookmark("chapter:Bonfire"));
		assertFalse(GuideRules.validBookmark(null));
		List<String> raw = new ArrayList<>(List.of("chapter:a", "chapter:a", "bad", "beast:zombie"));
		assertEquals(List.of("chapter:a", "beast:zombie"), GuideRules.sanitizeBookmarks(raw));
		List<String> many = new ArrayList<>();
		for (int i = 0; i < 40; i++) many.add("chapter:c" + i);
		assertEquals(GuideRules.MAX_BOOKMARKS, GuideRules.sanitizeBookmarks(many).size());
	}

	@Test public void toggleAddsRemovesAndRespectsTheCap() {
		List<String> one = GuideRules.toggle(List.of(), "chapter:bonfire");
		assertEquals(List.of("chapter:bonfire"), one);
		assertEquals(List.of(), GuideRules.toggle(one, "chapter:bonfire"));
		assertEquals(one, GuideRules.toggle(one, "not valid"));
		List<String> full = new ArrayList<>();
		for (int i = 0; i < GuideRules.MAX_BOOKMARKS; i++) full.add("chapter:c" + i);
		assertEquals(full, GuideRules.toggle(full, "chapter:extra"));
		assertEquals(GuideRules.MAX_BOOKMARKS - 1, GuideRules.toggle(full, "chapter:c3").size());
	}

	@Test public void percentIsBoundedAndSafe() {
		assertEquals(0, GuideRules.percent(3, 0));
		assertEquals(0, GuideRules.percent(-1, 10));
		assertEquals(33, GuideRules.percent(1, 3));
		assertEquals(100, GuideRules.percent(5, 5));
		assertEquals(100, GuideRules.percent(9, 5));
		assertEquals(100, GuideRules.percent(Long.MAX_VALUE / 200, Long.MAX_VALUE / 200));
	}

	@Test public void pathProgressCountsOneBranchAndKeystones() {
		Map<String, Integer> ranks = new HashMap<>();
		for (Step s : Step.values()) ranks.put(PathRules.nodeId("berserker", 2, s), 1); // ambas ramas (mundo antiguo)
		ranks.put(PathRules.nodeId("mage", 1, Step.S1), 3);
		ranks.put(PathRules.nodeId("mage", 1, Step.S2), 1);
		GuideRules.PathProgress p = GuideRules.pathProgress(id -> ranks.getOrDefault(id, 0), List.of("berserker", "mage", "druid"));
		assertEquals(GuideRules.STEPS_PER_PATH + 2, p.steps());
		assertEquals(3 * GuideRules.STEPS_PER_PATH, p.maxSteps());
		assertEquals(1, p.keystones());
		assertEquals(3, p.maxKeystones());
		assertEquals(7, GuideRules.STEPS_PER_PATH);
	}
}
