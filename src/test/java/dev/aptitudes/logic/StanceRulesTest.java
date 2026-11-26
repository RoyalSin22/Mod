package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StanceRulesTest {
	@Test
	void timerResetsOnEngagementAndDecaysToZero() {
		assertEquals(StanceRules.HOLD_TICKS, StanceRules.nextTimer(0, true));
		assertEquals(StanceRules.HOLD_TICKS, StanceRules.nextTimer(5, true));
		assertEquals(4, StanceRules.nextTimer(5, false));
		assertEquals(0, StanceRules.nextTimer(0, false));
		int t = StanceRules.HOLD_TICKS;
		for (int i = 0; i < StanceRules.HOLD_TICKS; i++) t = StanceRules.nextTimer(t, false);
		assertEquals(0, t);
	}

	@Test
	void stanceNeedsWeaponTimerAndFreeLocomotion() {
		assertTrue(StanceRules.active(10, true, false));
		assertFalse(StanceRules.active(0, true, false));
		assertFalse(StanceRules.active(10, false, false));
		assertFalse(StanceRules.active(10, true, true));
	}

	@Test
	void anyBlockingStateBlocks() {
		assertFalse(StanceRules.blocked(false, false, false, false, false, false, false, false));
		assertTrue(StanceRules.blocked(true, false, false, false, false, false, false, false));
		assertTrue(StanceRules.blocked(false, true, false, false, false, false, false, false));
		assertTrue(StanceRules.blocked(false, false, true, false, false, false, false, false));
		assertTrue(StanceRules.blocked(false, false, false, true, false, false, false, false));
		assertTrue(StanceRules.blocked(false, false, false, false, true, false, false, false));
		assertTrue(StanceRules.blocked(false, false, false, false, false, true, false, false));
		assertTrue(StanceRules.blocked(false, false, false, false, false, false, true, false));
		assertTrue(StanceRules.blocked(false, false, false, false, false, false, false, true));
	}

	@Test
	void everyStanceHasAnAnimationName() {
		for (StanceRules.Stance s : StanceRules.Stance.values()) {
			assertTrue(s.animation().startsWith("stance_"));
			assertTrue(PlayerAnimIds.ALL.contains(s.animation()));
		}
	}
}
