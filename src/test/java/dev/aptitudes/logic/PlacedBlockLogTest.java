package dev.aptitudes.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlacedBlockLogTest {

	@Test
	public void placedBlockIsConsumedOnce() {
		PlacedBlockLog log = new PlacedBlockLog(100);
		log.mark("overworld", 42L);
		assertTrue(log.consume("overworld", 42L));
		assertFalse(log.consume("overworld", 42L)); // al romperlo se olvida: el siguiente bloque natural da XP
	}

	@Test
	public void worldsAreIndependent() {
		PlacedBlockLog log = new PlacedBlockLog(100);
		log.mark("overworld", 7L);
		assertFalse(log.consume("the_nether", 7L));
		assertTrue(log.contains("overworld", 7L));
	}

	@Test
	public void capForgetsOldestFirstAndRemarkRefreshes() {
		PlacedBlockLog log = new PlacedBlockLog(3);
		log.mark("w", 1L);
		log.mark("w", 2L);
		log.mark("w", 3L);
		log.mark("w", 1L); // 1 pasa a ser el más reciente
		log.mark("w", 4L); // se olvida el más antiguo: 2
		assertEquals(3, log.size("w"));
		assertFalse(log.contains("w", 2L));
		assertTrue(log.contains("w", 1L));
		assertTrue(log.contains("w", 3L));
		assertTrue(log.contains("w", 4L));
	}

	@Test
	public void invalidCapIsAtLeastOne() {
		PlacedBlockLog log = new PlacedBlockLog(0);
		log.mark("w", 1L);
		log.mark("w", 2L);
		assertEquals(1, log.size("w"));
		assertTrue(log.contains("w", 2L));
	}
}
