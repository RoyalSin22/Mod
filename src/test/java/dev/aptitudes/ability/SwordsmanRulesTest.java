package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F4): cifras de las habilidades nuevas del Espadachín, tal como las describen los textos. */
class SwordsmanRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void duelNeedsExactlyOneEnemy() {
		assertEquals(0.05, SwordsmanRules.duel(1, 1), EPS);
		assertEquals(0.15, SwordsmanRules.duel(3, 1), EPS);
		assertEquals(0, SwordsmanRules.duel(3, 0), EPS);
		assertEquals(0, SwordsmanRules.duel(3, 2), EPS);
		assertEquals(0, SwordsmanRules.duel(0, 1), EPS);
	}

	@Test
	void armorPiercingBonusIsCapped() {
		assertEquals(0.10, SwordsmanRules.pierceBonus(1, 10), EPS);
		assertEquals(0.30, SwordsmanRules.pierceBonus(3, 10), EPS);
		assertEquals(0.30, SwordsmanRules.pierceBonus(3, 20), EPS, "tope de +30 %");
		assertEquals(0, SwordsmanRules.pierceBonus(3, 0), EPS);
		assertEquals(0, SwordsmanRules.pierceBonus(0, 20), EPS);
	}

	@Test
	void activeDamageMatchesTheDescriptions() {
		assertEquals(4.0, SwordsmanRules.risingDamage(1, 1), EPS);
		assertEquals(6.0, SwordsmanRules.risingDamage(3, 1), EPS);
		assertEquals(3.0, SwordsmanRules.windmillDamage(1, 1), EPS);
		assertEquals(5.0, SwordsmanRules.windmillDamage(3, 1), EPS);
		assertEquals(8.0, SwordsmanRules.leapDamage(1, 1), EPS);
		assertEquals(10.0, SwordsmanRules.leapDamage(2, 1), EPS);
		assertEquals(5.0, SwordsmanRules.leapDamage(2, 0.5), EPS);
	}

	@Test
	void streakStacksAndExpires() {
		assertEquals(0.04, SwordsmanRules.streakBonus(1, 1), EPS);
		assertEquals(0.30, SwordsmanRules.streakBonus(2, 5), EPS);
		assertEquals(0.30, SwordsmanRules.streakBonus(2, 9), EPS, "máximo 5 cargas");
		assertEquals(0, SwordsmanRules.streakBonus(0, 5), EPS);
		int stacks = 0;
		long last = 0;
		for (long now = 10; now <= 50; now += 10) {
			stacks = SwordsmanRules.streakAfterKill(stacks, last, now);
			last = now;
		}
		assertEquals(5, stacks);
		assertEquals(5, SwordsmanRules.streakAfterKill(stacks, last, last + 10), "no pasa de 5");
		assertEquals(1, SwordsmanRules.streakAfterKill(stacks, last, last + 200), "tras 8 s se reinicia");
	}

	@Test
	void steelWillLeavesOneHealthOnLethalHits() {
		assertEquals(1f, 20f - SwordsmanRules.surviveLethal(20f, 50f, true), EPS);
		assertEquals(19f, SwordsmanRules.surviveLethal(20f, 50f, true), EPS);
		assertEquals(19f, SwordsmanRules.surviveLethal(20f, 20f, true), EPS, "el golpe justo también es mortal");
		assertEquals(50f, SwordsmanRules.surviveLethal(20f, 50f, false), EPS, "en espera: no hace nada");
		assertEquals(5f, SwordsmanRules.surviveLethal(20f, 5f, true), EPS, "un golpe no mortal no se toca");
		assertEquals(0f, SwordsmanRules.surviveLethal(1f, 5f, true), EPS, "con 1 de vida el golpe se anula");
	}

	@Test
	void flowAndSheatheAndFinalCut() {
		assertEquals(160, SwordsmanRules.flowTicks(1));
		assertEquals(200, SwordsmanRules.flowTicks(2));
		assertEquals(0, SwordsmanRules.flowTicks(0));
		assertEquals(0.3, SwordsmanRules.sheatheBonus(1), EPS);
		assertEquals(0.5, SwordsmanRules.sheatheBonus(2), EPS);
		assertTrue(SwordsmanRules.sheathed(0, 60));
		assertFalse(SwordsmanRules.sheathed(0, 59));
		assertEquals(26.0, SwordsmanRules.finalDamage(20, 1), EPS);
		assertEquals(44.0, SwordsmanRules.finalDamage(1000, 1), EPS, "el 10 % de la vida tiene tope de 20");
		assertEquals(13.0, SwordsmanRules.finalDamage(20, 0.5), EPS);
	}
}
