package dev.aptitudes.forge;

import dev.aptitudes.forge.ForgeRules.Affix;
import dev.aptitudes.forge.ForgeRules.Profile;
import dev.aptitudes.forge.ForgeRules.Upgrade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ForgeRulesTest {
	@Test
	void reforgingAnOldWeaponStartsAtZeroAndSwitchingKeepsItsRank() {
		assertEquals(new Upgrade(new Affix(Profile.RESERVE, 0), 1, 8), ForgeRules.reforge(null, Profile.RESERVE));
		Affix current = new Affix(Profile.RESERVE, 2);
		assertNull(ForgeRules.reforge(current, Profile.RESERVE));
		assertEquals(new Upgrade(new Affix(Profile.IMPETUS, 2), 1, 8), ForgeRules.reforge(current, Profile.IMPETUS));
	}

	@Test
	void temperingRequiresAnAffixAndStopsAtTwo() {
		assertNull(ForgeRules.temper(null));
		for (Profile profile : Profile.values()) {
			Affix first = new Affix(profile, 1);
			Affix second = new Affix(profile, 2);
			assertEquals(new Upgrade(first, 1, 12), ForgeRules.temper(new Affix(profile, 0)));
			assertEquals(new Upgrade(second, 2, 16), ForgeRules.temper(first));
			assertNull(ForgeRules.temper(second));
		}
	}

	@Test
	void unknownStoredProfilesAndOutOfRangeRanksDisableTheAffix() {
		assertEquals(new Affix(Profile.RESERVE, 0), ForgeRules.read("reserve", 0));
		assertEquals(new Affix(Profile.IMPETUS, 2), ForgeRules.read("impetus", 2));
		assertNull(ForgeRules.read("missing", 0));
		assertNull(ForgeRules.read(null, 0));
		assertNull(ForgeRules.read("reserve", -1));
		assertNull(ForgeRules.read("reserve", Integer.MAX_VALUE));
	}

	@Test
	void reserveAppliesToTheCalculatedCostAndKeepsFreeAndSmallCosts() {
		assertEquals(98, ForgeRules.energyCost(100, new Affix(Profile.RESERVE, 0)), 1e-9);
		assertEquals(96, ForgeRules.energyCost(100, new Affix(Profile.RESERVE, 1)), 1e-9);
		Affix reserve = new Affix(Profile.RESERVE, 2);
		assertEquals(94, ForgeRules.energyCost(100, reserve), 1e-9);
		assertEquals(1, ForgeRules.energyCost(1.01, reserve), 1e-9);
		assertEquals(0.99, ForgeRules.energyCost(0.99, reserve), 1e-9);
		assertEquals(0, ForgeRules.energyCost(0, reserve), 1e-9);
		assertEquals(100, ForgeRules.energyCost(100, new Affix(Profile.IMPETUS, 2)), 1e-9);
		assertEquals(100, ForgeRules.energyCost(100, null), 1e-9);
	}

	@Test
	void impetusAppliesOnlyItsRankBonusToTheWeaponGain() {
		assertEquals(3.15f, ForgeRules.impactGain(3, new Affix(Profile.IMPETUS, 0)), 1e-6f);
		assertEquals(3.30f, ForgeRules.impactGain(3, new Affix(Profile.IMPETUS, 1)), 1e-6f);
		assertEquals(3.45f, ForgeRules.impactGain(3, new Affix(Profile.IMPETUS, 2)), 1e-6f);
		assertEquals(3, ForgeRules.impactGain(3, new Affix(Profile.RESERVE, 2)), 1e-6f);
		assertEquals(3, ForgeRules.impactGain(3, null), 1e-6f);
	}
}
