package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F7): cifras del Bardo. */
class BardRulesTest {
	@Test
	void passiveValues() {
		assertEquals(0.03, BardRules.harmonyReduction(1), 1e-9);
		assertEquals(0.09, BardRules.harmonyReduction(3), 1e-9);
		assertEquals(0, BardRules.harmonyReduction(0), 1e-9);
		assertEquals(0.9, BardRules.refrainCost(1), 1e-9);
		assertEquals(0.7, BardRules.refrainCost(3), 1e-9);
		assertEquals(1.0, BardRules.refrainCost(0), 1e-9);
		assertEquals(0.10, BardRules.counterBonus(1), 1e-9);
		assertEquals(0.20, BardRules.counterBonus(2), 1e-9);
	}

	@Test
	void choirCapsAtThreeAllies() {
		assertEquals(1, BardRules.choirMana(1, 1), 1e-9);
		assertEquals(6, BardRules.choirMana(2, 3), 1e-9);
		assertEquals(6, BardRules.choirMana(2, 9), 1e-9, "máximo 3 aliados");
		assertEquals(0, BardRules.choirMana(2, 0), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(5, BardRules.noteDamage(1, 1), 1e-9);
		assertEquals(9, BardRules.noteDamage(3, 1), 1e-9);
		assertEquals(60, BardRules.lullTicks(1));
		assertEquals(100, BardRules.lullTicks(3));
		assertEquals(3, BardRules.rumbleDamage(1, 1), 1e-9);
		assertEquals(4, BardRules.rumbleDamage(2, 1), 1e-9);
		assertEquals(160, BardRules.marchTicks(1));
		assertEquals(200, BardRules.marchTicks(2));
		assertEquals(4, BardRules.finaleDamage(1), 1e-9);
	}

	@Test
	void counterpointNeedsThreeDistinctSongs() {
		assertFalse(BardRules.counterReached(2));
		assertTrue(BardRules.counterReached(3));
	}
}
