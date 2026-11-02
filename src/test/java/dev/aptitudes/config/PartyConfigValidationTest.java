package dev.aptitudes.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PartyConfigValidationTest {
	@Test
	public void partySettingsAreBoundedToSafeValues() {
		AptitudesConfig config = new AptitudesConfig();
		config.partyMaxSize = 50;
		config.partyMaxXpShare = 4.0;
		config.partyXpBonusPerMember = 2.0;
		config.partyXpBonusMax = 3.0;

		AptitudesConfig validated = AptitudesConfig.validated(config);

		assertEquals(5, validated.partyMaxSize);
		assertEquals(0.5, validated.partyMaxXpShare, 1e-9);
		assertEquals(0.2, validated.partyXpBonusPerMember, 1e-9);
		assertEquals(0.2, validated.partyXpBonusMax, 1e-9);
	}

	@Test
	public void partySettingsHaveTheApprovedDefaults() {
		AptitudesConfig config = new AptitudesConfig();
		assertEquals(5, config.partyMaxSize);
		assertEquals(0.5, config.partyMaxXpShare, 1e-9);
		assertEquals(0.05, config.partyXpBonusPerMember, 1e-9);
		assertEquals(0.2, config.partyXpBonusMax, 1e-9);
	}
}
