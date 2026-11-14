package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67: tooltips de la pantalla de configuración. */
class ConfigTooltipRulesTest {
	private static final Pattern ENTRY = Pattern.compile("^\\s*\"([^\"]+)\":\\s*\"((?:[^\"\\\\]++|\\\\.)*+)\",?\\s*$", Pattern.MULTILINE);

	@Test
	void keyUsesTheFieldName() {
		assertEquals("config.aptitudes.field.maxSkillLevel", ConfigTooltipRules.descriptionKey("maxSkillLevel"));
	}

	@Test
	void searchMatchesNameOrDescriptionIgnoringCase() {
		assertTrue(ConfigTooltipRules.matches("", "xpBase", "XP base de un nivel"));
		assertTrue(ConfigTooltipRules.matches(null, "xpBase", "x"));
		assertTrue(ConfigTooltipRules.matches("XPBASE", "xpBase", "otra cosa"));
		assertTrue(ConfigTooltipRules.matches("nivel", "xpBase", "XP base de un Nivel"));
		assertFalse(ConfigTooltipRules.matches("eclipse", "xpBase", "XP base de un nivel"));
		assertTrue(ConfigTooltipRules.matches("  xp ", "xpBase", null));
		assertFalse(ConfigTooltipRules.matches("zzz", "xpBase", null));
	}

	@Test
	void everyEditableOptionHasADescriptionInSpanishAndEnglish() throws IOException {
		List<Field> fields = ConfigFieldRules.editableFields(AptitudesConfig.class);
		assertTrue(fields.size() > 150, "se esperaban todas las opciones editables");
		for (String locale : List.of("en_us", "es_es")) {
			Map<String, String> lang = load(locale);
			for (Field f : fields) {
				String text = lang.get(ConfigTooltipRules.descriptionKey(f.getName()));
				assertTrue(text != null && !text.isBlank(), locale + " sin descripción de " + f.getName());
				assertTrue(text.length() <= 150, locale + " descripción muy larga en " + f.getName() + ": " + text.length());
				assertFalse(text.contains("%"), locale + " usa un signo de porcentaje en " + f.getName());
			}
		}
	}

	@Test
	void noDescriptionBelongsToAnUnknownOption() throws IOException {
		java.util.Set<String> names = new java.util.HashSet<>();
		for (Field f : ConfigFieldRules.editableFields(AptitudesConfig.class)) names.add(f.getName());
		for (String key : load("en_us").keySet()) {
			if (key.startsWith(ConfigTooltipRules.KEY_PREFIX)) {
				assertTrue(names.contains(key.substring(ConfigTooltipRules.KEY_PREFIX.length())), "descripción huérfana: " + key);
			}
		}
	}

	private static Map<String, String> load(String locale) throws IOException {
		try (InputStream in = ConfigTooltipRulesTest.class.getResourceAsStream("/assets/aptitudes/lang/" + locale + ".json")) {
			assertTrue(in != null, "falta " + locale);
			String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			Map<String, String> out = new HashMap<>();
			Matcher m = ENTRY.matcher(text);
			while (m.find()) out.put(m.group(1), m.group(2));
			return out;
		}
	}
}
