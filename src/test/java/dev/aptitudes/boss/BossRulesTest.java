package dev.aptitudes.boss;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BossRulesTest {
	@Test void phaseThresholdsAreOrderedAndInvalidHealthStaysInPhaseOne() {
		assertEquals(1, BossRules.phase(0.9));
		assertEquals(2, BossRules.phase(2.0 / 3.0));
		assertEquals(3, BossRules.phase(1.0 / 3.0));
		assertEquals(1, BossRules.phase(Double.NaN));
	}

	@Test void strikeMatchesTheMarkedCircleAndDoesNotReachAnotherFloor() {
		assertTrue(BossRules.inStrike(4.5, 2.0, 0.0, 4.5));
		assertTrue(BossRules.inStrike(0.0, -2.0, 6.0, 6.0));
		assertFalse(BossRules.inStrike(4.0, 0.0, 4.0, 4.5));
		assertFalse(BossRules.inStrike(0.0, 2.01, 0.0, 6.0));
		assertFalse(BossRules.inStrike(0.0, -2.01, 0.0, 6.0));
	}

	@Test void weaponAcceptsEachOfThreeCoresOnlyOnce() {
		assertTrue(BossRules.canApplyCore(List.of(), "tower"));
		assertFalse(BossRules.canApplyCore(List.of("tower"), "tower"));
		assertTrue(BossRules.canApplyCore(List.of("tower"), "shrine"));
		assertFalse(BossRules.canApplyCore(List.of("tower", "shrine", "camp"), "camp"));
		assertFalse(BossRules.canApplyCore(List.of(), "unknown"));
		assertEquals(1.5, BossRules.damageBonus(9));
	}
}
