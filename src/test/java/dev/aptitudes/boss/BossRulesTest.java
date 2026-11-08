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

	@Test void oracleRingLeavesItsCenterSafe() {
		var ring = BossRules.StrikeShape.RING;
		assertFalse(ring.contains(0, 0, 0, 0));
		assertFalse(ring.contains(1.99, 0, 0, 0));
		assertTrue(ring.contains(2, 0, 0, 0));
		assertTrue(ring.contains(-6, 0, 0, 0));
		assertFalse(ring.contains(6.01, 0, 0, 0));
		assertFalse(ring.contains(4, 2.01, 0, 0));
	}

	@Test void championConeOnlyHitsInsideItsOriginalHeading() {
		var cone = BossRules.StrikeShape.CONE;
		assertTrue(cone.contains(6, 0, 0, 0));
		assertTrue(cone.contains(3, 0, 3, 0));
		assertFalse(cone.contains(-3, 0, 0, 0));
		assertFalse(cone.contains(0, 0, 3, 0));
		assertFalse(cone.contains(3, 0, 3.01, 0));
		assertTrue(cone.contains(0, 0, 3, Math.PI / 2));
		assertFalse(cone.contains(0, 0, 6.01, Math.PI / 2));
		assertFalse(cone.contains(4, 0, 0, Double.NaN));
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
