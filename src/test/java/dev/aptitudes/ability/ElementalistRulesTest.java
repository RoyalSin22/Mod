package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F5): cifras del Chamán. */
class ElementalistRulesTest {
	@Test
	void passiveValues() {
		assertEquals(0.10, ElementalistRules.tideBonus(1), 1e-9);
		assertEquals(0.20, ElementalistRules.tideBonus(3), 1e-9);
		assertEquals(0.08, ElementalistRules.echoChance(1), 1e-9);
		assertEquals(0.16, ElementalistRules.echoChance(3), 1e-9);
		assertEquals(0.25, ElementalistRules.chainRefund(1), 1e-9);
		assertEquals(0.40, ElementalistRules.chainRefund(2), 1e-9);
		assertEquals(0.10, ElementalistRules.harmonyBonus(1), 1e-9);
		assertEquals(0.20, ElementalistRules.harmonyBonus(2), 1e-9);
		assertEquals(0, ElementalistRules.tideBonus(0), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(6, ElementalistRules.lanceDamage(1, 1), 1e-9);
		assertEquals(10, ElementalistRules.lanceDamage(3, 1), 1e-9);
		assertEquals(3, ElementalistRules.wallDamage(1, 1), 1e-9);
		assertEquals(5, ElementalistRules.wallDamage(3, 1), 1e-9);
		assertEquals(4, ElementalistRules.totemDamage(1, 1), 1e-9);
		assertEquals(5, ElementalistRules.totemDamage(2, 1), 1e-9);
		assertEquals(160, ElementalistRules.formTicks(1));
		assertEquals(200, ElementalistRules.formTicks(2));
		assertEquals(25, ElementalistRules.collapseDamage(1), 1e-9);
		assertEquals(12.5, ElementalistRules.collapseDamage(0.5), 1e-9);
	}

	@Test
	void harmonyNeedsThreeDistinctElements() {
		assertFalse(ElementalistRules.harmonyReached(2));
		assertTrue(ElementalistRules.harmonyReached(3));
	}
}
