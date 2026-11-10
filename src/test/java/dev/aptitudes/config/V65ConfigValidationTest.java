package dev.aptitudes.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Límites de la configuración añadida en v65 (trance, mancha, pedidos y expediciones). */
class V65ConfigValidationTest {
	@Test void defaultsKeepTheCurrentBehaviour() {
		AptitudesConfig c = AptitudesConfig.validated(new AptitudesConfig());
		assertEquals(5, c.tranceGainCritical);
		assertEquals(8, c.tranceGainSkill);
		assertEquals(6.0, c.tranceDurationSeconds);
		assertEquals(1.1, c.tranceDamageMultiplier);
		assertEquals(25, c.soulStainLossPercent);
		assertEquals(48, c.expeditionZoneRadius);
		assertEquals(15, c.expeditionTimeLimitMinutes);
		assertEquals(8, c.expeditionRewardEmeralds);
		assertEquals(2, c.expeditionRewardSoulShards);
	}

	@Test void outOfRangeValuesAreClamped() {
		AptitudesConfig in = new AptitudesConfig();
		in.tranceGainCritical = 500;
		in.tranceDurationSeconds = 0;
		in.tranceDamageMultiplier = 9;
		in.soulStainLossPercent = 150;
		in.expeditionZoneRadius = 1;
		in.expeditionTimeLimitMinutes = 9999;
		AptitudesConfig c = AptitudesConfig.validated(in);
		assertEquals(100, c.tranceGainCritical);
		assertEquals(1.0, c.tranceDurationSeconds);
		assertEquals(3.0, c.tranceDamageMultiplier);
		assertEquals(100, c.soulStainLossPercent);
		assertEquals(8, c.expeditionZoneRadius);
		assertEquals(120, c.expeditionTimeLimitMinutes);
	}
}
