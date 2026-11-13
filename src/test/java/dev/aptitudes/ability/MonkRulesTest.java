package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F5): cifras del Monje. */
class MonkRulesTest {
	@Test
	void comboCountsThreeHitsInFourSeconds() {
		int c = MonkRules.comboCount(0, -100_000, 1000);
		assertEquals(1, c);
		c = MonkRules.comboCount(c, 1000, 1040);
		assertEquals(2, c);
		c = MonkRules.comboCount(c, 1040, 1100);
		assertEquals(3, c);
		assertEquals(1, MonkRules.comboCount(c, 1100, 1300), "más de 4 s: reinicia");
		assertEquals(0.10, MonkRules.comboBonus(1), 1e-9);
		assertEquals(0.30, MonkRules.comboBonus(3), 1e-9);
	}

	@Test
	void passiveValues() {
		assertEquals(0.04, MonkRules.bareHands(1), 1e-9);
		assertEquals(0.12, MonkRules.bareHands(3), 1e-9);
		assertEquals(0.15, MonkRules.sereneSpeed(1), 1e-9);
		assertEquals(0.25, MonkRules.sereneSpeed(2), 1e-9);
		assertFalse(MonkRules.sereneActive(1050, 1000));
		assertTrue(MonkRules.sereneActive(1100, 1000));
		assertEquals(0.4, MonkRules.echoFraction(1), 1e-9);
		assertEquals(0.6, MonkRules.echoFraction(2), 1e-9);
	}

	@Test
	void activeValues() {
		assertEquals(5, MonkRules.kickDamage(1, 1), 1e-9);
		assertEquals(7, MonkRules.kickDamage(3, 1), 1e-9);
		assertEquals(6, MonkRules.waveDamage(1, 1), 1e-9);
		assertEquals(10, MonkRules.waveDamage(3, 1), 1e-9);
		assertEquals(12, MonkRules.palmDamage(1, 1), 1e-9);
		assertEquals(16, MonkRules.palmDamage(2, 1), 1e-9);
		assertEquals(6, MonkRules.palmSplash(1, 1), 1e-9);
		assertEquals(8, MonkRules.palmSplash(2, 1), 1e-9);
		assertEquals(160, MonkRules.tigerTicks(1));
		assertEquals(200, MonkRules.tigerTicks(2));
	}

	@Test
	void tigerArmorIsCappedAndVoidPalmHasExtraCap() {
		int armor = 0;
		for (int i = 0; i < 9; i++) armor = MonkRules.tigerArmor(armor);
		assertEquals(5, armor);
		assertEquals(30 + 15, MonkRules.voidDamage(100, 1), 1e-9);
		assertEquals(30 + 20, MonkRules.voidDamage(500, 1), 1e-9, "extra topado en 20");
		assertEquals(30, MonkRules.voidDamage(0, 1), 1e-9);
	}
}
