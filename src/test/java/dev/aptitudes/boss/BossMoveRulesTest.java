package dev.aptitudes.boss;

import dev.aptitudes.boss.BossMoveRules.Move;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BossMoveRulesTest {
	private static final Move SWEEP = new Move("sweep", 1, 0, 4, 20, 40, 3);
	private static final Move SLAM = new Move("slam", 1, 0, 5, 30, 80, 2);
	private static final Move CHARGE = new Move("charge", 2, 5, 16, 15, 100, 2);
	private static final List<Move> ALL = List.of(SWEEP, SLAM, CHARGE);

	@Test public void phaseAndRangeFilterTheRepertoire() {
		assertEquals(List.of(SWEEP, SLAM), BossMoveRules.eligible(ALL, 1, 3, Map.of(), 0));
		assertEquals(List.of(), BossMoveRules.eligible(ALL, 1, 8, Map.of(), 0));
		assertEquals(List.of(CHARGE), BossMoveRules.eligible(ALL, 2, 8, Map.of(), 0));
		assertEquals(List.of(SLAM), BossMoveRules.eligible(ALL, 3, 4.5, Map.of(), 0));
	}

	@Test public void cooldownsBlockUntilReady() {
		Map<String, Long> ready = Map.of("sweep", 100L);
		assertEquals(List.of(SLAM), BossMoveRules.eligible(ALL, 1, 3, ready, 99));
		assertEquals(List.of(SWEEP, SLAM), BossMoveRules.eligible(ALL, 1, 3, ready, 100));
	}

	@Test public void choiceIsWeightedAndDeterministic() {
		// Pesos 3 (sweep) y 2 (slam): tiradas 0–2 → sweep, 3–4 → slam, y vuelve a empezar.
		assertEquals(SWEEP, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, 0));
		assertEquals(SWEEP, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, 2));
		assertEquals(SLAM, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, 3));
		assertEquals(SLAM, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, 4));
		assertEquals(SWEEP, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, 5));
		assertEquals(SLAM, BossMoveRules.choose(ALL, 1, 3, Map.of(), 0, -1));
		assertNull(BossMoveRules.choose(ALL, 1, 30, Map.of(), 0, 0));
	}

	@Test public void movesAreSanitised() {
		Move odd = new Move("x", 1, 0, 1, -5, -9, 0);
		assertEquals(1, odd.windup());
		assertEquals(0, odd.cooldown());
		assertEquals(1, odd.weight());
		assertThrows(IllegalArgumentException.class, () -> new Move(" ", 1, 0, 1, 1, 1, 1));
	}

	@Test public void arcFollowsTheFacing() {
		// Yaw 0 mira al sur (+z).
		assertTrue(BossMoveRules.inArc(0, 3, 0, 120, 4));
		assertFalse(BossMoveRules.inArc(0, -3, 0, 120, 4));
		assertFalse(BossMoveRules.inArc(0, 5, 0, 120, 4));
		// 50° a un lado entra en un arco de 120°, 70° no.
		assertTrue(BossMoveRules.inArc(Math.sin(Math.toRadians(50)) * 2, Math.cos(Math.toRadians(50)) * 2, 0, 120, 4));
		assertFalse(BossMoveRules.inArc(Math.sin(Math.toRadians(70)) * 2, Math.cos(Math.toRadians(70)) * 2, 0, 120, 4));
		// Yaw 90 mira al oeste (−x).
		assertTrue(BossMoveRules.inArc(-3, 0, 90, 60, 4));
		assertTrue(BossMoveRules.inArc(0, 0, 0, 10, 4));
	}

	@Test public void crackedArmorTakesMoreDamage() {
		assertEquals(1f, BossMoveRules.damageTaken(1, 0.25), 1e-6);
		assertEquals(1.25f, BossMoveRules.damageTaken(2, 0.25), 1e-6);
		assertEquals(2f, BossMoveRules.damageTaken(3, 5), 1e-6);
		assertEquals(1f, BossMoveRules.damageTaken(2, -1), 1e-6);
	}
}
