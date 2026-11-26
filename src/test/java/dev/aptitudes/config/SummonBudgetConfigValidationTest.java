package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummonBudgetConfigValidationTest {
	@Test
	void defaultIsTheApprovedBudget() {
		assertEquals(16, AptitudesConfig.validated(new AptitudesConfig()).summonBudget);
	}

	@Test
	void valuesAreClampedAndZeroMeansNoLimit() {
		AptitudesConfig c = new AptitudesConfig();
		c.summonBudget = 0;
		assertEquals(0, AptitudesConfig.validated(c).summonBudget);
		c = new AptitudesConfig();
		c.summonBudget = Integer.MAX_VALUE;
		assertEquals(64, AptitudesConfig.validated(c).summonBudget);
		c = new AptitudesConfig();
		c.summonBudget = Integer.MIN_VALUE; // negativo: vuelve al valor por defecto, como el resto de opciones
		assertEquals(16, AptitudesConfig.validated(c).summonBudget);
	}

	@Test
	void oldConfigurationGetsTheDefault() {
		AptitudesConfig c = AptitudesConfig.validated(new com.google.gson.Gson().fromJson("{\"fogCooldownDays\":7}", AptitudesConfig.class));
		assertEquals(16, c.summonBudget);
		assertEquals(7, c.fogCooldownDays);
	}
}
