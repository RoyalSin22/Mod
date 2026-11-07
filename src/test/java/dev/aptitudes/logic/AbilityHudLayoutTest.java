package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityHudLayoutTest {
	private static final int BAR = 170;

	@Test
	void putsTheBarBesideTheHotbarWhenItFits() {
		var layout = AbilityHudLayout.place(800, 300, BAR, 1, 0, 0);
		assertEquals(503, layout.x());
		assertEquals(272, layout.y());
		assertEquals(1, layout.scale());
		assertTrue(layout.contains(layout.x(), layout.y()));
		assertFalse(layout.contains(layout.right(), layout.y()));
	}

	@Test
	void movesTheBarAboveTheHotbarWhenTheSideIsTooNarrow() {
		var layout = AbilityHudLayout.place(480, 270, BAR, 1, 0, 0);
		assertEquals(1, layout.scale());
		assertEquals(198, layout.y());
		assertInside(layout, 480, 270);
	}

	@Test
	void fitsScaleAndBothIndicatorRowsInSmallWindows() {
		var layout = AbilityHudLayout.place(64, 48, BAR, 2, 0, 0);
		assertTrue(layout.scale() < 0.5f);
		assertInside(layout, 64, 48);
	}

	@Test
	void clampsInvalidScaleAndOffsetsWithoutOverflow() {
		assertEquals(1, AbilityHudLayout.validatedScale(Float.NaN));
		assertEquals(1, AbilityHudLayout.validatedScale(Float.POSITIVE_INFINITY));
		assertEquals(0.5f, AbilityHudLayout.validatedScale(-10));
		assertEquals(2, AbilityHudLayout.validatedScale(10));
		var layout = AbilityHudLayout.place(480, 270, BAR, 2, Integer.MAX_VALUE, Integer.MIN_VALUE);
		assertEquals(480, layout.right());
		assertEquals(0, layout.top());
		assertInside(layout, 480, 270);
	}

	private static void assertInside(AbilityHudLayout layout, int width, int height) {
		assertTrue(layout.left() >= 0);
		assertTrue(layout.top() >= 0);
		assertTrue(layout.right() <= width);
		assertTrue(layout.bottom() <= height);
	}
}
