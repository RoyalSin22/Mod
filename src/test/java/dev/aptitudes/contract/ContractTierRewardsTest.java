package dev.aptitudes.contract;

import dev.aptitudes.contract.ContractRules.Difficulty;
import dev.aptitudes.contract.ContractRules.Kind;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contratos de v65: tipos nuevos (Nether, criaturas nuevas, expedición) y recompensas por nivel. */
class ContractTierRewardsTest {
	@Test void newKindsHaveTheirGoals() {
		assertEquals(8, ContractRules.goal(Kind.NETHER, Difficulty.EASY, 0));
		assertEquals(2, ContractRules.goal(Kind.NEW_MOB, Difficulty.EASY, 0));
		assertEquals(1, ContractRules.goal(Kind.EXPEDITION, Difficulty.LEGENDARY, 0));
	}

	@Test void newKindsAppearOnlyAtTheirDifficulties() {
		assertTrue(ContractRules.kindsFor(Difficulty.LEGENDARY).contains(Kind.EXPEDITION));
		assertTrue(ContractRules.kindsFor(Difficulty.ELITE).contains(Kind.NETHER));
		assertTrue(!ContractRules.kindsFor(Difficulty.EASY).contains(Kind.NETHER));
	}

	@Test void rewardsNeverDropBelowTheBaseAsRankRises() {
		UUID player = UUID.randomUUID();
		for (long day = 0; day < 20; day++) {
			for (ContractRules.Offer offer : ContractRules.dailyOffers(player, day, 300, 0)) {
				int s = offer.difficulty().ordinal() + 1;
				assertTrue(offer.emeralds() >= 2 + s * 2, "esmeraldas por debajo de la base");
			}
		}
	}
}
