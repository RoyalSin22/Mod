package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** v67 (F3): cifras de las habilidades nuevas del Asesino, tal como las describen los textos. */
class AssassinRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void weakPointOnlyAgainstDebuffedEnemies() {
		assertEquals(0.05, AssassinRules.weakPointBonus(1, true), EPS);
		assertEquals(0.15, AssassinRules.weakPointBonus(3, true), EPS);
		assertEquals(0, AssassinRules.weakPointBonus(3, false), EPS);
		assertEquals(0, AssassinRules.weakPointBonus(0, true), EPS);
	}

	@Test
	void linkCutsFifteenToThirtyFivePercentOfWhatIsLeft() {
		assertEquals(0.15, AssassinRules.linkFraction(1), EPS);
		assertEquals(0.25, AssassinRules.linkFraction(2), EPS);
		assertEquals(0.35, AssassinRules.linkFraction(3), EPS);
		assertEquals(60, AssassinRules.linkReduction(400, 1));
		assertEquals(0, AssassinRules.linkReduction(0, 3));
		assertEquals(0, AssassinRules.linkReduction(-10, 3));
		assertEquals(0, AssassinRules.linkReduction(400, 0));
	}

	@Test
	void damageValuesMatchTheDescriptions() {
		assertEquals(8.0, AssassinRules.slashDamage(1, 1), EPS);
		assertEquals(12.0, AssassinRules.slashDamage(3, 1), EPS);
		assertEquals(6.0, AssassinRules.slashDamage(3, 0.5), EPS);
		assertEquals(5.0, AssassinRules.chainDamage(1, 1), EPS);
		assertEquals(6.0, AssassinRules.chainDamage(2, 1), EPS);
	}

	@Test
	void persistentShadowAndVeil() {
		assertEquals(0.4, AssassinRules.persistentBonus(1), EPS);
		assertEquals(0.6, AssassinRules.persistentBonus(2), EPS);
		assertEquals(0, AssassinRules.persistentBonus(0), EPS);
		assertEquals(120, AssassinRules.veilTicks(1));
		assertEquals(160, AssassinRules.veilTicks(2));
		assertEquals(0, AssassinRules.veilTicks(0));
	}

	@Test
	void venomDoublesDurationUpToTenSeconds() {
		assertEquals(120, AssassinRules.venomTicks(60));
		assertEquals(200, AssassinRules.venomTicks(150));
		assertEquals(200, AssassinRules.venomTicks(200));
		assertEquals(0, AssassinRules.venomTicks(-5));
	}

	@Test
	void harvestExtendsInvisibility() {
		assertEquals(40, AssassinRules.harvestExtend(1));
		assertEquals(60, AssassinRules.harvestExtend(2));
		assertEquals(0, AssassinRules.harvestExtend(0));
	}

	@Test
	void eternalNightTreatsEveryHitAsFromBehind() {
		assertEquals(0.5, AssassinRules.nightBackstab(2, false), EPS);
		assertEquals(0, AssassinRules.nightBackstab(2, true), EPS);
		assertEquals(0, AssassinRules.nightBackstab(-1, false), EPS);
	}
}
