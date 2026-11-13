package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F3): cifras de las habilidades nuevas del Herbolista, tal como las describen los textos. */
class HerbalistRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void springBoostsAllHealing() {
		assertEquals(1.0, HerbalistRules.healMultiplier(0), EPS);
		assertEquals(1.1, HerbalistRules.healMultiplier(1), EPS);
		assertEquals(1.3, HerbalistRules.healMultiplier(3), EPS);
		assertEquals(1.0, HerbalistRules.healMultiplier(-2), EPS);
	}

	@Test
	void pollenAbsorptionAndSeedDamage() {
		assertEquals(2f, HerbalistRules.pollenAbsorption(1));
		assertEquals(4f, HerbalistRules.pollenAbsorption(3));
		assertEquals(0f, HerbalistRules.pollenAbsorption(0));
		assertEquals(5.0, HerbalistRules.seedDamage(1, 1), EPS);
		assertEquals(6.5, HerbalistRules.seedDamage(2, 1), EPS);
		assertEquals(8.0, HerbalistRules.seedDamage(3, 1), EPS);
		assertEquals(4.0, HerbalistRules.seedDamage(3, 0.5), EPS);
	}

	@Test
	void rootZoneLastsTenToFourteenSeconds() {
		assertEquals(200, HerbalistRules.rootTicks(1));
		assertEquals(240, HerbalistRules.rootTicks(2));
		assertEquals(280, HerbalistRules.rootTicks(3));
	}

	@Test
	void livingBarkTriggersOnceBelowThirtyFivePercent() {
		assertEquals(1, HerbalistRules.barkAmplifier(1));
		assertEquals(2, HerbalistRules.barkAmplifier(2));
		assertEquals(2, HerbalistRules.barkAmplifier(5), "no pasa de Regeneración III");
		assertTrue(HerbalistRules.barkTriggers(6.9, 20, 100, 0));
		assertFalse(HerbalistRules.barkTriggers(7, 20, 100, 0));
		assertFalse(HerbalistRules.barkTriggers(1, 20, 100, 200), "todavía en espera");
		assertFalse(HerbalistRules.barkTriggers(1, 0, 100, 0));
	}

	@Test
	void locustsAndColossalBloom() {
		assertEquals(2.0, HerbalistRules.locustDamage(1, 1), EPS);
		assertEquals(3.0, HerbalistRules.locustDamage(2, 1), EPS);
		assertEquals(0.4, HerbalistRules.bloomFraction(1), EPS);
		assertEquals(0.6, HerbalistRules.bloomFraction(2), EPS);
		assertEquals(0, HerbalistRules.bloomFraction(0), EPS);
	}

	@Test
	void deepRootsReduceDamageAndReturnThorns() {
		assertEquals(0.1, HerbalistRules.rootedReduction(1), EPS);
		assertEquals(0.2, HerbalistRules.rootedReduction(2), EPS);
		assertEquals(1f, HerbalistRules.rootedThorns(1));
		assertEquals(2f, HerbalistRules.rootedThorns(2));
		assertEquals(0f, HerbalistRules.rootedThorns(-1));
	}

	@Test
	void livingSeedRainCleansesEveryThirdSecond() {
		int cleanses = 0;
		for (int second = 0; second < HerbalistRules.RAIN_SECONDS; second++) {
			if (HerbalistRules.rainCleanses(second)) cleanses++;
		}
		assertEquals(4, cleanses);
		assertFalse(HerbalistRules.rainCleanses(0));
		assertTrue(HerbalistRules.rainCleanses(2));
	}
}
