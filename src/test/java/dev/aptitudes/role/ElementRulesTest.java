package dev.aptitudes.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementRulesTest {
	@Test void poisonIsRetiredButOthersStaySelectable() {
		assertFalse(ElementRules.selectable("poison"));
		assertTrue(ElementRules.selectable("fire"));
		assertTrue(ElementRules.selectable("plant"));
		assertFalse(ElementRules.selectable(null));
	}

	@ParameterizedTest
	@CsvSource({
			"fire, 0, fire",
			"fire, 2, fire",
			"fire, 3, magma",
			"fire, 5, magma",
			"ice, 5, ice",    // sin evolución definida: se queda igual
	})
	void fireEvolvesToMagmaAtTheEvolutionRank(String element, int rank, String expected) {
		assertEquals(expected, ElementRules.form(element, rank));
	}

	@Test void evolutionRankIsThree() {
		assertEquals(3, ElementRules.EVOLUTION_RANK);
	}

	@Test void nullElementIsNotInvented() {
		assertNull(ElementRules.form(null, 5));
	}
}
