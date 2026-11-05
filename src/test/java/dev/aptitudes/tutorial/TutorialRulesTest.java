package dev.aptitudes.tutorial;

import dev.aptitudes.tutorial.TutorialRules.State;
import dev.aptitudes.tutorial.TutorialRules.Step;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TutorialRulesTest {
	@Test
	void stableKeysAndOrderMatchTheSixSteps() {
		assertEquals(List.of("guide", "bonfire", "primary_role", "active", "combo", "guild_craft"),
				TutorialRules.ORDER.stream().map(Step::key).toList());
		assertThrows(UnsupportedOperationException.class, () -> TutorialRules.ORDER.clear());
		assertEquals(Step.GUIDE, TutorialRules.next(TutorialRules.empty()));
		assertNull(TutorialRules.pendingReward(TutorialRules.empty()));
	}

	@Test
	void eachStepPaysExactlyOnceInOrder() {
		State state = TutorialRules.empty();
		int emeralds = 0;
		for (Step step : TutorialRules.ORDER) {
			assertEquals(step, TutorialRules.next(state));
			state = TutorialRules.observe(state, step);
			assertEquals(step, TutorialRules.pendingReward(state));
			state = TutorialRules.claim(state, step);
			emeralds += TutorialRules.emeralds(step);
			assertSame(state, TutorialRules.claim(state, step));
			assertSame(state, TutorialRules.observe(state, step));
		}
		assertEquals(6, emeralds);
		assertEquals(6, state.rewarded().size());
		assertNull(TutorialRules.next(state));
		assertNull(TutorialRules.pendingReward(state));
	}

	@Test
	void actionsOutOfOrderAreRememberedButDoNotBypassTheRewardOrder() {
		State state = TutorialRules.observe(TutorialRules.empty(), Step.PRIMARY_ROLE);
		state = TutorialRules.observe(state, Step.GUILD_CRAFT);
		assertEquals(Step.GUIDE, TutorialRules.next(state));
		assertNull(TutorialRules.pendingReward(state));
		assertSame(state, TutorialRules.claim(state, Step.PRIMARY_ROLE));
		state = TutorialRules.observe(state, Step.GUIDE);
		state = TutorialRules.claim(state, Step.GUIDE);
		assertEquals(Step.BONFIRE, TutorialRules.next(state));
		assertNull(TutorialRules.pendingReward(state));
		state = TutorialRules.observe(state, Step.BONFIRE);
		state = TutorialRules.claim(state, Step.BONFIRE);
		assertEquals(Step.PRIMARY_ROLE, TutorialRules.pendingReward(state));
		state = TutorialRules.claim(state, Step.PRIMARY_ROLE);
		assertEquals(Step.ACTIVE, TutorialRules.next(state));
		assertTrue(state.observed().contains(Step.GUILD_CRAFT));
	}

	@Test
	void fullInventoryCanLeaveTheRewardPendingWithoutRepeatingTheAction() {
		State state = TutorialRules.observe(TutorialRules.empty(), Step.GUIDE);
		for (int retry = 0; retry < 20; retry++) {
			assertEquals(Step.GUIDE, TutorialRules.pendingReward(state));
			assertSame(state, TutorialRules.observe(state, Step.GUIDE));
			assertTrue(state.rewarded().isEmpty());
		}
		state = TutorialRules.claim(state, Step.GUIDE);
		assertNull(TutorialRules.pendingReward(state));
		assertEquals(Step.BONFIRE, TutorialRules.next(state));
	}

	@Test
	void existingPlayersDoNotNeedToChooseARoleOrBuyASoulLevelAgain() {
		State state = TutorialRules.existingProgress(TutorialRules.empty(), true, 12, true);
		assertEquals(Set.of(Step.GUIDE, Step.BONFIRE, Step.PRIMARY_ROLE), state.observed());
		assertEquals(Step.ACTIVE, TutorialRules.next(state));
		assertTrue(state.rewarded().isEmpty());
		for (Step step : List.of(Step.GUIDE, Step.BONFIRE, Step.PRIMARY_ROLE)) {
			assertEquals(step, TutorialRules.pendingReward(state));
			state = TutorialRules.claim(state, step);
		}
		State restored = TutorialRules.existingProgress(state, true, Integer.MAX_VALUE, true);
		assertEquals(state, restored);
		assertNull(TutorialRules.pendingReward(restored));
	}

	@Test
	void absentOrNegativeExistingProgressDoesNotInventSteps() {
		assertEquals(TutorialRules.empty(), TutorialRules.existingProgress(TutorialRules.empty(), false, -1, false));
		assertEquals(TutorialRules.empty(), TutorialRules.existingProgress(TutorialRules.empty(), false, 0, false));
	}

	@Test
	void skippingIsIrreversibleAndDoesNotPayPendingOrFutureSteps() {
		State state = TutorialRules.observe(TutorialRules.empty(), Step.GUIDE);
		state = TutorialRules.skip(state);
		assertTrue(state.skipped());
		assertSame(state, TutorialRules.skip(state));
		assertNull(TutorialRules.next(state));
		assertNull(TutorialRules.pendingReward(state));
		for (Step step : TutorialRules.ORDER) {
			assertSame(state, TutorialRules.observe(state, step));
			assertSame(state, TutorialRules.claim(state, step));
		}
		assertTrue(state.rewarded().isEmpty());
		assertEquals(state, TutorialRules.existingProgress(state, true, 5, true));
	}

	@Test
	void skippingPreservesRewardsThatWereAlreadyPaid() {
		State state = TutorialRules.observe(TutorialRules.empty(), Step.GUIDE);
		state = TutorialRules.claim(state, Step.GUIDE);
		state = TutorialRules.skip(state);
		assertEquals(Set.of(Step.GUIDE), state.observed());
		assertEquals(Set.of(Step.GUIDE), state.rewarded());
		assertSame(state, TutorialRules.claim(state, Step.GUIDE));
	}

	@Test
	void malformedListsKeepKnownKeysAndNeverEraseAPaidStep() {
		List<String> keys = new ArrayList<>();
		for (int i = 0; i < 50; i++) keys.addAll(List.of("guide", "combo", "-1", "999", "unknown"));
		keys.add(null);
		State state = TutorialRules.restore(keys, List.of("guide", "bonfire", "bonfire", "unknown"), false);
		assertEquals(Set.of(Step.GUIDE, Step.BONFIRE, Step.COMBO), state.observed());
		assertEquals(Set.of(Step.GUIDE, Step.BONFIRE), state.rewarded());
		assertEquals(Step.PRIMARY_ROLE, TutorialRules.next(state));
		assertNull(TutorialRules.pendingReward(state));
		assertSame(state, TutorialRules.claim(state, Step.GUIDE));
	}

	@Test
	void twoPlayersHaveIndependentImmutableState() {
		EnumSet<Step> input = EnumSet.of(Step.GUIDE);
		State first = new State(input, Set.of(), false);
		State second = TutorialRules.empty();
		input.add(Step.BONFIRE);
		assertEquals(Set.of(Step.GUIDE), first.observed());
		assertThrows(UnsupportedOperationException.class, () -> first.observed().clear());
		assertEquals(Step.GUIDE, TutorialRules.next(second));
		assertTrue(second.observed().isEmpty());
		assertEquals(new State(null, null, false), second);
	}

	@Test
	void comboRequiresFiveRealHitsAndNeverUsesOverflowingArithmetic() {
		for (int count : new int[]{Integer.MIN_VALUE, -1, 0, 1, 4}) assertFalse(TutorialRules.comboReached(count));
		for (int count : new int[]{5, 6, Integer.MAX_VALUE}) assertTrue(TutorialRules.comboReached(count));
	}

	@Test
	void invalidClaimsAndNullEventsCannotCreateRewards() {
		State state = TutorialRules.empty();
		for (Step step : TutorialRules.ORDER) assertSame(state, TutorialRules.claim(state, step));
		assertSame(state, TutorialRules.claim(state, null));
		assertSame(state, TutorialRules.observe(state, null));
		assertEquals(0, TutorialRules.emeralds(null));
	}
}
