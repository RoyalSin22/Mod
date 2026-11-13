package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F6): cifras del Invocador de armas. */
class WeaponSummonerRulesTest {
	@Test
	void sharpAndBalance() {
		assertEquals(0.10, WeaponSummonerRules.sharpBonus(1), 1e-9);
		assertEquals(0.30, WeaponSummonerRules.sharpBonus(3), 1e-9);
		assertEquals(0.04, WeaponSummonerRules.balanceBonus(1, 3), 1e-9);
		assertEquals(0, WeaponSummonerRules.balanceBonus(3, 2), 1e-9, "con menos de 3 espadas no hay bonificación");
		assertEquals(0.12, WeaponSummonerRules.balanceBonus(3, 5), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(6, WeaponSummonerRules.boomerangDamage(1, 1), 1e-9);
		assertEquals(10, WeaponSummonerRules.boomerangDamage(3, 1), 1e-9);
		assertEquals(3, WeaponSummonerRules.wallDamage(1, 1), 1e-9);
		assertEquals(5, WeaponSummonerRules.wallDamage(3, 1), 1e-9);
		assertEquals(3, WeaponSummonerRules.danceDamage(1, 1), 1e-9);
		assertEquals(4, WeaponSummonerRules.danceDamage(2, 1), 1e-9);
		assertEquals(160, WeaponSummonerRules.livingTicks(1));
		assertEquals(200, WeaponSummonerRules.livingTicks(2));
		assertEquals(0.3, WeaponSummonerRules.echoChance(1), 1e-9);
		assertEquals(0.5, WeaponSummonerRules.echoChance(2), 1e-9);
	}

	@Test
	void spareBladesNeedAHurtThatDoesNotKill() {
		assertTrue(WeaponSummonerRules.spareTriggers(10, 5, 20, true));
		assertFalse(WeaponSummonerRules.spareTriggers(10, 10, 20, true), "mortal");
		assertFalse(WeaponSummonerRules.spareTriggers(20, 5, 20, true), "queda el 75 %");
		assertFalse(WeaponSummonerRules.spareTriggers(10, 5, 20, false), "en espera");
		assertEquals(3, WeaponSummonerRules.spareBlades(1));
		assertEquals(5, WeaponSummonerRules.spareBlades(2));
	}

	@Test
	void perfectEdgeEveryTenthHit() {
		int count = 0;
		int perfect = 0;
		for (int i = 0; i < 30; i++) {
			if (WeaponSummonerRules.perfectHit(count)) perfect++;
			count = WeaponSummonerRules.perfectCount(count);
		}
		assertEquals(3, perfect);
		assertEquals(0, count);
	}

	@Test
	void judgmentCountsAllSwordsPlusFive() {
		assertEquals(5, WeaponSummonerRules.judgmentSwords(0));
		assertEquals(10, WeaponSummonerRules.judgmentSwords(5));
		assertEquals(6, WeaponSummonerRules.judgmentDamage(1), 1e-9);
		assertEquals(3, WeaponSummonerRules.judgmentSplash(1), 1e-9);
	}
}
