package dev.aptitudes.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.aptitudes.guide.BestiaryRules;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** T2 de la cadena narrativa: los recursos de datos del Núcleo del Osario y del Ancla del Velo están completos. */
class VeilAnchorResourcesTest {
	private static final Path RES = Path.of("src/main/resources");
	private static final String[] ITEMS = {"nucleo_osario", "ancla_del_velo"};

	@Test void recipeIsShapelessAtTheGuildDeskAndFitsTheGrid() throws IOException {
		JsonObject recipe = json(RES.resolve("data/aptitudes/recipe/ancla_del_velo.json"));
		assertEquals("aptitudes:guild_crafting_shapeless", recipe.get("type").getAsString());
		Map<String, Integer> counts = new HashMap<>();
		JsonArray ingredients = recipe.getAsJsonArray("ingredients");
		for (JsonElement e : ingredients) counts.merge(e.getAsJsonObject().get("item").getAsString(), 1, Integer::sum);
		assertEquals(Map.of("aptitudes:jiron_velo_espectral", 3, "aptitudes:nucleo_osario", 2,
				"aptitudes:soul_shard", 3, "minecraft:diamond", 1), counts);
		assertTrue(ingredients.size() <= 9, "la cuadrícula de la Mesa del gremio es de 3×3");
		assertEquals("aptitudes:ancla_del_velo", recipe.getAsJsonObject("result").get("id").getAsString());
		assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
	}

	@Test void ossuaryDropsTheCoreOnlyWhenKilledByAPlayerWithNormalChance() throws IOException {
		JsonObject loot = json(RES.resolve("data/aptitudes/loot_table/entities/osario_quebrado.json"));
		JsonObject pool = loot.getAsJsonArray("pools").get(0).getAsJsonObject();
		JsonArray conditions = pool.getAsJsonArray("conditions");
		assertEquals("minecraft:killed_by_player", conditions.get(0).getAsJsonObject().get("condition").getAsString());
		JsonObject chance = conditions.get(1).getAsJsonObject();
		assertEquals("minecraft:random_chance", chance.get("condition").getAsString());
		// Misma probabilidad «normal» que el Errante Velado con su Jirón.
		JsonObject errante = json(RES.resolve("data/aptitudes/loot_table/entities/errante_velado.json"))
				.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("conditions").get(1).getAsJsonObject();
		assertEquals(errante.get("chance").getAsDouble(), chance.get("chance").getAsDouble());
		assertEquals("aptitudes:nucleo_osario",
				pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
	}

	@Test void modelsAndTexturesExist() throws IOException {
		for (String item : ITEMS) {
			JsonObject model = json(RES.resolve("assets/aptitudes/models/item/" + item + ".json"));
			assertEquals("item/generated", model.get("parent").getAsString());
			assertEquals("aptitudes:item/" + item, model.getAsJsonObject("textures").get("layer0").getAsString());
			var image = ImageIO.read(RES.resolve("assets/aptitudes/textures/item/" + item + ".png").toFile());
			assertNotNull(image, item);
			assertEquals(16, image.getWidth());
			assertEquals(16, image.getHeight());
		}
	}

	@Test void namesAndTooltipExistInEnglishAndSpanish() throws IOException {
		for (String locale : new String[] {"en_us", "es_es"}) {
			JsonObject lang = json(RES.resolve("assets/aptitudes/lang/" + locale + ".json"));
			for (String key : new String[] {"item.aptitudes.nucleo_osario", "item.aptitudes.ancla_del_velo",
					"item.aptitudes.ancla_del_velo.tooltip"}) {
				assertTrue(lang.has(key) && !lang.get(key).getAsString().isBlank(), locale + " " + key);
			}
		}
	}

	@Test void bestiaryListsTheNewDrops() {
		assertTrue(BestiaryRules.byId("osario_quebrado").drops().contains("aptitudes:nucleo_osario"));
		assertTrue(BestiaryRules.byId("errante_velado").drops().contains("aptitudes:jiron_velo_espectral"));
	}

	private static JsonObject json(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
