package dev.aptitudes.souls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlaskRulesTest {
	@Test
	void preservesBaseFlasksAndImprovesEachRank() {
		for (int rank = 0; rank <= FlaskRules.MAX_RANK; rank++) {
			assertEquals(5 + rank, FlaskRules.capacity(rank));
			assertEquals(8 + 2 * rank, FlaskRules.estusHealing(rank));
			assertEquals(40 + 10 * rank, FlaskRules.etherPercent(rank));
		}
	}

	@Test
	void offersExactlyOneRankWithIncreasingCosts() {
		assertEquals(new FlaskRules.Upgrade(1, 8), FlaskRules.next(0, 2));
		assertEquals(new FlaskRules.Upgrade(2, 16), FlaskRules.next(1, 2));
		assertEquals(new FlaskRules.Upgrade(3, 24), FlaskRules.next(2, 3));
		assertNull(FlaskRules.next(2, 2));
		assertNull(FlaskRules.next(3, Integer.MAX_VALUE));
	}

	@Test
	void boundsCorruptRanksAndSpentCharges() {
		assertEquals(5, FlaskRules.capacity(Integer.MIN_VALUE));
		assertEquals(8, FlaskRules.capacity(Integer.MAX_VALUE));
		assertEquals(6, FlaskRules.remaining(1, Integer.MIN_VALUE));
		assertEquals(0, FlaskRules.remaining(1, Integer.MAX_VALUE));
		assertEquals(1, FlaskRules.remaining(1, 5));
	}

	@Test
	void loweringThePurchaseLimitDoesNotReducePurchasedFlasks() {
		assertNull(FlaskRules.next(0, -10));
		assertNull(FlaskRules.next(2, 1));
		assertEquals(7, FlaskRules.capacity(2));
		assertEquals(12, FlaskRules.estusHealing(2));
		assertEquals(60, FlaskRules.etherPercent(2));
	}
}
