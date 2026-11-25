package dev.aptitudes.social;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** C7 (S5): catálogo de las notas en el suelo y sus textos en los 11 idiomas. */
class MessageCatalogTest {
	private static final Pattern ENTRY = Pattern.compile("^\\s*\"([^\"]+)\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"", Pattern.MULTILINE);
	private static final List<String> LOCALES = List.of("en_us", "es_es", "es_mx", "es_ar", "es_cl", "es_ec", "es_uy",
			"es_ve", "pt_br", "fr_fr", "de_de");
	/** AGENTS.md §3: conceptos excluidos (palabras completas, en inglés y español). */
	private static final Pattern EXCLUDED = Pattern.compile(
			"(?i)\\b(lineage|bloodline|race|races|pact|pacts|heritage|ancestry|linaje|raza|razas|pacto|pactos|herencia|ascendencia)\\b");

	@Test
	void catalogHasTheApprovedSize() {
		assertEquals(12, MessageCatalog.TEMPLATES.size());
		assertEquals(12, new HashSet<>(MessageCatalog.TEMPLATES).size(), "plantillas repetidas");
		assertEquals(59, MessageCatalog.words().size());
		for (MessageCatalog.Category c : MessageCatalog.Category.values()) {
			assertTrue(MessageCatalog.words().containsValue(c), "categoría vacía: " + c);
		}
	}

	@Test
	void onlyCatalogPiecesAreValid() {
		assertTrue(MessageCatalog.isValid("danger", "ossuary"));
		assertFalse(MessageCatalog.isValid("danger", "texto libre"));
		assertFalse(MessageCatalog.isValid("hola", "ossuary"));
		assertFalse(MessageCatalog.isValid(null, "ossuary"));
		assertFalse(MessageCatalog.isValid("danger", null));
		assertFalse(MessageCatalog.isValid("ossuary", "danger"), "plantilla y palabra no son intercambiables");
	}

	@Test
	void everyPieceHasTextInEveryLanguage() throws IOException {
		for (String locale : LOCALES) {
			Map<String, String> lang = load(locale);
			for (String t : MessageCatalog.TEMPLATES) {
				String text = lang.get(MessageCatalog.templateKey(t));
				assertNotNull(text, locale + " sin plantilla " + t);
				assertEquals(1, count(text, "%s"), locale + ": la plantilla " + t + " debe tener un único %s");
				assertEquals(1, count(text, "%"), locale + ": la plantilla " + t + " no admite otros %");
			}
			for (String w : MessageCatalog.words().keySet()) {
				String text = lang.get(MessageCatalog.wordKey(w));
				assertNotNull(text, locale + " sin palabra " + w);
				assertFalse(text.isBlank() || text.contains("%"), locale + ": palabra " + w);
			}
			for (MessageCatalog.Category c : MessageCatalog.Category.values()) {
				assertNotNull(lang.get(MessageCatalog.categoryKey(c)), locale + " sin categoría " + c);
			}
		}
	}

	@Test
	void noExcludedConcepts() throws IOException {
		for (String locale : List.of("en_us", "es_es")) {
			Map<String, String> lang = load(locale);
			for (String t : MessageCatalog.TEMPLATES) {
				assertFalse(EXCLUDED.matcher(lang.get(MessageCatalog.templateKey(t))).find(), locale + " " + t);
			}
			for (String w : MessageCatalog.words().keySet()) {
				assertFalse(EXCLUDED.matcher(lang.get(MessageCatalog.wordKey(w))).find(), locale + " " + w);
			}
		}
	}

	private static int count(String text, String part) {
		int n = 0;
		for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) n++;
		return n;
	}

	private static Map<String, String> load(String locale) throws IOException {
		try (InputStream in = MessageCatalogTest.class.getResourceAsStream("/assets/aptitudes/lang/" + locale + ".json")) {
			assertNotNull(in, "falta " + locale);
			String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			Map<String, String> out = new HashMap<>();
			Matcher m = ENTRY.matcher(text);
			while (m.find()) out.put(m.group(1), m.group(2));
			return out;
		}
	}
}
