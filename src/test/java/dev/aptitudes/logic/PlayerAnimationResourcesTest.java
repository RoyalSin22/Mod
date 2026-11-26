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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** V78: los archivos de animación del jugador los acepta el cargador real de PAL y coinciden con los nombres del código. */
class PlayerAnimationResourcesTest {
	private static final List<String> FILES = List.of("berserker.json", "mage.json", "assassin.json", "herbalist.json", "engineer.json", "lancer.json", "swordsman.json", "paladin.json", "necromancer.json", "druid.json", "merchant.json", "monk.json", "gunslinger.json", "weaponsummoner.json", "shapeshifter.json", "hunter.json", "bard.json", "warlock.json", "tamer.json", "elementalist.json", "grand.json");
	private static final Set<String> BONES = Set.of("head", "torso", "body", "right_arm", "left_arm", "right_leg", "left_leg");

	private static InputStream open(String file) {
		InputStream in = PlayerAnimationResourcesTest.class.getResourceAsStream("/assets/aptitudes/player_animations/" + file);
		assertNotNull(in, "falta player_animations/" + file);
		return in;
	}

	/** Las animaciones de todos los archivos, por nombre. */
	private static JsonObject animations() throws IOException {
		JsonObject all = new JsonObject();
		for (String file : FILES) {
			try (InputStream in = open(file)) {
				JsonObject animations = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("animations");
				for (String name : animations.keySet()) {
					assertFalse(all.has(name), "animación repetida en varios archivos: " + name);
					all.add(name, animations.get(name));
				}
			}
		}
		return all;
	}

	@Test
	void everyNameUsedByTheCodeExistsInTheFiles() throws IOException {
		JsonObject animations = animations();
		for (String id : PlayerAnimIds.ALL) assertTrue(animations.has(id), "falta la animación " + id);
		for (String name : animations.keySet()) assertTrue(PlayerAnimIds.ALL.contains(name), "animación sin constante en PlayerAnimIds: " + name);
	}

	@Test
	void palsRealLoaderAcceptsEveryAnimation() throws IOException {
		Map<String, Animation> loaded = new HashMap<>();
		for (String file : FILES) {
			try (InputStream in = open(file)) {
				loaded.putAll(UniversalAnimLoader.loadAnimations(in));
			}
		}
		assertEquals(Set.copyOf(PlayerAnimIds.ALL), loaded.keySet());
		for (var entry : loaded.entrySet()) {
			Animation a = entry.getValue();
			assertTrue(a.length() > 0, entry.getKey() + " sin duración");
			assertFalse(a.boneAnimations().isEmpty(), entry.getKey() + " sin huesos");
			for (String bone : a.boneAnimations().keySet()) assertTrue(BONES.contains(bone), entry.getKey() + ": hueso desconocido " + bone);
		}
		// 0,5 s = 10 ticks: el Torbellino encadena tres giros de 10 ticks.
		assertEquals(10f, loaded.get(PlayerAnimIds.BERSERKER_WHIRL).length(), 1e-3);
		assertEquals(20f, loaded.get(PlayerAnimIds.BERSERKER_RAGE).length(), 1e-3);
		assertEquals(16f, loaded.get(PlayerAnimIds.BERSERKER_WAR_CRY).length(), 1e-3);
		assertEquals(15f, loaded.get(PlayerAnimIds.BERSERKER_SLAM).length(), 1e-3);
		assertEquals(12f, loaded.get(PlayerAnimIds.MAGE_BOLT).length(), 1e-3);
		assertEquals(18f, loaded.get(PlayerAnimIds.MAGE_TIME_WARP).length(), 1e-3);
	}

	@Test
	void impactTicksMatchAKeyframeOfAnArm() throws IOException {
		JsonObject animations = animations();
		for (var entry : PlayerAnimIds.IMPACT_TICKS.entrySet()) {
			JsonObject bones = animations.getAsJsonObject(entry.getKey()).getAsJsonObject("bones");
			boolean found = false;
			for (String arm : List.of("right_arm", "left_arm")) {
				if (!bones.has(arm)) continue;
				found |= bones.getAsJsonObject(arm).getAsJsonObject("rotation").keySet().stream()
						.anyMatch(t -> Math.abs(Double.parseDouble(t) * 20 - entry.getValue()) < 0.01);
			}
			assertTrue(found, entry.getKey() + ": ningún brazo tiene una clave en el tick de impacto " + entry.getValue());
		}
	}

	@Test
	void anglesAndOffsetsStayInSaneRanges() throws IOException {
		JsonObject animations = animations();
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
