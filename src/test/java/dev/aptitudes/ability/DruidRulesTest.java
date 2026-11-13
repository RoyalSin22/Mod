package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F5): cifras del Druida. */
class DruidRulesTest {
	@Test
	void passiveValuesScaleWithRank() {
		assertEquals(0.04, DruidRules.grassReduction(1), 1e-9);
		assertEquals(0.12, DruidRules.grassReduction(3), 1e-9);
		assertEquals(0, DruidRules.grassReduction(0), 1e-9);
		assertEquals(1, DruidRules.clawDamage(1, 1), 1e-9);
		assertEquals(1.5, DruidRules.clawDamage(3, 0.5), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(4, DruidRules.whipDamage(1, 1), 1e-9);
		assertEquals(6, DruidRules.whipDamage(3, 1), 1e-9);
		assertEquals(0.25, DruidRules.regrowFraction(1), 1e-9);
		assertEquals(0.45, DruidRules.regrowFraction(3), 1e-9);
		assertEquals(4, DruidRules.roarDamage(1, 1), 1e-9);
		assertEquals(5, DruidRules.roarDamage(2, 1), 1e-9);
		assertEquals(160, DruidRules.direTicks(1));
		assertEquals(200, DruidRules.direTicks(2));
		assertEquals(4, DruidRules.awakenDamage(1), 1e-9);
	}

	@Test
	void lethargyOnlyTriggersBelowThresholdWithoutDying() {
		assertFalse(DruidRules.lethargyTriggers(20, 8, 20, true), "queda 12/20 = 60 %: no baja del umbral");
		assertTrue(DruidRules.lethargyTriggers(10, 5, 20, true), "queda 5/20 = 25 %");
		assertFalse(DruidRules.lethargyTriggers(10, 10, 20, true), "mortal: no actúa");
		assertFalse(DruidRules.lethargyTriggers(10, 5, 20, false), "en espera");
		assertEquals(0.2, DruidRules.lethargyHeal(1), 1e-9);
		assertEquals(0.3, DruidRules.lethargyHeal(2), 1e-9);
	}

	@Test
	void seasonNeedsTwentySecondsOfCalm() {
		assertEquals(3, DruidRules.seasonHeal(1), 1e-9);
		assertEquals(5, DruidRules.seasonHeal(2), 1e-9);
		assertFalse(DruidRules.seasonReady(1000, 700, -100_000));
		assertTrue(DruidRules.seasonReady(1000, 600, -100_000));
		assertFalse(DruidRules.seasonReady(1000, -100_000, 700), "tras el efecto hay que esperar otros 20 s");
	}
}
