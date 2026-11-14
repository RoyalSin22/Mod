package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleFlowRules.DoorUse;
import dev.aptitudes.temple.TempleFlowRules.Flow;
import org.junit.jupiter.api.Test;

import static dev.aptitudes.temple.TempleFlowRules.*;
import static org.junit.jupiter.api.Assertions.*;

class TempleFlowRulesTest {
	@Test void flowOrderIsStableBecauseItIsSavedAsAnOrdinal() {
		assertArrayEquals(new Flow[] {Flow.DORMANT, Flow.AUTOMATON, Flow.PUZZLE, Flow.DOOR_LOCKED, Flow.CUSTODIAN, Flow.CLEARED},
				Flow.values());
	}

	@Test void unknownOrdinalsFallBackToTheStart() {
		assertEquals(Flow.PUZZLE, flowFromOrdinal(2));
		assertEquals(Flow.DORMANT, flowFromOrdinal(-1));
		assertEquals(Flow.DORMANT, flowFromOrdinal(99));
		assertEquals(Flow.DORMANT, flowFromOrdinal(Integer.MIN_VALUE));
	}

	@Test void layoutOnlyAcceptsTheTwoKnownValues() {
		assertEquals(LAYOUT_OLD, clampLayout(0));
		assertEquals(LAYOUT_CUSTODIAN, clampLayout(1));
		assertEquals(LAYOUT_OLD, clampLayout(7));
		assertEquals(LAYOUT_OLD, clampLayout(-1));
	}

	@Test void automatonOnlyWakesFromRestWithAPlayerNearby() {
		assertTrue(shouldWake(Flow.DORMANT, true));
		assertFalse(shouldWake(Flow.DORMANT, false));
		for (Flow f : Flow.values()) if (f != Flow.DORMANT) assertFalse(shouldWake(f, true), f.name());
	}

	@Test void puzzleIsLockedUntilTheAutomatonFalls() {
		assertTrue(puzzleActive(Flow.PUZZLE));
		for (Flow f : Flow.values()) if (f != Flow.PUZZLE) assertFalse(puzzleActive(f), f.name());
	}

	@Test void fullRouteAdvancesOneStepAtATime() {
		Flow f = Flow.DORMANT;
		f = afterAutomatonDown(f);
		assertEquals(Flow.DORMANT, f, "sin haber despertado no se salta nada");
		f = Flow.AUTOMATON;
		f = afterAutomatonDown(f);
		assertEquals(Flow.PUZZLE, f);
		f = afterPuzzleSolved(f);
		assertEquals(Flow.DOOR_LOCKED, f);
		f = afterDoorOpened(f);
		assertEquals(Flow.CUSTODIAN, f);
		f = afterCustodianDown(f);
		assertEquals(Flow.CLEARED, f);
	}

	@Test void transitionsIgnoreOutOfOrderEvents() {
		assertEquals(Flow.AUTOMATON, afterPuzzleSolved(Flow.AUTOMATON));
		assertEquals(Flow.PUZZLE, afterDoorOpened(Flow.PUZZLE));
		assertEquals(Flow.DOOR_LOCKED, afterCustodianDown(Flow.DOOR_LOCKED));
		assertEquals(Flow.CLEARED, afterAutomatonDown(Flow.CLEARED));
		assertEquals(Flow.CLEARED, afterCustodianDown(Flow.CLEARED));
	}

	@Test void doorNeedsTheAnchorOnlyWhenItIsReady() {
		assertEquals(DoorUse.OPEN, useDoor(Flow.DOOR_LOCKED, true));
		assertEquals(DoorUse.NEEDS_ANCHOR, useDoor(Flow.DOOR_LOCKED, false));
		for (Flow f : new Flow[] {Flow.DORMANT, Flow.AUTOMATON, Flow.PUZZLE}) {
			assertEquals(DoorUse.NOT_READY, useDoor(f, true), f.name());
			assertEquals(DoorUse.NOT_READY, useDoor(f, false), f.name());
		}
		for (Flow f : new Flow[] {Flow.CUSTODIAN, Flow.CLEARED}) {
			assertEquals(DoorUse.ALREADY_OPEN, useDoor(f, true), f.name());
			assertEquals(DoorUse.ALREADY_OPEN, useDoor(f, false), f.name());
		}
	}

	@Test void bossRespawnsAfterSixMissingChecks() {
		assertFalse(shouldRespawn(0));
		assertFalse(shouldRespawn(MISSING_CHECKS - 1));
		assertTrue(shouldRespawn(MISSING_CHECKS));
		assertTrue(shouldRespawn(MISSING_CHECKS + 10));
	}

	@Test void automatonHealthScalesLikeTheGuardian() {
		for (int ring = -2; ring <= 8; ring++) {
			assertEquals(TempleRules.guardianHealthBonus(ring), automatonHealthBonus(ring), 1e-9, "anillo " + ring);
		}
		assertEquals(0.0, automatonHealthBonus(0));
		assertEquals(1.25, automatonHealthBonus(5), 1e-9);
	}

	@Test void triggerRadiusIsASphere() {
		assertTrue(insideTrigger(0, 0, 0));
		assertTrue(insideTrigger(TRIGGER_RADIUS, 0, 0));
		assertFalse(insideTrigger(TRIGGER_RADIUS + 0.01, 0, 0));
		assertFalse(insideTrigger(7, 7, 7));
		assertFalse(insideTrigger(Double.MAX_VALUE, 0, 0));
	}
}
