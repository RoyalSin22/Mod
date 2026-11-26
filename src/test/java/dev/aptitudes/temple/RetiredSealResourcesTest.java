package dev.aptitudes.temple;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** V87: la Piedra sellada, el Sello del templo y la Llave del templo se retiraron; los sustituyen la Reja y el controlador oculto. */
class RetiredSealResourcesTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/aptitudes");

	@Test void retiredBlocksAndItemsLeaveNoResources() {
		for (String id : new String[] {"sealed_stone", "temple_seal"}) {
			assertFalse(Files.exists(ASSETS.resolve("blockstates/" + id + ".json")), id);
			assertFalse(Files.exists(ASSETS.resolve("models/block/" + id + ".json")), id);
			assertFalse(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), id);
		}
		assertFalse(Files.exists(ASSETS.resolve("models/item/temple_key.json")));
		assertFalse(Files.exists(ASSETS.resolve("textures/item/temple_key.png")));
		assertFalse(Files.exists(ASSETS.resolve("textures/block/sealed_stone.png")));
		assertFalse(Files.exists(ASSETS.resolve("textures/block/temple_seal_top.png")));
		assertFalse(Files.exists(ASSETS.resolve("textures/block/temple_seal_side.png")));
	}

	@Test void replacementsHaveTheirResources() {
		assertTrue(Files.exists(ASSETS.resolve("blockstates/vigil_gate.json")));
		assertTrue(Files.exists(ASSETS.resolve("models/block/vigil_gate.json")));
		assertTrue(Files.exists(ASSETS.resolve("models/item/vigil_gate.json")));
		assertTrue(Files.exists(ASSETS.resolve("textures/block/vigil_gate.png")));
		assertTrue(Files.exists(ASSETS.resolve("blockstates/temple_controller.json")));
		assertTrue(Files.exists(ASSETS.resolve("models/block/temple_controller.json")));
	}
}
