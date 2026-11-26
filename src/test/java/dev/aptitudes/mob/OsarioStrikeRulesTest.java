package dev.aptitudes.mob;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.UUID;

import static dev.aptitudes.mob.OsarioStrikeRules.*;
import static org.junit.jupiter.api.Assertions.*;

class OsarioStrikeRulesTest {
	private Bounds point(double x, double y, double z) {
		return new Bounds(x, y, z, x, y, z);
	}

	@Test
	void sectorsRespectWidthReachHeightAndScale() {
		assertNotNull(contact(Attack.CLAW, point(.35, .7, 1.75), 0, 1, 1));
		assertNull(contact(Attack.CLAW, point(1.2, .7, 1.4), 0, 1, 1));
		assertNotNull(contact(Attack.HEAVY, point(1.2, .7, 1.4), 0, 1, 1));
		assertNull(contact(Attack.HEAVY, point(.2, .7, -.4), 0, 1, 1));
		assertNull(contact(Attack.CLAW, point(0, .19, 1.75), 0, 1, 1));
		assertNull(contact(Attack.HEAVY, point(0, 1.71, 1.75), 0, 1, 1));
		assertNull(contact(Attack.CLAW, point(0, 1, 2.2), 0, 1, 1));
		assertNotNull(contact(Attack.CLAW, point(0, 2, 2.2), 0, 1, 1.35));
		assertNotNull(contact(Attack.CLAW, point(-1.75, 1, 0), -1, 0, 1));
	}

	@Test
	void volumeUsesBoxEdgeAndSlamUsesOffsetDisk() {
		assertNotNull(contact(Attack.CLAW, new Bounds(-1.5, -.5, 1.75, 1.5, 2, 3.75), 0, 1, 1));
		assertNotNull(contact(Attack.SLAM, point(2, .3, 1.5), 0, 1, 1));
		assertNotNull(contact(Attack.SLAM, point(0, .3, -.59), 0, 1, 1));
		assertNull(contact(Attack.SLAM, point(0, .3, -.61), 0, 1, 1));
		assertNull(contact(Attack.SLAM, point(0, 1.41, 1.5), 0, 1, 1));
		assertNull(contact(Attack.SLAM, point(2.2, .3, 1.5), 0, 1, 1));
	}

	@Test
	void sweptDashReturnsFirstContactAndRoundedCorners() {
		var first = dashContact(new Bounds(-.3, 0, 1, .3, 2, 1.6), 0, 0, 3, 1);
		assertNotNull(first);
		assertEquals(.15, first.fraction(), 1e-9);
		assertEquals(1, first.point().z(), 1e-9);
		assertNull(dashContact(point(.54, .8, 3.54), 0, 0, 3, 1)); // fuera del arco final
		assertNull(dashContact(point(0, 1.9, 2.2), 0, 0, 3, 1));
		assertNotNull(dashContact(point(.62, 1.9, 2.2), 0, 0, 3, 1.35));
		assertEquals(0, dashContact(point(.20, 1, 0), 0, 0, 0, 1).fraction(), 1e-9);
		assertNotNull(dashContact(point(0, 2, 1), 0, 1, 2, 1));
	}

	@Test
	void actionClockAndImpactDeduplication() {
		for (var attack : Attack.values()) {
			assertTrue(attack.warning < attack.cancelBefore);
			assertTrue(attack.cancelBefore <= attack.impact);
			assertTrue(attack.lastActive < attack.end(true));
			long time = 190_451;
			Action action = Action.of(attack);
			assertEquals(action, Action.from(action.stamp(time)));
			assertEquals(time, action.stamp(time) >>> Action.BITS);
			var execution = new Execution(attack, time, false);
			UUID a = new UUID(1, 1), b = new UUID(1, 2);
			assertFalse(execution.firstAttempt(a, time + attack.impact - 1));
			assertTrue(execution.firstAttempt(a, time + attack.impact));
			assertFalse(execution.firstAttempt(a, time + attack.lastActive));
			assertTrue(execution.firstAttempt(b, time + attack.lastActive));
			assertFalse(execution.firstAttempt(new UUID(1, 3), time + attack.lastActive + 1));
		}
		assertEquals(7, Attack.LUNGE.lastActive - Attack.LUNGE.impact + 1);
		assertEquals(Action.NONE, Action.from(0x0f));
		assertEquals(120, Attack.SLAM.cooldown(true));
		assertEquals(200, Attack.SLAM.cooldown(false));
	}

	@Test
	void selectionRespectsAvailabilityContextAndRepeatCap() {
		var all = EnumSet.allOf(Attack.class);
		var last = new Choice(1.6, true, true, true, false, false, true, all, Attack.CLAW, 2);
		for (int i = 0; i < 128; i++) {
			var selected = choose(last, i);
			assertTrue(selected == Attack.HEAVY || selected == Attack.SLAM);
			assertEquals(selected, choose(last, i));
		}
		assertEquals(Attack.LUNGE, choose(new Choice(4, false, false, false, true, true, false, all, null, 0), 50));
		assertNull(choose(new Choice(4, false, false, false, false, false, false,
				EnumSet.of(Attack.CLAW), null, 0), 50));
		assertNull(choose(new Choice(3.1, false, false, false, false, false, false, all, null, 0), 50));
	}

	@Test
	void threeSlotsPerTargetAndReleaseOnRemoval() {
		var pressure = new Pressure();
		UUID player = new UUID(2, 1), other = new UUID(2, 2);
		UUID a = new UUID(1, 1), b = new UUID(1, 2), c = new UUID(1, 3), d = new UUID(1, 4);
		assertTrue(pressure.acquire(player, a));
		assertTrue(pressure.acquire(player, b));
		assertTrue(pressure.acquire(player, c));
		assertFalse(pressure.acquire(player, d));
		assertTrue(pressure.acquire(player, a));
		assertTrue(pressure.acquire(other, d));
		pressure.release(player, b);
		assertTrue(pressure.acquire(player, d));
		pressure.removeTarget(player);
		assertTrue(pressure.acquire(player, b));
	}
}
