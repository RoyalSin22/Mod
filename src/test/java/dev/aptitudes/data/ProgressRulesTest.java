package dev.aptitudes.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProgressRulesTest {
	@Test
	public void xpCurveIsPositiveAndIncreasing() {
		ProgressRules rules = new ProgressRules(50, 20.0, 1.35, 99, 0.02);
		assertEquals(20, rules.xpToNext(0));
		int previous = 0;
		for (int level = 0; level < 50; level++) {
			int xp = rules.xpToNext(level);
			assertTrue(xp >= 1 && xp >= previous);
			previous = xp;
		}
	}

	@Test
	public void extremeConfigurationIsClampedToIntRange() {
		ProgressRules rules = new ProgressRules(1000, 1e12, 4.0, 99, 0.02);
		assertEquals(Integer.MAX_VALUE, rules.xpToNext(999));
	}
}
