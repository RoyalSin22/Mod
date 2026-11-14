package dev.aptitudes.temple;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** T3a: los recursos de la Puerta del Custodio y los textos del flujo del templo nuevo están completos. */
class CustodianDoorResourcesTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/aptitudes");
	private static final String[] KEYS = {
			"block.aptitudes.custodian_door",
			"temple.aptitudes.stairs_open",
			"temple.aptitudes.door.opened", "temple.aptitudes.door.needs_anchor",
			"temple.aptitudes.door.not_ready", "temple.aptitudes.door.already_open",
			"temple.aptitudes.seal.flow_dormant", "temple.aptitudes.seal.flow_automaton",
			"temple.aptitudes.seal.flow_door_locked", "temple.aptitudes.seal.flow_custodian",
			"temple.aptitudes.seal.flow_cleared"};

	@Test void blockstateModelsAndTextureExist() throws IOException {
		JsonObject state = json(ASSETS.resolve("blockstates/custodian_door.json"));
		assertEquals("aptitudes:block/custodian_door",
				state.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
		JsonObject block = json(ASSETS.resolve("models/block/custodian_door.json"));
		assertEquals("minecraft:block/cube_all", block.get("parent").getAsString());
		assertEquals("aptitudes:block/custodian_door", block.getAsJsonObject("textures").get("all").getAsString());
		assertEquals("aptitudes:block/custodian_door", json(ASSETS.resolve("models/item/custodian_door.json")).get("parent").getAsString());
		var image = ImageIO.read(ASSETS.resolve("textures/block/custodian_door.png").toFile());
		assertNotNull(image);
		assertEquals(16, image.getWidth());
		assertEquals(16, image.getHeight());
	}

	@Test void everyFlowHasASealMessageInEnglishAndSpanish() throws IOException {
		for (String locale : new String[] {"en_us", "es_es"}) {
			JsonObject lang = json(ASSETS.resolve("lang/" + locale + ".json"));
			for (String key : KEYS) assertTrue(lang.has(key) && !lang.get(key).getAsString().isBlank(), locale + " " + key);
			for (TempleFlowRules.Flow flow : TempleFlowRules.Flow.values()) {
				if (flow == TempleFlowRules.Flow.PUZZLE) continue; // el puzle usa los textos de palancas y placas
				String key = "temple.aptitudes.seal.flow_" + flow.name().toLowerCase(java.util.Locale.ROOT);
				assertTrue(lang.has(key), locale + " falta " + key);
			}
		}
	}

	private static JsonObject json(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
