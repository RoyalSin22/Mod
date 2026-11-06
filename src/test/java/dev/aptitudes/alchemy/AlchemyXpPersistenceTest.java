package dev.aptitudes.alchemy;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlchemyXpPersistenceTest {
	@Test
	void savingAndLoadingDoesNotResetTheDiminishingWindow() {
		AlchemyXpData data = new AlchemyXpData(0, 0);
		for (int i = 0; i < 13; i++) data = data.record(1000 + i, 300);
		var json = AlchemyXpData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
		AlchemyXpData loaded = AlchemyXpData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(data, loaded);
		assertEquals(14, loaded.record(1100, 300).brews());
		assertEquals(0, BrewingRules.xp(4, loaded.brews(), 3));
	}

	@Test
	void resetsOnlyAtTheWindowBoundary() {
		AlchemyXpData data = new AlchemyXpData(1000, 13);
		assertEquals(14, data.record(6999, 300).brews());
		assertEquals(new AlchemyXpData(7000, 1), data.record(7000, 300));
		assertEquals(new AlchemyXpData(999, 1), data.record(999, 300));
	}

	@Test
	void corruptAndMissingDataAreBounded() {
		assertEquals(new AlchemyXpData(0, 0), AlchemyXpData.CODEC.parse(JsonOps.INSTANCE,
				JsonParser.parseString("{}")).getOrThrow());
		assertEquals(new AlchemyXpData(0, 0), new AlchemyXpData(-1, -100));
		assertEquals(1_000_000, new AlchemyXpData(10, Integer.MAX_VALUE).record(11, Integer.MAX_VALUE).brews());
		assertEquals(new AlchemyXpData(Long.MAX_VALUE, 1), new AlchemyXpData(0, 3).record(Long.MAX_VALUE, Integer.MAX_VALUE));
	}
}
