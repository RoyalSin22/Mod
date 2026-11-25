package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** C7 · S5: opciones de los mensajes en el suelo con valor por defecto seguro y límites. */
class GroundMessageConfigValidationTest {
	@Test void defaults() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.groundMessagesEnabled);
		assertEquals(3, c.groundMessagesPerPlayer);
		assertEquals(7, c.groundMessageLifetimeDays);
		assertEquals(30, c.groundMessageCooldownSeconds);
	}

	@Test void limitsAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.groundMessagesPerPlayer = Integer.MAX_VALUE;
		c.groundMessageLifetimeDays = Integer.MAX_VALUE;
		c.groundMessageCooldownSeconds = Integer.MAX_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(20, c.groundMessagesPerPlayer);
		assertEquals(365, c.groundMessageLifetimeDays);
		assertEquals(3600, c.groundMessageCooldownSeconds);
		c = new AptitudesConfig();
		c.groundMessagesPerPlayer = 0;
		c.groundMessageLifetimeDays = 0;
		c.groundMessageCooldownSeconds = 0;
		c = AptitudesConfig.validated(c);
		assertEquals(1, c.groundMessagesPerPlayer);
		assertEquals(1, c.groundMessageLifetimeDays);
		assertEquals(1, c.groundMessageCooldownSeconds);
	}

	@Test void negativeValuesFallBackToTheDefaults() {
		AptitudesConfig c = new AptitudesConfig();
		c.groundMessagesPerPlayer = -1;
		c.groundMessageLifetimeDays = -1;
		c.groundMessageCooldownSeconds = -1;
		c = AptitudesConfig.validated(c);
		assertEquals(3, c.groundMessagesPerPlayer);
		assertEquals(7, c.groundMessageLifetimeDays);
		assertEquals(30, c.groundMessageCooldownSeconds);
	}
}
