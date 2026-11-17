package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossHudLayoutTest {
	@Test
	void anchorsThePanelAtTheTopCenter() {
		var layout = BossHudLayout.place(426, 240, 1, 1);

		assertEquals(new BossHudLayout(95, 10, 236, 26, 1), layout);
	}

	@Test
	void growsDownwardWithMoreEntries() {
		var layout = BossHudLayout.place(426, 240, 4, 6);

		assertEquals(10, layout.y());
		assertEquals(4, layout.rows());
		assertEquals(91, layout.height());
	}

	@Test
	void neverReachesPastHalfTheScreenAndKeepsTheSummary() {
		var layout = BossHudLayout.place(240, 170, 4, 4);

		assertEquals(3, layout.rows());
		assertTrue(layout.y() + layout.height() <= 85);
	}

	@Test
	void hidesThePanelWhenNotEvenOneRowFits() {
		assertEquals(0, BossHudLayout.place(426, 60, 1, 1).rows());
		assertEquals(0, BossHudLayout.place(120, 240, 1, 1).rows());
	}
}
