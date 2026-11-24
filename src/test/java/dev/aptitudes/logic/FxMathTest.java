package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class FxMathTest {
	@Test
	void envelopeRisesHoldsAndDecays() {
		assertEquals(0f, FxMath.envelope(-1, 100, 50, 200));
		assertEquals(0f, FxMath.envelope(0, 100, 50, 200), 1e-6);
		assertTrue(FxMath.envelope(50, 100, 50, 200) > 0.5f);
		assertEquals(1f, FxMath.envelope(100, 100, 50, 200), 1e-6);
		assertEquals(1f, FxMath.envelope(140, 100, 50, 200), 1e-6);
		float early = FxMath.envelope(180, 100, 50, 200);
		float late = FxMath.envelope(300, 100, 50, 200);
		assertTrue(early > late && late > 0f);
		assertEquals(0f, FxMath.envelope(350, 100, 50, 200));
		assertEquals(350, FxMath.envelopeLength(100, 50, 200));
	}

	@Test
	void envelopeWithoutAttackStartsAtFull() {
		assertEquals(1f, FxMath.envelope(0, 0, 0, 100), 1e-6);
	}

	@Test
	void falloffIsSmoothAndBounded() {
		assertEquals(1f, FxMath.falloff(0, 20), 1e-6);
		assertEquals(0f, FxMath.falloff(20, 20));
		assertEquals(0f, FxMath.falloff(35, 20));
		assertEquals(0.5f, FxMath.falloff(10, 20), 1e-6);
		assertTrue(FxMath.falloff(5, 20) > FxMath.falloff(15, 20));
		assertEquals(0f, FxMath.falloff(3, 0));
	}

	@Test
	void ringExpandsAndSlowsDown() {
		assertEquals(0, FxMath.ringRadius(0, 10, 8), 1e-9);
		assertEquals(8, FxMath.ringRadius(10, 10, 8), 1e-9);
		double first = FxMath.ringRadius(1, 10, 8) - FxMath.ringRadius(0, 10, 8);
		double last = FxMath.ringRadius(10, 10, 8) - FxMath.ringRadius(9, 10, 8);
		assertTrue(first > last);
		assertEquals(8, FxMath.ringRadius(3, 0, 8), 1e-9);
	}

	@Test
	void noiseIsDeterministicSmoothAndInRange() {
		assertEquals(FxMath.smoothNoise(3.37f, 5), FxMath.smoothNoise(3.37f, 5));
		assertNotEquals(FxMath.smoothNoise(3.37f, 5), FxMath.smoothNoise(3.37f, 6));
		float prev = FxMath.smoothNoise(0f, 9);
		for (float x = 0.01f; x < 20f; x += 0.01f) {
			float v = FxMath.smoothNoise(x, 9);
			assertTrue(v >= -1.001f && v <= 1.001f);
			assertTrue(Math.abs(v - prev) < 0.12f, "salto brusco en x=" + x);
			prev = v;
		}
	}

	@Test
	void coneDirectionIsUnitAndInsideTheCone() {
		Random rng = new Random(7);
		for (int i = 0; i < 500; i++) {
			double[] d = FxMath.coneDirection(rng, 90, 20, 10, 50);
			assertEquals(1.0, Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]), 1e-9);
			assertTrue(d[1] >= Math.sin(Math.toRadians(10)) - 1e-9 && d[1] <= Math.sin(Math.toRadians(50)) + 1e-9);
			double yaw = Math.toDegrees(Math.atan2(-d[0], d[2]));
			assertTrue(Math.abs(yaw - 90) <= 20.0001, "fuera del cono: " + yaw);
		}
	}

	@Test
	void coneAtYawZeroPointsToPositiveZ() {
		double[] d = FxMath.coneDirection(new Random(1), 0, 0, 0, 0);
		assertEquals(0, d[0], 1e-9);
		assertEquals(0, d[1], 1e-9);
		assertEquals(1, d[2], 1e-9);
	}

	@Test
	void ringPointLiesOnTheCircle() {
		double[] p = FxMath.ringPoint(Math.PI / 3, 4);
		assertEquals(4, Math.hypot(p[0], p[1]), 1e-9);
	}

	@Test
	void scaledCountRespectsSettingsButNeverVanishesWhenEnabled() {
		assertEquals(40, FxMath.scaledCount(40, 1f, 1f));
		assertEquals(20, FxMath.scaledCount(40, 1f, 0.5f));
		assertEquals(1, FxMath.scaledCount(2, 0.1f, 0.25f));
		assertEquals(0, FxMath.scaledCount(40, 0f, 1f));
		assertEquals(0, FxMath.scaledCount(40, 1f, 0f));
		assertEquals(0, FxMath.scaledCount(0, 1f, 1f));
	}

	@Test
	void fxIdsAreUniqueAndValid() {
		int[] ids = {FxIds.BERSERKER_RAGE, FxIds.BERSERKER_WAR_CRY, FxIds.BERSERKER_LEAP_TAKEOFF,
				FxIds.BERSERKER_LEAP_TRAIL, FxIds.BERSERKER_SLAM, FxIds.BERSERKER_WHIRL,
				FxIds.MAGE_BOLT, FxIds.MAGE_BLINK, FxIds.MAGE_TIME_WARP, FxIds.MAGE_MISSILE,
				FxIds.ASSASSIN_VANISH, FxIds.ASSASSIN_STEP, FxIds.ASSASSIN_FAN, FxIds.ASSASSIN_KNIFE, FxIds.ASSASSIN_SHURIKEN,
				FxIds.HERBALIST_BALM, FxIds.HERBALIST_BALM_ALLY, FxIds.HERBALIST_RAIN, FxIds.HERBALIST_SPORE,
				FxIds.ENGINEER_GRAPPLE, FxIds.ENGINEER_GRENADE};
		java.util.Set<Integer> seen = new java.util.HashSet<>();
		for (int id : ids) {
			assertTrue(FxIds.valid(id));
			assertTrue(seen.add(id), "id repetido " + id);
		}
		assertFalse(FxIds.valid(0));
		assertFalse(FxIds.valid(FxIds.MAX + 1));
	}
}
