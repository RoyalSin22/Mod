package dev.aptitudes.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** T3b: los datos de generación del Templo del Custodio y la conservación de los del templo antiguo. */
class CustodianTempleResourcesTest {
	private static final Path WORLDGEN = Path.of("src/main/resources/data/aptitudes/worldgen");

	@Test void newFeatureIsConfiguredAndPlacedLikeTheOldTemple() throws IOException {
		JsonObject configured = json("configured_feature/custodian_temple.json");
		assertEquals("aptitudes:custodian_temple", configured.get("type").getAsString());
		JsonObject placed = json("placed_feature/custodian_temple.json");
		assertEquals("aptitudes:custodian_temple", placed.get("feature").getAsString());
		// Misma colocación que el templo antiguo (frecuencia sin cambios hasta el playtest).
		assertEquals(json("placed_feature/temple.json").get("placement"), placed.get("placement"));
	}

	@Test void oldTempleKeepsItsDataSoExistingWorldsStillLoad() throws IOException {
		assertEquals("aptitudes:temple", json("configured_feature/temple.json").get("type").getAsString());
		assertEquals("aptitudes:temple", json("placed_feature/temple.json").get("feature").getAsString());
	}

	private static JsonObject json(String file) throws IOException {
		try (var reader = Files.newBufferedReader(WORLDGEN.resolve(file))) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
