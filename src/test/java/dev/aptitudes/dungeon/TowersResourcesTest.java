package dev.aptitudes.dungeon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** V86 · fase 3: recursos del controlador, la recompensa y el logro de Las Tres Torres Señoriales. */
class TowersResourcesTest {
	private static final Path RES = Path.of("src/main/resources");

	private static String read(String path) throws IOException {
		return Files.readString(RES.resolve(path));
	}

	@Test void controllerBlockHasItsBlockstateAndModelAndNames() throws IOException {
		assertTrue(read("assets/aptitudes/blockstates/three_towers_controller.json").contains("aptitudes:block/three_towers_controller"));
		JsonObject model = JsonParser.parseString(read("assets/aptitudes/models/block/three_towers_controller.json")).getAsJsonObject();
		assertTrue(model.getAsJsonObject("textures").has("side"));
		for (String lang : new String[] {"en_us", "es_es", "pt_br", "fr_fr", "de_de"}) {
			String text = read("assets/aptitudes/lang/" + lang + ".json");
			for (String key : new String[] {"block.aptitudes.three_towers_controller", "towers.aptitudes.guardian_woke", "towers.aptitudes.guardian_down",
					"towers.aptitudes.lord_open", "towers.aptitudes.colossus_woke", "towers.aptitudes.colossus_down",
					"advancement.aptitudes.three_towers.title", "advancement.aptitudes.three_towers.description", "aptitudes.structure.three_towers"}) {
				assertTrue(text.contains("\"" + key + "\""), lang + " sin " + key);
			}
		}
	}

	@Test void everyRewardChestHasTheAscensionTemplateExactlyOnce() throws IOException {
		JsonObject table = JsonParser.parseString(read("data/aptitudes/loot_table/chests/three_towers_reward.json")).getAsJsonObject();
		assertEquals("minecraft:chest", table.get("type").getAsString());
		var first = table.getAsJsonArray("pools").get(0).getAsJsonObject();
		assertEquals(1, first.get("rolls").getAsInt(), "una tirada");
		var entries = first.getAsJsonArray("entries");
		assertEquals(1, entries.size(), "una sola entrada: la plantilla está garantizada");
		assertEquals("aptitudes:ascended_template", entries.get(0).getAsJsonObject().get("name").getAsString());
	}

	@Test void theAchievementIsGeneratedAndChildOfSlayBoss() throws IOException {
		JsonObject adv = JsonParser.parseString(read("data/aptitudes/advancement/aptitudes/three_towers.json")).getAsJsonObject();
		assertEquals("aptitudes:aptitudes/slay_boss", adv.get("parent").getAsString());
		assertEquals("challenge", adv.getAsJsonObject("display").get("frame").getAsString());
	}

	@Test void theControllerTagsAreStableForSavedBosses() {
		assertEquals("aptitudes.towers:", TowersBlocks.CONTROLLER_TAG);
		assertEquals("aptitudes.towers_role:", TowersBlocks.ROLE_TAG);
		for (TowersLayout.Role r : TowersLayout.Role.values()) assertEquals(r, TowersLayout.Role.valueOf(r.name()));
	}
}
