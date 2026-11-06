package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreatRulesTest {
	@Test void usesHorizontalDistanceFromWorldSpawnWithExactBoundaries() {
		assertEquals(0, ThreatRules.ring(1999.99, -2000, 1000, -2000, 1000, 5));
		assertEquals(1, ThreatRules.ring(2000, -2000, 1000, -2000, 1000, 5));
		assertEquals(2, ThreatRules.ring(2200, -400, 1000, -2000, 1000, 5));
		assertEquals(5, ThreatRules.ring(30_000_000, -30_000_000, 0, 0, 1000, 5));
	}
	@Test void supportsNegativeCoordinatesAndBothAxesSymmetrically() {
		assertEquals(1, ThreatRules.ring(-1000, 0, 0, 0, 1000, 5));
		assertEquals(1, ThreatRules.ring(0, -1000, 0, 0, 1000, 5));
		assertEquals(0, ThreatRules.ring(600, 600, 0, 0, 1000, 5));
	}
	@Test void boundsSizeAndMaximumWithoutIntegerOverflow() {
		assertEquals(5, ThreatRules.ring(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 1000));
		assertEquals(1, ThreatRules.ring(100, 0, 0, 0, 0, 0));
		assertEquals(2, ThreatRules.ring(9000, 0, 0, 0, 1000, 2));
		assertEquals(0, ThreatRules.ring(999_999, 0, 0, 0, Integer.MAX_VALUE, 5));
	}
	@Test void handlesNonFinitePositionsAndOverflowingSubtractions() {
		assertEquals(0, ThreatRules.ring(Double.NaN, 0, 0, 0, 1000, 5));
		assertEquals(0, ThreatRules.ring(0, Double.POSITIVE_INFINITY, 0, 0, 1000, 5));
		assertEquals(0, ThreatRules.ring(0, 0, Double.NaN, 0, 1000, 5));
		assertEquals(5, ThreatRules.ring(Double.MAX_VALUE, 0, -Double.MAX_VALUE, 0, 1000, 5));
	}
	@Test void excludesOtherWorldsPassiveMobsBossesSummonsEventsAndNames() {
		assertTrue(ThreatRules.eligible(true, true, false, false, false, false));
		assertFalse(ThreatRules.eligible(false, true, false, false, false, false));
		assertFalse(ThreatRules.eligible(true, false, false, false, false, false));
		assertFalse(ThreatRules.eligible(true, true, true, false, false, false));
		assertFalse(ThreatRules.eligible(true, true, false, true, false, false));
		assertFalse(ThreatRules.eligible(true, true, false, false, true, false));
		assertFalse(ThreatRules.eligible(true, true, false, false, false, true));
	}
	@Test void defaultMultipliersReachSeventyFiveAndFiftyPercentAtV() {
		assertEquals(1, ThreatRules.healthMultiplier(0, 0.15));
		assertEquals(1.75, ThreatRules.healthMultiplier(5, 0.15));
		assertEquals(1.5, ThreatRules.damageMultiplier(5, 0.10, 3));
		assertEquals(0.5, ThreatRules.lootChance(5, 0.10));
		assertEquals(0.1125, ThreatRules.eliteChance(0.05, 5, 0.25), 1e-12);
	}
	@Test void neverExceedsHardLimitsOrTheConfiguredDamageCap() {
		assertEquals(3, ThreatRules.healthMultiplier(Integer.MAX_VALUE, Double.MAX_VALUE));
		assertEquals(2, ThreatRules.damageMultiplier(Integer.MAX_VALUE, Double.MAX_VALUE, 0));
		assertEquals(1.1, ThreatRules.damageMultiplier(5, 0.2, 1.1));
		assertEquals(1, ThreatRules.lootChance(Integer.MAX_VALUE, Double.MAX_VALUE));
		assertEquals(1, ThreatRules.eliteChance(Double.MAX_VALUE, 5, Double.MAX_VALUE));
	}
	@Test void corruptBonusValuesCannotIncreaseRewards() {
		assertEquals(1, ThreatRules.healthMultiplier(5, Double.NaN));
		assertEquals(1, ThreatRules.damageMultiplier(5, Double.POSITIVE_INFINITY, Double.NaN));
		assertEquals(0, ThreatRules.lootChance(5, -1));
		assertEquals(0, ThreatRules.eliteChance(Double.NaN, 5, 1));
		assertEquals(0.05, ThreatRules.eliteChance(0.05, -10, 1));
	}
	@Test void bonusLootRequiresOneUnclaimedValidSurvivalKill() {
		assertTrue(ThreatRules.bonusLoot(true, true, false, 0.5, 0.49));
		assertFalse(ThreatRules.bonusLoot(true, true, false, 0.5, 0.5));
		assertFalse(ThreatRules.bonusLoot(false, true, false, 1, 0));
		assertFalse(ThreatRules.bonusLoot(true, false, false, 1, 0));
		assertFalse(ThreatRules.bonusLoot(true, true, true, 1, 0));
		assertFalse(ThreatRules.bonusLoot(true, true, false, 0, 0));
	}
	@Test void invalidRollsAndNonFiniteLootCannotPay() {
		for (double roll : new double[]{-1, 1, Double.NaN, Double.POSITIVE_INFINITY})
			assertFalse(ThreatRules.bonusLoot(true, true, false, 1, roll));
		assertFalse(ThreatRules.bonusLoot(true, true, false, Double.NaN, 0));
	}
}
