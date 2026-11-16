package dev.aptitudes.boss;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** V68: los recursos del Gran Osario Quebrado y de su trofeo están completos y son coherentes con el código. */
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
		JsonObject geo = json(ASSETS.resolve("geo/entity/" + BOSS + ".geo.json"));
		var bones = geo.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().getAsJsonArray("bones");
		List<String> names = bones.asList().stream().map(b -> b.getAsJsonObject().get("name").getAsString()).toList();
		assertTrue(names.containsAll(List.of("root", "body", "head", "left_arm", "right_arm", "left_leg", "right_leg")), names.toString());
		JsonObject anims = json(ASSETS.resolve("animations/entity/" + BOSS + ".animation.json")).getAsJsonObject("animations");
		// Cada ataque de GreatOssuaryEntity dispara la animación de su mismo nombre (contrato de docs/V63_JEFES.md).
		for (String name : List.of("idle", "walk", "phase_two", "stun", "rake", "mist_ring", "soul_lash", "wail", "harvest")) {
			assertTrue(anims.has(name), "falta la animación " + name);
		}
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
