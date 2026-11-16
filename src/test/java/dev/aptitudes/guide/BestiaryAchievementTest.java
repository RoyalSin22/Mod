package dev.aptitudes.guide;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** Regla del logro «Cronista»: haber descubierto todas las criaturas del bestiario (al menos una muerte de cada una). */
public class BestiaryAchievementTest {
	private static Map<String, Integer> all(int kills) {
		Map<String, Integer> map = new HashMap<>();
		for (BestiaryRules.Entry e : BestiaryRules.entries()) map.put(e.id(), kills);
		return map;
	}

	@Test public void emptyBestiaryIsNotComplete() {
		assertFalse(BestiaryRules.complete(Map.of()));
	}

	@Test public void oneKillOfEveryEntryCompletesIt() {
		assertTrue(BestiaryRules.complete(all(1)));
	}

	@Test public void missingOrZeroEntryKeepsItIncomplete() {
		Map<String, Integer> map = all(5);
		String first = BestiaryRules.entries().get(0).id();
		map.put(first, 0);
		assertFalse("una entrada en 0 no cuenta", BestiaryRules.complete(map));
		map.remove(first);
		assertFalse("una entrada ausente no cuenta", BestiaryRules.complete(map));
	}

	@Test public void unknownIdsDoNotCountTowardsCompletion() {
		Map<String, Integer> map = all(1);
		map.remove(BestiaryRules.entries().get(0).id());
		map.put("minecraft:zombie", 50);
		assertFalse(BestiaryRules.complete(map));
	}

	@Test public void nullIsHandled() {
		assertFalse(BestiaryRules.complete(null));
	}
}
