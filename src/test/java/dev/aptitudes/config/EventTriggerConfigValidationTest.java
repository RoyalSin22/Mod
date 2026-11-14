package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTriggerConfigValidationTest {
	@Test void defaultsMatchTheApprovedDesign() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertEquals(10, c.fogCooldownDays);
		assertEquals(24_000, c.fogDurationTicks);
		assertEquals(5, c.emberRainCooldownDays);
	}

	@Test void oldConfigurationKeepsItsValuesAndGetsSafeDefaults() {
		AptitudesConfig c = new com.google.gson.Gson().fromJson("{\"worldEventWeightSoulFog\":0,\"eclipseIntervalDays\":7}", AptitudesConfig.class);
		c = AptitudesConfig.validated(c);
		assertEquals(0, c.worldEventWeightSoulFog);
		assertEquals(7, c.eclipseIntervalDays);
		assertEquals(10, c.fogCooldownDays);
		assertEquals(24_000, c.fogDurationTicks);
		assertEquals(5, c.emberRainCooldownDays);
	}

	@Test void valuesAboveTheLimitAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.fogCooldownDays = c.emberRainCooldownDays = c.fogDurationTicks = Integer.MAX_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(365, c.fogCooldownDays);
		assertEquals(365, c.emberRainCooldownDays);
		assertEquals(240_000, c.fogDurationTicks);
	}

	@Test void zeroIsClampedToTheMinimum() {
		AptitudesConfig c = new AptitudesConfig();
		c.fogCooldownDays = c.emberRainCooldownDays = c.fogDurationTicks = 0;
		c = AptitudesConfig.validated(c);
		assertEquals(1, c.fogCooldownDays);
		assertEquals(1, c.emberRainCooldownDays);
		assertEquals(1_200, c.fogDurationTicks);
	}

	/** Regla general del proyecto: un número negativo vuelve al valor por defecto (y se avisa en el log). */
	@Test void negativeValuesFallBackToTheDefaults() {
		AptitudesConfig c = new AptitudesConfig();
		c.fogCooldownDays = c.emberRainCooldownDays = c.fogDurationTicks = Integer.MIN_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(10, c.fogCooldownDays);
		assertEquals(5, c.emberRainCooldownDays);
		assertEquals(24_000, c.fogDurationTicks);
	}
}
