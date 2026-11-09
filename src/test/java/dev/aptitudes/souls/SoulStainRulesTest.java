package dev.aptitudes.souls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoulStainRulesTest {
	@ParameterizedTest
	@CsvSource({
			"0,      0",
			"-50,    0",
			"4,      3",     // 25 % de 4 = 1
			"100,    75",
			"1000,   750",
			"999,    750",   // 25 % de 999 son 249,99; la división entera lo redondea a 249
	})
	void dyingAgainLosesAQuarterOfTheStain(long stain, long expected) {
		assertEquals(expected, SoulStainRules.afterDeathWhileStained(stain));
	}

	@Test void hugeValuesDoNotOverflow() {
		long huge = Long.MAX_VALUE / 2;
		long result = SoulStainRules.afterDeathWhileStained(huge);
		assertEquals(true, result > 0 && result < huge);
	}
}
