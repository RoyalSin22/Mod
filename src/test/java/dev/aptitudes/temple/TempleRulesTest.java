package dev.aptitudes.temple;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TempleRulesTest {
	@Test public void puzzleGrowsWithTheThreatRing() {
		assertEquals(3, TempleRules.leverCount(0));
		assertEquals(3, TempleRules.leverCount(1));
		assertEquals(4, TempleRules.leverCount(2));
		assertEquals(4, TempleRules.leverCount(99));
		assertEquals(0, TempleRules.plateCount(-3));
		assertEquals(0, TempleRules.plateCount(1));
		assertEquals(3, TempleRules.plateCount(2));
		assertEquals(3, TempleRules.plateCount(3));
		assertEquals(4, TempleRules.plateCount(5));
	}

	@Test public void patternIsNeverTrivial() {
		for (int count = 3; count <= 4; count++) {
			for (int bits = 0; bits < 64; bits++) {
				boolean[] p = TempleRules.pattern(bits, count);
				assertEquals(count, p.length);
				int on = 0;
				for (boolean b : p) if (b) on++;
				assertTrue("todas apagadas", on > 0);
				assertTrue("todas encendidas", on < count);
			}
		}
		assertArrayEquals(new boolean[]{true, false, true}, TempleRules.pattern(0b101, 3));
	}

	@Test public void leversMustMatchExactly() {
		boolean[] target = {true, false, true};
		assertTrue(TempleRules.leversSolved(new boolean[]{true, false, true}, target));
		assertFalse(TempleRules.leversSolved(new boolean[]{true, true, true}, target));
		assertFalse(TempleRules.leversSolved(new boolean[]{true, false}, target));
		assertFalse(TempleRules.leversSolved(null, target));
	}

	@Test public void platesAdvanceInOrderAndResetOnMistakes() {
		boolean[] none = {false, false, false};
		assertEquals(0, TempleRules.plateProgress(0, none));
		assertEquals(1, TempleRules.plateProgress(0, new boolean[]{true, false, false}));
		// Quedarse sobre la ya superada no cambia nada.
		assertEquals(1, TempleRules.plateProgress(1, new boolean[]{true, false, false}));
		assertEquals(2, TempleRules.plateProgress(1, new boolean[]{true, true, false}));
		// Saltarse una reinicia.
		assertEquals(0, TempleRules.plateProgress(1, new boolean[]{false, false, true}));
		assertEquals(0, TempleRules.plateProgress(0, new boolean[]{false, true, false}));
		// Completo: estable.
		assertEquals(3, TempleRules.plateProgress(3, none));
		assertEquals(3, TempleRules.plateProgress(9, none));
	}

	@Test public void guardianScales() {
		assertEquals(0.0, TempleRules.guardianHealthBonus(0), 1e-9);
		assertEquals(1.25, TempleRules.guardianHealthBonus(5), 1e-9);
		assertEquals(1.25, TempleRules.guardianHealthBonus(12), 1e-9);
	}
}
