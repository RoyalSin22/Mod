package dev.aptitudes.temple;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/** T4a: las cuatro llaves y las cuatro puertas de cámara tienen todos sus recursos. */
class ChamberKeyResourcesTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/aptitudes");

	@Test void everyMetalHasKeyAndDoorResources() throws IOException {
		for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
			String m = metal.name().toLowerCase(Locale.ROOT);
			// Llave: modelo de objeto y textura.
			JsonObject keyModel = json(ASSETS.resolve("models/item/custodian_key_" + m + ".json"));
			assertEquals("item/generated", keyModel.get("parent").getAsString());
			assertEquals("aptitudes:item/custodian_key_" + m, keyModel.getAsJsonObject("textures").get("layer0").getAsString());
			assertSixteen(ASSETS.resolve("textures/item/custodian_key_" + m + ".png"));
			// Puerta: estado, modelos y textura.
			assertEquals("aptitudes:block/chamber_door_" + m, json(ASSETS.resolve("blockstates/chamber_door_" + m + ".json"))
					.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
			JsonObject block = json(ASSETS.resolve("models/block/chamber_door_" + m + ".json"));
			assertEquals("aptitudes:block/chamber_door_" + m, block.getAsJsonObject("textures").get("all").getAsString());
			assertEquals("aptitudes:block/chamber_door_" + m, json(ASSETS.resolve("models/item/chamber_door_" + m + ".json")).get("parent").getAsString());
			assertSixteen(ASSETS.resolve("textures/block/chamber_door_" + m + ".png"));
		}
	}

	@Test void namesTooltipsAndMessagesExistInEnglishAndSpanish() throws IOException {
		for (String locale : new String[] {"en_us", "es_es"}) {
			JsonObject lang = json(ASSETS.resolve("lang/" + locale + ".json"));
			for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
				String m = metal.name().toLowerCase(Locale.ROOT);
				for (String key : new String[] {"item.aptitudes.custodian_key_" + m, "block.aptitudes.chamber_door_" + m}) {
					assertTrue(lang.has(key) && !lang.get(key).getAsString().isBlank(), locale + " " + key);
				}
			}
			for (TempleKeyRules.KeyUse use : TempleKeyRules.KeyUse.values()) {
				if (use == TempleKeyRules.KeyUse.OPEN) continue;
				String key = "temple.aptitudes.chamber." + use.name().toLowerCase(Locale.ROOT);
				assertTrue(lang.has(key), locale + " falta " + key);
			}
			for (String key : new String[] {"temple.aptitudes.chamber.opened", "temple.aptitudes.guardian_defeated_keys",
					"item.aptitudes.custodian_key.tooltip", "item.aptitudes.custodian_key.blank"}) {
				assertTrue(lang.has(key) && !lang.get(key).getAsString().isBlank(), locale + " " + key);
			}
		}
	}

	private static void assertSixteen(Path png) throws IOException {
		var image = ImageIO.read(png.toFile());
		assertNotNull(image, png.toString());
		assertEquals(16, image.getWidth());
		assertEquals(16, image.getHeight());
	}

	private static JsonObject json(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
