package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** C7 · S6: opciones de los ecos de muerte; desactivados por defecto. */
class DeathEchoConfigValidationTest {
	@Test void defaultsAreSafe() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertFalse(c.deathEchoesEnabled, "genera entidades: se activa a propósito");
		assertEquals(120, c.deathEchoLifetimeSeconds);
		assertEquals(6, c.deathEchoMaxActive);
	}

	@Test void limitsAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.deathEchoLifetimeSeconds = Integer.MAX_VALUE;
		c.deathEchoMaxActive = Integer.MAX_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(1800, c.deathEchoLifetimeSeconds);
		assertEquals(50, c.deathEchoMaxActive);
		c = new AptitudesConfig();
		c.deathEchoLifetimeSeconds = 0;
		c.deathEchoMaxActive = 0;
		c = AptitudesConfig.validated(c);
		assertEquals(10, c.deathEchoLifetimeSeconds);
		assertEquals(1, c.deathEchoMaxActive);
	}

	@Test void negativeValuesFallBackToTheDefaults() {
		AptitudesConfig c = new AptitudesConfig();
		c.deathEchoLifetimeSeconds = -1;
		c.deathEchoMaxActive = -1;
		c = AptitudesConfig.validated(c);
		assertEquals(120, c.deathEchoLifetimeSeconds);
		assertEquals(6, c.deathEchoMaxActive);
	}
}
