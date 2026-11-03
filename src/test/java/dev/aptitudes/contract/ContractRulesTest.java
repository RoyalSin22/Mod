package dev.aptitudes.contract;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ContractRulesTest {
	@Test public void dailyOffersAreStableForPlayerAndDay() {
		UUID id = UUID.fromString("8d8ccbd8-b135-4afe-8cc8-8f4d6e897940");
		assertEquals(ContractRules.dailyOffers(id, 10, 50, 0), ContractRules.dailyOffers(id, 10, 50, 0));
		assertNotEquals(ContractRules.dailyOffers(id, 10, 50, 0), ContractRules.dailyOffers(id, 11, 50, 0));
	}

	@Test public void reputationAndEvolutionUnlockSlotsAndRareDifficulties() {
		assertEquals(2, ContractRules.activeLimit(299));
		assertEquals(3, ContractRules.activeLimit(300));
		assertEquals("MAESTRO", ContractRules.rank(700));
		assertEquals(3, ContractRules.rankIndex(700));
		// La oferta destacada (la primera) es siempre de la dificultad máxima permitida.
		for (long day = 0; day < 50; day++) {
			assertEquals(ContractRules.Difficulty.LEGENDARY, ContractRules.dailyOffers(new UUID(1, 2), day, 0, 2).get(0).difficulty());
		}
	}

	@Test public void progressIsClamped() {
		assertEquals(8, ContractRules.progress(7, 9, 8));
		assertEquals(2, ContractRules.progress(-2, 2, 8));
	}

	@Test public void eliteAndLegendaryRequireTheMatchingEvolution() {
		assertEquals(ContractRules.Difficulty.EASY, ContractRules.maxDifficulty(0, 0));
		assertEquals(ContractRules.Difficulty.ELITE, ContractRules.maxDifficulty(0, 1));
		assertEquals(ContractRules.Difficulty.LEGENDARY, ContractRules.maxDifficulty(0, 2));
	}

	@Test public void bossContractsAreOnlyLegendaryAndEventsStartAtHard() {
		for (ContractRules.Difficulty d : ContractRules.Difficulty.values()) {
			assertEquals(d == ContractRules.Difficulty.LEGENDARY, ContractRules.kindsFor(d).contains(ContractRules.Kind.BOSS));
			assertEquals(d.ordinal() >= ContractRules.Difficulty.HARD.ordinal(), ContractRules.kindsFor(d).contains(ContractRules.Kind.EVENT));
		}
		UUID id = new UUID(7, 9);
		for (long day = 0; day < 200; day++) {
			for (ContractRules.Offer o : ContractRules.dailyOffers(id, day, 0, 0)) {
				assertFalse(o.kind() == ContractRules.Kind.BOSS || o.kind() == ContractRules.Kind.EVENT);
			}
		}
	}

	@Test public void eventBossAndForgeGoalsAreReachable() {
		for (ContractRules.Difficulty d : ContractRules.Difficulty.values()) {
			assertEquals(1, ContractRules.goal(ContractRules.Kind.EVENT, d, 2));
			assertEquals(1, ContractRules.goal(ContractRules.Kind.BOSS, d, 2));
			assertTrue(ContractRules.goal(ContractRules.Kind.FORGE, d, 2) <= 2);
		}
	}

	@Test public void deliveryNeedsTheFullAmount() {
		assertTrue(ContractRules.canDeliver(16, 16));
		assertFalse(ContractRules.canDeliver(15, 16));
		assertFalse(ContractRules.canDeliver(5, 0));
	}
}
