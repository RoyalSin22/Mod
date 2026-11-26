package dev.aptitudes.logic;

import dev.aptitudes.logic.SummonBudgetRules.Entry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummonBudgetRulesTest {
	private static Entry e(int n, int tick, int cost) {
		return new Entry(new UUID(0, n), tick, cost);
	}

	@Test
	void costsByType() {
		assertEquals(1, SummonBudgetRules.costOfType("minecraft:vex"));
		assertEquals(2, SummonBudgetRules.costOfType("minecraft:zombie"));
		assertEquals(3, SummonBudgetRules.costOfType("minecraft:wolf"));
		assertEquals(4, SummonBudgetRules.costOfType("minecraft:iron_golem"));
		assertEquals(2, SummonBudgetRules.costOfType("minecraft:cow"));
		assertEquals(2, SummonBudgetRules.costOfType(null));
	}

	@Test
	void fitsWithoutEvicting() {
		assertTrue(SummonBudgetRules.evictions(List.of(e(1, 0, 3), e(2, 5, 3)), 3, 16).isEmpty());
	}

	@Test
	void evictsTheOldestFirstUntilTheNewOneFits() {
		List<Entry> now = List.of(e(3, 30, 4), e(1, 10, 4), e(2, 20, 4)); // 12 puntos, desordenadas
		List<UUID> out = SummonBudgetRules.evictions(now, 8, 16); // 12 + 8 = 20 > 16: sale la más antigua
		assertEquals(List.of(new UUID(0, 1)), out);
		assertEquals(List.of(new UUID(0, 1), new UUID(0, 2)), SummonBudgetRules.evictions(now, 12, 16));
	}

	@Test
	void zeroOrNegativeBudgetMeansUnlimited() {
		assertTrue(SummonBudgetRules.evictions(List.of(e(1, 0, 50)), 50, 0).isEmpty());
		assertTrue(SummonBudgetRules.evictions(List.of(e(1, 0, 50)), 50, -3).isEmpty());
	}

	@Test
	void aHugeNewSummonEvictsEverythingButIsStillAccepted() {
		List<UUID> out = SummonBudgetRules.evictions(List.of(e(1, 0, 2), e(2, 1, 2)), 40, 16);
		assertEquals(2, out.size());
	}

	@Test
	void tiesInSpawnTickAreBrokenByIdForADeterministicResult() {
		List<UUID> out = SummonBudgetRules.evictions(List.of(e(2, 5, 4), e(1, 5, 4)), 12, 16);
		assertEquals(List.of(new UUID(0, 1)), out);
	}

	@Test
	void totalIgnoresNegativeCostsAndClampKeepsTheRange() {
		assertEquals(5, SummonBudgetRules.total(List.of(e(1, 0, 5), e(2, 0, -9))));
		assertEquals(0, SummonBudgetRules.clamp(-4));
		assertEquals(64, SummonBudgetRules.clamp(500));
		assertEquals(16, SummonBudgetRules.clamp(16));
	}
}
