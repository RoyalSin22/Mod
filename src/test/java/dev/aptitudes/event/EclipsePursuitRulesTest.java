package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EclipsePursuitRulesTest {
	@Test
	void staggersPositiveAndNegativeKeysOnceInEverySecond() {
		for (int key : new int[]{0, 1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
			int checks = 0;
			for (long tick = 120; tick < 140; tick++) {
				if (EclipsePursuitRules.due(key, tick)) checks++;
			}
			assertEquals(1, checks);
		}
		assertFalse(EclipsePursuitRules.due(0, 1));
		assertTrue(EclipsePursuitRules.due(0, 20));
	}

	@Test
	void detectionIsBoundedToFortyEightBlocks() {
		assertTrue(EclipsePursuitRules.inRange(48 * 48));
		assertFalse(EclipsePursuitRules.inRange(48 * 48 + 0.01));
		assertFalse(EclipsePursuitRules.inRange(-1));
		assertFalse(EclipsePursuitRules.inRange(Double.NaN));
		assertFalse(EclipsePursuitRules.inRange(Double.POSITIVE_INFINITY));
	}

	@Test
	void searchEndsOnArrivalOrAfterSixSeconds() {
		assertTrue(EclipsePursuitRules.canSearch(219, 100, 4));
		assertFalse(EclipsePursuitRules.canSearch(220, 100, 4));
		assertFalse(EclipsePursuitRules.canSearch(101, 100, 2.25));
		assertFalse(EclipsePursuitRules.canSearch(101, 100, 2500));
	}

	@Test
	void rejectsInvalidClockWithoutOverflow() {
		assertFalse(EclipsePursuitRules.canSearch(99, 100, 4));
		assertFalse(EclipsePursuitRules.canSearch(Long.MAX_VALUE, Long.MIN_VALUE, 4));
		assertFalse(EclipsePursuitRules.canSearch(Long.MIN_VALUE, Long.MAX_VALUE, 4));
		assertTrue(EclipsePursuitRules.canSearch(Long.MAX_VALUE, Long.MAX_VALUE - 119, 4));
	}
}
