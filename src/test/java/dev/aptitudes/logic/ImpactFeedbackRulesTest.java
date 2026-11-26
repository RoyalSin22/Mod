package dev.aptitudes.logic;

import dev.aptitudes.logic.ImpactFeedbackRules.Kind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpactFeedbackRulesTest {
	@Test
	void criticalAlwaysWins() {
		assertEquals(Kind.CRITICAL, ImpactFeedbackRules.classify(1, 20, true, false));
		assertEquals(Kind.CRITICAL, ImpactFeedbackRules.classify(30, 20, true, true));
	}

	@Test
	void heavyByRatioOrByAbsoluteDamage() {
		assertEquals(Kind.HEAVY, ImpactFeedbackRules.classify(4, 20, false, false)); // 20 % de la vida
		assertEquals(Kind.HEAVY, ImpactFeedbackRules.classify(8, 300, false, false)); // 8 de daño aunque sea poco para un jefe
		assertNull(ImpactFeedbackRules.classify(3, 20, false, false));
	}

	@Test
	void plainAbilityHitsGetANormalSignalButPlainVanillaHitsDoNot() {
		assertEquals(Kind.NORMAL, ImpactFeedbackRules.classify(3, 20, false, true));
		assertNull(ImpactFeedbackRules.classify(3, 20, false, false));
	}

	@Test
	void invalidAmountsGetNoSignal() {
		assertNull(ImpactFeedbackRules.classify(0, 20, true, true));
		assertNull(ImpactFeedbackRules.classify(-5, 20, false, true));
		assertNull(ImpactFeedbackRules.classify(Double.NaN, 20, false, true));
		assertEquals(Kind.HEAVY, ImpactFeedbackRules.classify(9, 0, false, false)); // sin vida máxima conocida: solo el daño absoluto
	}

	@Test
	void effectiveHealStopsAtTheMaximum() {
		assertEquals(4, ImpactFeedbackRules.effectiveHeal(16, 20, 6), 1e-9);
		assertEquals(2, ImpactFeedbackRules.overheal(16, 20, 6), 1e-9);
		assertEquals(0, ImpactFeedbackRules.effectiveHeal(20, 20, 6), 1e-9);
		assertEquals(6, ImpactFeedbackRules.overheal(20, 20, 6), 1e-9);
		assertEquals(6, ImpactFeedbackRules.effectiveHeal(5, 20, 6), 1e-9);
		assertEquals(0, ImpactFeedbackRules.effectiveHeal(5, 20, -3), 1e-9);
		assertEquals(0, ImpactFeedbackRules.effectiveHeal(5, 0, 3), 1e-9);
	}

	@Test
	void throttleLetsTheFirstSignalThroughAndSpacesTheRest() {
		assertTrue(ImpactFeedbackRules.due(-1, 100, 2));
		assertFalse(ImpactFeedbackRules.due(100, 101, 2));
		assertTrue(ImpactFeedbackRules.due(100, 102, 2));
		assertTrue(ImpactFeedbackRules.due(500, 100, 10)); // el reloj retrocedió (otro mundo o reinicio): se deja pasar
	}

	@Test
	void weightIsTheClampedDamageFraction() {
		assertEquals(0.5f, ImpactFeedbackRules.weight(10, 20), 1e-6);
		assertEquals(1f, ImpactFeedbackRules.weight(50, 20), 1e-6);
		assertEquals(0f, ImpactFeedbackRules.weight(5, 0), 1e-6);
	}

	@Test
	void kindIndexRoundTripsAndFallsBack() {
		for (Kind k : Kind.values()) assertEquals(k, Kind.byIndex(k.ordinal()));
		assertEquals(Kind.NORMAL, Kind.byIndex(99));
		assertEquals(Kind.NORMAL, Kind.byIndex(-1));
	}
}
