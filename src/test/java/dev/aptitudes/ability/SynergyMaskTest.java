package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SynergyMaskTest {
	@Test
	void completesOnlyWithAllFive() {
		int mask = 0;
		for (int i = 0; i < 4; i++) mask = SynergyMask.add(mask, i);
		assertFalse(SynergyMask.complete(mask));
		assertEquals(4, SynergyMask.count(mask));
		mask = SynergyMask.add(mask, 4);
		assertTrue(SynergyMask.complete(mask));
	}

	@Test
	void ignoresOutOfRangeAndRepeats() {
		assertEquals(0, SynergyMask.add(0, -1));
		assertEquals(0, SynergyMask.add(0, 5));
		int mask = SynergyMask.add(0, 2);
		assertEquals(mask, SynergyMask.add(mask, 2));
		assertTrue(SynergyMask.has(mask, 2));
		assertFalse(SynergyMask.has(mask, 3));
	}
}
