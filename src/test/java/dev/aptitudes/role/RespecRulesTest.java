package dev.aptitudes.role;

import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.role.RespecRules.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RespecRulesTest {
	@Test void costGrowsWithTheSpentPointsAndRoundsUp() {
		assertEquals(0, RespecRules.cost(0, 2, 0.5, 20), "sin puntos gastados no cuesta nada");
		assertEquals(3, RespecRules.cost(1, 2, 0.5, 20)); // 2 + 0,5 = 2,5 → 3
		assertEquals(12, RespecRules.cost(20, 2, 0.5, 20)); // 2 + 10
		assertEquals(13, RespecRules.cost(21, 2, 0.5, 20)); // 2 + 10,5 → 13
	}

	@Test void theDefaultCostIsTwentyLevelsWhateverThePointsSpent() {
		AptitudesConfig c = new AptitudesConfig();
		int base = c.treeRespecLevelsBase, max = c.treeRespecLevelsMax;
		double perPoint = c.treeRespecLevelsPerPoint;
		assertEquals(0, RespecRules.cost(0, base, perPoint, max), "sin puntos gastados no cuesta nada");
		assertEquals(20, RespecRules.cost(1, base, perPoint, max));
		assertEquals(20, RespecRules.cost(37, base, perPoint, max));
		assertEquals(20, RespecRules.cost(500, base, perPoint, max));
	}

	@Test void costIsCappedAtTheMaximum() {
		assertEquals(20, RespecRules.cost(1000, 2, 0.5, 20));
		assertEquals(0, RespecRules.cost(50, 2, 0.5, 0), "un máximo de 0 lo hace gratis");
	}

	@Test void hostileInputsNeverOverflowOrGoNegative() {
		assertEquals(30, RespecRules.cost(10, Integer.MAX_VALUE, Double.MAX_VALUE, 30));
		assertEquals(30, RespecRules.cost(Integer.MAX_VALUE, 5, 1e300, 30));
		assertEquals(30, RespecRules.cost(10, 5, Double.POSITIVE_INFINITY, 30));
		assertEquals(0, RespecRules.cost(-5, 2, 0.5, 20));
		assertTrue(RespecRules.cost(10, -100, -3.0, 20) >= 0);
		assertEquals(0, RespecRules.cost(10, 5, Double.NaN, 0));
	}

	@Test void aDisabledServerRejectsEverythingExceptAnUnavailablePlayer() {
		assertEquals(Result.DISABLED, RespecRules.check(false, true, 10, 99, 5, false));
		assertEquals(Result.UNAVAILABLE, RespecRules.check(false, false, 10, 99, 5, false));
	}

	@Test void aDeadOrSpectatorPlayerCannotReset() {
		assertEquals(Result.UNAVAILABLE, RespecRules.check(true, false, 10, 99, 5, false));
	}

	@Test void withoutSpentPointsThereIsNothingToReset() {
		assertEquals(Result.NOTHING_TO_RESET, RespecRules.check(true, true, 0, 99, 0, false));
		assertEquals(Result.NOTHING_TO_RESET, RespecRules.check(true, true, 0, 99, 0, true));
	}

	@Test void thePlayerNeedsEnoughExperienceLevelsUnlessCreative() {
		assertEquals(Result.NOT_ENOUGH_LEVELS, RespecRules.check(true, true, 10, 6, 7, false));
		assertEquals(Result.OK, RespecRules.check(true, true, 10, 7, 7, false), "con justo los niveles necesarios basta");
		assertEquals(Result.OK, RespecRules.check(true, true, 10, 0, 7, true), "en Creativo no se paga");
	}
}
