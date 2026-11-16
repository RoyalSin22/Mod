package dev.aptitudes.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas puras del Gran Osario Quebrado (V68): aparición, reparto del trofeo, curas, escudo y disolución. */
class OsarioBossRulesTest {
	private static final long DAY = 24_000L;

	// ------------------------------------------------------------------------------------------- Aparición

	@Test void threshold_isThePercentOfTheEventDuration() {
		assertEquals(18_000L, OsarioBossRules.spawnThreshold(DAY, 75));
		assertEquals(2_400L, OsarioBossRules.spawnThreshold(DAY, 10));
		assertEquals(DAY, OsarioBossRules.spawnThreshold(DAY, 100));
	}

	@Test void threshold_clampsAbsurdPercentsAndDurations() {
		assertEquals(2_400L, OsarioBossRules.spawnThreshold(DAY, -5));
		assertEquals(DAY, OsarioBossRules.spawnThreshold(DAY, 400));
		assertEquals(0L, OsarioBossRules.spawnThreshold(-1, 75));
	}

	@Test void spawns_onlyOncePerSession_afterTheThreshold() {
		assertFalse(OsarioBossRules.shouldSpawn(17_999, DAY, 75, false, true, false, 1));
		assertTrue(OsarioBossRules.shouldSpawn(18_000, DAY, 75, false, true, false, 1));
		assertTrue(OsarioBossRules.shouldSpawn(23_999, DAY, 75, false, true, false, 3));
		assertFalse(OsarioBossRules.shouldSpawn(20_000, DAY, 75, true, true, false, 1));
	}

	@Test void doesNotSpawn_whenDisabledPeacefulOrNobodyIsInTheOverworld() {
		assertFalse(OsarioBossRules.shouldSpawn(20_000, DAY, 75, false, false, false, 1));
		assertFalse(OsarioBossRules.shouldSpawn(20_000, DAY, 75, false, true, true, 1));
		assertFalse(OsarioBossRules.shouldSpawn(20_000, DAY, 75, false, true, false, 0));
	}

	@Test void spawnDistance_staysInsideTheDesignedBand() {
		for (double roll = 0; roll <= 1.0; roll += 0.1) {
			double d = OsarioBossRules.spawnDistance(roll);
			assertTrue(d >= OsarioBossRules.SPAWN_MIN && d <= OsarioBossRules.SPAWN_MAX, "d=" + d);
		}
		assertEquals(OsarioBossRules.SPAWN_MIN, OsarioBossRules.spawnDistance(-3));
		assertEquals(OsarioBossRules.SPAWN_MAX, OsarioBossRules.spawnDistance(9));
		assertEquals(OsarioBossRules.SPAWN_MIN, OsarioBossRules.spawnDistance(Double.NaN));
	}

	// ----------------------------------------------------------------------------------------- Disolución

	@Test void dissolves_afterTheLingerTimeSinceTheFogClosed() {
		assertFalse(OsarioBossRules.shouldDissolve(1_000 + 5_999, 1_000, 6_000));
		assertTrue(OsarioBossRules.shouldDissolve(1_000 + 6_000, 1_000, 6_000));
		assertTrue(OsarioBossRules.shouldDissolve(1_000, 1_000, 0));
	}

	@Test void dissolves_neverWhileTheFogIsStillOpen() {
		assertFalse(OsarioBossRules.shouldDissolve(99_999, -1, 6_000));
	}

	// ---------------------------------------------------------------------------------- Reparto del trofeo

	@Test void eligible_needsTheMinimumDamageShare() {
		assertFalse(OsarioBossRules.eligible(33.9f, 340f, 10));
		assertTrue(OsarioBossRules.eligible(34f, 340f, 10));
		assertTrue(OsarioBossRules.eligible(340f, 340f, 10));
		assertFalse(OsarioBossRules.eligible(0f, 340f, 10));
	}

	@Test void eligible_isSafeWithBadInput() {
		assertFalse(OsarioBossRules.eligible(50f, 0f, 10));
		assertFalse(OsarioBossRules.eligible(Float.NaN, 340f, 10));
		assertFalse(OsarioBossRules.eligible(-5f, 340f, 10));
		assertTrue(OsarioBossRules.eligible(3.4f, 340f, 1));
		assertTrue(OsarioBossRules.eligible(340f, 340f, 500), "un porcentaje fuera de rango se acota a 100");
		assertFalse(OsarioBossRules.eligible(339f, 340f, 500));
	}

	@Test void shards_stayInTheDesignedRange() {
		assertEquals(OsarioBossRules.SHARD_MIN, OsarioBossRules.shards(0));
		assertEquals(OsarioBossRules.SHARD_MAX, OsarioBossRules.shards(OsarioBossRules.SHARD_MAX - OsarioBossRules.SHARD_MIN));
		assertEquals(OsarioBossRules.SHARD_MIN, OsarioBossRules.shards(-4));
		assertEquals(OsarioBossRules.SHARD_MAX, OsarioBossRules.shards(99));
	}

	// -------------------------------------------------------------------------------- Escudo y acompañantes

	@Test void pact_reducesDamageOnlyWhileAddsLive() {
		assertEquals(1.0f, OsarioBossRules.damageMultiplier(0));
		assertEquals(0.65f, OsarioBossRules.damageMultiplier(1), 1e-6);
		assertEquals(0.65f, OsarioBossRules.damageMultiplier(4), 1e-6);
		assertEquals(1.0f, OsarioBossRules.damageMultiplier(-2));
	}

	@Test void wail_isOnlyUsedWithFewerThanTwoAddsAndNeverExceedsTheCap() {
		assertTrue(OsarioBossRules.canWail(0));
		assertTrue(OsarioBossRules.canWail(1));
		assertFalse(OsarioBossRules.canWail(2));
		assertEquals(2, OsarioBossRules.addsToSummon(0));
		assertEquals(2, OsarioBossRules.addsToSummon(2));
		assertEquals(1, OsarioBossRules.addsToSummon(3));
		assertEquals(0, OsarioBossRules.addsToSummon(4));
		assertEquals(0, OsarioBossRules.addsToSummon(9));
		assertEquals(2, OsarioBossRules.addsToSummon(-3));
	}

	// ---------------------------------------------------------------------------------------------- Curas

	@Test void lash_healsPerPlayerHitWithACapPerStrike() {
		assertEquals(0f, OsarioBossRules.lashHeal(0, 340f));
		assertEquals(340f * 0.015f, OsarioBossRules.lashHeal(1, 340f), 1e-4);
		assertEquals(340f * 0.03f, OsarioBossRules.lashHeal(2, 340f), 1e-4);
		assertEquals(340f * 0.03f, OsarioBossRules.lashHeal(8, 340f), 1e-4, "tope del 3 %");
		assertEquals(0f, OsarioBossRules.lashHeal(-1, 340f));
		assertEquals(0f, OsarioBossRules.lashHeal(2, 0f));
	}

	@Test void harvest_healsPerConsumedAddWithACapPerStrike() {
		assertEquals(0f, OsarioBossRules.harvestHeal(0, 340f));
		assertEquals(340f * 0.02f, OsarioBossRules.harvestHeal(1, 340f), 1e-4);
		assertEquals(340f * 0.06f, OsarioBossRules.harvestHeal(3, 340f), 1e-4);
		assertEquals(340f * 0.06f, OsarioBossRules.harvestHeal(4, 340f), 1e-4, "tope del 6 %");
		assertEquals(0f, OsarioBossRules.harvestHeal(-2, 340f));
	}

	@Test void nothingHealsThroughMaxHealth() {
		assertEquals(10f, OsarioBossRules.healedTo(8f, 2f, 10f));
		assertEquals(10f, OsarioBossRules.healedTo(9f, 50f, 10f));
		assertEquals(5f, OsarioBossRules.healedTo(5f, 0f, 10f));
		assertEquals(5f, OsarioBossRules.healedTo(5f, -3f, 10f));
	}
}
