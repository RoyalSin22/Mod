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
		// Misma colocación que el templo antiguo salvo la frecuencia: 1 de cada 120 chunks (antes 1 de cada 400).
		var oldPlacement = json("placed_feature/temple.json").getAsJsonArray("placement");
		var newPlacement = placed.getAsJsonArray("placement");
		assertEquals(oldPlacement.size(), newPlacement.size());
		assertEquals("minecraft:rarity_filter", newPlacement.get(0).getAsJsonObject().get("type").getAsString());
		assertEquals(120, newPlacement.get(0).getAsJsonObject().get("chance").getAsInt());
		assertEquals(400, oldPlacement.get(0).getAsJsonObject().get("chance").getAsInt(), "el templo antiguo no se toca");
		for (int i = 1; i < oldPlacement.size(); i++) assertEquals(oldPlacement.get(i), newPlacement.get(i));
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
