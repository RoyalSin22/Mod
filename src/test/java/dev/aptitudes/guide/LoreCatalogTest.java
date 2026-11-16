package dev.aptitudes.guide;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** V71 (T7b): el catálogo de Crónicas es coherente con los desbloqueos y con los textos de los idiomas. */
class LoreCatalogTest {
	private static final List<String> LOCALES = List.of("en_us", "es_es", "es_mx", "pt_br", "fr_fr", "de_de");

	@Test void idsAreUniqueAndSourcesAreRealUnlocks() {
		Set<String> ids = new HashSet<>();
		for (LoreCatalog.Entry e : LoreCatalog.ENTRIES) {
			assertTrue(ids.add(e.id()), "repetida " + e.id());
			assertTrue(e.source().equals(LoreCatalog.STARTER) || e.source().equals(LoreCatalog.NEVER)
					|| LoreRules.isValidLoreId(e.source()), "fuente desconocida " + e.source() + " en " + e.id());
		}
		assertEquals(18, LoreCatalog.total());
	}

	@Test void everyUnlockOfTheGameRevealsAtLeastOneEntry() {
		for (String unlock : LoreRules.validLoreIds()) {
			assertTrue(LoreCatalog.ENTRIES.stream().anyMatch(e -> e.source().equals(unlock)), "sin entradas: " + unlock);
		}
	}

	@Test void starterEntriesAreAlwaysVisibleAndNeverOnesNeverAre() {
		assertEquals(3, LoreCatalog.unlockedCount(id -> false));
		assertFalse(LoreCatalog.unlocked(LoreCatalog.byId("arc2.unfinished_oath"), id -> true));
		assertTrue(LoreCatalog.unlocked(LoreCatalog.byId("cosmology.essence_within"), id -> false));
	}

	@Test void aSourceRevealsExactlyItsEntries() {
		Set<String> has = Set.of(LoreRules.LORE_CRYPT_CLEARED);
		Set<String> shown = new HashSet<>();
		for (LoreCatalog.Entry e : LoreCatalog.ENTRIES) if (LoreCatalog.unlocked(e, has::contains) && !e.source().isEmpty()) shown.add(e.id());
		assertEquals(Set.of("chronology.council_three_ways", "faction.concordance", "cosmology.veil_frontier", "faction.permanence"), shown);
	}

	@Test void everyEntryHasTitleAndBodyInEveryLanguage() throws IOException {
		for (String locale : LOCALES) {
			JsonObject lang = JsonParser.parseString(Files.readString(
					Path.of("src/main/resources/assets/aptitudes/lang/" + locale + ".json"), StandardCharsets.UTF_8)).getAsJsonObject();
			for (LoreCatalog.Entry e : LoreCatalog.ENTRIES) {
				for (String key : new String[] {e.titleKey(), e.bodyKey()}) {
					assertTrue(lang.has(key), locale + ": falta " + key);
					assertFalse(lang.get(key).getAsString().isBlank(), locale + ": vacío " + key);
				}
			}
		}
	}

	@Test void unknownIdIsNull() {
		assertNull(LoreCatalog.byId("no.existe"));
	}
}
