package dev.aptitudes.boss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Contrato de recursos del Gran Osario Quebrado V85 y de su trofeo. */
class GreatOssuaryResourcesTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/aptitudes");
	private static final String BOSS = "gran_osario_quebrado";
	private static final String TROPHY = "craneo_gran_osario";
	private static final List<String> LOCALES = List.of("en_us", "es_es", "es_mx", "pt_br", "fr_fr", "de_de");
	private static final List<String> TEXT_KEYS = List.of(
			"entity.aptitudes." + BOSS, "item.aptitudes." + BOSS + "_spawn_egg",
			"item.aptitudes." + TROPHY, "item.aptitudes." + TROPHY + ".tooltip",
			"bestiary.aptitudes." + BOSS + ".tip", "bestiary.aptitudes." + BOSS + ".lore",
			"event.aptitudes.soul_fog.boss", "boss.aptitudes.gran_osario_reward",
			"config.aptitudes.field.osarioBossEnabled", "config.aptitudes.field.osarioBossFogPercent",
			"config.aptitudes.field.osarioBossLingerTicks", "config.aptitudes.field.osarioBossMinDamagePercent");

	private static JsonObject json(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	@Test void modelHasTheBonesTheAnimationsAndTheEntityNeed() throws IOException {
		JsonObject resource = json(ASSETS.resolve("geo/entity/" + BOSS + ".geo.json"));
		assertEquals("1.12.0", resource.get("format_version").getAsString());
		JsonObject geo = resource.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
		JsonObject description = geo.getAsJsonObject("description");
		assertEquals("geometry." + BOSS, description.get("identifier").getAsString());
		assertEquals(256, description.get("texture_width").getAsInt());
		assertEquals(256, description.get("texture_height").getAsInt());

		var bones = geo.getAsJsonArray("bones");
		var parents = new HashMap<String, String>();
		int cubes = 0;
		for (var entry : bones) {
			JsonObject bone = entry.getAsJsonObject();
			String name = bone.get("name").getAsString();
			assertFalse(parents.containsKey(name), name);
			parents.put(name, bone.has("parent") ? bone.get("parent").getAsString() : null);
			if (bone.has("cubes")) cubes += bone.getAsJsonArray("cubes").size();
		}
		assertEquals(47, parents.size());
		assertEquals(315, cubes);
		assertTrue(parents.containsKey("root"));
		assertNull(parents.get("root"));
		assertChain(parents, "root", "rise", "pelvis", "body", "spine_mid", "shock", "spine_upper",
				"chest", "neck", "head", "jaw");
		for (String side : List.of("left", "right")) {
			assertChain(parents, "rise", "thigh_" + side, "shin_" + side, "foot_" + side);
			assertChain(parents, "chest", "shoulder_" + side, "upper_" + side,
					"fore_" + side, "hand_" + side);
		}
		for (String core : List.of("core_main", "core_left", "core_right")) {
			assertChain(parents, "chest", core, core + "_live");
		}
		assertChain(parents, "hand_right", "fingers_right");
		assertChain(parents, "mass_left_a", "p3_arm_a");
		assertChain(parents, "mass_right_a", "p3_arm_b");

		JsonObject animation = json(ASSETS.resolve("animations/entity/" + BOSS + ".animation.json"));
		assertEquals("1.8.0", animation.get("format_version").getAsString());
		JsonObject clips = animation.getAsJsonObject("animations");
		var durations = Map.ofEntries(Map.entry("idle", 3.2), Map.entry("walk", 1.4), Map.entry("rake", 1.3),
				Map.entry("mist_ring", 2.0), Map.entry("soul_lash", 1.8), Map.entry("wail", 2.4),
				Map.entry("harvest", 2.6), Map.entry("phase_two", 1.5), Map.entry("phase_three", 2.4),
				Map.entry("stun", 2.0), Map.entry("spawn", 3.0), Map.entry("hurt", 0.4), Map.entry("death", 1.0));
		assertEquals(durations.keySet(), clips.keySet());
		for (var expected : durations.entrySet()) {
			String name = expected.getKey();
			JsonObject clip = clips.getAsJsonObject(name);
			assertEquals(expected.getValue(), clip.get("animation_length").getAsDouble(), 0.0001, name);
			if (name.equals("death")) {
				assertEquals("hold_on_last_frame", clip.get("loop").getAsString());
			} else {
				assertEquals(Set.of("idle", "walk").contains(name),
						clip.has("loop") && clip.get("loop").getAsBoolean(), name);
			}
			var tracks = clip.getAsJsonObject("bones").keySet();
			assertFalse(tracks.isEmpty(), name);
			assertTrue(parents.keySet().containsAll(tracks), name);
			if (name.equals("hurt")) assertEquals(Set.of("shock"), tracks);
			else assertFalse(tracks.contains("shock"), name);
		}

		// Tiempos de aviso del jefe en segundos a 20 ticks/s.
		var impacts = Map.of("rake", 0.8, "mist_ring", 1.4, "soul_lash", 1.2, "wail", 1.7, "harvest", 2.0);
		for (var impact : impacts.entrySet()) {
			JsonObject tracks = clips.getAsJsonObject(impact.getKey()).getAsJsonObject("bones");
			var marked = tracks.asMap().values().stream()
					.flatMap(track -> track.getAsJsonObject().asMap().values().stream())
					.filter(channel -> channel.isJsonObject())
					.flatMap(channel -> channel.getAsJsonObject().keySet().stream())
					.mapToDouble(Double::parseDouble)
					.anyMatch(time -> Math.abs(time - impact.getValue()) < 0.0001);
			assertTrue(marked, impact.getKey());
		}
	}

	private static void assertChain(Map<String, String> parents, String... names) {
		for (int i = 1; i < names.length; i++) {
			assertEquals(names[i - 1], parents.get(names[i]), names[i]);
		}
	}

	@Test void emissiveMapsMatchTheAtlas() throws IOException {
		var dir = ASSETS.resolve("textures/entity");
		var base = ImageIO.read(dir.resolve(BOSS + ".png").toFile());
		var glow = ImageIO.read(dir.resolve(BOSS + "_glowmask.png").toFile());
		var fissures = ImageIO.read(dir.resolve(BOSS + "_fisuras_fase3.png").toFile());
		for (var image : List.of(base, glow, fissures)) {
			assertEquals(512, image.getWidth());
			assertEquals(512, image.getHeight());
		}
		int lit = 0;
		int split = 0;
		for (int y = 0; y < base.getHeight(); y++) {
			for (int x = 0; x < base.getWidth(); x++) {
				int alpha = base.getRGB(x, y) >>> 24;
				int emissive = glow.getRGB(x, y) >>> 24;
				int phaseThree = fissures.getRGB(x, y) >>> 24;
				assertTrue(alpha == 0 || alpha == 255);
				assertTrue(emissive == 0 || emissive == 255);
				assertTrue(phaseThree == 0 || phaseThree == 255);
				assertTrue(emissive == 0 || phaseThree == 0);
				assertTrue((emissive | phaseThree) == 0 || alpha == 255);
				lit += emissive;
				split += phaseThree;
			}
		}
		assertTrue(lit > 0);
		assertTrue(split > 0);
	}

	@Test void textureAndItemResourcesExist() throws IOException {
		assertNotNull(ImageIO.read(ASSETS.resolve("textures/entity/" + BOSS + ".png").toFile()), "textura del jefe");
		var icon = ImageIO.read(ASSETS.resolve("textures/item/" + TROPHY + ".png").toFile());
		assertNotNull(icon, "textura del trofeo");
		assertEquals(16, icon.getWidth());
		assertEquals(16, icon.getHeight());
		assertEquals("aptitudes:item/" + TROPHY, json(ASSETS.resolve("models/item/" + TROPHY + ".json"))
				.getAsJsonObject("textures").get("layer0").getAsString());
		assertEquals("minecraft:item/template_spawn_egg", json(ASSETS.resolve("models/item/" + BOSS + "_spawn_egg.json"))
				.get("parent").getAsString());
	}

	@Test void everyLanguageHasEveryText() throws IOException {
		for (String locale : LOCALES) {
			JsonObject lang = json(ASSETS.resolve("lang/" + locale + ".json"));
			for (String key : TEXT_KEYS) {
				assertTrue(lang.has(key), locale + ": falta " + key);
				assertFalse(lang.get(key).getAsString().isBlank(), locale + ": vacío " + key);
			}
		}
	}

	@Test void descriptionsAvoidPercentSigns() throws IOException {
		// Los textos de Minecraft tratan % como formato; las descripciones de configuración y el bestiario no lo usan.
		for (String locale : LOCALES) {
			JsonObject lang = json(ASSETS.resolve("lang/" + locale + ".json"));
			for (String key : TEXT_KEYS) {
				if (key.startsWith("config.") || key.startsWith("bestiary.")) {
					assertFalse(lang.get(key).getAsString().contains("%"), locale + ": % en " + key);
				}
			}
		}
	}
}
