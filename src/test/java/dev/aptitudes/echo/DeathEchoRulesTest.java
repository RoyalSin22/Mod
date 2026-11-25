package dev.aptitudes.echo;

import org.junit.jupiter.api.Test;

import static dev.aptitudes.echo.DeathEchoRules.*;
import static org.junit.jupiter.api.Assertions.*;

class DeathEchoRulesTest {
	@Test void keepsFiveSecondsOfSamples() {
		assertEquals(50, CAPACITY);
		assertTrue(shouldSample(0));
		assertFalse(shouldSample(1));
		assertTrue(shouldSample(40));
	}

	@Test void snapshotIsChronologicalBeforeAndAfterWrapping() {
		Track t = new Track();
		for (int i = 0; i < 10; i++) t.add(i, 0, 0, 0);
		Frames f = t.snapshot();
		assertEquals(10, f.size());
		assertEquals(0, f.x()[0]);
		assertEquals(9, f.x()[9]);

		for (int i = 10; i < CAPACITY + 7; i++) t.add(i, 0, 0, 0); // da la vuelta al búfer
		Frames g = t.snapshot();
		assertEquals(CAPACITY, g.size());
		assertEquals(7, g.x()[0], "la más antigua que queda");
		assertEquals(CAPACITY + 6, g.x()[CAPACITY - 1], "la más reciente al final");
		for (int i = 1; i < g.size(); i++) assertEquals(g.x()[i - 1] + 1, g.x()[i]);
	}

	@Test void snapshotIsACopy() {
		Track t = new Track();
		t.add(1, 2, 3, 4);
		Frames f = t.snapshot();
		t.add(9, 9, 9, 9);
		t.clear();
		assertEquals(1, f.size());
		assertEquals(1, f.x()[0]);
		assertEquals(0, t.size());
	}

	@Test void replayLoopsWithAPause() {
		int frames = 20;
		assertEquals(0, frameAt(0, frames));
		assertEquals(1, frameAt(SAMPLE_INTERVAL, frames));
		assertEquals(19, frameAt(19L * SAMPLE_INTERVAL, frames));
		assertEquals(-1, frameAt(20L * SAMPLE_INTERVAL, frames), "pausa");
		assertEquals(-1, frameAt(20L * SAMPLE_INTERVAL + LOOP_PAUSE_TICKS - 1, frames));
		assertEquals(0, frameAt(20L * SAMPLE_INTERVAL + LOOP_PAUSE_TICKS, frames), "vuelve a empezar");
		assertEquals(-1, frameAt(5, 0));
		assertTrue(frameAt(-3, frames) >= -1, "tiempo negativo no lanza excepción");
	}

	@Test void wakeRadiusAndLimits() {
		assertTrue(withinWakeRadius(24 * 24));
		assertFalse(withinWakeRadius(24 * 24 + 1));
		assertTrue(canSpawn(5, 6));
		assertFalse(canSpawn(6, 6));
		assertFalse(canSpawn(0, 0));
	}

	@Test void expiryUsesLong() {
		assertFalse(expired(0, 120 * 20 - 1, 120));
		assertTrue(expired(0, 120 * 20, 120));
		assertFalse(expired(0, 19, 0), "mínimo un segundo… no caduca antes");
		assertFalse(expired(0, 100, Integer.MAX_VALUE));
	}
}
