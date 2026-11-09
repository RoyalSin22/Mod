package dev.aptitudes.event;

import dev.aptitudes.event.ExpeditionRules.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpeditionRulesTest {
	@ParameterizedTest
	@CsvSource({"0, 3", "1, 4", "2, 5", "-3, 3", "9, 5"})
	void difficultySetsTheLengthWithinThreeToFive(int difficulty, int expectedLength) {
		assertEquals(expectedLength, ExpeditionRules.compose(42L, difficulty).size());
	}

	@Test void expeditionAlwaysStartsWithCombatAndEndsWithTheBoss() {
		for (long seed = 0; seed < 50; seed++) {
			List<Module> modules = ExpeditionRules.compose(seed, 2);
			assertEquals(Module.COMBAT, modules.get(0));
			assertEquals(Module.BOSS, modules.get(modules.size() - 1));
		}
	}

	@Test void middleModulesNeverIncludeCombatOrBossAgain() {
		for (long seed = 0; seed < 50; seed++) {
			List<Module> middle = ExpeditionRules.compose(seed, 2).subList(1, 4);
			assertFalse(middle.contains(Module.COMBAT));
			assertFalse(middle.contains(Module.BOSS));
		}
	}

	@Test void sameSeedGivesTheSameExpeditionForEveryPlayer() {
		assertEquals(ExpeditionRules.compose(7L, 2), ExpeditionRules.compose(7L, 2));
		assertEquals(ExpeditionRules.modifiers(7L, 2), ExpeditionRules.modifiers(7L, 2));
	}

	@ParameterizedTest
	@CsvSource({"1", "2", "3", "9", "0"})
	void modifiersAreDistinctAndFromTheCatalog(int count) {
		List<String> picked = ExpeditionRules.modifiers(99L, count);
		assertEquals(Math.max(1, Math.min(3, count)), picked.size());
		assertEquals(picked.size(), picked.stream().distinct().count());
		assertTrue(ExpeditionRules.MODIFIERS.containsAll(picked));
	}
}
