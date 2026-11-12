package dev.aptitudes.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestLimiterTest {
	private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
	private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

	@Test void aRequestInsideTheIntervalIsRejectedAndOutsideIsAccepted() {
		RequestLimiter limiter = new RequestLimiter();
		assertTrue(limiter.tryAcquire(A, "stats", 100, 5));
		assertFalse(limiter.tryAcquire(A, "stats", 102, 5));
		assertFalse(limiter.tryAcquire(A, "stats", 104, 5));
		assertTrue(limiter.tryAcquire(A, "stats", 105, 5));
	}

	@Test void aRejectedRequestDoesNotExtendTheWindow() {
		RequestLimiter limiter = new RequestLimiter();
		assertTrue(limiter.tryAcquire(A, "stats", 100, 5));
		assertFalse(limiter.tryAcquire(A, "stats", 103, 5));
		assertTrue(limiter.tryAcquire(A, "stats", 105, 5)); // cuenta desde la última aceptada (100), no desde la rechazada
	}

	@Test void eachActionAndEachPlayerHaveTheirOwnCounter() {
		RequestLimiter limiter = new RequestLimiter();
		assertTrue(limiter.tryAcquire(A, "stats", 100, 20));
		assertTrue(limiter.tryAcquire(A, "invite", 100, 20));
		assertTrue(limiter.tryAcquire(B, "stats", 100, 20));
		assertFalse(limiter.tryAcquire(A, "stats", 101, 20));
	}

	@Test void aClockThatGoesBackwardsIsAcceptedAndRestartsTheWindow() {
		RequestLimiter limiter = new RequestLimiter();
		assertTrue(limiter.tryAcquire(A, "stats", 5000, 20));
		assertTrue(limiter.tryAcquire(A, "stats", 10, 20)); // el mundo se recargó y los ticks empezaron de nuevo
		assertFalse(limiter.tryAcquire(A, "stats", 12, 20));
	}

	@Test void aZeroOrNegativeIntervalNeverRejects() {
		RequestLimiter limiter = new RequestLimiter();
		assertTrue(limiter.tryAcquire(A, "x", 1, 0));
		assertTrue(limiter.tryAcquire(A, "x", 1, -5));
	}

	@Test void forgettingAPlayerRemovesOnlyTheirEntries() {
		RequestLimiter limiter = new RequestLimiter();
		limiter.tryAcquire(A, "stats", 1, 5);
		limiter.tryAcquire(A, "invite", 1, 5);
		limiter.tryAcquire(B, "stats", 1, 5);
		limiter.forget(A);
		assertEquals(1, limiter.size());
		assertTrue(limiter.tryAcquire(A, "stats", 2, 5));
	}
}
