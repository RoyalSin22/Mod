package dev.aptitudes.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** V80: los recursos del set único están completos y no quedan restos de las 20 armaduras de rol. */
class AscendedArmorResourcesTest {
	private static final Path RES = Path.of("src/main/resources");
	private static final String[] PIECES = {"helmet", "chestplate", "leggings", "boots"};

	@Test void everyPieceHasASmithingRecipeFromNetherite() throws IOException {
		for (String piece : PIECES) {
			JsonObject recipe = json(RES.resolve("data/aptitudes/recipe/ascended_" + piece + ".json"));
			assertEquals("minecraft:smithing_transform", recipe.get("type").getAsString());
			assertEquals("aptitudes:ascended_template", recipe.getAsJsonObject("template").get("item").getAsString());
			assertEquals("minecraft:netherite_" + piece, recipe.getAsJsonObject("base").get("item").getAsString());
			assertEquals("aptitudes:champion_heart", recipe.getAsJsonObject("addition").get("item").getAsString());
			assertEquals("aptitudes:ascended_" + piece, recipe.getAsJsonObject("result").get("id").getAsString());
		}
	}

	@Test void modelsAndTexturesExist() throws IOException {
		for (String piece : PIECES) {
			assertTrue(Files.exists(RES.resolve("assets/aptitudes/models/item/ascended_" + piece + ".json")), piece);
			BufferedImage icon = ImageIO.read(RES.resolve("assets/aptitudes/textures/item/ascended_" + piece + ".png").toFile());
			assertEquals(16, icon.getWidth());
			assertEquals(16, icon.getHeight());
		}
		assertTrue(Files.exists(RES.resolve("assets/aptitudes/textures/item/ascended_template.png")));
		assertTrue(Files.exists(RES.resolve("assets/aptitudes/models/item/ascended_template.json")));
		for (String layer : new String[]{"ascended_layer_1", "ascended_layer_2"}) {
			BufferedImage image = ImageIO.read(RES.resolve("assets/aptitudes/textures/models/armor/" + layer + ".png").toFile());
			assertEquals(64, image.getWidth());
			assertEquals(32, image.getHeight());
		}
	}

	@Test void noRoleArmorLeftovers() throws IOException {
		try (var files = Files.list(RES.resolve("data/aptitudes/recipe"))) {
			files.forEach(f -> {
				String name = f.getFileName().toString();
				if (name.endsWith("_helmet.json") || name.endsWith("_chestplate.json")
						|| name.endsWith("_leggings.json") || name.endsWith("_boots.json")) {
					assertTrue(name.startsWith("ascended_"), "receta antigua: " + name);
				}
			});
		}
		try (var files = Files.list(RES.resolve("assets/aptitudes/textures/models/armor"))) {
			files.forEach(f -> assertTrue(f.getFileName().toString().startsWith("ascended_"), "capa antigua: " + f));
		}
	}

	@Test void armorTagsListOnlyTheAscendedPieces() throws IOException {
		for (String[] pair : new String[][]{{"head", "helmet"}, {"chest", "chestplate"}, {"leg", "leggings"}, {"foot", "boots"}}) {
			JsonObject tag = json(RES.resolve("data/minecraft/tags/item/" + pair[0] + "_armor.json"));
			assertEquals(1, tag.getAsJsonArray("values").size());
			assertEquals("aptitudes:ascended_" + pair[1], tag.getAsJsonArray("values").get(0).getAsString());
		}
	}

	private static JsonObject json(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
