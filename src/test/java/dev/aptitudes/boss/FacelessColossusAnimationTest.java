package dev.aptitudes.boss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacelessColossusAnimationTest {
	private static final String RESOURCE = "/assets/aptitudes/animations/entity/faceless_colossus.animation.json";

	@Test
	void actionClipsCoverTheServerWindupAndRecovery() throws Exception {
		try (var reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(RESOURCE)),
				StandardCharsets.UTF_8)) {
			JsonObject document = JsonParser.parseReader(reader).getAsJsonObject();
			assertEquals("1.8.0", document.get("format_version").getAsString());
			JsonObject clips = document.getAsJsonObject("animations");
			for (var move : FacelessColossusMoves.ALL) {
				assertActionLength(clips, move.id(), move.windup() + FacelessColossusMoves.recoveryTicks(move));
			}
			assertActionLength(clips, "phase_two", FacelessColossusMoves.PHASE_ANIMATION_TICKS);
			assertActionLength(clips, "stun", PostureRules.STUN_TICKS);
			assertTrue(clips.getAsJsonObject("idle").get("loop").getAsBoolean());
			assertTrue(clips.getAsJsonObject("walk").get("loop").getAsBoolean());
		}
	}

	private static void assertActionLength(JsonObject clips, String name, int ticks) {
		JsonObject animation = clips.getAsJsonObject(name);
		assertEquals(ticks / 20.0, animation.get("animation_length").getAsDouble(), 1e-6, name);
		assertFalse(animation.has("loop") && animation.get("loop").getAsBoolean(), name);
	}
}
