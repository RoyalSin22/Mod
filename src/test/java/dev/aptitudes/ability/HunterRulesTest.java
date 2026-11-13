package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** v67 (F6): cifras del Cazador. */
class HunterRulesTest {
	@Test
	void steadyOnlyAtLongRange() {
		assertEquals(0.06, HunterRules.steadyBonus(1, 12), 1e-9);
		assertEquals(0.18, HunterRules.steadyBonus(3, 30), 1e-9);
		assertEquals(0, HunterRules.steadyBonus(3, 11.9), 1e-9);
	}

	@Test
	void woundedBelowHalfHealth() {
		assertEquals(0.04, HunterRules.woundedBonus(1, 9, 20), 1e-9);
		assertEquals(0.12, HunterRules.woundedBonus(3, 1, 20), 1e-9);
		assertEquals(0, HunterRules.woundedBonus(3, 10, 20), 1e-9, "justo la mitad no cuenta");
		assertEquals(0, HunterRules.woundedBonus(3, 5, 0), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(40, HunterRules.netTicks(1));
		assertEquals(80, HunterRules.netTicks(3));
		assertEquals(6, HunterRules.arrowDamage(1, 1), 1e-9);
		assertEquals(10, HunterRules.arrowDamage(3, 1), 1e-9);
		assertEquals(0.5, HunterRules.arrowSplashFraction(), 1e-9);
		assertEquals(5, HunterRules.fanDamage(1, 1), 1e-9);
		assertEquals(6, HunterRules.fanDamage(2, 1), 1e-9);
		assertEquals(160, HunterRules.sightTicks(1));
		assertEquals(200, HunterRules.sightTicks(2));
		assertEquals(8, HunterRules.finalDamage(1), 1e-9);
	}

	@Test
	void trapsAndLoot() {
		assertEquals(1.0, HunterRules.trapFactor(0), 1e-9);
		assertEquals(1.5, HunterRules.trapFactor(1), 1e-9);
		assertEquals(2.0, HunterRules.trapFactor(2), 1e-9);
		assertEquals(2.0, HunterRules.trapFactor(9), 1e-9);
		assertEquals(4, HunterRules.lootExperience(1));
		assertEquals(8, HunterRules.lootExperience(2));
	}
}
