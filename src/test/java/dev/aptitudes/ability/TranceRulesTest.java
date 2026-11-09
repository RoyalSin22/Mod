package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TranceRulesTest {
	@ParameterizedTest
	@CsvSource({
			"0,   10,  10",
			"90,  15,  100",  // no pasa del máximo
			"100, 10,  100",
			"-5,  10,  10",   // nunca negativa
			"20,  -7,  20",   // nunca resta
	})
	void gainStaysBetweenZeroAndMax(int gauge, int amount, int expected) {
		assertEquals(expected, TranceRules.gain(gauge, amount));
	}

	@Test void fullOnlyAtTheMaximum() {
		assertFalse(TranceRules.full(TranceRules.MAX_GAUGE - 1));
		assertTrue(TranceRules.full(TranceRules.MAX_GAUGE));
	}

	@Test void tranceLastsTenSecondsAndIsNotActiveBeforeStart() {
		assertFalse(TranceRules.active(TranceRules.NONE, 0));
		assertTrue(TranceRules.active(100, 100));
		assertTrue(TranceRules.active(100, 100 + TranceRules.DURATION_TICKS - 1));
		assertFalse(TranceRules.active(100, 100 + TranceRules.DURATION_TICKS));
		assertEquals(200, TranceRules.DURATION_TICKS);
	}

	@Test void criticalsGiveLessThanSkills() {
		assertTrue(TranceRules.GAIN_CRITICAL < TranceRules.GAIN_SKILL);
	}
}
