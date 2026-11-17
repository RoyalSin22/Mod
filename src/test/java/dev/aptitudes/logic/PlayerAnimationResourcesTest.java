package dev.aptitudes.logic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** V78: los archivos de animación del jugador los acepta el cargador real de PAL y coinciden con los nombres del código. */
class PlayerAnimationResourcesTest {
	private static final String PATH = "/assets/aptitudes/player_animations/berserker.json";
	private static final Set<String> BONES = Set.of("head", "torso", "body", "right_arm", "left_arm", "right_leg", "left_leg");

	private static JsonObject json() throws IOException {
		try (InputStream in = PlayerAnimationResourcesTest.class.getResourceAsStream(PATH)) {
			assertNotNull(in, "falta " + PATH);
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	@Test
	void everyNameUsedByTheCodeExistsInTheFiles() throws IOException {
		JsonObject animations = json().getAsJsonObject("animations");
		for (String id : PlayerAnimIds.ALL) assertTrue(animations.has(id), "falta la animación " + id);
		for (String name : animations.keySet()) assertTrue(PlayerAnimIds.ALL.contains(name), "animación sin constante en PlayerAnimIds: " + name);
	}

	@Test
	void palsRealLoaderAcceptsEveryAnimation() throws IOException {
		try (InputStream in = PlayerAnimationResourcesTest.class.getResourceAsStream(PATH)) {
			Map<String, Animation> loaded = UniversalAnimLoader.loadAnimations(in);
			assertEquals(Set.copyOf(PlayerAnimIds.ALL), loaded.keySet());
			for (var entry : loaded.entrySet()) {
				Animation a = entry.getValue();
				assertTrue(a.length() > 0, entry.getKey() + " sin duración");
				assertFalse(a.boneAnimations().isEmpty(), entry.getKey() + " sin huesos");
				for (String bone : a.boneAnimations().keySet()) assertTrue(BONES.contains(bone), entry.getKey() + ": hueso desconocido " + bone);
			}
			// 0,5 s = 10 ticks: el Torbellino encadena tres giros de 10 ticks.
			assertEquals(10f, loaded.get(PlayerAnimIds.BERSERKER_WHIRL).length(), 1e-3);
			assertEquals(30f, loaded.get(PlayerAnimIds.BERSERKER_RAGE).length(), 1e-3);
		}
	}

	@Test
	void anglesAndOffsetsStayInSaneRanges() throws IOException {
		JsonObject animations = json().getAsJsonObject("animations");
		for (String name : animations.keySet()) {
			JsonObject bones = animations.getAsJsonObject(name).getAsJsonObject("bones");
			for (String bone : bones.keySet()) {
				JsonObject b = bones.getAsJsonObject(bone);
				if (b.has("rotation")) b.getAsJsonObject("rotation").entrySet().forEach(e -> {
					for (var v : e.getValue().getAsJsonObject().getAsJsonArray("vector")) {
						assertTrue(Math.abs(v.getAsDouble()) <= 360.0, name + "." + bone + " rotación fuera de rango");
					}
				});
				if (b.has("position")) b.getAsJsonObject("position").entrySet().forEach(e -> {
					for (var v : e.getValue().getAsJsonObject().getAsJsonArray("vector")) {
						assertTrue(Math.abs(v.getAsDouble()) <= 8.0, name + "." + bone + " desplazamiento fuera de rango");
					}
				});
			}
		}
	}
}
