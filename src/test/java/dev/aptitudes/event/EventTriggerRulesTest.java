package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static dev.aptitudes.event.EventTriggerRules.*;
import static org.junit.jupiter.api.Assertions.*;

class EventTriggerRulesTest {
	@Test void cooldownIsComputedInLongWithoutOverflow() {
		assertEquals(10 * 24_000L, cooldownTicks(10));
		assertEquals(0, cooldownTicks(-5));
		assertEquals(Integer.MAX_VALUE * 24_000L, cooldownTicks(Integer.MAX_VALUE));
		assertTrue(cooldownTicks(Integer.MAX_VALUE) > Integer.MAX_VALUE);
	}

	@Test void neverActivatedIsAlwaysReady() {
		assertTrue(ready(NEVER, 0, 10));
		assertTrue(ready(NEVER, 5_000_000, 365));
	}

	@Test void waitsExactlyTheCooldown() {
		long start = 100_000;
		assertFalse(ready(start, start, 10));
		assertFalse(ready(start, start + 10 * 24_000L - 1, 10));
		assertTrue(ready(start, start + 10 * 24_000L, 10));
		assertTrue(ready(start, start + 10 * 24_000L + 1, 10));
	}

	@Test void anomalousFutureStartDoesNotLockTheEvent() {
		assertTrue(ready(500_000, 1_000, 10));
	}

	@Test void remainingTicksCountsDown() {
		long start = 1_000;
		assertEquals(5 * 24_000L, remainingTicks(start, start, 5));
		assertEquals(5 * 24_000L - 100, remainingTicks(start, start + 100, 5));
		assertEquals(0, remainingTicks(start, start + 5 * 24_000L, 5));
		assertEquals(0, remainingTicks(NEVER, 0, 5));
	}

	@Test void emberRainWaitsFiveDays() {
		assertFalse(ready(0, 4 * 24_000L, 5));
		assertTrue(ready(0, 5 * 24_000L, 5));
	}

	@Test void fogDurationIsClamped() {
		assertEquals(24_000, fogDuration(24_000));
		assertEquals(MIN_FOG_TICKS, fogDuration(0));
		assertEquals(MIN_FOG_TICKS, fogDuration(-1));
		assertEquals(MAX_FOG_TICKS, fogDuration(Long.MAX_VALUE));
	}

	@Test void villagerRepliesByPriority() {
		assertEquals(Reply.START, villagerReply(false, true));
		assertEquals(Reply.WAIT, villagerReply(false, false));
		assertEquals(Reply.BUSY, villagerReply(true, true));
		assertEquals(Reply.BUSY, villagerReply(true, false));
	}

	@Test void portalOpensOnlyWhenNewPortalBlocksAppear() {
		assertTrue(portalOpened(0, 6));
		assertFalse(portalOpened(0, 0));
		assertFalse(portalOpened(6, 6));
		assertTrue(portalOpened(6, 12));
		assertFalse(portalOpened(6, 3));
		assertTrue(portalOpened(-1, 1));
	}

	@Test void portalChecksAreCapped() {
		assertTrue(canQueuePortalCheck(0));
		assertTrue(canQueuePortalCheck(MAX_PENDING_PORTAL_CHECKS - 1));
		assertFalse(canQueuePortalCheck(MAX_PENDING_PORTAL_CHECKS));
	}
}
