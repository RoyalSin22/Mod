package dev.aptitudes.data;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.aptitudes.logic.MasteryRules;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MasteryProgressCodecTest {
	@Test void roundTripsSkillsAndRolesWithoutRecountingAnAward() {
		MasteryProgress data = new MasteryProgress();
		data.add("skill/mining", 2250);
		data.add("role/mage", 1000);
		var json = MasteryProgress.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
		MasteryProgress loaded = MasteryProgress.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(data.saved(), loaded.saved());
		assertEquals(2, MasteryRules.points(loaded.xp("skill/mining"), 1000, 250));
		assertEquals(2250, loaded.xp("skill/mining"));
		loaded.add("skill/mining", 1);
		assertEquals(2251, loaded.xp("skill/mining"));
		assertEquals(2250, data.xp("skill/mining"));
	}
	@Test void startsEmptyAndBoundsCorruptValuesAndKeys() {
		var data = MasteryProgress.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
				"{\"skill/mining\":-9,\"role/mage\":9223372036854775807,\"invalid\":20}")).getOrThrow();
		assertEquals(0, data.xp("skill/mining"));
		assertEquals(MasteryRules.MAX_XP, data.xp("role/mage"));
		assertFalse(data.saved().containsKey("invalid"));
		assertTrue(MasteryProgress.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow().saved().isEmpty());
	}
	@Test void limitsStoredEntriesAndDoesNotShareMapsBetweenPlayers() {
		Map<String, Long> input = new LinkedHashMap<>();
		for (int i = 0; i < 100; i++) input.put("skill/" + "a".repeat(1 + i / 26) + (char) ('a' + i % 26), 1000L);
		MasteryProgress first = new MasteryProgress(input);
		assertEquals(64, first.saved().size());
		input.clear();
		assertEquals(64, first.saved().size());
		MasteryProgress second = new MasteryProgress(Map.of("role/mage", 1000L, "skill/mining", 2000L));
		second.clearRoles();
		assertEquals(0, second.xp("role/mage"));
		assertEquals(2000, second.xp("skill/mining"));
		assertEquals(64, first.saved().size());
		assertThrows(UnsupportedOperationException.class, () -> second.saved().clear());
	}
}
