package dev.aptitudes.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderRulesTest {
	@ParameterizedTest
	@CsvSource({
			"1, 1",
			"2, 2",
			"3, 4",
			"4, 5",
			"0, 1",    // fuera de rango: se ajusta
			"9, 5",
	})
	void higherTiersNeedMorePlayers(int tier, int expected) {
		assertEquals(expected, OrderRules.requiredParticipants(tier));
	}

	@ParameterizedTest
	@CsvSource({
			"100, 10",
			"0,   0",
			"-50, 0",
			"999, 99",
	})
	void commissionIsTenPercent(long price, long expected) {
		assertEquals(expected, OrderRules.commission(price));
	}

	@Test void splitKeepsTheExactTotal() {
		long[] shares = OrderRules.split(10, 3);
		assertEquals(3, shares.length);
		assertEquals(10, shares[0] + shares[1] + shares[2]);
		assertEquals(4, shares[0]); // el resto va a los primeros
		assertEquals(3, shares[2]);
	}

	@Test void splitWithOneParticipantGivesEverythingToThem() {
		assertEquals(7, OrderRules.split(7, 1)[0]);
	}

	@Test void participantsAreCappedAtFive() {
		assertEquals(5, OrderRules.split(100, 99).length);
		assertEquals(1, OrderRules.split(100, 0).length);
	}
}
