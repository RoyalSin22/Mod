package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F2): cifras de las habilidades nuevas del Bersérker, tal como las describen los textos. */
class BerserkerRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void scarsNeedAHeavyHitAndStackUpToTheRankLimit() {
		assertFalse(BerserkerRules.scarTriggers(3.0, 20.0), "15 % justo no basta");
		assertTrue(BerserkerRules.scarTriggers(3.1, 20.0));
		assertFalse(BerserkerRules.scarTriggers(5, 0), "sin vida máxima no hay cicatrices");
		assertEquals(3, BerserkerRules.maxScars(1));
		assertEquals(5, BerserkerRules.maxScars(3));
		assertEquals(0, BerserkerRules.maxScars(0));
		assertEquals(0.15, BerserkerRules.scarReduction(5), EPS);
		assertEquals(0, BerserkerRules.scarReduction(-2), EPS);
	}

	@Test
	void boilingBloodBelowHalfHealth() {
		assertTrue(BerserkerRules.boiling(9.9, 20));
		assertFalse(BerserkerRules.boiling(10, 20));
		assertEquals(2, BerserkerRules.boilingFireSeconds(1));
		assertEquals(4, BerserkerRules.boilingFireSeconds(3));
	}

	@Test
	void activeDamageMatchesTheDescriptions() {
		assertEquals(5.0, BerserkerRules.axeDamage(1, 1), EPS);
		assertEquals(6.5, BerserkerRules.axeDamage(2, 1), EPS);
		assertEquals(8.0, BerserkerRules.axeDamage(3, 1), EPS);
		assertEquals(4.0, BerserkerRules.echoDamage(1, 1), EPS);
		assertEquals(6.0, BerserkerRules.echoDamage(2, 1), EPS);
		assertEquals(2.5, BerserkerRules.rendSlashDamage(1, 1), EPS);
		assertEquals(14.0, BerserkerRules.ragnarokDamage(1), EPS);
		assertEquals(7.0, BerserkerRules.ragnarokDamage(0.5), EPS, "la escala de la configuración se aplica");
	}

	@Test
	void chainBonusGrowsFiftyToOneHundredPercent() {
		assertEquals(0.5, BerserkerRules.chainBonus(1), EPS);
		assertEquals(0.75, BerserkerRules.chainBonus(2), EPS);
		assertEquals(1.0, BerserkerRules.chainBonus(3), EPS);
		assertEquals(0, BerserkerRules.chainBonus(0), EPS);
	}

	@Test
	void bleedingStacksAreCappedAtThree() {
		int stacks = 0;
		for (int i = 0; i < 5; i++) stacks = BerserkerRules.addBleedStack(stacks);
		assertEquals(3, stacks);
		assertEquals(1.5, BerserkerRules.rendBleedPerStack(1), EPS);
		assertEquals(2.0, BerserkerRules.rendBleedPerStack(2), EPS);
		assertEquals(6.0, BerserkerRules.bleedTick(3, 2.0, 1), EPS);
		assertEquals(0, BerserkerRules.bleedTick(-1, 2.0, 1), EPS);
	}

	@Test
	void roarOathAndAvatar() {
		assertEquals(0, BerserkerRules.roarAmplifier(1));
		assertEquals(1, BerserkerRules.roarAmplifier(2));
		assertEquals(1, BerserkerRules.roarAmplifier(5), "con bonificaciones no pasa de Debilidad II");
		assertEquals(2.0, BerserkerRules.oathCost(20), EPS);
		assertEquals(240, BerserkerRules.oathDuration(160));
		assertEquals(200, BerserkerRules.avatarTicks(1));
		assertEquals(280, BerserkerRules.avatarTicks(2));
	}

	@Test
	void windowsCloseAtTheirTick() {
		assertTrue(BerserkerRules.windowOpen(10, 11));
		assertFalse(BerserkerRules.windowOpen(11, 11));
	}
}
