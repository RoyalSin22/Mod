package dev.aptitudes.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** V80: la Armadura Ascendida es una mejora real de la netherita y su bonus común está acotado. */
class AscendedRulesTest {
	private static final int[] NETHERITE = {3, 6, 8, 3}; // botas, grebas, peto, casco

	@Test void everyPieceBeatsNetherite() {
		assertEquals(4, AscendedRules.DEFENSE.length);
		for (int i = 0; i < 4; i++) assertTrue(AscendedRules.DEFENSE[i] > NETHERITE[i], "pieza " + i);
		assertTrue(AscendedRules.totalDefense() > 20);
		assertTrue(AscendedRules.TOUGHNESS > 3.0f);
		assertTrue(AscendedRules.DURABILITY > 37);
	}

	@Test void commonBonusIsModest() {
		assertTrue(AscendedRules.COST_MULTIPLIER < 1.0 && AscendedRules.COST_MULTIPLIER >= 0.75);
		assertTrue(AscendedRules.COOLDOWN_REDUCTION > 0 && AscendedRules.COOLDOWN_REDUCTION <= 0.20);
		assertTrue(AscendedRules.ABILITY_DAMAGE_BONUS > AscendedRules.MELEE_DAMAGE_BONUS);
		assertTrue(AscendedRules.ABILITY_DAMAGE_BONUS <= 0.15f);
	}

	@Test void damageBonusDependsOnTheHitKind() {
		assertEquals(AscendedRules.MELEE_DAMAGE_BONUS, AscendedRules.damageBonus(true));
		assertEquals(AscendedRules.ABILITY_DAMAGE_BONUS, AscendedRules.damageBonus(false));
	}

	@Test void reduceNeverDropsBelowHalf() {
		assertEquals(90.0, AscendedRules.reduce(100, 0.10), 1e-9);
		assertEquals(50.0, AscendedRules.reduce(100, 0.90), 1e-9);
		assertEquals(0.0, AscendedRules.reduce(0, 0.5), 1e-9);
	}

	@Test void championTemplateChanceIsRare() {
		assertTrue(AscendedRules.CHAMPION_TEMPLATE_CHANCE > 0 && AscendedRules.CHAMPION_TEMPLATE_CHANCE <= 0.25f);
	}
}
