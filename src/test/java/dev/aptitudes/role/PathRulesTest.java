package dev.aptitudes.role;

import dev.aptitudes.role.PathRules.Step;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PathRulesTest {
	private final Map<String, Integer> ranks = new HashMap<>();

	private int rank(String id) { return ranks.getOrDefault(id, 0); }

	@Test public void idsKeepTheOldFormatAndAddTheNewSteps() {
		assertEquals("berserker_a1", PathRules.nodeId("berserker", 1, Step.S1));
		assertEquals("berserker_c3", PathRules.nodeId("berserker", 3, Step.S3));
		assertEquals("mage_b5x", PathRules.nodeId("mage", 2, Step.S5X));
		assertEquals("mage_b5y", PathRules.nodeId("mage", 2, Step.S5Y));
		assertEquals("weapon_summoner_a6", PathRules.nodeId("weapon_summoner", 1, Step.S6));
		assertThrows(IllegalArgumentException.class, () -> PathRules.nodeId("mage", 4, Step.S1));
	}

	@Test public void stepsHaveTheApprovedLevelsAndOnlyS6IsBoosted() {
		int[] levels = {10, 14, 20, 26, 32, 32, 40};
		for (Step s : Step.values()) {
			assertEquals(s.name(), levels[s.ordinal()], s.level());
			assertEquals(s == Step.S6 ? 1.5 : 1.0, s.multiplier(), 1e-9);
			// Con 3 rangos y +5 niveles por rango, el último rango cabe en el nivel 50.
			assertTrue(s.level() + 10 <= 50);
		}
		assertEquals(Step.S5Y, Step.S5X.otherBranch());
		assertNull(Step.S4.otherBranch());
		assertFalse(Step.S3.added());
		assertTrue(Step.S4.added());
	}

	@Test public void pathsAreExclusive() {
		assertFalse(PathRules.pathLocked(this::rank, "druid", 2));
		ranks.put("druid_a1", 1);
		assertEquals(1, PathRules.chosenPath(this::rank, "druid"));
		assertFalse(PathRules.pathLocked(this::rank, "druid", 1));
		assertTrue(PathRules.pathLocked(this::rank, "druid", 2));
		assertTrue(PathRules.pathLocked(this::rank, "druid", 3));
		// Otro rol no se ve afectado.
		assertFalse(PathRules.pathLocked(this::rank, "mage", 2));
	}

	@Test public void theForkAllowsOnlyOneBranchAndS6AcceptsEither() {
		assertFalse(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S4));
		ranks.put("monk_a3", 2);
		assertTrue(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S4));
		assertFalse(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S5X));
		ranks.put("monk_a4", 1);
		assertTrue(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S5X));
		assertFalse(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S6));
		ranks.put("monk_a5y", 1);
		assertTrue(PathRules.branchLocked(this::rank, "monk", 1, Step.S5X));
		assertFalse(PathRules.branchLocked(this::rank, "monk", 1, Step.S5Y));
		assertEquals(Step.S5Y, PathRules.chosenBranch(this::rank, "monk", 1));
		assertTrue(PathRules.prerequisiteMet(this::rank, "monk", 1, Step.S6));
	}

	@Test public void migrationKeepsThePathWithMoreRanksAndRefundsTheRest() {
		ranks.put("berserker_a1", 3);
		ranks.put("berserker_a2", 1);          // A = 4
		ranks.put("berserker_b1", 3);
		ranks.put("berserker_b2", 3);          // B = 6
		assertArrayEquals(new int[] {4, 6, 0}, PathRules.ranksPerPath(this::rank, "berserker"));
		assertEquals(2, PathRules.chosenPath(this::rank, "berserker"));
		List<String> refunds = PathRules.refunds(this::rank, "berserker");
		assertEquals(List.of("berserker_a1", "berserker_a2"), refunds);
	}

	@Test public void migrationTieKeepsTheFirstPathAndNothingIsRefundedWithOnePath() {
		ranks.put("bard_b1", 2);
		assertTrue(PathRules.refunds(this::rank, "bard").isEmpty());
		ranks.put("bard_c1", 2);
		assertEquals(List.of("bard_c1"), PathRules.refunds(this::rank, "bard"));
		assertEquals(1, PathRules.keepPath(new int[] {0, 0, 0}));
		assertEquals(0, PathRules.chosenPath(id -> 0, "bard"));
	}

	@Test public void negativeOrHugeRanksDoNotBreakTheCount() {
		ranks.put("tamer_a1", -5);
		ranks.put("tamer_b1", Integer.MAX_VALUE);
		ranks.put("tamer_b2", Integer.MAX_VALUE);
		int[] r = PathRules.ranksPerPath(this::rank, "tamer");
		assertEquals(0, r[0]);
		assertEquals(Integer.MAX_VALUE, r[1]);
	}
}
