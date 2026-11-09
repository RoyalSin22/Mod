package dev.aptitudes.elite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlyphRulesTest {
	private static final List<String> AFFIXES = List.of("fast", "armored", "vampiric", "brutal", "regenerating",
			"explosive", "frost", "blinking", "giant", "thorny", "searing");

	@ParameterizedTest
	@ValueSource(strings = {"fast", "armored", "vampiric", "brutal", "regenerating", "explosive", "frost", "blinking", "giant", "thorny", "searing"})
	void everyAffixHasAVisibleShape(String affix) {
		assertTrue(GlyphRules.shape(affix).size() >= 3);
	}

	@Test void eachAffixHasADifferentShape() {
		Set<List<GlyphRules.Point>> shapes = new HashSet<>();
		for (String affix : AFFIXES) shapes.add(GlyphRules.shape(affix));
		assertEquals(AFFIXES.size(), shapes.size());
	}

	@Test void shapesStayInsideASmallArea() {
		for (String affix : AFFIXES) {
			for (GlyphRules.Point p : GlyphRules.shape(affix)) {
				assertTrue(Math.abs(p.x()) <= 1.5 && Math.abs(p.y()) <= 1.6, affix);
			}
		}
	}

	@Test void unknownOrNullAffixHasNoShape() {
		assertTrue(GlyphRules.shape("unknown").isEmpty());
		assertTrue(GlyphRules.shape(null).isEmpty());
	}

	@Test void threeSlotsAreOrderedFromFeetToHead() {
		assertEquals(3, GlyphRules.MAX_MARKS);
		assertTrue(GlyphRules.slotHeight(0) < GlyphRules.slotHeight(1));
		assertTrue(GlyphRules.slotHeight(1) < GlyphRules.slotHeight(2));
		assertFalse(GlyphRules.slotHeight(9) < GlyphRules.slotHeight(0));
		assertNotEquals(GlyphRules.slotHeight(0), GlyphRules.slotHeight(2));
	}
}
