package dev.aptitudes.boss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummonRulesTest {
	private static final long DEFEAT = 1_000L;

	@Test void neverDefeatedCanBeSummonedWhenNotAlive() {
		assertTrue(SummonRules.canSummon(false, SummonRules.NEVER, 0));
		assertEquals(0, SummonRules.remainingTicks(SummonRules.NEVER, 0));
	}

	@Test void aliveBossCannotBeSummonedAgain() {
		assertFalse(SummonRules.canSummon(true, SummonRules.NEVER, 0));
		assertFalse(SummonRules.canSummon(true, DEFEAT, DEFEAT + SummonRules.COOLDOWN_TICKS * 10));
	}

	@ParameterizedTest
	@CsvSource({
			"0,      false",   // justo tras la derrota
			"23999,  false",   // a un tick de la espera
			"24000,  true",    // se cumple la espera
			"100000, true",    // mucho después
	})
	void cooldownIsCountedFromTheDefeat(long elapsed, boolean expected) {
		assertEquals(expected, SummonRules.canSummon(false, DEFEAT, DEFEAT + elapsed));
	}

	@Test void remainingTicksCountsDownAndStopsAtZero() {
		assertEquals(SummonRules.COOLDOWN_TICKS, SummonRules.remainingTicks(DEFEAT, DEFEAT));
		assertEquals(1, SummonRules.remainingTicks(DEFEAT, DEFEAT + SummonRules.COOLDOWN_TICKS - 1));
		assertEquals(0, SummonRules.remainingTicks(DEFEAT, DEFEAT + SummonRules.COOLDOWN_TICKS));
	}

	@Test void timeGoingBackwardsDoesNotExtendOrBreakTheCooldown() {
		// Un reloj que retrocede (datos antiguos) no debe dejar al jefe bloqueado más de la espera completa.
		assertEquals(SummonRules.COOLDOWN_TICKS, SummonRules.remainingTicks(DEFEAT, DEFEAT - 500));
	}
}
