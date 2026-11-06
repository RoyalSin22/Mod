package dev.aptitudes.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasteryConfigValidationTest {
	@Test void preservesTheDefaultCurveAndTenPercentCap() {
		var c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.masteryEnabled);
		assertEquals(1000, c.masteryXpBase);
		assertEquals(250, c.masteryXpStep);
		assertEquals(0.10, c.masteryMaxBonus);
	}
	@Test void boundsHugeAndNonFiniteConfiguration() {
		var c = new AptitudesConfig();
		c.masteryXpBase = Integer.MAX_VALUE; c.masteryXpStep = Integer.MAX_VALUE; c.masteryMaxBonus = Double.MAX_VALUE;
		AptitudesConfig.validated(c);
		assertEquals(1_000_000, c.masteryXpBase); assertEquals(1_000_000, c.masteryXpStep); assertEquals(0.25, c.masteryMaxBonus);
		c.masteryMaxBonus = Double.NaN; c.masteryXpBase = -1; c.masteryXpStep = -1;
		AptitudesConfig.validated(c);
		assertEquals(0.10, c.masteryMaxBonus); assertEquals(1000, c.masteryXpBase); assertEquals(250, c.masteryXpStep);
	}
	@Test void allowsDisablingWithoutRequiringAZeroCostCurve() {
		var c = new AptitudesConfig();
		c.masteryEnabled = false; c.masteryMaxBonus = 0; c.masteryXpBase = 0; c.masteryXpStep = 0;
		AptitudesConfig.validated(c);
		assertFalse(c.masteryEnabled); assertEquals(0, c.masteryMaxBonus); assertEquals(1, c.masteryXpBase); assertEquals(0, c.masteryXpStep);
	}
}
