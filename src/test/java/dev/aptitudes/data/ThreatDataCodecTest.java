package dev.aptitudes.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.aptitudes.logic.ThreatRules;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreatDataCodecTest {
	@Test void roundTripKeepsTheSpawnSnapshotAndClaimAfterReload() {
		var original = new ThreatData(5, 1.75, 1.5, 0.5, false).claim();
		var json = ThreatData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		var loaded = ThreatData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(original, loaded);
		assertFalse(ThreatRules.bonusLoot(true, true, loaded.claimed(), loaded.loot(), 0));
		assertEquals(loaded, loaded.claim());
	}
	@Test void emptyOrPartialDataHasSafeDefaults() {
		var empty = ThreatData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow();
		assertEquals(new ThreatData(0, 1, 1, 0, false), empty);
		var partial = ThreatData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"ring\":2}")).getOrThrow();
		assertEquals(new ThreatData(2, 1, 1, 0, false), partial);
	}
	@Test void corruptPersistedValuesAreBoundedByTheConstructor() {
		var bounded = ThreatData.CODEC.parse(JsonOps.INSTANCE,
				JsonParser.parseString("{\"ring\":200,\"health\":1e99,\"damage\":1e99,\"loot\":1e99}")).getOrThrow();
		assertEquals(new ThreatData(5, 3, 2, 1, false), bounded);
		assertEquals(new ThreatData(0, 1, 1, 0, false), new ThreatData(-1, 3, 2, 1, false));
		assertEquals(new ThreatData(5, 1, 1, 0, true), new ThreatData(5, Double.NaN, -3, Double.POSITIVE_INFINITY, true));
	}
}
