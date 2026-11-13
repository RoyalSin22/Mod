package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F4): cifras de las habilidades nuevas del Nigromante, tal como las describen los textos. */
class NecromancerRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void orderRangeAndBanquet() {
		assertEquals(12, NecromancerRules.orderRange(1), EPS);
		assertEquals(16, NecromancerRules.orderRange(2), EPS);
		assertEquals(20, NecromancerRules.orderRange(3), EPS);
		assertEquals(0, NecromancerRules.orderRange(0), EPS);
		assertEquals(1f, NecromancerRules.banquetHeal(1));
		assertEquals(3f, NecromancerRules.banquetHeal(3));
		assertEquals(4, NecromancerRules.banquetMana(1), EPS);
		assertEquals(8, NecromancerRules.banquetMana(3), EPS);
		assertEquals(0, NecromancerRules.banquetMana(0), EPS);
	}

	@Test
	void handAndSacrifice() {
		assertEquals(4.0, NecromancerRules.handDamage(1, 1), EPS);
		assertEquals(6.0, NecromancerRules.handDamage(3, 1), EPS);
		assertEquals(40, NecromancerRules.rootTicks(1));
		assertEquals(60, NecromancerRules.rootTicks(2));
		assertEquals(80, NecromancerRules.rootTicks(3));
		assertEquals(8.0, NecromancerRules.sacrificeDamage(1, 1), EPS);
		assertEquals(12.0, NecromancerRules.sacrificeDamage(3, 1), EPS);
		assertEquals(6.0, NecromancerRules.sacrificeDamage(3, 0.5), EPS);
	}

	@Test
	void auraAndAccursedGround() {
		assertEquals(1.0, NecromancerRules.auraDamage(1, 1), EPS);
		assertEquals(2.0, NecromancerRules.auraDamage(2, 1), EPS);
		assertEquals(0, NecromancerRules.auraDamage(-1, 1), EPS);
		assertEquals(3.0, NecromancerRules.groundDamage(1, 1), EPS);
		assertEquals(4.0, NecromancerRules.groundDamage(2, 1), EPS);
	}

	@Test
	void resurgeOnlyWhenTheHitWouldKillAndItIsReady() {
		assertTrue(NecromancerRules.resurges(10, 10, true));
		assertTrue(NecromancerRules.resurges(10, 50, true));
		assertFalse(NecromancerRules.resurges(10, 9.9, true));
		assertFalse(NecromancerRules.resurges(10, 50, false));
	}

	@Test
	void frenzyAndBoneBond() {
		assertEquals(200, NecromancerRules.frenzyTicks(1));
		assertEquals(280, NecromancerRules.frenzyTicks(2));
		assertEquals(0, NecromancerRules.frenzyTicks(0));
		assertEquals(0.15, NecromancerRules.boneShare(1), EPS);
		assertEquals(0.25, NecromancerRules.boneShare(2), EPS);
		assertEquals(0, NecromancerRules.boneShare(0), EPS);
	}

	@Test
	void soulStormHealsAtMostTen() {
		float healed = 0;
		int hits = 0;
		while (NecromancerRules.stormHeal(healed) > 0) {
			healed += NecromancerRules.stormHeal(healed);
			hits++;
		}
		assertEquals(10f, healed);
		assertEquals(20, hits);
		assertEquals(0.25f, NecromancerRules.stormHeal(9.75f));
		assertEquals(0f, NecromancerRules.stormHeal(10f));
		assertEquals(0f, NecromancerRules.stormHeal(12f));
	}
}
