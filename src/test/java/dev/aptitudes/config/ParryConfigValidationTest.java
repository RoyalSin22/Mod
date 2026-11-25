package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** C7 (S3): opciones de la parada y el golpe de gracia (los límites coinciden con docs/CONFIGURATION.md). */
class ParryConfigValidationTest {
	@Test void defaultsMatchTheApprovedDesign() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.combatParryEnabled);
		assertEquals(30, c.parryStunTicks);
		assertEquals(1.5, c.parryGraceMultiplier, 1e-9);
		assertEquals(40, c.parryGraceTicks);
	}

	@Test void oldConfigurationGetsSafeDefaults() {
		AptitudesConfig c = new com.google.gson.Gson().fromJson("{\"fogCooldownDays\":3}", AptitudesConfig.class);
		c = AptitudesConfig.validated(c);
		assertTrue(c.combatParryEnabled);
		assertEquals(30, c.parryStunTicks);
		assertEquals(1.5, c.parryGraceMultiplier, 1e-9);
		assertEquals(40, c.parryGraceTicks);
	}

	@Test void valuesAreClampedToTheirRange() {
		AptitudesConfig c = new AptitudesConfig();
		c.parryStunTicks = Integer.MAX_VALUE;
		c.parryGraceTicks = Integer.MAX_VALUE;
		c.parryGraceMultiplier = 1000;
		c = AptitudesConfig.validated(c);
		assertEquals(200, c.parryStunTicks);
		assertEquals(200, c.parryGraceTicks);
		assertEquals(3.0, c.parryGraceMultiplier, 1e-9);

		c = new AptitudesConfig();
		c.parryStunTicks = 0;
		c.parryGraceTicks = 0;
		c.parryGraceMultiplier = 0.2;
		c = AptitudesConfig.validated(c);
		assertEquals(0, c.parryStunTicks);
		assertEquals(0, c.parryGraceTicks);
		assertEquals(1.0, c.parryGraceMultiplier, 1e-9, "el golpe de gracia nunca reduce el daño");
	}
}
