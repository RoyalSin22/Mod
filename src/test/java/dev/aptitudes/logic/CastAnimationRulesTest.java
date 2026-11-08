package dev.aptitudes.logic;

import dev.aptitudes.logic.CastAnimationRules.Kind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CastAnimationRulesTest {
	@Test
	void boundsTheTimelineAndFadesAtBothEnds() {
		for (Kind kind : Kind.values()) {
			assertEquals(0f, CastAnimationRules.frame(kind, Long.MIN_VALUE).progress());
			assertEquals(0f, CastAnimationRules.frame(kind, 0).opacity());
			assertEquals(1f, CastAnimationRules.frame(kind, Long.MAX_VALUE).progress());
			assertEquals(0f, CastAnimationRules.frame(kind, Long.MAX_VALUE).opacity());
			assertTrue(CastAnimationRules.frame(kind, 130).opacity() > 0.9f);
		}
	}

	@Test
	void distinguishesUltimateTimingAndSizeFromAnActiveCast() {
		assertEquals(1f, CastAnimationRules.frame(Kind.ACTIVE, 440).progress());
		assertTrue(CastAnimationRules.frame(Kind.ULTIMATE, 440).progress() < 1f);
		assertTrue(CastAnimationRules.frame(Kind.ULTIMATE, 300).scale()
				> CastAnimationRules.frame(Kind.ACTIVE, 300).scale());
	}

	@Test
	void rejectsUnknownKindsFromTheWire() {
		assertNull(Kind.byIndex(-1));
		assertNull(Kind.byIndex(Integer.MAX_VALUE));
		assertEquals(Kind.UNIQUE, Kind.byIndex(Kind.UNIQUE.ordinal()));
	}
}
