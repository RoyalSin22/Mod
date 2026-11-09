package dev.aptitudes.mob;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OsarioRulesTest {
	@ParameterizedTest
	@CsvSource({"0, true", "9, true", "10, false", "99, false", "-1, false"})
	void tenPercentOfFogSpawnsAreOsarios(int roll, boolean expected) {
		assertEquals(expected, OsarioRules.spawnsOsario(roll));
	}
}
