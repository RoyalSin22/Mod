package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v36: guardar y recuperar el registro de bloques colocados (orden, tope y mundos separados). */
class PlacedBlockPersistenceTest {
	@Test
	void snapshotKeepsInsertionOrderAndRoundTrips() {
		PlacedBlockLog log = new PlacedBlockLog(10);
		log.mark("overworld", 5);
		log.mark("overworld", 7);
		log.mark("overworld", 5); // reinsertado: pasa al final
		assertArrayEquals(new long[] {7, 5}, log.snapshot("overworld"));

		PlacedBlockLog restored = new PlacedBlockLog(10);
		restored.load("overworld", log.snapshot("overworld"));
		assertTrue(restored.contains("overworld", 5));
		assertTrue(restored.contains("overworld", 7));
		assertArrayEquals(log.snapshot("overworld"), restored.snapshot("overworld"));
	}

	@Test
	void loadKeepsOnlyTheNewestWhenOverCap() {
		PlacedBlockLog log = new PlacedBlockLog(3);
		log.load("w", new long[] {1, 2, 3, 4, 5});
		assertEquals(3, log.size("w"));
		assertFalse(log.contains("w", 2));
		assertArrayEquals(new long[] {3, 4, 5}, log.snapshot("w"));
	}

	@Test
	void worldsStaySeparateAndClearForgets() {
		PlacedBlockLog log = new PlacedBlockLog(10);
		log.mark("a", 1);
		log.load("b", new long[] {2});
		assertTrue(log.contains("a", 1));
		assertFalse(log.contains("a", 2));
		log.clear("a");
		assertEquals(0, log.size("a"));
		assertEquals(1, log.size("b"));
		log.load("b", null);
		assertEquals(0, log.size("b"));
		assertArrayEquals(new long[0], log.snapshot("missing"));
	}
}
