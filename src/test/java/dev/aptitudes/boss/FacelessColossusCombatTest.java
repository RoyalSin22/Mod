package dev.aptitudes.boss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FacelessColossusCombatTest {
	@Test
	void recoveryMatchesTheActualClips() throws Exception {
		String path = "/assets/aptitudes/animations/entity/faceless_colossus.animation.json";
		try (var stream = getClass().getResourceAsStream(path)) {
			assertNotNull(stream);
			var animations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
					.getAsJsonObject().getAsJsonObject("animations");
			for (var move : Map.of("sweep", 18, "slam", 28, "charge", 16).entrySet()) {
				int clipTicks = ticks(animations, move.getKey());
				var timing = new BossMoveRules.Move(move.getKey(), 1, 0, 1, move.getValue(), 0, 1);
				assertEquals(clipTicks, FacelessColossusCombat.attackClipTicks(move.getKey()));
				assertEquals(clipTicks + FacelessColossusCombat.TRANSITION_TICKS,
						timing.windup() + FacelessColossusCombat.recoveryTicks(timing));
			}
			assertEquals(ticks(animations, "phase_two"), FacelessColossusCombat.PHASE_CLIP_TICKS);
			assertEquals(PostureRules.STUN_TICKS, ticks(animations, "stun") + FacelessColossusCombat.TRANSITION_TICKS);
		}
	}

	@Test
	void hitboxReachesTheCrownAndEyesStayInsideTheHelmet() throws Exception {
		String path = "/assets/aptitudes/geo/entity/faceless_colossus.geo.json";
		try (var stream = getClass().getResourceAsStream(path)) {
			assertNotNull(stream);
			var bones = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
					.getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0)
					.getAsJsonObject().getAsJsonArray("bones");
			for (var entry : bones) {
				var bone = entry.getAsJsonObject();
				if (!bone.get("name").getAsString().equals("head")) continue;
				double crown = 0;
				for (var part : bone.getAsJsonArray("cubes")) {
					var cube = part.getAsJsonObject();
					double top = cube.getAsJsonArray("origin").get(1).getAsDouble()
							+ cube.getAsJsonArray("size").get(1).getAsDouble();
					crown = Math.max(top, crown);
				}
				assertEquals(crown / 16, FacelessColossusCombat.BODY_HEIGHT, 0.001);
				assertTrue(FacelessColossusCombat.EYE_HEIGHT < FacelessColossusCombat.BODY_HEIGHT);
				return;
			}
			fail("Falta el casco en la geometría del Coloso");
		}
	}

	@Test
	void bladeCanReachAStandingTargetButNotASeparatedFloor() {
		double low = FacelessColossusCombat.SWEEP_LOW;
		double high = FacelessColossusCombat.SWEEP_HIGH;
		assertEquals(1.5, FacelessColossusCombat.contactHeight(0, 1.8, low, high), 0.00001);
		assertTrue(Double.isNaN(FacelessColossusCombat.contactHeight(-2, -0.2, low, high)));
		assertTrue(Double.isNaN(FacelessColossusCombat.contactHeight(high, high + 1.8, low, high)));
	}

	@Test
	void slamRequiresOverlapWithTheGroundShockwave() {
		double low = FacelessColossusCombat.SLAM_LOW;
		double high = FacelessColossusCombat.SLAM_HIGH;
		assertEquals(0.625, FacelessColossusCombat.contactHeight(0, 1.8, low, high));
		assertTrue(Double.isNaN(FacelessColossusCombat.contactHeight(2, 3.8, low, high)));
	}

	private static int ticks(JsonObject animations, String name) {
		return (int) Math.round(animations.getAsJsonObject(name).get("animation_length").getAsDouble() * 20);
	}
}
