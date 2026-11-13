package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** v67 (F6): cifras del Cambiaformas. */
class ShapeshifterRulesTest {
	@Test
	void passiveValues() {
		assertEquals(0.5f, ShapeshifterRules.metabolismHeal(1), 1e-6);
		assertEquals(1.5f, ShapeshifterRules.metabolismHeal(3), 1e-6);
		assertEquals(0.04, ShapeshifterRules.coldBlood(1), 1e-9);
		assertEquals(0.12, ShapeshifterRules.coldBlood(3), 1e-9);
		assertEquals(4f, ShapeshifterRules.skinAbsorption(1), 1e-6);
		assertEquals(6f, ShapeshifterRules.skinAbsorption(2), 1e-6);
		assertEquals(0f, ShapeshifterRules.skinAbsorption(0), 1e-6);
	}

	@Test
	void activeValues() {
		assertEquals(6, ShapeshifterRules.biteDamage(1, 1), 1e-9);
		assertEquals(10, ShapeshifterRules.biteDamage(3, 1), 1e-9);
		assertEquals(2f, ShapeshifterRules.biteHeal(1), 1e-6);
		assertEquals(4f, ShapeshifterRules.biteHeal(3), 1e-6);
		assertEquals(60, ShapeshifterRules.swiftTicks(1));
		assertEquals(100, ShapeshifterRules.swiftTicks(3));
		assertEquals(5, ShapeshifterRules.swipeDamage(1, 1), 1e-9);
		assertEquals(6, ShapeshifterRules.swipeDamage(2, 1), 1e-9);
		assertEquals(160, ShapeshifterRules.furyTicks(1));
		assertEquals(200, ShapeshifterRules.furyTicks(2));
		assertEquals(8, ShapeshifterRules.stampedeDamage(1), 1e-9);
	}

	@Test
	void nextFormCyclesBackToHuman() {
		assertEquals(0, ShapeshifterRules.nextForm(-1, 3));
		assertEquals(2, ShapeshifterRules.nextForm(1, 3));
		assertEquals(-1, ShapeshifterRules.nextForm(2, 3), "tras la última, vuelve a humano");
		assertEquals(-1, ShapeshifterRules.nextForm(-1, 0));
	}

	@Test
	void thirdChangeMakesPowerCheaper() {
		assertEquals(0.8, ShapeshifterRules.thirdChangeCost(1), 1e-9);
		assertEquals(0.6, ShapeshifterRules.thirdChangeCost(2), 1e-9);
		assertEquals(1.0, ShapeshifterRules.thirdChangeCost(0), 1e-9);
		assertEquals(0.6, ShapeshifterRules.thirdChangeCost(9), 1e-9, "tope de rango 2");
	}
}
