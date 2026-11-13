package dev.aptitudes.event;

import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FogErranteRulesTest {
	@Test
	void administrativeAndLegacyMobsAreNeverAssignedToAnUnrelatedSession() {
		UUID active = new UUID(2, 3);
		assertFalse(FogErranteRules.eventOwned(null, false));
		assertTrue(FogErranteRules.eventOwned(null, true));
		assertFalse(FogErranteRules.belongsToSession(null, active, true));
		assertFalse(FogErranteRules.belongsToSession(new UUID(5, 6), active, true));
		assertFalse(FogErranteRules.belongsToSession(active, active, false));
		assertTrue(FogErranteRules.belongsToSession(active, active, true));
	}

	@Test
	void exclusiveLootRequiresAnActiveMatchingSessionWithoutRetirement() {
		UUID origin = new UUID(7, 8);
		assertTrue(FogErranteRules.eligibleLoot(origin, origin, true, false));
		assertFalse(FogErranteRules.eligibleLoot(origin, origin, true, true));
		assertFalse(FogErranteRules.eligibleLoot(origin, null, true, false));
		assertFalse(FogErranteRules.eligibleLoot(null, origin, true, false));
		assertFalse(FogErranteRules.eligibleLoot(origin, new UUID(9, 10), true, false));
	}

	@Test
	void deadlinesAreStableAndDistributedAcrossTheApprovedWindow() {
		long end = 1000;
		for (int index = 0; index <= 100; index++) {
			UUID entity = new UUID(0, index);
			assertEquals(end + 20 + index, FogErranteRules.removalAt(entity, end));
			assertEquals(FogErranteRules.removalAt(entity, end), FogErranteRules.removalAt(entity, end));
		}
		assertEquals(8, FogErranteRules.MAX_REMOVALS_PER_TICK);
	}

	@Test
	void oldAndCurrentWorldStateRoundTrip() {
		WorldEventState legacy = WorldEventState.fromNbt(new NbtCompound(), null);
		assertNull(legacy.fogSession);
		assertNull(legacy.lastClosedFogSession);
		legacy.fogSession = new UUID(13, 17);
		legacy.lastClosedFogSession = new UUID(11, 12);
		legacy.lastFogCloseTick = 12000;
		WorldEventState saved = WorldEventState.fromNbt(legacy.writeNbt(new NbtCompound(), null), null);
		assertEquals(legacy.fogSession, saved.fogSession);
		assertEquals(legacy.lastClosedFogSession, saved.lastClosedFogSession);
		assertEquals(legacy.lastFogCloseTick, saved.lastFogCloseTick);
	}
}
