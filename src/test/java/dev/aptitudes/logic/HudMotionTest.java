package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HudMotionTest {
	@Test
	void fadesOnlyInsideTheNoticeLifetime() {
		assertEquals(0f, HudMotion.notice(-1, 4000, true));
		assertEquals(0f, HudMotion.notice(0, 4000, true));
		assertEquals(0.5f, HudMotion.notice(90, 4000, true));
		assertEquals(1f, HudMotion.notice(2000, 4000, true));
		assertEquals(0.5f, HudMotion.notice(3825, 4000, true));
		assertEquals(0f, HudMotion.notice(4000, 4000, true));
		assertEquals(1f, HudMotion.notice(0, 4000, false));
		assertEquals(0f, HudMotion.notice(4000, 4000, false));
	}

	@Test
	void retargetsABarWithoutJumpingAndEndsAtTheNewValue() {
		var fill = new HudMotion.Fill();
		assertEquals(1f, fill.update(1f, 0, 200));
		assertEquals(1f, fill.update(0f, 100, 200));
		assertEquals(0.5f, fill.update(1f, 200, 200));
		assertEquals(0.75f, fill.update(1f, 300, 200));
		assertEquals(1f, fill.update(1f, 400, 200));
	}

	@Test
	void snapsWithMotionOffAndBoundsTheDisplayedFraction() {
		var fill = new HudMotion.Fill();
		assertEquals(1f, fill.update(2f, 0, 200));
		assertEquals(0f, fill.update(Float.NaN, 10, 0));
		assertEquals(0.25f, fill.update(0.25f, 20, 0));
		fill.clear();
		assertEquals(0.75f, fill.update(0.75f, 40, 200));
	}

	@Test
	void levelPulsePeaksOnceAndEnds() {
		assertEquals(0f, HudMotion.levelPulse(-1));
		assertEquals(0f, HudMotion.levelPulse(0));
		assertEquals(1f, HudMotion.levelPulse(325));
		assertEquals(0f, HudMotion.levelPulse(650));
	}
}
