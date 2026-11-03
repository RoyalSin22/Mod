package dev.aptitudes.contract;

import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

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
		assertTrue(ContractRules.dailyOffers(new UUID(1, 2), 0, 0, 2).stream().anyMatch(o -> o.difficulty() == ContractRules.Difficulty.LEGENDARY));
	}
	@Test public void progressIsClamped() {
		assertEquals(8, ContractRules.progress(7, 9, 8));
		assertEquals(2, ContractRules.progress(-2, 2, 8));
	}
}
