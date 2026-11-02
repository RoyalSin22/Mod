package dev.aptitudes.role;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoleRulesTest {
	private final RoleRules rules = new RoleRules(30, 30.0, 1.4, 3);

	@Test
	public void treePointsFollowTheDocumentedFormula() {
		assertEquals(0, rules.earnedPoints(0));
		assertEquals(2, rules.earnedPoints(1));   // 1 + 3/3
		assertEquals(31, rules.earnedPoints(30)); // 1 + 90/3 (documentado: 31 puntos a nivel 30)
	}

	@Test
	public void pointsNeverDecreaseAndXpGrows() {
		int previousPoints = 0;
		int previousXp = 0;
		for (int level = 0; level <= 30; level++) {
			assertTrue(rules.earnedPoints(level) >= previousPoints);
			assertTrue(rules.xpToNext(level) >= previousXp);
			assertTrue(rules.xpToNext(level) >= 1);
			previousPoints = rules.earnedPoints(level);
			previousXp = rules.xpToNext(level);
		}
	}

	@Test
	public void zeroIntervalDoesNotDivideByZero() {
		assertEquals(91, new RoleRules(30, 30, 1.4, 0).earnedPoints(30)); // intervalo 0 se trata como 1
	}

	@Test
	public void hugeLevelsDoNotOverflow() {
		assertTrue(new RoleRules(1000, 1e9, 4.0, 3).xpToNext(999) > 0);
	}
}
