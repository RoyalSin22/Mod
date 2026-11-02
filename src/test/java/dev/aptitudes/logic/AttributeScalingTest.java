package dev.aptitudes.logic;

import org.junit.Test;

import static dev.aptitudes.logic.AttributeScaling.Mode.LINEAR;
import static dev.aptitudes.logic.AttributeScaling.Mode.SATURATION;
import static dev.aptitudes.logic.AttributeScaling.Mode.SOFTCAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AttributeScalingTest {
	private static final double EPS = 1e-9;

	private static double e(AttributeScaling.Mode mode, int points) {
		return AttributeScaling.effectivePoints(mode, points, 40, 60, 50.0);
	}

	@Test
	public void linearIsTheOriginalBehaviour() {
		for (int p = 0; p <= 200; p++) assertEquals(p, e(LINEAR, p), EPS);
		assertEquals(0, e(LINEAR, -5), EPS); // negativos se tratan como 0
	}

	@Test
	public void softcapIsIdenticalUpToTheFirstThreshold() {
		for (int p = 0; p <= 40; p++) assertEquals(p, e(SOFTCAP, p), EPS);
		assertEquals(45, e(SOFTCAP, 50), EPS);           // 40 + 0,5·10
		assertEquals(50, e(SOFTCAP, 60), EPS);           // 40 + 0,5·20
		assertEquals(57.8, e(SOFTCAP, 99), EPS);         // 50 + 0,2·39
	}

	@Test
	public void saturationStartsLikeLinearAndNeverExceedsTheLimit() {
		assertEquals(0, e(SATURATION, 0), EPS);
		assertEquals(1.0, AttributeScaling.marginal(SATURATION, 0, 40, 60, 50.0), 0.02); // pendiente inicial ≈ 1
		assertEquals(50 * (1 - Math.exp(-99 / 50.0)), e(SATURATION, 99), EPS);
		for (int p = 0; p <= 10_000; p += 37) assertTrue(e(SATURATION, p) <= 50.0); // asíntota (en double llega a 50,0)
	}

	@Test
	public void everyModeIsMonotonicAndNeverAboveLinear() {
		for (AttributeScaling.Mode mode : AttributeScaling.Mode.values()) {
			double previous = -1;
			for (int p = 0; p <= 300; p++) {
				double v = e(mode, p);
				assertTrue(mode + " no decreciente en " + p, v >= previous);
				assertTrue(mode + " nunca supera el lineal", v <= p + EPS);
				assertTrue(AttributeScaling.marginal(mode, p, 40, 60, 50.0) >= 0);
				previous = v;
			}
		}
	}

	@Test
	public void parseAcceptsAnyCaseAndRejectsUnknown() {
		assertEquals(SOFTCAP, AttributeScaling.Mode.parse(" softcap "));
		assertEquals(LINEAR, AttributeScaling.Mode.parse("LINEAR"));
		assertNull(AttributeScaling.Mode.parse("cuadrática"));
		assertNull(AttributeScaling.Mode.parse(null));
	}

	@Test
	public void badThresholdsDoNotBreak() {
		// S2 menor que S1 se trata como S1; L < 1 se trata como 1.
		assertEquals(10, AttributeScaling.effectivePoints(SOFTCAP, 10, 40, 5, 50), EPS);
		assertTrue(AttributeScaling.effectivePoints(SATURATION, 10, 40, 60, 0) <= 1.0);
	}
}
