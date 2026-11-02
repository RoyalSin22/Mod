package dev.aptitudes.event;

import dev.aptitudes.event.EclipseRules.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EclipseRulesTest {
	@Test
	void calendarEveryTenDaysFromDayTen() {
		assertFalse(EclipseRules.isEclipseDay(0, 10, 10));
		assertFalse(EclipseRules.isEclipseDay(9, 10, 10));
		assertTrue(EclipseRules.isEclipseDay(10, 10, 10));
		assertTrue(EclipseRules.isEclipseDay(20, 10, 10));
		assertFalse(EclipseRules.isEclipseDay(25, 10, 10));
		// Intervalo inválido: se trata como 1 (no divide entre cero).
		assertTrue(EclipseRules.isEclipseDay(12, 0, 10));
	}

	@Test
	void daysUntilNext() {
		assertEquals(10, EclipseRules.daysUntilNext(0, 0, 10, 10));
		assertEquals(0, EclipseRules.daysUntilNext(10, 500, 10, 10));
		assertEquals(10, EclipseRules.daysUntilNext(10, 12_000, 10, 10));
		assertEquals(5, EclipseRules.daysUntilNext(15, 0, 10, 10));
	}

	@Test
	void dayAndTimeFromTotalTime() {
		assertEquals(0, EclipseRules.dayOf(-5));
		assertEquals(10, EclipseRules.dayOf(240_000 + 123));
		assertEquals(123, EclipseRules.timeInDay(240_000 + 123));
	}

	@Test
	void phaseBoundaries() {
		assertEquals(Phase.PENUMBRA, EclipseRules.phase(0));
		assertEquals(Phase.PENUMBRA, EclipseRules.phase(2_999));
		assertEquals(Phase.TOTALITY, EclipseRules.phase(3_000));
		assertEquals(Phase.TOTALITY, EclipseRules.phase(6_000));
		assertEquals(Phase.TOTALITY, EclipseRules.phase(8_999));
		assertEquals(Phase.RETREAT, EclipseRules.phase(9_000));
		assertEquals(Phase.RETREAT, EclipseRules.phase(11_999));
		assertEquals(Phase.NONE, EclipseRules.phase(12_000));
		assertEquals(Phase.NONE, EclipseRules.phase(-1));
	}

	@Test
	void intensityStaysInUnitRange() {
		for (long t = -100; t <= 12_100; t += 37) {
			float i = EclipseRules.intensity(t);
			assertTrue(i >= 0f && i <= 1f, "t=" + t + " → " + i);
		}
		assertEquals(0f, EclipseRules.intensity(0));
		assertEquals(1f, EclipseRules.intensity(6_000));
		assertEquals(0.5f, EclipseRules.intensity(10_500), 1e-6);
	}

	@Test
	void spawnsNeverExceedCap() {
		assertEquals(4, EclipseRules.allowedSpawns(0, 12, EclipseRules.waveSize(Phase.TOTALITY)));
		assertEquals(2, EclipseRules.allowedSpawns(10, 12, 4));
		assertEquals(0, EclipseRules.allowedSpawns(15, 12, 4));
		assertEquals(0, EclipseRules.allowedSpawns(0, 0, 4));
		assertEquals(0, EclipseRules.allowedSpawns(Integer.MIN_VALUE, Integer.MAX_VALUE, -3));
		assertEquals(0, EclipseRules.waveSize(Phase.RETREAT));
	}

	@Test
	void heraldScalingIsCapped() {
		assertEquals(1.0, EclipseRules.heraldHealthMultiplier(1));
		assertEquals(1.0, EclipseRules.heraldHealthMultiplier(-4));
		assertEquals(2.0, EclipseRules.heraldHealthMultiplier(3));
		assertEquals(4.0, EclipseRules.heraldHealthMultiplier(Integer.MAX_VALUE));
	}

	@Test
	void fragmentsAndBounds() {
		assertEquals(8, EclipseRules.fragments(true, true, true, 0.9));
		assertEquals(3, EclipseRules.fragments(false, true, true, 0.9));
		assertEquals(1, EclipseRules.fragments(false, false, true, 0.9));
		assertEquals(1, EclipseRules.fragments(false, false, false, 0.05));
		assertEquals(0, EclipseRules.fragments(false, false, false, 0.5));
		assertEquals(1.0, EclipseRules.boundedMultiplier(Double.NaN, 1.0, 5.0));
		assertEquals(5.0, EclipseRules.boundedMultiplier(99, 1.0, 5.0));
		assertEquals(200L, EclipseRules.waveIntervalTicks(1));
		assertEquals(6_000L, EclipseRules.waveIntervalTicks(10_000));
	}
}
