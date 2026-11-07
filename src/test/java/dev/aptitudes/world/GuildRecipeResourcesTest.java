package dev.aptitudes.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuildRecipeResourcesTest {
	private static final Path RECIPES = Path.of("src/main/resources/data/aptitudes/recipe");

	@Test
	void onlyTheDeskUsesVanillaCrafting() throws IOException {
		try (var files = Files.list(RECIPES)) {
			for (Path path : files.filter(p -> p.toString().endsWith(".json")).toList()) {
				String type = recipe(path).get("type").getAsString();
				if (path.getFileName().toString().equals("guild_desk.json")) {
					assertEquals("minecraft:crafting_shaped", type);
				} else {
					assertTrue(type.equals("aptitudes:guild_crafting") || type.equals("aptitudes:guild_crafting_shapeless"),
							() -> path + " tiene tipo " + type);
				}
			}
		}
	}

	@Test
	void theFirstBonfireAndGuideUseTheirOriginalRecipeShapesAtTheDesk() throws IOException {
		JsonObject bonfire = recipe(RECIPES.resolve("bonfire.json"));
		JsonObject guide = recipe(RECIPES.resolve("guide_book.json"));
		assertEquals("aptitudes:guild_crafting", bonfire.get("type").getAsString());
		assertTrue(bonfire.has("pattern"));
		assertEquals("aptitudes:guild_crafting_shapeless", guide.get("type").getAsString());
		assertTrue(guide.has("ingredients"));
	}

	private static JsonObject recipe(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
