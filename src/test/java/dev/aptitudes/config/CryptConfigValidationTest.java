package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** V69: opciones de la Cripta de la Niebla y del Gran Osario (los límites coinciden con docs/CONFIGURATION.md). */
class CryptConfigValidationTest {
	@Test void defaultsMatchTheApprovedDesign() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.cryptEnabled);
		assertEquals(30, c.cryptIdleResetSeconds);
		assertEquals(100, c.cryptWaveSizePercent);
	}

	@Test void oldConfigurationGetsSafeDefaults() {
		AptitudesConfig c = new com.google.gson.Gson().fromJson("{\"fogCooldownDays\":3}", AptitudesConfig.class);
		c = AptitudesConfig.validated(c);
		assertEquals(3, c.fogCooldownDays);
		assertTrue(c.cryptEnabled);
		assertEquals(30, c.cryptIdleResetSeconds);
		assertEquals(100, c.cryptWaveSizePercent);
	}

	@Test void valuesAreClampedToTheirRange() {
		AptitudesConfig c = new AptitudesConfig();
		c.cryptIdleResetSeconds = Integer.MAX_VALUE;
		c.cryptWaveSizePercent = Integer.MAX_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(300, c.cryptIdleResetSeconds);
		assertEquals(300, c.cryptWaveSizePercent);

		c = new AptitudesConfig();
		c.cryptIdleResetSeconds = 0;
		c.cryptWaveSizePercent = 0;
		c = AptitudesConfig.validated(c);
		assertEquals(5, c.cryptIdleResetSeconds);
		assertEquals(25, c.cryptWaveSizePercent);
	}

	@Test void negativeValuesFallBackToTheDefaults() {
		AptitudesConfig c = new AptitudesConfig();
		c.cryptIdleResetSeconds = -50;
		c.cryptWaveSizePercent = -1;
		c = AptitudesConfig.validated(c);
		assertEquals(30, c.cryptIdleResetSeconds);
		assertEquals(100, c.cryptWaveSizePercent);
	}
}
