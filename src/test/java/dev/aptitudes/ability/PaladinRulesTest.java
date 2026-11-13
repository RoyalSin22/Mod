package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** v67 (F4): cifras de las habilidades nuevas del Paladín, tal como las describen los textos. */
class PaladinRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void undeadReductionAndTriumph() {
		assertEquals(0.08, PaladinRules.undeadReduction(1), EPS);
		assertEquals(0.24, PaladinRules.undeadReduction(3), EPS);
		assertEquals(0, PaladinRules.undeadReduction(-1), EPS);
		assertEquals(2f, PaladinRules.triumphHeal(1));
		assertEquals(4f, PaladinRules.triumphHeal(3));
		assertEquals(0f, PaladinRules.triumphHeal(0));
	}

	@Test
	void rayHealsAllies() {
		assertEquals(4f, PaladinRules.rayHeal(1));
		assertEquals(8f, PaladinRules.rayHeal(3));
	}

	@Test
	void rayHurtsEnemiesAndDoublesAgainstUndead() {
		assertEquals(6.0, PaladinRules.rayDamage(1, false, 1), EPS);
		assertEquals(10.0, PaladinRules.rayDamage(3, false, 1), EPS);
		assertEquals(12.0, PaladinRules.rayDamage(1, true, 1), EPS);
		assertEquals(10.0, PaladinRules.rayDamage(3, true, 0.5), EPS);
	}

	@Test
	void bashDamageAndStun() {
		assertEquals(5.0, PaladinRules.bashDamage(1, 1), EPS);
		assertEquals(7.0, PaladinRules.bashDamage(3, 1), EPS);
		assertEquals(20, PaladinRules.bashStunTicks(1));
		assertEquals(30, PaladinRules.bashStunTicks(2));
		assertEquals(40, PaladinRules.bashStunTicks(3));
	}

	@Test
	void prayerWallAndBanner() {
		assertEquals(1f, PaladinRules.prayerHeal(1));
		assertEquals(2f, PaladinRules.prayerHeal(2));
		assertEquals(3.0, PaladinRules.wallDamage(1, 1), EPS);
		assertEquals(4.0, PaladinRules.wallDamage(2, 1), EPS);
		assertEquals(200, PaladinRules.bannerTicks(1));
		assertEquals(280, PaladinRules.bannerTicks(2));
		assertEquals(0, PaladinRules.bannerTicks(0));
	}

	@Test
	void wrathGrowsWithLostHealthAndIsCappedByRank() {
		double charge = PaladinRules.wrathCharge(0, 2, 20); // 10 % de la vida → +20 %
		assertEquals(0.2, charge, EPS);
		charge = PaladinRules.wrathCharge(charge, 5, 20); // 25 % más → +50 %
		assertEquals(0.7, charge, EPS);
		assertEquals(1.0, PaladinRules.wrathCharge(0.9, 10, 20), EPS, "la carga no pasa de +100 %");
		assertEquals(0.3, PaladinRules.wrathBonus(0.7, 1), EPS, "rango 1: tope +30 %");
		assertEquals(0.5, PaladinRules.wrathBonus(0.7, 2), EPS, "rango 2: tope +50 %");
		assertEquals(0.2, PaladinRules.wrathBonus(0.2, 2), EPS);
		assertEquals(0, PaladinRules.wrathBonus(0.7, 0), EPS);
		assertEquals(0.3, PaladinRules.wrathCharge(0.3, 0, 20), EPS, "sin daño no cambia");
		assertEquals(0.3, PaladinRules.wrathCharge(0.3, 5, 0), EPS, "sin vida máxima no cambia");
	}

	@Test
	void pillarDoublesAgainstUndead() {
		assertEquals(6.0, PaladinRules.pillarDamage(false, 1), EPS);
		assertEquals(12.0, PaladinRules.pillarDamage(true, 1), EPS);
		assertEquals(6.0, PaladinRules.pillarDamage(true, 0.5), EPS);
	}
}
