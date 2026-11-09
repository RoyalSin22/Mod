package dev.aptitudes.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ItemStatRulesTest {
	@Test public void formatsWithOneDecimalAtMost() {
		assertEquals("7", ItemStatRules.fmt(7.0));
		assertEquals("1.6", ItemStatRules.fmt(1.6));
		assertEquals("1.7", ItemStatRules.fmt(1.66));
		assertEquals("-2.4", ItemStatRules.fmt(-2.4));
		assertEquals("0", ItemStatRules.fmt(Double.NaN));
		assertEquals("0", ItemStatRules.fmt(Double.POSITIVE_INFINITY));
	}

	@Test public void ticksAreShownInSeconds() {
		assertEquals("0.7", ItemStatRules.seconds(14));
		assertEquals("1", ItemStatRules.seconds(20));
		assertEquals("0", ItemStatRules.seconds(-5));
	}

	@Test public void dpsUsesThePlayerBase() {
		// Espada de diamante de vanilla: +6 de daño y −2,4 de velocidad → 7 × 1,6 = 11,2.
		assertEquals(11.2, ItemStatRules.dps(6, -2.4), 1e-9);
		// Hacha del Bersérker: hierro (+2) + 7 = +9 de daño (10 en total) y −3 → 10 × 1 = 10.
		assertEquals(10.0, ItemStatRules.dps(9, -3.0), 1e-9);
		// Nunca negativo.
		assertEquals(0.0, ItemStatRules.dps(-5, -9), 1e-9);
	}
}
