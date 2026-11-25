package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static dev.aptitudes.event.EclipseSummoningRules.*;
import static org.junit.jupiter.api.Assertions.*;

/** C3: permiso y espera para convocar el Eclipse (Astrolabio del Ocaso y Damnorath). */
class EclipseSummoningRulesTest {
	private static final long DAY = 24_000L;
	private static final long NEVER = EventTriggerRules.NEVER;

	@Test void operatorOrPartyLeaderMaySummonWithAstrolabe() {
		assertEquals(Verdict.ALLOWED, astrolabe(true, false, true, false, NEVER, 0, 3));
		assertEquals(Verdict.ALLOWED, astrolabe(false, true, true, false, NEVER, 0, 3));
		assertEquals(Verdict.ALLOWED, astrolabe(true, true, true, false, NEVER, 0, 3));
	}

	@Test void ordinaryPlayerOrPartyMemberIsDenied() {
		assertEquals(Verdict.NOT_AUTHORIZED, astrolabe(false, false, true, false, NEVER, 0, 3));
	}

	@Test void damnorathNeedsNoPermission() {
		assertEquals(Verdict.ALLOWED, damnorath(true, false, NEVER, 0, 3));
	}

	@Test void onlyTheOverworldCounts() {
		assertEquals(Verdict.WRONG_WORLD, astrolabe(true, true, false, false, NEVER, 0, 3));
		assertEquals(Verdict.WRONG_WORLD, damnorath(false, false, NEVER, 0, 3));
	}

	@Test void anotherEventInProgressBlocksBothItems() {
		assertEquals(Verdict.BUSY, astrolabe(true, true, true, true, NEVER, 0, 3));
		assertEquals(Verdict.BUSY, damnorath(true, true, NEVER, 0, 3));
	}

	@Test void waitsExactlyThreeDaysFromTheEndOfTheLastEclipse() {
		long end = 50_000;
		assertEquals(Verdict.COOLDOWN, damnorath(true, false, end, end, 3));
		assertEquals(Verdict.COOLDOWN, damnorath(true, false, end, end + 3 * DAY - 1, 3));
		assertEquals(Verdict.ALLOWED, damnorath(true, false, end, end + 3 * DAY, 3));
		assertEquals(Verdict.COOLDOWN, astrolabe(true, true, true, false, end, end + 3 * DAY - 1, 3));
		assertEquals(Verdict.ALLOWED, astrolabe(true, true, true, false, end, end + 3 * DAY, 3));
	}

	@Test void zeroOrNegativeWaitDisablesTheCooldown() {
		assertEquals(Verdict.ALLOWED, damnorath(true, false, 1_000, 1_000, 0));
		assertEquals(Verdict.ALLOWED, damnorath(true, false, 1_000, 1_000, -4));
	}

	@Test void aFutureEndTickDoesNotLockTheWorld() {
		assertEquals(Verdict.ALLOWED, damnorath(true, false, 9_000_000, 1_000, 3));
	}

	@Test void denialPriorityIsPermissionThenWorldThenBusyThenCooldown() {
		assertEquals(Verdict.NOT_AUTHORIZED, astrolabe(false, false, false, true, 0, 0, 3));
		assertEquals(Verdict.WRONG_WORLD, astrolabe(true, false, false, true, 0, 0, 3));
		assertEquals(Verdict.BUSY, astrolabe(true, false, true, true, 0, 0, 3));
		assertEquals(Verdict.COOLDOWN, astrolabe(true, false, true, false, 0, 0, 3));
	}

	@Test void remainingDaysRoundsUpAndNeverOverflows() {
		assertEquals(0, remainingDays(NEVER, 0, 3));
		assertEquals(3, remainingDays(0, 0, 3));
		assertEquals(3, remainingDays(0, 1, 3));
		assertEquals(1, remainingDays(0, 3 * DAY - 1, 3));
		assertEquals(0, remainingDays(0, 3 * DAY, 3));
		assertEquals(Integer.MAX_VALUE, remainingDays(0, 0, Integer.MAX_VALUE));
	}
}
