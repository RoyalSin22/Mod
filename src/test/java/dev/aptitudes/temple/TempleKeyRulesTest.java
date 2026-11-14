package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleFlowRules.Flow;
import dev.aptitudes.temple.TempleKeyRules.KeyUse;
import dev.aptitudes.temple.TempleKeyRules.Metal;
import org.junit.jupiter.api.Test;

import static dev.aptitudes.temple.TempleKeyRules.*;
import static org.junit.jupiter.api.Assertions.*;

class TempleKeyRulesTest {
	@Test void metalOrderIsStableBecauseItIsSavedAsANumber() {
		assertArrayEquals(new Metal[] {Metal.COPPER, Metal.IRON, Metal.GOLD, Metal.DIAMOND}, Metal.values());
		assertEquals(Metal.GOLD, metalFromOrdinal(2));
		assertNull(metalFromOrdinal(-1));
		assertNull(metalFromOrdinal(4));
		assertNull(metalFromOrdinal(Integer.MAX_VALUE));
	}

	@Test void customTempleDropsAllFourKeysAndTheOldOneNone() {
		assertArrayEquals(Metal.values(), keysToDrop(TempleFlowRules.LAYOUT_CUSTODIAN));
		assertEquals(0, keysToDrop(TempleFlowRules.LAYOUT_OLD).length);
		assertEquals(0, keysToDrop(-3).length);
		assertEquals(0, keysToDrop(99).length);
	}

	@Test void chambersStayClosedUntilTheCustodianFalls() {
		for (Flow f : Flow.values()) {
			if (f == Flow.CLEARED) continue;
			assertEquals(KeyUse.NOT_READY, useChamberDoor(f, Metal.GOLD, Metal.GOLD, true), f.name());
			assertEquals(KeyUse.NOT_READY, useChamberDoor(f, Metal.GOLD, null, false), f.name());
		}
	}

	@Test void onlyTheMatchingKeyOfTheSameTempleOpens() {
		for (Metal door : Metal.values()) {
			for (Metal key : Metal.values()) {
				KeyUse expected = door == key ? KeyUse.OPEN : KeyUse.WRONG_METAL;
				assertEquals(expected, useChamberDoor(Flow.CLEARED, door, key, true), door + "/" + key);
				assertEquals(KeyUse.WRONG_TEMPLE, useChamberDoor(Flow.CLEARED, door, key, false), door + "/" + key);
			}
			assertEquals(KeyUse.NO_KEY, useChamberDoor(Flow.CLEARED, door, null, true));
			assertEquals(KeyUse.NO_KEY, useChamberDoor(Flow.CLEARED, door, null, false));
		}
	}

	@Test void openedMaskTracksEachChamberIndependently() {
		int mask = 0;
		for (Metal m : Metal.values()) assertFalse(isOpened(mask, m));
		mask = withOpened(mask, Metal.IRON);
		assertTrue(isOpened(mask, Metal.IRON));
		assertFalse(isOpened(mask, Metal.COPPER));
		mask = withOpened(withOpened(mask, Metal.IRON), Metal.DIAMOND);
		assertTrue(isOpened(mask, Metal.DIAMOND));
		assertEquals(0b1010, mask);
	}

	@Test void savedMaskIsReducedToKnownMetals() {
		assertEquals(0b1111, sanitizeMask(-1));
		assertEquals(0b0101, sanitizeMask(0b10101));
		assertEquals(0, sanitizeMask(0));
		assertEquals(0b1111, sanitizeMask(Integer.MAX_VALUE));
	}
}
