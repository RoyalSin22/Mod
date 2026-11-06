package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlchemyConfigValidationTest {
	@Test
	void defaultsKeepThePromisedFiftyPercentBonuses() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertTrue(c.alchemyHerbalistEnabled);
		assertEquals(8, c.alchemyHerbalistRadius);
		assertEquals(0.5, c.alchemyBrewingSpeedBonus);
		assertEquals(0.5, c.alchemyDurationBonus);
		assertEquals(4, c.alchemyRoleXpPerBrew);
		assertEquals(300, c.alchemyXpWindowSeconds);
		assertEquals(3, c.alchemyXpFullRewardBrews);
	}

	@Test
	void clampsAllNewNumericFields() {
		AptitudesConfig c = new AptitudesConfig();
		c.alchemyHerbalistRadius = Integer.MAX_VALUE;
		c.alchemyBrewingSpeedBonus = 100;
		c.alchemyDurationBonus = Double.MAX_VALUE;
		c.alchemyRoleXpPerBrew = Integer.MAX_VALUE;
		c.alchemyXpWindowSeconds = Integer.MAX_VALUE;
		c.alchemyXpFullRewardBrews = 0;
		AptitudesConfig.validated(c);
		assertEquals(16, c.alchemyHerbalistRadius);
		assertEquals(1, c.alchemyBrewingSpeedBonus);
		assertEquals(1, c.alchemyDurationBonus);
		assertEquals(20, c.alchemyRoleXpPerBrew);
		assertEquals(3600, c.alchemyXpWindowSeconds);
		assertEquals(1, c.alchemyXpFullRewardBrews);
	}

	@Test
	void rejectsNegativeAndNonFiniteNumbersAndAllowsDisablingBonuses() {
		AptitudesConfig c = new AptitudesConfig();
		c.alchemyBrewingSpeedBonus = Double.NaN;
		c.alchemyDurationBonus = Double.POSITIVE_INFINITY;
		c.alchemyRoleXpPerBrew = -1;
		c.alchemyXpWindowSeconds = 0;
		AptitudesConfig.validated(c);
		assertEquals(0.5, c.alchemyBrewingSpeedBonus);
		assertEquals(0.5, c.alchemyDurationBonus);
		assertEquals(4, c.alchemyRoleXpPerBrew);
		assertEquals(10, c.alchemyXpWindowSeconds);
		c.alchemyHerbalistRadius = 0;
		c.alchemyBrewingSpeedBonus = 0;
		c.alchemyDurationBonus = 0;
		c.alchemyRoleXpPerBrew = 0;
		AptitudesConfig.validated(c);
		assertEquals(0, c.alchemyHerbalistRadius);
		assertEquals(0, c.alchemyBrewingSpeedBonus);
		assertEquals(0, c.alchemyDurationBonus);
		assertEquals(0, c.alchemyRoleXpPerBrew);
	}
}
