package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F4): cifras de las habilidades nuevas del Lancero, tal como las describen los textos. */
class LancerRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void distanceBonusNeedsThreeBlocks() {
		assertEquals(0.05, LancerRules.distanceBonus(1, 3), EPS);
		assertEquals(0.15, LancerRules.distanceBonus(3, 5), EPS);
		assertEquals(0, LancerRules.distanceBonus(3, 2.9), EPS);
		assertEquals(0, LancerRules.distanceBonus(0, 5), EPS);
	}

	@Test
	void markAndCleaveFollowTheRanks() {
		assertEquals(0.04, LancerRules.markBonus(1), EPS);
		assertEquals(0.12, LancerRules.markBonus(3), EPS);
		assertEquals(0, LancerRules.markBonus(-1), EPS);
		assertEquals(0.4, LancerRules.cleaveFraction(1), EPS);
		assertEquals(0.6, LancerRules.cleaveFraction(2), EPS);
		assertEquals(0, LancerRules.cleaveFraction(0), EPS);
	}

	@Test
	void activeDamageMatchesTheDescriptions() {
		assertEquals(6.0, LancerRules.javelinDamage(1, 1), EPS);
		assertEquals(10.0, LancerRules.javelinDamage(3, 1), EPS);
		assertEquals(5.0, LancerRules.javelinDamage(3, 0.5), EPS);
		assertEquals(3.0, LancerRules.wallDamage(1, 1), EPS);
		assertEquals(5.0, LancerRules.wallDamage(3, 1), EPS);
		assertEquals(4.0, LancerRules.fanDamage(1, 1), EPS);
		assertEquals(5.0, LancerRules.fanDamage(2, 1), EPS);
		assertEquals(12.0, LancerRules.BARRAGE_DAMAGE, EPS);
	}

	@Test
	void firstContactResetsAfterTenSeconds() {
		assertTrue(LancerRules.isFirstContact(null, 1000));
		assertFalse(LancerRules.isFirstContact(900L, 1000));
		assertFalse(LancerRules.isFirstContact(800L, 1000), "justo a los 10 s todavía cuenta como el mismo combate");
		assertTrue(LancerRules.isFirstContact(799L, 1000));
	}

	@Test
	void ancestralSpearAndDoubleThrust() {
		assertEquals(160, LancerRules.ancestralTicks(1));
		assertEquals(200, LancerRules.ancestralTicks(2));
		assertEquals(0, LancerRules.ancestralTicks(0));
		assertEquals(0.1, LancerRules.doubleChance(1), EPS);
		assertEquals(0.2, LancerRules.doubleChance(2), EPS);
		assertEquals(0, LancerRules.doubleChance(-1), EPS);
	}
}
