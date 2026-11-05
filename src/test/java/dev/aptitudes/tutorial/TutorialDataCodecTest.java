package dev.aptitudes.tutorial;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.aptitudes.tutorial.TutorialRules.Step;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** El historial real del attachment puede probarse con JSON sin cargar registros de Minecraft. */
class TutorialDataCodecTest {
	@Test
	void emptySavedDataDefaultsToTheFirstStep() {
		TutorialData data = decode("{}");
		assertEquals(new TutorialData(), data);
		assertEquals(Step.GUIDE, TutorialRules.next(data.state()));
		assertFalse(data.state().skipped());
	}

	@Test
	void reloadKeepsActionsOutOfOrderAndPaidRewardsSeparate() {
		TutorialData before = new TutorialData().observe(Step.GUIDE).claim(Step.GUIDE)
				.observe(Step.PRIMARY_ROLE).observe(Step.GUILD_CRAFT);
		TutorialData loaded = reload(before);
		assertEquals(before, loaded);
		assertEquals(Set.of(Step.GUIDE), loaded.state().rewarded());
		assertEquals(Step.BONFIRE, TutorialRules.next(loaded.state()));
		assertNull(TutorialRules.pendingReward(loaded.state()));
		assertEquals(loaded, loaded.claim(Step.GUIDE));
	}

	@Test
	void pendingRewardSurvivesRepeatedReloadsAndIsOnlyPaidOnce() {
		TutorialData data = new TutorialData().observe(Step.GUIDE);
		for (int i = 0; i < 10; i++) {
			data = reload(data);
			assertEquals(Step.GUIDE, TutorialRules.pendingReward(data.state()));
		}
		data = reload(data.claim(Step.GUIDE));
		assertNull(TutorialRules.pendingReward(data.state()));
		assertEquals(data, reload(data.observe(Step.GUIDE).claim(Step.GUIDE)));
	}

	@Test
	void skippedFlagAndAlreadyPaidRewardsSurviveReload() {
		TutorialData data = new TutorialData().observe(Step.GUIDE).claim(Step.GUIDE).observe(Step.BONFIRE).skip();
		TutorialData loaded = reload(data);
		assertEquals(data, loaded);
		assertTrue(loaded.state().skipped());
		assertNull(TutorialRules.next(loaded.state()));
		assertNull(TutorialRules.pendingReward(loaded.state()));
		assertEquals(loaded, loaded.observe(Step.ACTIVE).claim(Step.BONFIRE));
	}

	@Test
	void duplicateAndUnknownKeysAreBoundedAndPaidEvidenceIsKept() {
		TutorialData data = decode("""
				{"observed":["guide","guide","combo","unknown","-1","999"],
				 "rewarded":["guide","bonfire","bonfire","unknown"],"skipped":false}
				""");
		assertEquals(Set.of(Step.GUIDE, Step.BONFIRE, Step.COMBO), data.state().observed());
		assertEquals(Set.of(Step.GUIDE, Step.BONFIRE), data.state().rewarded());
		assertEquals(data, reload(data));
		JsonElement encoded = encode(data);
		assertEquals(JsonParser.parseString("[\"guide\",\"bonfire\",\"combo\"]"), encoded.getAsJsonObject().get("observed"));
		assertEquals(JsonParser.parseString("[\"guide\",\"bonfire\"]"), encoded.getAsJsonObject().get("rewarded"));
	}

	@Test
	void fullyPaidTutorialCannotRestartAfterReload() {
		TutorialData data = new TutorialData();
		for (Step step : TutorialRules.ORDER) data = data.observe(step).claim(step);
		TutorialData loaded = reload(data);
		assertEquals(6, loaded.state().rewarded().size());
		assertNull(TutorialRules.next(loaded.state()));
		assertNull(TutorialRules.pendingReward(loaded.state()));
		for (Step step : TutorialRules.ORDER) assertEquals(loaded, loaded.observe(step).claim(step));
	}

	private static JsonElement encode(TutorialData data) {
		return TutorialData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
	}

	private static TutorialData decode(String json) {
		return TutorialData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
	}

	private static TutorialData reload(TutorialData data) { return decode(encode(data).toString()); }
}
