package dev.aptitudes.mob;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpectralRendRulesTest {
	@Test
	void checksSectorRangeAndHeight() {
		double sin60 = Math.sqrt(3) / 2;
		assertTrue(SpectralRendRules.contains(sin60, 0.25, 0.5, 0, 1, 1));
		assertFalse(SpectralRendRules.contains(0.9, 1, 0.49, 0, 1, 1));
		assertFalse(SpectralRendRules.contains(0, 1, -0.01, 0, 1, 1));
		assertTrue(SpectralRendRules.contains(0, 1.55, 1.75, 0, 1, 1));
		assertFalse(SpectralRendRules.contains(0, 1.551, 1.75, 0, 1, 1));
		assertFalse(SpectralRendRules.contains(0, 0.249, 0.4, 0, 1, 1));
		assertFalse(SpectralRendRules.contains(0, 1, 1.751, 0, 1, 1));
	}

	@Test
	void checksTargetBoxAndGiantScale() {
		assertNotNull(SpectralRendRules.contact(-0.2, 0, 1.74, 0.2, 1, 2.2, 0, 1, 1));
		assertNull(SpectralRendRules.contact(-0.2, 0, 1.76, 0.2, 1, 2.2, 0, 1, 1));
		assertNotNull(SpectralRendRules.contact(-0.2, 1.6, 2.30, 0.2, 1.7, 2.6, 0, 1, 1.35));
		assertNull(SpectralRendRules.contact(-0.2, 1.6, 2.37, 0.2, 1.7, 2.6, 0, 1, 1.35));
		assertNull(SpectralRendRules.contact(-0.2, 0, -1, 0.2, 1, -0.05, 0, 1, 1));
		assertNotNull(SpectralRendRules.contact(0.85, 0, 0.49, 1.1, 1, 0.8, 0, 1, 1));
		assertNotNull(SpectralRendRules.contact(-2, 0, -2, 2, 1, 2, 0, 1, 1));
	}

	@Test
	void strikesOnlyAtTenAndOnlyOncePerTarget() {
		var execution = new SpectralRendRules.Execution(100);
		var first = new UUID(0, 1);
		var second = new UUID(0, 2);
		assertFalse(execution.firstAttempt(first));
		assertFalse(execution.warn(105));
		assertTrue(execution.warn(106));
		assertFalse(execution.warn(106));
		for (int tick = 100; tick < 110; tick++) assertFalse(execution.strike(tick));
		assertTrue(execution.strike(110));
		assertFalse(execution.strike(110));
		assertTrue(execution.firstAttempt(first));
		assertFalse(execution.firstAttempt(first));
		assertTrue(execution.firstAttempt(second));
		assertFalse(execution.finished(119));
		assertTrue(execution.finished(120));
		assertEquals(40, SpectralRendRules.WEAKNESS_TICKS);
	}

	@Test
	void cancellationPreventsWarningAndImpact() {
		var execution = new SpectralRendRules.Execution(200);
		execution.cancel();
		assertFalse(execution.warn(206));
		assertFalse(execution.strike(210));
		assertFalse(execution.firstAttempt(new UUID(0, 3)));
		assertTrue(execution.finished(201));
	}
}
