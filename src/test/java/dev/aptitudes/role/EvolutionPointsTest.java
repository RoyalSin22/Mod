package dev.aptitudes.role;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Puntos de árbol extra por evolución (v23). */
public class EvolutionPointsTest {
	// Mismos valores que Evolution.BASE/MASTER/ULTIMATE (aquí literales: Evolution carga clases de Minecraft).
	private static final int BASE = 0, MASTER = 1, ULTIMATE = 2;
	private final RoleRules rules = new RoleRules(30, 30.0, 1.4, 3, 10, 5);

	@Test
	public void bonusPointsDependOnTier() {
		assertEquals(31, rules.earnedPoints(30, BASE));
		assertEquals(41, rules.earnedPoints(30, MASTER));
		assertEquals(46, rules.earnedPoints(30, ULTIMATE));
		assertEquals(rules.earnedPoints(30), rules.earnedPoints(30, BASE)); // sin cambios sin evolución
	}

	@Test
	public void legacyConstructorHasNoBonusAndNegativesAreIgnored() {
		RoleRules legacy = new RoleRules(30, 30.0, 1.4, 3);
		assertEquals(31, legacy.earnedPoints(30, ULTIMATE));
		RoleRules broken = new RoleRules(30, 30.0, 1.4, 3, -50, -50);
		assertEquals(31, broken.earnedPoints(30, ULTIMATE));
	}

	@Test
	public void hugeValuesDoNotOverflow() {
		RoleRules huge = new RoleRules(1000, 30.0, 1.4, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, huge.earnedPoints(1000, ULTIMATE));
	}
}
