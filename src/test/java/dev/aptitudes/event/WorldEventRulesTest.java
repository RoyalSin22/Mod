package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v41: rotación de eventos del mundo. */
class WorldEventRulesTest {
	@Test
	void eventIndexFollowsTheCalendar() {
		assertEquals(-1, WorldEventRules.eventIndex(9, 10, 10));
		assertEquals(1, WorldEventRules.eventIndex(10, 10, 10));
		assertEquals(2, WorldEventRules.eventIndex(20, 10, 10));
		assertEquals(-1, WorldEventRules.eventIndex(25, 10, 10));
	}

	@Test
	void neverRepeatsThePreviousWhenThereIsAnotherOption() {
		int[] weights = {1, 1, 1};
		int previous = -1;
		for (long i = 1; i < 500; i++) {
			int next = WorldEventRules.choose(12345L, i, weights, previous);
			assertTrue(next >= 0 && next < 3);
			assertNotEquals(previous, next);
			previous = next;
		}
	}

	@Test
	void isDeterministicAndUsesAllEvents() {
		int[] weights = {1, 1, 1};
		int[] seen = new int[3];
		for (long i = 0; i < 300; i++) {
			int a = WorldEventRules.choose(99L, i, weights, -1);
			assertEquals(a, WorldEventRules.choose(99L, i, weights, -1));
			seen[a]++;
		}
		for (int s : seen) assertTrue(s > 50, "reparto muy desigual");
	}

	@Test
	void weightsDisableEventsAndSingleOptionMayRepeat() {
		assertEquals(-1, WorldEventRules.choose(1L, 5, new int[] {0, 0, 0}, -1));
		for (long i = 0; i < 50; i++) {
			int k = WorldEventRules.choose(7L, i, new int[] {0, 3, 2}, 1);
			assertEquals(2, k); // el 0 está desactivado y el 1 fue el anterior
		}
		assertEquals(0, WorldEventRules.choose(7L, 3, new int[] {5, 0, 0}, 0)); // solo hay uno: se repite
	}

	@Test
	void rampRisesHoldsAndFalls() {
		assertEquals(0f, WorldEventRules.ramp(-1, 1000, 100));
		assertEquals(0.5f, WorldEventRules.ramp(50, 1000, 100), 1e-6);
		assertEquals(1f, WorldEventRules.ramp(500, 1000, 100));
		assertEquals(0.5f, WorldEventRules.ramp(950, 1000, 100), 1e-6);
		assertEquals(0f, WorldEventRules.ramp(1000, 1000, 100));
	}
}
