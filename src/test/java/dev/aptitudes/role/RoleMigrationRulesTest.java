package dev.aptitudes.role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleMigrationRulesTest {
	@ParameterizedTest
	@CsvSource({
			"mage,            arcanist",
			"elementalist,    shaman",
			"herbalist,       apothecary",
			"swordsman,       duelist",
			"merchant,        tactical_merchant",
			"warlock,         abyssal_warlock",
			"necromancer,     necromancer",
			"druid,           druid",
	})
	void legacyIdsMapToTheirApprovedReplacementAndOthersStayAsTheyAre(String saved, String expected) {
		assertEquals(expected, RoleMigrationRules.resolve(saved));
	}

	@Test void nullAndUnknownKeysAreNotInvented() {
		assertNull(RoleMigrationRules.resolve(null));
		assertEquals("future_role", RoleMigrationRules.resolve("future_role"));
	}

	@Test void targetsAreUniqueAndNeverReuseALegacyId() {
		Set<String> targets = new HashSet<>(RoleMigrationRules.LEGACY_TO_NEW.values());
		assertEquals(RoleMigrationRules.LEGACY_TO_NEW.size(), targets.size(), "destinos repetidos");
		for (String target : targets) {
			assertFalse(RoleMigrationRules.isLegacy(target), target + " reutiliza un id antiguo");
		}
	}

	@Test void onlySixRolesAreMigratedAndTheRestKeepTheirIds() {
		assertEquals(6, RoleMigrationRules.LEGACY_TO_NEW.size());
		assertTrue(RoleMigrationRules.isLegacy("mage"));
		assertFalse(RoleMigrationRules.isLegacy("necromancer"));
		assertFalse(RoleMigrationRules.isLegacy("druid"));
	}
}
