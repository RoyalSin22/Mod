package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasteryRulesTest {
	@Test void requiresGameplayAndALivingNonCreativeNonSpectatorPlayer() {
		assertTrue(MasteryRules.canEarn(true, true, true, false, false));
		assertFalse(MasteryRules.canEarn(false, true, true, false, false));
		assertFalse(MasteryRules.canEarn(true, false, true, false, false));
		assertFalse(MasteryRules.canEarn(true, true, false, false, false));
		assertFalse(MasteryRules.canEarn(true, true, true, true, false));
		assertFalse(MasteryRules.canEarn(true, true, true, false, true));
	}
	@Test void raisingTheLevelCapSuspendsTheBonusAndRolesNeedAtLeastFifty() {
		assertFalse(MasteryRules.atCap(49, 50, 1));
		assertTrue(MasteryRules.atCap(50, 50, 1));
		assertFalse(MasteryRules.atCap(50, 60, 1));
		assertFalse(MasteryRules.atCap(30, 30, 50));
		assertTrue(MasteryRules.atCap(50, 30, 50));
		assertFalse(MasteryRules.atCap(50, 60, 50));
		assertTrue(MasteryRules.atCap(60, 60, 50));
	}
	@Test void usesAnIncreasingCurveAndKeepsUnspentRemainders() {
		assertEquals(1000, MasteryRules.required(1, 1000, 250));
		assertEquals(2250, MasteryRules.required(2, 1000, 250));
		assertEquals(67500, MasteryRules.required(20, 1000, 250));
		assertEquals(0, MasteryRules.points(999, 1000, 250));
		assertEquals(1, MasteryRules.points(2249, 1000, 250));
		assertEquals(2, MasteryRules.points(2250, 1000, 250));
	}
	@Test void sumsManyEventsLikeOneGrantWithoutLosingXp() {
		long xp = 0;
		for (int i = 0; i < 2250; i++) xp = MasteryRules.add(xp, 1);
		assertEquals(2250, xp);
		assertEquals(2, MasteryRules.points(xp, 1000, 250));
		for (int i = 0; i < 100; i++) assertEquals(2, MasteryRules.points(xp, 1000, 250));
		assertEquals(2250, xp);
	}
	@Test void rejectsNegativeXpAndSaturatesWithoutOverflow() {
		assertEquals(0, MasteryRules.add(Long.MIN_VALUE, Long.MIN_VALUE));
		assertEquals(MasteryRules.MAX_XP, MasteryRules.add(Long.MAX_VALUE, Long.MAX_VALUE));
		assertEquals(MasteryRules.MAX_XP, MasteryRules.add(MasteryRules.MAX_XP - 1, Long.MAX_VALUE));
		assertEquals(1000, MasteryRules.points(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertEquals(0, MasteryRules.points(-1, 1, 0));
		assertTrue(MasteryRules.required(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) > 0);
	}
	@Test void acceptsAConstantCurveButNeverAZeroCostPoint() {
		assertEquals(3, MasteryRules.points(3000, 1000, 0));
		assertEquals(0, MasteryRules.points(0, -1, -1));
		assertEquals(1, MasteryRules.points(1, -1, -1));
	}
	@Test void givesHalfAPercentPerPointAndHonorsBothCaps() {
		assertEquals(0.005, MasteryRules.bonus(1, 0.1), 1e-12);
		assertEquals(0.1, MasteryRules.bonus(20, 0.1), 1e-12);
		assertEquals(0.1, MasteryRules.bonus(1000, 0.1), 1e-12);
		assertEquals(0.25, MasteryRules.bonus(Integer.MAX_VALUE, Double.MAX_VALUE), 1e-12);
		assertEquals(0, MasteryRules.bonus(-1, 0.1));
		for (double cap : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) assertEquals(0, MasteryRules.bonus(10, cap));
	}
	@Test void creditsOnlyTheNewSurplusAndNeverAdministrativeOrCreativeXp() {
		assertEquals(15, MasteryRules.surplus(15, 100, true));
		assertEquals(100, MasteryRules.surplus(Long.MAX_VALUE, 100, true));
		assertEquals(0, MasteryRules.surplus(1000, 1000, false));
		assertEquals(0, MasteryRules.surplus(-1, 100, true));
		assertEquals(0, MasteryRules.surplus(100, Integer.MIN_VALUE, true));
	}
	@Test void discountsRealEnergyEvenWithOnePointAndKeepsAllValuesFinite() {
		assertEquals(20, MasteryRules.energyCost(20, 0));
		assertEquals(20 / 1.005, MasteryRules.energyCost(20, 0.005), 1e-12);
		assertEquals(20 / 1.1, MasteryRules.energyCost(20, 0.1), 1e-12);
		assertEquals(0, MasteryRules.energyCost(-1, 0.1));
		assertEquals(20, MasteryRules.energyCost(20, Double.NaN));
		assertTrue(Double.isFinite(MasteryRules.energyCost(Integer.MAX_VALUE, Double.MAX_VALUE)));
	}
}
