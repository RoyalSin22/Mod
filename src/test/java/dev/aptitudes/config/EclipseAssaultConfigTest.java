package dev.aptitudes.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EclipseAssaultConfigTest {
	@Test void oldConfigurationKeepsItsValuesAndGetsSafeNewDefaults() {
		AptitudesConfig c = new com.google.gson.Gson().fromJson("{\"eclipseWaveIntervalSeconds\":45}", AptitudesConfig.class);
		c = AptitudesConfig.validated(c);
		assertEquals(45, c.eclipseWaveIntervalSeconds);
		assertEquals(4, c.eclipsePenumbraWaveSize);
		assertEquals(8, c.eclipseTotalityWaveSize);
		assertEquals(128, c.eclipseWorldMobCap);
		assertTrue(c.eclipseGuaranteedEliteArmor);
	}

	@Test void defaultsIncreaseWavesWithoutChangingExistingBalanceFields() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertEquals(4, c.eclipsePenumbraWaveSize);
		assertEquals(8, c.eclipseTotalityWaveSize);
		assertEquals(24, c.eclipseZoneMobCap);
		assertEquals(128, c.eclipseWorldMobCap);
		assertEquals(1, c.eclipseMinElitesPerWave);
		assertEquals(1, c.eclipseMinChampionsTotality);
		assertTrue(c.eclipseGuaranteedEliteArmor);
		assertEquals(30, c.eclipseWaveIntervalSeconds);
		assertEquals(12, c.eclipseMaxMobsPerPlayer);
		assertEquals(1.5, c.eclipseMobHealthMultiplier);
		assertEquals(1.5, c.eclipseMobDamageMultiplier);
		assertEquals(0.5, c.eclipseEliteChance);
		assertEquals(0.1, c.eclipseChampionChance);
		assertEquals(0.35, c.eclipseArmorChance);
	}

	@Test void newIntegersClampAndSupportZeroAsDisableValue() {
		AptitudesConfig c = new AptitudesConfig();
		c.eclipsePenumbraWaveSize = c.eclipseTotalityWaveSize = c.eclipseZoneMobCap = c.eclipseWorldMobCap
				= c.eclipseMinElitesPerWave = c.eclipseMinChampionsTotality = Integer.MAX_VALUE;
		c = AptitudesConfig.validated(c);
		assertEquals(16, c.eclipsePenumbraWaveSize);
		assertEquals(24, c.eclipseTotalityWaveSize);
		assertEquals(160, c.eclipseZoneMobCap);
		assertEquals(512, c.eclipseWorldMobCap);
		assertEquals(4, c.eclipseMinElitesPerWave);
		assertEquals(2, c.eclipseMinChampionsTotality);
		c.eclipsePenumbraWaveSize = c.eclipseTotalityWaveSize = c.eclipseZoneMobCap = c.eclipseWorldMobCap
				= c.eclipseMinElitesPerWave = c.eclipseMinChampionsTotality = 0;
		c = AptitudesConfig.validated(c);
		assertEquals(0, c.eclipsePenumbraWaveSize);
		assertEquals(0, c.eclipseTotalityWaveSize);
		assertEquals(0, c.eclipseZoneMobCap);
		assertEquals(0, c.eclipseWorldMobCap);
		assertEquals(0, c.eclipseMinElitesPerWave);
		assertEquals(0, c.eclipseMinChampionsTotality);
	}
}
