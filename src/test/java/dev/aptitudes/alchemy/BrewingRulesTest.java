package dev.aptitudes.alchemy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrewingRulesTest {
	@Test
	void extendsDurationWithoutIncreasingPotencyOrInstantEffects() {
		assertEquals(300, BrewingRules.duration(200, 0.5));
		assertEquals(1, BrewingRules.duration(1, 0.5));
		assertEquals(-1, BrewingRules.duration(-1, 0.5));
		assertEquals(0, BrewingRules.duration(0, 0.5));
	}

	@Test
	void boundsDurationAndNonFiniteConfiguration() {
		assertEquals(24_000, BrewingRules.duration(Integer.MAX_VALUE, Double.MAX_VALUE));
		assertEquals(400, BrewingRules.duration(200, 9));
		for (double bonus : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
			assertEquals(200, BrewingRules.duration(200, bonus));
		}
	}

	@Test
	void speedBonusAddsOnlyOneExtraTickEveryTwoTicksByDefault() {
		double credit = 0;
		int extra = 0;
		for (int tick = 0; tick < 400; tick++) {
			credit = BrewingRules.speedCredit(credit, 0.5);
			if (credit >= 1) { extra++; credit--; }
		}
		assertEquals(200, extra);
		assertEquals(0, credit);
	}

	@Test
	void corruptedCreditNeverCreatesAnUnboundedCatchUpLoop() {
		for (double credit : new double[]{-1, Double.MAX_VALUE, Double.NaN, Double.POSITIVE_INFINITY}) {
			assertEquals(1, BrewingRules.speedCredit(credit, 100));
		}
		assertEquals(0, BrewingRules.speedCredit(0, Double.NaN));
	}

	@Test
	void xpFallsToZeroRatherThanRoundingBackUpForever() {
		assertEquals(4, BrewingRules.xp(4, 1, 3));
		assertEquals(4, BrewingRules.xp(4, 3, 3));
		assertEquals(3, BrewingRules.xp(4, 4, 3));
		assertEquals(2, BrewingRules.xp(4, 6, 3));
		assertEquals(1, BrewingRules.xp(4, 12, 3));
		assertEquals(0, BrewingRules.xp(4, 13, 3));
		assertEquals(0, BrewingRules.xp(0, 1, 3));
		assertEquals(20, BrewingRules.xp(Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
		int total = 0;
		for (int i = 1; i <= 1000; i++) total += BrewingRules.xp(4, i, 3);
		assertEquals(25, total);
	}
}
