package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F7): cifras del Domador. */
class TamerRulesTest {
	@Test
	void passiveValues() {
		assertEquals(2f, TamerRules.furAbsorption(1), 1e-6);
		assertEquals(6f, TamerRules.furAbsorption(3), 1e-6);
		assertEquals(6f, TamerRules.furAbsorption(9), 1e-6, "tope de rango 3");
		assertEquals(1f, TamerRules.careHeal(1), 1e-6);
		assertEquals(2f, TamerRules.careHeal(2), 1e-6);
		assertEquals(0.1f, TamerRules.bloodShare(1), 1e-6);
		assertEquals(0.2f, TamerRules.bloodShare(2), 1e-6);
	}

	@Test
	void careOnlyBelowHalfHealth() {
		assertTrue(TamerRules.wounded(9, 20));
		assertFalse(TamerRules.wounded(10, 20), "justo la mitad no cuenta");
		assertFalse(TamerRules.wounded(5, 0));
	}

	@Test
	void activeValues() {
		assertEquals(60, TamerRules.gatherTicks(1));
		assertEquals(100, TamerRules.gatherTicks(3));
		assertEquals(4, TamerRules.strikeDamage(1, 1), 1e-9);
		assertEquals(6, TamerRules.strikeDamage(3, 1), 1e-9);
		assertEquals(4f, TamerRules.mantleAbsorption(1), 1e-6);
		assertEquals(6f, TamerRules.mantleAbsorption(2), 1e-6);
		assertEquals(160, TamerRules.rageTicks(1));
		assertEquals(200, TamerRules.rageTicks(2));
	}
}
