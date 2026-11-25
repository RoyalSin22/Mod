package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** C7 · S3: opciones de la parada con valor por defecto seguro y límites. */
class ParryConfigValidationTest {
	@Test void defaults() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.parryEnabled);
		assertEquals(40, c.parryStunTicks);
		assertEquals(60, c.parryFinisherTicks);
		assertEquals(1.5, c.parryFinisherMultiplier, 1e-9);
	}

	@Test void upperBoundsAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.parryStunTicks = Integer.MAX_VALUE;
		c.parryFinisherTicks = Integer.MAX_VALUE;
		c.parryFinisherMultiplier = 1e9;
		c = AptitudesConfig.validated(c);
		assertEquals(200, c.parryStunTicks);
		assertEquals(200, c.parryFinisherTicks);
		assertEquals(4.0, c.parryFinisherMultiplier, 1e-9);
	}

	@Test void lowerBoundsAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.parryStunTicks = 0; // 0 = sin aturdimiento: es válido
		c.parryFinisherTicks = 1;
		c.parryFinisherMultiplier = 0.1;
		c = AptitudesConfig.validated(c);
		assertEquals(0, c.parryStunTicks);
		assertEquals(10, c.parryFinisherTicks);
		assertEquals(1.0, c.parryFinisherMultiplier, 1e-9);
	}

	@Test void negativeAndNanFallBackToTheDefaults() {
		AptitudesConfig c = new AptitudesConfig();
		c.parryStunTicks = -5;
		c.parryFinisherTicks = -5;
		c.parryFinisherMultiplier = Double.NaN;
		c = AptitudesConfig.validated(c);
		assertEquals(40, c.parryStunTicks);
		assertEquals(60, c.parryFinisherTicks);
		assertEquals(1.5, c.parryFinisherMultiplier, 1e-9);
	}
}
