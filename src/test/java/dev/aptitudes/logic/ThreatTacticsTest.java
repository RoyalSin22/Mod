package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreatTacticsTest {
	@Test void startingZoneHasNoTactics() {
		assertEquals(0, ThreatTactics.callRadius(0));
		assertEquals(0, ThreatTactics.dodgeChance(0, 0.06));
		assertFalse(ThreatTactics.enrages(0, 0.1f, false));
		assertEquals(0, ThreatTactics.knockbackResistance(0));
	}

	@Test void callRadiusGrowsWithTheRingAndIsBounded() {
		assertEquals(10, ThreatTactics.callRadius(1));
		assertEquals(18, ThreatTactics.callRadius(5));
		assertEquals(18, ThreatTactics.callRadius(99));
		assertEquals(0, ThreatTactics.callRadius(-3));
	}

	@Test void dodgeStartsAtRingTwoAndIsCapped() {
		assertEquals(0, ThreatTactics.dodgeChance(1, 0.06));
		assertEquals(0.06, ThreatTactics.dodgeChance(2, 0.06), 1e-9);
		assertEquals(0.24, ThreatTactics.dodgeChance(5, 0.06), 1e-9);
		assertEquals(ThreatTactics.MAX_DODGE, ThreatTactics.dodgeChance(5, 0.15), 1e-9);
		assertEquals(0, ThreatTactics.dodgeChance(5, Double.NaN));
		assertEquals(0, ThreatTactics.dodgeChance(5, -1));
	}

	@Test void enrageHappensOnceFromRingThreeBelowThirtyFivePercent() {
		assertFalse(ThreatTactics.enrages(2, 0.2f, false));
		assertTrue(ThreatTactics.enrages(3, 0.35f, false));
		assertFalse(ThreatTactics.enrages(3, 0.36f, false));
		assertFalse(ThreatTactics.enrages(5, 0.1f, true));
		assertFalse(ThreatTactics.enrages(5, 0f, false));
		assertFalse(ThreatTactics.enrages(5, Float.NaN, false));
	}

	@Test void enrageScalesWithTheRing() {
		assertEquals(100, ThreatTactics.enrageTicks(3));
		assertEquals(140, ThreatTactics.enrageTicks(4));
		assertEquals(180, ThreatTactics.enrageTicks(5));
		assertEquals(0, ThreatTactics.enrageAmplifier(4));
		assertEquals(1, ThreatTactics.enrageAmplifier(5));
	}

	@Test void knockbackResistanceIsCapped() {
		assertEquals(0.08, ThreatTactics.knockbackResistance(1), 1e-9);
		assertEquals(0.4, ThreatTactics.knockbackResistance(5), 1e-9);
		assertEquals(0.4, ThreatTactics.knockbackResistance(50), 1e-9);
	}
}
