package dev.aptitudes.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** V86 · fase 2: los datos de generación de Las Tres Torres Señoriales y su rareza frente al Templo del Custodio. */
class ThreeTowersResourcesTest {
	private static final Path WORLDGEN = Path.of("src/main/resources/data/aptitudes/worldgen");

	private static JsonObject json(String file) throws IOException {
		return JsonParser.parseString(Files.readString(WORLDGEN.resolve(file))).getAsJsonObject();
	}

	@Test void featureIsConfiguredAndPlacedOnAGridOfRegions() throws IOException {
		assertEquals("aptitudes:three_towers", json("configured_feature/three_towers.json").get("type").getAsString());
		JsonObject placed = json("placed_feature/three_towers.json");
		assertEquals("aptitudes:three_towers", placed.get("feature").getAsString());
		var grid = placed.getAsJsonArray("placement").get(0).getAsJsonObject();
		assertEquals("aptitudes:region_grid", grid.get("type").getAsString());
		assertEquals("three_towers", grid.get("kind").getAsString());
		assertEquals(StructureLocateRules.regionSize("three_towers"), grid.get("region_size").getAsInt(), "el JSON y el código usan la misma región");
		// Mismos modificadores finales que el Custodio (cuadrícula, in_square, heightmap, bioma).
		var custodian = json("placed_feature/custodian_temple.json").getAsJsonArray("placement");
		var towers = placed.getAsJsonArray("placement");
		assertEquals(custodian.size(), towers.size());
		for (int i = 1; i < custodian.size(); i++) assertEquals(custodian.get(i), towers.get(i));
	}

	@Test void theCustodianTempleIsEvenRarerThanTheTowers() {
		int towers = StructureLocateRules.regionSize("three_towers");
		int custodian = StructureLocateRules.regionSize("custodian_temple");
		assertTrue(custodian > towers, "decisión del autor: el Custodio es aún más raro");
		// Una estructura por región de lado N chunks: la frecuencia por chunk² es 1/N². Con 32 frente a 24 el Custodio es ~1,78 veces más raro.
		assertTrue((long) custodian * custodian > (long) towers * towers);
		assertEquals(32, custodian, "el Custodio no se toca (D-35)");
	}

	@Test void itIsALocatableKindWithItsOwnDesignatedChunks() {
		assertTrue(StructureLocateRules.isKind("three_towers"));
		assertFalse(StructureLocateRules.searchable("three_towers"), "locate usa el índice sin generar chunks");
		int[] d = StructureLocateRules.designatedChunk(987654321L, "three_towers", 3, -2, 24);
		assertTrue(d[0] >= 3 * 24 && d[0] < 4 * 24 && d[1] >= -2 * 24 && d[1] < -1 * 24, "dentro de su región");
		assertTrue(StructureLocateRules.isDesignated(987654321L, "three_towers", d[0], d[1], 24));
		// Su chunk designado no coincide con el del Custodio en la misma región (la clave entra en la semilla).
		assertFalse(java.util.Arrays.equals(d, StructureLocateRules.designatedChunk(987654321L, "custodian_temple", 3, -2, 24)));
	}
}
