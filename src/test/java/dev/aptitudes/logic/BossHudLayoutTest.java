package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossHudLayoutTest {
	@Test
	void centersThePanelAboveActionMessages() {
		var layout = BossHudLayout.place(426, 240, 1, 1, false, null);

		assertEquals(new BossHudLayout(95, 136, 236, 26, 1), layout);
	}

	@Test
	void leavesRoomForTheWorldEventNotice() {
		var layout = BossHudLayout.place(426, 240, 4, 6, true, null);

		assertEquals(new BossHudLayout(95, 41, 236, 91, 4), layout);
	}

	@Test
	void movesUpOnlyWhenTheAbilityBarOverlaps() {
		var overlapping = new AbilityHudLayout(244, 170, 1, 238, 150, 420, 196);
		var away = new AbilityHudLayout(15, 175, 1, 9, 150, 90, 201);

		var shifted = BossHudLayout.place(426, 240, 1, 1, false, overlapping);
		var unchanged = BossHudLayout.place(426, 240, 1, 1, false, away);

		assertEquals(146, shifted.y() + shifted.height());
		assertEquals(162, unchanged.y() + unchanged.height());
	}

	@Test
	void keepsTheSummaryWhenOnlyTwoRowsFit() {
		var layout = BossHudLayout.place(240, 170, 4, 4, true, null);

		assertEquals(new BossHudLayout(8, 7, 224, 55, 2), layout);
	}

	@Test
	void hidesThePanelWhenNotEvenOneRowFits() {
		assertEquals(0, BossHudLayout.place(426, 100, 1, 1, false, null).rows());
		assertEquals(0, BossHudLayout.place(120, 240, 1, 1, false, null).rows());
	}
}
