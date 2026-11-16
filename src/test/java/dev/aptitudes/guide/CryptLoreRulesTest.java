package dev.aptitudes.guide;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** V69: el desbloqueo de lore de la Cripta de la Niebla es un id válido y propio. */
class CryptLoreRulesTest {
	@Test void cryptClearedUnlocksItsOwnValidId() {
		assertEquals(Set.of(LoreRules.LORE_CRYPT_CLEARED), LoreRules.unlocksOnCryptCleared());
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_CRYPT_CLEARED));
		assertTrue(LoreRules.validLoreIds().contains("crypt_cleared_lore"));
	}

	@Test void cryptIdDoesNotCollideWithTheOtherHooks() {
		for (Set<String> other : java.util.List.of(LoreRules.unlocksOnCustodianEncounter(),
				LoreRules.unlocksOnDiamondChamberComplete(), LoreRules.unlocksOnBeheritSacrifice(), LoreRules.unlocksOnEclipseEnd())) {
			assertTrue(java.util.Collections.disjoint(other, LoreRules.unlocksOnCryptCleared()));
		}
	}
}
