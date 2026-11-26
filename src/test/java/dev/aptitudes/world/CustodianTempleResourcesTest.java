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

	@Test void newFeatureIsConfiguredAndPlacedOnARegionGrid() throws IOException {
		JsonObject configured = json("configured_feature/custodian_temple.json");
		assertEquals("aptitudes:custodian_temple", configured.get("type").getAsString());
		JsonObject placed = json("placed_feature/custodian_temple.json");
		assertEquals("aptitudes:custodian_temple", placed.get("feature").getAsString());
		// Un chunk designado por región (cuadrícula de 32×32 chunks, que permite localizarlo con /aptitudes locate).
		var grid = placed.getAsJsonArray("placement").get(0).getAsJsonObject();
		assertEquals("aptitudes:region_grid", grid.get("type").getAsString());
		assertEquals("custodian_temple", grid.get("kind").getAsString());
		assertEquals(32, grid.get("region_size").getAsInt());
		assertEquals(dev.aptitudes.world.StructureLocateRules.regionSize("custodian_temple"), grid.get("region_size").getAsInt(), "el JSON y el código usan la misma región");
	}

	@Test void theRemovedOldTempleLeavesNoWorldgenData() {
		assertFalse(Files.exists(WORLDGEN.resolve("configured_feature/temple.json")));
		assertFalse(Files.exists(WORLDGEN.resolve("placed_feature/temple.json")));
	}

	private static JsonObject json(String file) throws IOException {
		try (var reader = Files.newBufferedReader(WORLDGEN.resolve(file))) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
