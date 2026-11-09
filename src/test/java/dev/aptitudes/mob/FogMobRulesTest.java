package dev.aptitudes.mob;

import dev.aptitudes.mob.FogMobRules.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FogMobRulesTest {
	@ParameterizedTest
	@CsvSource({"0, OSARIO", "9, OSARIO", "10, ERRANTE", "19, ERRANTE", "20, VANILLA", "99, VANILLA"})
	void fogVariantsFollowTheirBands(int roll, Variant expected) {
		assertEquals(expected, FogMobRules.choose(roll));
	}
}
