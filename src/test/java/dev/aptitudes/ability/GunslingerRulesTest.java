package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F6): cifras del Mago pistolero. */
class GunslingerRulesTest {
	@Test
	void steadyOnlyAtLongRange() {
		assertEquals(0.05, GunslingerRules.steadyBonus(1, 15), 1e-9);
		assertEquals(0.15, GunslingerRules.steadyBonus(3, 40), 1e-9);
		assertEquals(0, GunslingerRules.steadyBonus(3, 14.9), 1e-9);
	}

	@Test
	void valuesByRank() {
		assertEquals(2, GunslingerRules.coolMana(1), 1e-9);
		assertEquals(4, GunslingerRules.coolMana(3), 1e-9);
		assertEquals(6, GunslingerRules.stunDamage(1, 1), 1e-9);
		assertEquals(10, GunslingerRules.stunDamage(3, 1), 1e-9);
		assertEquals(8, GunslingerRules.grenadeDamage(1, 1), 1e-9);
		assertEquals(12, GunslingerRules.grenadeDamage(3, 1), 1e-9);
		assertEquals(0.10, GunslingerRules.pressureChance(1), 1e-9);
		assertEquals(0.20, GunslingerRules.pressureChance(2), 1e-9);
		assertEquals(12, GunslingerRules.throughDamage(1, 1), 1e-9);
		assertEquals(16, GunslingerRules.throughDamage(2, 1), 1e-9);
		assertEquals(160, GunslingerRules.doubleTicks(1));
		assertEquals(200, GunslingerRules.doubleTicks(2));
		assertEquals(0.4, GunslingerRules.pulseBonus(1), 1e-9);
		assertEquals(0.6, GunslingerRules.pulseBonus(2), 1e-9);
	}

	@Test
	void pulseNeedsThreeSecondsOfSilence() {
		assertFalse(GunslingerRules.pulseReady(1050, 1000));
		assertTrue(GunslingerRules.pulseReady(1060, 1000));
	}

	@Test
	void executionSkipsBossesAndHealthyTargets() {
		assertTrue(GunslingerRules.executes(24, 100, false));
		assertFalse(GunslingerRules.executes(25, 100, false), "justo el 25 % no basta");
		assertFalse(GunslingerRules.executes(10, 100, true), "jefes: no");
		assertFalse(GunslingerRules.executes(10, 0, false));
		assertEquals(25, GunslingerRules.executeDamage(1), 1e-9);
		assertEquals(12.5, GunslingerRules.executeDamage(0.5), 1e-9);
	}
}
