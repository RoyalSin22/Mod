package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TauntRulesTest {
	private static final long START = 1000;

	@ParameterizedTest
	@CsvSource({
			"true,  false, true",   // élite
			"false, false, false",  // criatura común
			"true,  true,  false",  // jefe: nunca se provoca
			"false, true,  false",
	})
	void onlyElitesAndChampionsCanBeTaunted(boolean elite, boolean boss, boolean expected) {
		assertEquals(expected, TauntRules.eligible(elite, boss));
	}

	@Test void tauntLastsTheConfiguredTicks() {
		assertTrue(TauntRules.isTaunting(START, START));
		assertTrue(TauntRules.isTaunting(START, START + TauntRules.DURATION_TICKS - 1));
		assertFalse(TauntRules.isTaunting(START, START + TauntRules.DURATION_TICKS));
	}

	@Test void noTauntBeforeStartOrWithoutTaunt() {
		assertFalse(TauntRules.isTaunting(TauntRules.NONE, START));
		assertFalse(TauntRules.isTaunting(START, START - 1));
	}

	@Test void cooldownStartsWhenTheTauntIsUsed() {
		long ready = TauntRules.readyAfter(START);
		assertEquals(START + TauntRules.COOLDOWN_TICKS, ready);
		assertFalse(TauntRules.canTaunt(ready, ready - 1));
		assertTrue(TauntRules.canTaunt(ready, ready));
	}

	@ParameterizedTest
	@CsvSource({
			"true,  0.0,  true",
			"true,  8.0,  true",   // justo en el radio
			"true,  8.01, false",
			"false, 2.0,  false",  // sin provocación activa
	})
	void mobRetargetsOnlyInsideTheRadiusWhileTaunted(boolean taunting, double distance, boolean expected) {
		assertEquals(expected, TauntRules.shouldRetarget(taunting, distance));
	}
}
