package dev.aptitudes.rune;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v35: reglas puras de los valores configurables, las runas nuevas y la extracción. */
class RuneBalanceTest {
	@Test
	void secondsAreClampedAndConverted() {
		assertEquals(80L, RuneRules.secondsToTicks(4.0, 0.5, 60));
		assertEquals(10L, RuneRules.secondsToTicks(0.0, 0.5, 60));
		assertEquals(1_200L, RuneRules.secondsToTicks(1e9, 0.5, 60));
		assertEquals(10L, RuneRules.secondsToTicks(Double.NaN, 0.5, 60));
	}

	@Test
	void cooldownReadiness() {
		assertTrue(RuneRules.ready(100, null, 80));
		assertFalse(RuneRules.ready(150, 100L, 80));
		assertTrue(RuneRules.ready(180, 100L, 80));
		assertTrue(RuneRules.ready(50, 100L, 80)); // reloj hacia atrás
	}

	@Test
	void guardAbsorptionNeverExceedsCapNorReducesExisting() {
		assertEquals(2f, RuneRules.absorptionAfter(0f, 2f, 6f));
		assertEquals(6f, RuneRules.absorptionAfter(5f, 2f, 6f));
		assertEquals(8f, RuneRules.absorptionAfter(8f, 2f, 6f));
		assertEquals(0f, RuneRules.absorptionAfter(Float.NaN, -3f, 6f));
	}

	@Test
	void chanceTargetsAndCost() {
		assertTrue(RuneRules.roll(0.25, 0.1));
		assertFalse(RuneRules.roll(0.25, 0.3));
		assertFalse(RuneRules.roll(Double.NaN, 0.0));
		assertTrue(RuneRules.roll(5.0, 0.999));
		assertEquals(0, RuneRules.thunderTargets(-2));
		assertEquals(5, RuneRules.thunderTargets(99));
		assertEquals(4, RuneRules.removalCost(4, false));
		assertEquals(0, RuneRules.removalCost(4, true));
		assertEquals(64, RuneRules.removalCost(1_000, false));
	}

	@Test
	void removalDropsOnlyTheLastRune() {
		assertEquals(List.of("flame"), RuneRules.afterRemoval(List.of("flame", "frost")));
		assertEquals(List.of(), RuneRules.afterRemoval(List.of("thunder")));
		assertEquals(List.of(), RuneRules.afterRemoval(List.of()));
		assertEquals(List.of(), RuneRules.afterRemoval(null));
	}

	@Test
	void newRunesAreKnownAndSocketable() {
		assertTrue(RuneRules.canSocket(List.of("flame"), "frost"));
		assertTrue(RuneRules.canSocket(List.of(), "thunder"));
		assertTrue(RuneRules.canSocket(List.of("eclipse"), "guard"));
		assertFalse(RuneRules.canSocket(List.of("guard"), "guard"));
	}
}
