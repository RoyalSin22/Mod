package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F7): cifras del Brujo abisal. */
class WarlockRulesTest {
	@Test
	void passiveValues() {
		assertEquals(0.04, WarlockRules.cursedBonus(1), 1e-9);
		assertEquals(0.12, WarlockRules.cursedBonus(3), 1e-9);
		assertEquals(3, WarlockRules.bloodMana(1), 1e-9);
		assertEquals(7, WarlockRules.bloodMana(3), 1e-9);
		assertEquals(0, WarlockRules.bloodMana(0), 1e-9);
		assertEquals(0.2, WarlockRules.cravingBonus(1), 1e-9);
		assertEquals(0.3, WarlockRules.cravingBonus(2), 1e-9);
		assertEquals(3, WarlockRules.harvestDamage(1, 1), 1e-9);
		assertEquals(5, WarlockRules.harvestDamage(2, 1), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(8, WarlockRules.evilEyeDamage(1, 1), 1e-9);
		assertEquals(12, WarlockRules.evilEyeDamage(3, 1), 1e-9);
		assertEquals(60, WarlockRules.chainsTicks(1));
		assertEquals(100, WarlockRules.chainsTicks(3));
		assertEquals(3, WarlockRules.cloudDamage(1, 1), 1e-9);
		assertEquals(4, WarlockRules.cloudDamage(2, 1), 1e-9);
		assertEquals(160, WarlockRules.demonTicks(1));
		assertEquals(200, WarlockRules.demonTicks(2));
		assertEquals(6, WarlockRules.pitDamage(1), 1e-9);
		assertEquals(3, WarlockRules.pitDamage(0.5), 1e-9);
	}

	@Test
	void desperationBelowThirtyPercentWhenReady() {
		assertTrue(WarlockRules.desperationTriggers(5, 20, true));
		assertFalse(WarlockRules.desperationTriggers(6, 20, true), "justo el 30 % no basta");
		assertFalse(WarlockRules.desperationTriggers(5, 20, false));
		assertFalse(WarlockRules.desperationTriggers(5, 0, true));
	}
}
