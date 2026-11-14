package dev.aptitudes.temple;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** T4b: las cuatro tablas de botín de las cámaras existen, son válidas y crecen en valor con el metal. */
class ChamberLootTablesTest {
	private static final Path LOOT = Path.of("src/main/resources/data/aptitudes/loot_table/chests");
	private static final Path LANG = Path.of("src/main/resources/assets/aptitudes/lang/en_us.json");

	private static JsonObject table(TempleKeyRules.Metal metal) throws IOException {
		return json(LOOT.resolve("custodian_" + metal.name().toLowerCase(Locale.ROOT) + ".json"));
	}

	private static Set<String> items(JsonObject table) {
		Set<String> out = new HashSet<>();
		for (JsonElement e : table.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries")) {
			out.add(e.getAsJsonObject().get("name").getAsString());
		}
		return out;
	}

	private static int maxRolls(JsonObject table) {
		return table.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonObject("rolls").get("max").getAsInt();
	}

	@Test void everyMetalHasAValidLootTable() throws IOException {
		for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
			JsonObject table = table(metal);
			assertEquals("minecraft:chest", table.get("type").getAsString(), metal.name());
			JsonObject pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
			JsonObject rolls = pool.getAsJsonObject("rolls");
			assertTrue(rolls.get("min").getAsInt() >= 1 && rolls.get("min").getAsInt() <= rolls.get("max").getAsInt(), metal.name());
			JsonArray entries = pool.getAsJsonArray("entries");
			assertTrue(entries.size() >= 5, metal.name());
			for (JsonElement e : entries) {
				JsonObject entry = e.getAsJsonObject();
				assertTrue(entry.get("weight").getAsInt() > 0, metal.name() + " " + entry);
				if (entry.has("functions")) {
					JsonObject count = entry.getAsJsonArray("functions").get(0).getAsJsonObject().getAsJsonObject("count");
					assertTrue(count.get("min").getAsInt() >= 1 && count.get("min").getAsInt() <= count.get("max").getAsInt(), entry.toString());
				}
			}
		}
	}

	@Test void modItemsInTheTablesAreRealItems() throws IOException {
		JsonObject lang = json(LANG);
		for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
			for (String name : items(table(metal))) {
				if (!name.startsWith("aptitudes:")) {
					assertTrue(name.startsWith("minecraft:"), name);
					continue;
				}
				String key = "item.aptitudes." + name.substring("aptitudes:".length());
				assertTrue(lang.has(key), metal + " usa un objeto sin texto (¿existe?): " + name);
			}
		}
	}

	@Test void richnessGrowsWithTheMetal() throws IOException {
		int previous = 0;
		for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
			int rolls = maxRolls(table(metal));
			assertTrue(rolls >= previous, metal + " no debe dar menos tiradas que el metal anterior");
			previous = rolls;
		}
		assertTrue(maxRolls(table(TempleKeyRules.Metal.DIAMOND)) > maxRolls(table(TempleKeyRules.Metal.COPPER)));
	}

	@Test void onlyTheDiamondChamberHoldsTheRarestRewards() throws IOException {
		for (TempleKeyRules.Metal metal : TempleKeyRules.Metal.values()) {
			Set<String> items = items(table(metal));
			boolean top = metal == TempleKeyRules.Metal.DIAMOND;
			assertEquals(top, items.contains("minecraft:enchanted_golden_apple"), metal.name());
			assertEquals(top, items.contains("minecraft:netherite_scrap"), metal.name());
			assertEquals(top, items.contains("minecraft:diamond_block"), metal.name());
		}
	}

	private static JsonObject json(Path path) throws IOException {
		try (var reader = Files.newBufferedReader(path)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}
}
