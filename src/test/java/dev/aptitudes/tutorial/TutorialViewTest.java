package dev.aptitudes.tutorial;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TutorialViewTest {
	@Test
	void initialAndOutOfOrderProgressKeepTheFirstMissingGoal() {
		var state = TutorialRules.empty();
		assertEquals(new TutorialView(0, 0, 0, false), TutorialView.from(state));
		assertTrue(TutorialView.from(state).visible());
		state = TutorialRules.observe(state, TutorialRules.Step.COMBO);
		assertEquals(new TutorialView(0, 1, 0, false), TutorialView.from(state));
		assertNull(TutorialRules.pendingReward(state));
	}

	@Test
	void finishedGoalsStayVisibleOnlyWhileRewardsArePending() {
		var state = TutorialRules.empty();
		for (var step : TutorialRules.ORDER) state = TutorialRules.observe(state, step);
		var view = TutorialView.from(state);
		assertEquals(-1, view.nextStep());
		assertEquals(6, view.pendingRewards());
		assertTrue(view.visible());
		for (var step : TutorialRules.ORDER) state = TutorialRules.claim(state, step);
		assertFalse(TutorialView.from(state).visible());
		assertEquals(0, TutorialView.from(state).pendingRewards());
	}

	@Test
	void repeatedStatusReadsCannotObserveOrClaimAnything() {
		var state = TutorialRules.observe(TutorialRules.empty(), TutorialRules.Step.GUIDE);
		for (int i = 0; i < 100; i++) assertEquals(new TutorialView(1, 1, 0, false), TutorialView.from(state));
		assertTrue(state.rewarded().isEmpty());
		assertEquals(TutorialRules.Step.GUIDE, TutorialRules.pendingReward(state));
	}

	@Test
	void skippingSurvivesTheRealCodecAndNeverMakesPendingRewardsClaimable() {
		TutorialData data = new TutorialData().observe(TutorialRules.Step.GUIDE).claim(TutorialRules.Step.GUIDE)
				.observe(TutorialRules.Step.BONFIRE).skip();
		var json = TutorialData.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
		TutorialData loaded = TutorialData.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(data, loaded);
		assertFalse(TutorialView.from(loaded.state()).visible());
		assertEquals(0, TutorialView.from(loaded.state()).pendingRewards());
		assertEquals(1, TutorialView.from(loaded.state()).rewarded());
		for (var step : TutorialRules.ORDER) {
			assertEquals(loaded, loaded.claim(step).observe(step).skip());
		}
	}

	@Test
	void corruptDisplayValuesCannotIndexOutsideTheCatalogOrCreateNegativeCounts() {
		assertEquals(new TutorialView(-1, 6, 6, false), new TutorialView(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, false));
		assertEquals(new TutorialView(-1, 0, 0, true), new TutorialView(Integer.MIN_VALUE, -1, -1, true));
		assertEquals(0, new TutorialView(5, 0, 6, false).rewarded());
	}
}
