package dev.aptitudes.event;

import dev.aptitudes.event.EclipseRules.Phase;
import dev.aptitudes.event.EclipseAssaultRules.Grade;
import dev.aptitudes.event.StrongEventRules.EclipseMob;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EclipseAssaultRulesTest {
	@Test void onlyLivingSurvivalPlayersParticipate() {
		assertTrue(EclipseAssaultRules.eligible(true, false, false));
		assertFalse(EclipseAssaultRules.eligible(false, false, false));
		assertFalse(EclipseAssaultRules.eligible(true, true, false));
		assertFalse(EclipseAssaultRules.eligible(true, false, true));
	}

	@Test void wavesIncreaseButRetreatNeverSpawns() {
		assertEquals(4, EclipseAssaultRules.waveSize(Phase.PENUMBRA, 4, 8));
		assertEquals(8, EclipseAssaultRules.waveSize(Phase.TOTALITY, 4, 8));
		assertEquals(0, EclipseAssaultRules.waveSize(Phase.RETREAT, 4, 8));
		assertEquals(0, EclipseAssaultRules.waveSize(Phase.NONE, 4, 8));
		assertEquals(0, EclipseAssaultRules.waveSize(Phase.PENUMBRA, -1, 8));
		assertEquals(24, EclipseAssaultRules.waveSize(Phase.TOTALITY, 4, Integer.MAX_VALUE));
	}

	@Test void localBudgetSharedAcrossTwoPlayersAndBoundedForLargeGroups() {
		assertEquals(12, EclipseAssaultRules.localCap(12, 1, 24));
		assertEquals(24, EclipseAssaultRules.localCap(12, 2, 24));
		assertEquals(24, EclipseAssaultRules.localCap(12, 5, 24));
		assertEquals(160, EclipseAssaultRules.localCap(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertEquals(0, EclipseAssaultRules.localCap(12, 0, 24));
		assertEquals(0, EclipseAssaultRules.localCap(12, 1, 0));
	}

	@Test void respectsLocalAndWorldSlotsWithoutOverflowOrNegativeCounts() {
		assertEquals(8, EclipseAssaultRules.spawnBudget(2, 12, 2, 128, 8));
		assertEquals(2, EclipseAssaultRules.spawnBudget(10, 12, 10, 128, 8));
		assertEquals(1, EclipseAssaultRules.spawnBudget(0, 12, 127, 128, 8));
		assertEquals(0, EclipseAssaultRules.spawnBudget(0, 12, 128, 128, 8));
		assertEquals(0, EclipseAssaultRules.spawnBudget(20, 12, 20, 128, 8));
		assertEquals(0, EclipseAssaultRules.spawnBudget(0, 12, 0, 0, 8));
		assertEquals(0, EclipseAssaultRules.spawnBudget(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
		assertEquals(24, EclipseAssaultRules.spawnBudget(-1, Integer.MAX_VALUE, -1, Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	@Test void totalityOpensWithChampionThenEliteAndPenumbraWithElite() {
		assertEquals(Grade.CHAMPION, grade(Phase.TOTALITY, 0, 0.99));
		assertEquals(Grade.ELITE, grade(Phase.TOTALITY, 1, 0.99));
		assertEquals(Grade.NORMAL, grade(Phase.TOTALITY, 2, 0.99));
		assertEquals(Grade.ELITE, grade(Phase.PENUMBRA, 0, 0.99));
		assertEquals(Grade.CHAMPION, grade(Phase.PENUMBRA, 1, 0.01));
		assertEquals(Grade.NORMAL, grade(Phase.RETREAT, 0, 0.01));
	}

	@Test void zeroChancesDisableGuaranteesAndZeroMinimumKeepsOriginalRolls() {
		assertEquals(Grade.NORMAL, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 4, 2, 0, 0, 0));
		assertEquals(Grade.ELITE, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 1, 1, 0.5, 0, 0.99));
		assertEquals(Grade.NORMAL, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 0, 0, 0.5, 0.1, 0.9));
		assertEquals(Grade.ELITE, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 0, 0, 0.5, 0.1, 0.4));
		assertEquals(Grade.CHAMPION, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 0, 0, 0.5, 0.1, 0.05));
	}

	@Test void corruptProbabilitiesAndRollsNeverCreateUnboundedRanks() {
		assertEquals(Grade.NORMAL, EclipseAssaultRules.grade(Phase.TOTALITY, 0, 1, 1, Double.NaN, Double.POSITIVE_INFINITY, 0));
		assertEquals(Grade.NORMAL, EclipseAssaultRules.grade(Phase.TOTALITY, 10, 1, 1, 0.5, 0.1, Double.NaN));
		assertEquals(Grade.CHAMPION, EclipseAssaultRules.grade(Phase.TOTALITY, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, 4, 5, 0.99));
		assertEquals(Grade.ELITE, EclipseAssaultRules.grade(Phase.TOTALITY, 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 4, 5, 0.99));
	}

	@Test void elitesAndChampionsHaveFullArmorButNormalUnitsRespectChance() {
		assertEquals(1.0, EclipseAssaultRules.armorChance(Grade.ELITE, true, 0.35));
		assertEquals(1.0, EclipseAssaultRules.armorChance(Grade.CHAMPION, true, 0.35));
		assertEquals(0.35, EclipseAssaultRules.armorChance(Grade.NORMAL, true, 0.35));
		assertEquals(0.35, EclipseAssaultRules.armorChance(Grade.ELITE, false, 0.35));
		assertEquals(0.0, EclipseAssaultRules.armorChance(Grade.NORMAL, false, Double.NaN));
	}

	@Test void guaranteedUnitsUseBodiesWithVisibleArmorAndNormalWeightsStayIntact() {
		for (Phase phase : new Phase[] {Phase.PENUMBRA, Phase.TOTALITY}) {
			for (int roll = -1; roll <= 101; roll++) {
				for (Grade grade : new Grade[] {Grade.ELITE, Grade.CHAMPION}) {
					EclipseMob type = EclipseAssaultRules.mob(phase, grade, true, roll);
					assertTrue(type == EclipseMob.ZOMBIE || type == EclipseMob.SKELETON
							|| phase == Phase.TOTALITY && type == EclipseMob.WITHER_SKELETON);
				}
				assertEquals(StrongEventRules.eclipseMob(phase, roll), EclipseAssaultRules.mob(phase, Grade.NORMAL, true, roll));
				assertEquals(StrongEventRules.eclipseMob(phase, roll), EclipseAssaultRules.mob(phase, Grade.ELITE, false, roll));
			}
		}
	}

	private static Grade grade(Phase phase, int spawned, double roll) {
		return EclipseAssaultRules.grade(phase, spawned, 1, 1, 0.5, 0.1, roll);
	}
}
