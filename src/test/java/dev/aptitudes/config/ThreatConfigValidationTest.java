package dev.aptitudes.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreatConfigValidationTest {
	@Test void defaultsEnableThousandBlockRingsAndConservativeBonuses() {
		var c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.threatEnabled); assertEquals(1000, c.threatRingSize); assertEquals(5, c.threatMaxRing);
		assertEquals(0.15, c.threatHealthPerRing); assertEquals(0.10, c.threatDamagePerRing);
		assertEquals(0.10, c.threatLootPerRing); assertEquals(0.25, c.threatEliteChancePerRing);
		assertEquals(0.05, c.eliteChance);
	}
	@Test void clampsHugeValuesAndSupportsZeroBonusesAndDisabledFeature() {
		var c = new AptitudesConfig();
		c.threatRingSize = Integer.MAX_VALUE; c.threatMaxRing = Integer.MAX_VALUE;
		c.threatHealthPerRing = c.threatDamagePerRing = c.threatLootPerRing = c.threatEliteChancePerRing = Double.MAX_VALUE;
		AptitudesConfig.validated(c);
		assertEquals(1_000_000, c.threatRingSize); assertEquals(5, c.threatMaxRing);
		assertEquals(0.4, c.threatHealthPerRing); assertEquals(0.2, c.threatDamagePerRing);
		assertEquals(0.2, c.threatLootPerRing); assertEquals(1, c.threatEliteChancePerRing);
		c.threatEnabled = false; c.threatRingSize = 0; c.threatMaxRing = 0;
		c.threatHealthPerRing = c.threatDamagePerRing = c.threatLootPerRing = c.threatEliteChancePerRing = 0;
		AptitudesConfig.validated(c);
		assertFalse(c.threatEnabled); assertEquals(100, c.threatRingSize); assertEquals(1, c.threatMaxRing);
		assertEquals(0, c.threatHealthPerRing); assertEquals(0, c.threatDamagePerRing);
		assertEquals(0, c.threatLootPerRing); assertEquals(0, c.threatEliteChancePerRing);
	}
	@Test void resetsNegativeAndNonFiniteValuesToTheirDefaults() {
		var c = new AptitudesConfig();
		c.threatRingSize = -1; c.threatMaxRing = -1; c.threatHealthPerRing = Double.NaN;
		c.threatDamagePerRing = Double.POSITIVE_INFINITY; c.threatLootPerRing = -1; c.threatEliteChancePerRing = Double.NEGATIVE_INFINITY;
		AptitudesConfig.validated(c);
		assertEquals(1000, c.threatRingSize); assertEquals(5, c.threatMaxRing);
		assertEquals(0.15, c.threatHealthPerRing); assertEquals(0.10, c.threatDamagePerRing);
		assertEquals(0.10, c.threatLootPerRing); assertEquals(0.25, c.threatEliteChancePerRing);
	}
}
