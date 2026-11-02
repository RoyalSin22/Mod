package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v40: interpretación del texto escrito en la pantalla de configuración y campos editables. */
class ConfigFieldRulesTest {
	@SuppressWarnings("unused")
	static class Sample {
		public static int CONSTANT = 1;
		public final int fixed = 2;
		public int number = 3;
		public double ratio = 0.5;
		public boolean flag = true;
		public String mode = "LINEAR";
		public long unsupported = 4L;
		private int hidden = 5;
	}

	@Test
	void onlyPublicMutableSupportedFieldsAreEditable() {
		List<Field> fields = ConfigFieldRules.editableFields(Sample.class);
		List<String> names = fields.stream().map(Field::getName).sorted().toList();
		assertEquals(List.of("flag", "mode", "number", "ratio"), names);
	}

	@Test
	void parsesEachSupportedType() {
		assertEquals(42, ConfigFieldRules.parse(int.class, " 42 "));
		assertEquals(1.5, ConfigFieldRules.parse(double.class, "1,5"));
		assertEquals(Boolean.TRUE, ConfigFieldRules.parse(boolean.class, "Sí"));
		assertEquals(Boolean.FALSE, ConfigFieldRules.parse(boolean.class, "false"));
		assertEquals("SOFTCAP", ConfigFieldRules.parse(String.class, "SOFTCAP"));
	}

	@Test
	void rejectsInvalidInput() {
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(int.class, "3.5"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(int.class, "99999999999"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(double.class, "NaN"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(double.class, "abc"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(boolean.class, "quizá"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(String.class, "x".repeat(65)));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(long.class, "1"));
		assertThrows(IllegalArgumentException.class, () -> ConfigFieldRules.parse(int.class, null));
	}

	@Test
	void formatsValuesForDisplay() {
		assertEquals("2", ConfigFieldRules.format(2.0));
		assertEquals("0.15", ConfigFieldRules.format(0.15));
		assertEquals("true", ConfigFieldRules.format(true));
		assertTrue(ConfigFieldRules.supported(String.class));
		assertFalse(ConfigFieldRules.supported(long.class));
	}
}
