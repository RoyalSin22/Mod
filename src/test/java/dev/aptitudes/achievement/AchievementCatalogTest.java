package dev.aptitudes.achievement;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Coherencia entre el enum {@link Achievement}, los JSON de avances (generados por
 * {@code tools/advancements/gen_advancements.py}) y los textos. Lee los recursos desde el directorio del
 * proyecto (Gradle ejecuta las pruebas en él); sin Minecraft ni Gson.
 */
public class AchievementCatalogTest {
	private static final Path ADVANCEMENTS = Path.of("src/main/resources/data/aptitudes/advancement/aptitudes");
	private static final Path LANG = Path.of("src/main/resources/assets/aptitudes/lang");

	private static String read(Path path) throws IOException {
		return Files.readString(path, StandardCharsets.UTF_8);
	}

	@Test public void idsAreUniqueAndTheTreeHasASingleRoot() {
		Set<String> ids = new HashSet<>();
		Set<Achievement> seen = new HashSet<>();
		for (Achievement a : Achievement.values()) {
			assertTrue("id repetido: " + a.id(), ids.add(a.id()));
			assertTrue("id no válido: " + a.id(), a.id().matches("[a-z0-9_]+"));
			if (a == Achievement.ROOT) assertNull(a.parent());
			else assertTrue("el padre de " + a.id() + " debe declararse antes", a.parent() != null && seen.contains(a.parent()));
			seen.add(a);
		}
		assertEquals(Achievement.ROOT, Achievement.values()[0]);
	}

	@Test public void everyAchievementHasAMatchingAdvancementJson() throws IOException {
		for (Achievement a : Achievement.values()) {
			Path file = ADVANCEMENTS.resolve(a.id() + ".json");
			assertTrue("falta " + file, Files.exists(file));
			String json = read(file);
			assertTrue(a.id() + ": icono", json.contains("\"id\": \"" + a.icon() + "\""));
			assertTrue(a.id() + ": marco", json.contains("\"frame\": \"" + a.frame().name().toLowerCase(Locale.ROOT) + "\""));
			assertTrue(a.id() + ": título", json.contains("\"translate\": \"" + a.titleKey() + "\""));
			if (a == Achievement.ROOT) {
				assertTrue(json.contains("\"minecraft:tick\""));
			} else {
				assertTrue(a.id() + ": padre", json.contains("\"parent\": \"aptitudes:" + a.parent().path() + "\""));
				assertTrue(a.id() + ": criterio", json.contains("\"" + Achievement.CRITERION + "\": {")
						&& json.contains("\"minecraft:impossible\""));
			}
		}
		// Y ningún JSON sobrante (un logro borrado del enum dejaría un avance imposible de conseguir).
		try (var files = Files.list(ADVANCEMENTS)) {
			assertEquals(Achievement.values().length, files.filter(p -> p.toString().endsWith(".json")).count());
		}
	}

	@Test public void everyAchievementIsTranslated() throws IOException {
		for (String lang : new String[] {"en_us", "es_es", "es_mx", "es_ar", "es_cl", "es_ec", "es_uy", "es_ve"}) {
			String text = read(LANG.resolve(lang + ".json"));
			for (Achievement a : Achievement.values()) {
				assertTrue(lang + ": " + a.titleKey(), text.contains("\"" + a.titleKey() + "\":"));
				assertTrue(lang + ": " + a.descriptionKey(), text.contains("\"" + a.descriptionKey() + "\":"));
			}
		}
	}
}
