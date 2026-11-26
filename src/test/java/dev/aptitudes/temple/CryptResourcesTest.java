package dev.aptitudes.temple;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** V69: receta, botín, bloque, objeto y textos de la Cripta de la Niebla y la Llave del Velo. */
class CryptResourcesTest {
	private static final Path RES = Path.of("src/main/resources");
	private static final Path ASSETS = RES.resolve("assets/aptitudes");
	private static final List<String> LOCALES = List.of("en_us", "es_es", "es_mx", "pt_br", "fr_fr", "de_de");
	private static final List<String> KEYS = List.of(
			"item.aptitudes.llave_del_velo", "item.aptitudes.llave_del_velo.tooltip", "block.aptitudes.crypt_hatch",
			"temple.aptitudes.crypt.opened", "temple.aptitudes.crypt.not_ready", "temple.aptitudes.crypt.needs_key",
			"temple.aptitudes.crypt.already_open", "temple.aptitudes.crypt.no_crypt", "temple.aptitudes.crypt.wave_1",
			"temple.aptitudes.crypt.wave_2", "temple.aptitudes.crypt.wave_3", "temple.aptitudes.crypt.next_wave",
			"temple.aptitudes.crypt.climax", "temple.aptitudes.crypt.done", "temple.aptitudes.crypt.champion",
			"config.aptitudes.field.cryptEnabled", "config.aptitudes.field.cryptIdleResetSeconds",
			"config.aptitudes.field.cryptWaveSizePercent");

	private static JsonObject json(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	@Test void recipeIsShapelessAtTheGuildDeskWithTheDesignedIngredients() throws IOException {
		JsonObject recipe = json(RES.resolve("data/aptitudes/recipe/llave_del_velo.json"));
		assertEquals("aptitudes:guild_crafting_shapeless", recipe.get("type").getAsString());
		Map<String, Integer> counts = new HashMap<>();
		JsonArray ingredients = recipe.getAsJsonArray("ingredients");
		for (JsonElement e : ingredients) counts.merge(e.getAsJsonObject().get("item").getAsString(), 1, Integer::sum);
		assertEquals(Map.of("aptitudes:craneo_gran_osario", 1, "aptitudes:jiron_velo_espectral", 3, "aptitudes:soul_shard", 4), counts);
		assertTrue(ingredients.size() <= 9, "la cuadrícula de la Mesa del gremio es de 3×3");
		assertEquals("aptitudes:llave_del_velo", recipe.getAsJsonObject("result").get("id").getAsString());
		assertEquals(1, recipe.getAsJsonObject("result").get("count").getAsInt());
	}

	@Test void theKeyDoesNotReplaceTheAnchorOrTheFourKeysRecipes() throws IOException {
		JsonObject anchor = json(RES.resolve("data/aptitudes/recipe/ancla_del_velo.json"));
		assertEquals("aptitudes:ancla_del_velo", anchor.getAsJsonObject("result").get("id").getAsString());
		for (JsonElement e : anchor.getAsJsonArray("ingredients")) {
			assertNotEquals("aptitudes:craneo_gran_osario", e.getAsJsonObject().get("item").getAsString());
		}
	}

	@Test void lootTableBeatsTheDiamondChamberWithoutNewPower() throws IOException {
		JsonObject crypt = json(RES.resolve("data/aptitudes/loot_table/chests/custodian_crypt.json"));
		JsonObject diamond = json(RES.resolve("data/aptitudes/loot_table/chests/custodian_diamond.json"));
		JsonObject cp = crypt.getAsJsonArray("pools").get(0).getAsJsonObject();
		JsonObject dp = diamond.getAsJsonArray("pools").get(0).getAsJsonObject();
		assertEquals("minecraft:chest", crypt.get("type").getAsString());
		assertTrue(cp.getAsJsonObject("rolls").get("min").getAsInt() > dp.getAsJsonObject("rolls").get("min").getAsInt());
		assertTrue(cp.getAsJsonObject("rolls").get("max").getAsInt() > dp.getAsJsonObject("rolls").get("max").getAsInt());
		for (JsonElement e : cp.getAsJsonArray("entries")) {
			String name = e.getAsJsonObject().get("name").getAsString();
			assertNotEquals("aptitudes:resonador_del_umbral", name, "sin Damnorath extra");
			assertNotEquals("aptitudes:craneo_gran_osario", name, "sin trofeos: no puede ser una fuente de llaves");
			assertNotEquals("aptitudes:llave_del_velo", name, "sin llaves");
		}
	}

	@Test void blockAndItemResourcesExist() throws IOException {
		assertEquals("aptitudes:block/crypt_hatch", json(ASSETS.resolve("blockstates/crypt_hatch.json"))
				.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
		assertEquals("aptitudes:block/crypt_hatch", json(ASSETS.resolve("models/block/crypt_hatch.json"))
				.getAsJsonObject("textures").get("all").getAsString());
		assertEquals("aptitudes:block/crypt_hatch", json(ASSETS.resolve("models/item/crypt_hatch.json")).get("parent").getAsString());
		assertEquals("aptitudes:item/llave_del_velo", json(ASSETS.resolve("models/item/llave_del_velo.json"))
				.getAsJsonObject("textures").get("layer0").getAsString());
		for (String png : List.of("textures/block/crypt_hatch.png", "textures/item/llave_del_velo.png")) {
			var image = ImageIO.read(ASSETS.resolve(png).toFile());
			assertNotNull(image, png);
			assertEquals(16, image.getWidth(), png);
			assertEquals(16, image.getHeight(), png);
		}
	}

	@Test void everyLanguageHasEveryText() throws IOException {
		for (String locale : LOCALES) {
			JsonObject lang = json(ASSETS.resolve("lang/" + locale + ".json"));
			for (String key : KEYS) {
				assertTrue(lang.has(key), locale + ": falta " + key);
				assertFalse(lang.get(key).getAsString().isBlank(), locale + ": vacío " + key);
			}
		}
	}
}
