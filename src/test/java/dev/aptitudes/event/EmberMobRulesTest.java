package dev.aptitudes.event;

import dev.aptitudes.event.EmberMobRules.EmberMob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmberMobRulesTest {
	@Test void weightsSumToOneHundred() {
		int total = 0;
		for (EmberMob mob : EmberMob.values()) total += EmberMobRules.weight(mob);
		assertEquals(100, total);
	}

	@Test void everyNetherCreatureCanBeChosen() {
		for (EmberMob mob : EmberMob.values()) {
			boolean found = false;
			for (int roll = 0; roll < 100 && !found; roll++) found = EmberMobRules.choose(roll) == mob;
			assertEquals(true, found, mob.name());
		}
	}

	@ParameterizedTest
	@CsvSource({"0, ZOMBIFIED_PIGLIN", "24, ZOMBIFIED_PIGLIN", "25, HOGLIN", "-5, ZOMBIFIED_PIGLIN", "999, WITHER_SKELETON"})
	void rollsMapToTheirBand(int roll, EmberMob expected) {
		assertEquals(expected, EmberMobRules.choose(roll));
	}
}
