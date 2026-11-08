package dev.aptitudes.role.datapack;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.aptitudes.role.datapack.TalentProgressRules.key;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalentProgressRulesTest {
	private static final String LIGHT = key("swordsman", "pilot:light");
	private static final String FIRM = key("swordsman", "pilot:firm");
	private static final String HEAVY = key("swordsman", "pilot:heavy");

	@Test
	void clampsSavedRanksAndLimitsEntries() {
		Map<String, Integer> saved = new HashMap<>();
		for (int i = 0; i < 40; i++) saved.put(key("swordsman", "pilot:n" + i), Integer.MAX_VALUE);
		saved.put("invalid", 1);
		saved.put(key("swordsman", "pilot:UPPER"), 2);
		saved.put(key("swordsman", "pilot:negative"), -1);

		var ranks = TalentProgressRules.readRanks(saved);
		assertEquals(32, ranks.size());
		assertTrue(ranks.keySet().stream().allMatch(id -> id.startsWith("swordsman/pilot:n")));
		assertTrue(ranks.values().stream().allMatch(rank -> rank == 3));
		assertEquals(Map.of(), TalentProgressRules.readRanks(Map.of()));
	}

	@Test
	void refundsRanksAboveNewLevelAndRankCap() {
		var requirements = Map.of(
				LIGHT, new TalentProgressRules.Requirement(12, 12, 3, "parry"),
				FIRM, new TalentProgressRules.Requirement(30, 16, 1, LIGHT));
		assertEquals(Map.of(LIGHT, 1, FIRM, 1),
				TalentProgressRules.reconcile(Map.of(LIGHT, 3, FIRM, 2), requirements, id -> 1));
	}

	@Test
	void refundsWholeBranchIfAnchorIsUnlearned() {
		var requirements = Map.of(
				LIGHT, new TalentProgressRules.Requirement(30, 12, 1, "parry"),
				FIRM, new TalentProgressRules.Requirement(30, 16, 2, LIGHT),
				HEAVY, new TalentProgressRules.Requirement(30, 21, 1, FIRM));
		var ranks = Map.of(LIGHT, 1, FIRM, 2, HEAVY, 1);
		assertEquals(ranks, TalentProgressRules.reconcile(ranks, requirements, id -> 1));
		assertEquals(Map.of(), TalentProgressRules.reconcile(ranks, requirements, id -> 0));
	}

	@Test
	void removingOrMovingTalentDoesNotTransferRank() {
		var requirements = Map.of(
				key("guardian", "pilot:light"), new TalentProgressRules.Requirement(30, 12, 1, "guard"),
				FIRM, new TalentProgressRules.Requirement(30, 16, 2, LIGHT));
		assertEquals(Map.of(), TalentProgressRules.reconcile(Map.of(LIGHT, 1, FIRM, 2), requirements, id -> 1));
	}

	@Test
	void checksSnapshotAndRankBeforeCharging() {
		assertTrue(TalentProgressRules.matches(5, 5, 0, 0));
		assertFalse(TalentProgressRules.matches(5, 5, 1, 0));
		assertFalse(TalentProgressRules.matches(6, 5, 0, 0));
		assertEquals(21, TalentProgressRules.levelForRank(16, 2));
	}

	@Test
	void capsSpeedAcrossBothRoles() {
		assertEquals(0, TalentProgressRules.speedRanks(-1));
		assertEquals(2, TalentProgressRules.speedRanks(2));
		assertEquals(3, TalentProgressRules.speedRanks(16));
	}
}
