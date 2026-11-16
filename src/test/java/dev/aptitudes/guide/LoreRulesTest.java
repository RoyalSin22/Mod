package dev.aptitudes.guide;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class LoreRulesTest {
	@Test
	void validLoreIdsNotEmpty() {
		assertTrue(LoreRules.validLoreIds().size() > 0);
	}

	@Test
	void validLoreIdsAreAllValid() {
		for (String id : LoreRules.validLoreIds()) {
			assertTrue(LoreRules.isValidLoreId(id), "Id should be valid: " + id);
		}
	}

	@Test
	void isValidLoreId() {
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_SOUL_FOG_TRIGGER));
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_EMBER_RAIN_TRIGGER));
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_CUSTODIAN_DEFEATED));
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_BEHELIT_FOUND));
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_ECLIPSE_SACRIFICE));
		assertTrue(LoreRules.isValidLoreId(LoreRules.LORE_ECLIPSE_AFTERMATH));
		assertFalse(LoreRules.isValidLoreId("invalid_lore_id"));
	}

	@Test
	void unlocksOnGuildmasterTalk() {
		assertEquals(Set.of(LoreRules.LORE_SOUL_FOG_TRIGGER), LoreRules.unlocksOnGuildmasterTalk("fog"));
		assertEquals(Set.of(LoreRules.LORE_EMBER_RAIN_TRIGGER), LoreRules.unlocksOnGuildmasterTalk("rain"));
		assertEquals(Set.of(), LoreRules.unlocksOnGuildmasterTalk("unknown"));
	}

	@Test
	void unlocksOnCustodianEncounter() {
		assertEquals(Set.of(LoreRules.LORE_CUSTODIAN_DEFEATED), LoreRules.unlocksOnCustodianEncounter());
	}

	@Test
	void unlocksOnDiamondChamberComplete() {
		assertEquals(Set.of(LoreRules.LORE_BEHELIT_FOUND), LoreRules.unlocksOnDiamondChamberComplete());
	}

	@Test
	void unlocksOnBeheritSacrifice() {
		assertEquals(Set.of(LoreRules.LORE_ECLIPSE_SACRIFICE), LoreRules.unlocksOnBeheritSacrifice());
	}

	@Test
	void unlocksOnEclipseEnd() {
		assertEquals(Set.of(LoreRules.LORE_ECLIPSE_AFTERMATH), LoreRules.unlocksOnEclipseEnd());
	}
}
