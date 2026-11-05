package dev.aptitudes.alchemy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlchemyRulesTest {
	@Test
	void restoresOnlyTheMissingEnergy() {
		assertEquals(54, AlchemyRules.restoredEnergy(50, 100, 4));
		assertEquals(100, AlchemyRules.restoredEnergy(98, 100, 4));
		assertEquals(100, AlchemyRules.restoredEnergy(100, 100, 4));
	}

	@Test
	void repeatedDosesNeverExceedTheCurrentMaximum() {
		double energy = 0;
		for (int i = 0; i < 1000; i++) energy = AlchemyRules.restoredEnergy(energy, 120, 4);
		assertEquals(120, energy);
		assertEquals(80, AlchemyRules.restoredEnergy(energy, 80, 4));
	}

	@Test
	void hugeFiniteValuesDoNotOverflow() {
		assertEquals(Double.MAX_VALUE, AlchemyRules.restoredEnergy(Double.MAX_VALUE / 2,
				Double.MAX_VALUE, Double.MAX_VALUE));
	}

	@Test
	void corruptEnergyAndNegativeBalancesAreSanitized() {
		for (double current : new double[]{-100, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
			assertEquals(4, AlchemyRules.restoredEnergy(current, 100, 4));
		}
	}

	@Test
	void invalidRecoveryCannotDrainOrAddEnergy() {
		for (double requested : new double[]{-1, 0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
			assertEquals(50, AlchemyRules.restoredEnergy(50, 100, requested));
		}
	}

	@Test
	void invalidMaximumNeverPropagatesNonFiniteNumbers() {
		for (double maximum : new double[]{-1, 0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
			assertEquals(0, AlchemyRules.restoredEnergy(50, maximum, 4));
		}
	}

	@Test
	void veilNeedsBothProtectionAndAnEventCreature() {
		assertEquals(8, AlchemyRules.eventDamage(10, true, true));
		assertEquals(10, AlchemyRules.eventDamage(10, false, true));
		assertEquals(10, AlchemyRules.eventDamage(10, true, false));
		assertEquals(10, AlchemyRules.eventDamage(10, false, false));
	}

	@Test
	void veilAlwaysReducesWithoutCrossingZero() {
		for (float damage : new float[]{Float.MIN_VALUE, 0.1f, 1, 1000, Float.MAX_VALUE}) {
			float reduced = AlchemyRules.eventDamage(damage, true, true);
			assertTrue(Float.isFinite(reduced));
			assertTrue(reduced >= 0 && reduced <= damage);
		}
	}

	@Test
	void veilDoesNotTurnInvalidOrNonPositiveDamageIntoAHit() {
		for (float damage : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
			assertEquals(damage, AlchemyRules.eventDamage(damage, true, true));
		}
	}
}
