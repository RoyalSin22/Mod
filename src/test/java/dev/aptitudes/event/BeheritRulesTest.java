package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static dev.aptitudes.event.BeheritRules.Result.*;
import static dev.aptitudes.event.EventTriggerRules.NEVER;
import static org.junit.jupiter.api.Assertions.*;

class BeheritRulesTest {
	private static final long DAY = 24_000L;

	@Test void neverUsedIsReady() {
		assertEquals(READY, BeheritRules.check(true, false, NEVER, 0, 3));
		assertEquals(0, BeheritRules.hoursLeft(NEVER, 0, 3));
	}

	@Test void waitsThreeDaysCountedFromTheEndOfTheEclipse() {
		long end = 500_000;
		assertEquals(COOLDOWN, BeheritRules.check(true, false, end, end, 3));
		assertEquals(COOLDOWN, BeheritRules.check(true, false, end, end + 3 * DAY - 1, 3));
		assertEquals(READY, BeheritRules.check(true, false, end, end + 3 * DAY, 3));
	}

	@Test void zeroDaysMeansNoWait() {
		assertEquals(READY, BeheritRules.check(true, false, 1_000, 1_000, 0));
	}

	@Test void disabledAndBusyWinOverTheCooldown() {
		assertEquals(DISABLED, BeheritRules.check(false, true, 0, 1, 3));
		assertEquals(BUSY, BeheritRules.check(true, true, NEVER, 0, 3));
		assertEquals(BUSY, BeheritRules.check(true, true, 0, 1, 3));
	}

	@Test void aRecordInTheFutureDoesNotLockTheRitualForever() {
		assertEquals(READY, BeheritRules.check(true, false, 10 * DAY, DAY, 3));
	}

	@Test void hoursLeftRoundUpAndCountInGameHours() {
		long end = 0;
		assertEquals(72, BeheritRules.hoursLeft(end, 0, 3));
		assertEquals(72, BeheritRules.hoursLeft(end, 1, 3)); // 71 h y 999 ticks → 72
		assertEquals(1, BeheritRules.hoursLeft(end, 3 * DAY - 1, 3));
		assertEquals(0, BeheritRules.hoursLeft(end, 3 * DAY, 3));
	}

	@Test void hugeConfiguredDaysDoNotOverflow() {
		assertEquals(COOLDOWN, BeheritRules.check(true, false, 0, 1, Integer.MAX_VALUE));
		assertTrue(BeheritRules.hoursLeft(0, 1, Integer.MAX_VALUE) > Integer.MAX_VALUE);
	}
}
