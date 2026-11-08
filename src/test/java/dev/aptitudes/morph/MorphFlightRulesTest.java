package dev.aptitudes.morph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphFlightRulesTest {
	@Test void steeringUsesPitchAndClampsVelocity() {
		assertEquals(0.16, MorphFlightRules.verticalSpeed(0));
		assertEquals(0.32, MorphFlightRules.verticalSpeed(100));
		assertEquals(-0.15, MorphFlightRules.verticalSpeed(-100));
		assertEquals(0, MorphFlightRules.verticalSpeed(Double.NaN));
		assertTrue(MorphFlightRules.verticalSpeed(0.25) > MorphFlightRules.verticalSpeed(-0.25));
	}
}
