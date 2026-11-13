package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F5): cifras del Mercader táctico. */
class MerchantRulesTest {
	@Test
	void thriftValues() {
		assertEquals(0.15, MerchantRules.waiveChance(1), 1e-9);
		assertEquals(0.45, MerchantRules.waiveChance(3), 1e-9);
		assertEquals(0, MerchantRules.waiveChance(0), 1e-9);
		assertEquals(10, MerchantRules.creditCost(1), 1e-9);
		assertEquals(5, MerchantRules.creditCost(2), 1e-9);
	}

	@Test
	void dividendNeedsMinimumAndRespectsCap() {
		assertEquals(30, MerchantRules.dividendThreshold(1));
		assertEquals(10, MerchantRules.dividendThreshold(3));
		assertFalse(MerchantRules.dividendDue(2, 19));
		assertTrue(MerchantRules.dividendDue(2, 20));
		assertFalse(MerchantRules.dividendDue(2, 64), "tope de 64");
		assertFalse(MerchantRules.dividendDue(0, 40));
	}

	@Test
	void activeValues() {
		assertEquals(6, MerchantRules.coinDamage(1, 1), 1e-9);
		assertEquals(10, MerchantRules.coinDamage(3, 1), 1e-9);
		assertEquals(0.10, MerchantRules.auctionBonus(1), 1e-9);
		assertEquals(0.20, MerchantRules.auctionBonus(3), 1e-9);
		assertEquals(160, MerchantRules.caravanTicks(1));
		assertEquals(200, MerchantRules.caravanTicks(2));
		assertEquals(200, MerchantRules.investTicks(1));
		assertEquals(280, MerchantRules.investTicks(2));
	}

	@Test
	void ledgerIsCapped() {
		assertEquals(3, MerchantRules.ledgerCharges(0, 3));
		assertEquals(5, MerchantRules.ledgerCharges(4, 3));
		assertEquals(0.15, MerchantRules.ledgerBonus(9), 1e-9);
		assertEquals(0.03, MerchantRules.ledgerBonus(1), 1e-9);
	}

	@Test
	void bountyAndLiquidation() {
		assertEquals(2, MerchantRules.bountyDrop(1));
		assertEquals(4, MerchantRules.bountyDrop(2));
		assertEquals(20, MerchantRules.liquidationDamage(0, 1), 1e-9);
		assertEquals(50, MerchantRules.liquidationDamage(10, 1), 1e-9);
		assertEquals(50, MerchantRules.liquidationDamage(30, 1), 1e-9, "máximo 10 esmeraldas");
	}
}
