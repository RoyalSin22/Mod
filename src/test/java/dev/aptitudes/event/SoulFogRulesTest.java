package dev.aptitudes.event;

import dev.aptitudes.event.SoulFogRules.Climate;
import dev.aptitudes.event.SoulFogRules.FogMob;
import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoulFogRulesTest {
	@Test public void weightsSumToOneHundredInEveryClimate() {
		for (Climate c : Climate.values()) {
			int sum = 0;
			for (FogMob m : FogMob.values()) {
				assertTrue(SoulFogRules.weight(c, m) >= 0);
				sum += SoulFogRules.weight(c, m);
			}
			assertEquals(c.name(), 100, sum);
		}
	}

	@Test public void climateFollowsWaterThenTemperature() {
		assertEquals(Climate.WATER, SoulFogRules.climate(true, 2.0f));
		assertEquals(Climate.WATER, SoulFogRules.climate(true, -0.5f));
		assertEquals(Climate.HOT, SoulFogRules.climate(false, 2.0f));   // desierto
		assertEquals(Climate.TEMPERATE, SoulFogRules.climate(false, 1.2f)); // sabana
		assertEquals(Climate.TEMPERATE, SoulFogRules.climate(false, 0.8f)); // llanura
		assertEquals(Climate.COLD, SoulFogRules.climate(false, 0.0f));  // llanura nevada
		assertEquals(Climate.TEMPERATE, SoulFogRules.climate(false, Float.NaN));
	}

	@Test public void everyRollGivesTheWeightedDistribution() {
		for (Climate c : Climate.values()) {
			Map<FogMob, Integer> counts = new EnumMap<>(FogMob.class);
			for (int roll = 0; roll < 100; roll++) counts.merge(SoulFogRules.choose(c, roll), 1, Integer::sum);
			for (FogMob m : FogMob.values()) {
				assertEquals(c + "/" + m, SoulFogRules.weight(c, m), (int) counts.getOrDefault(m, 0));
			}
		}
	}

	@Test public void variantsBelongToTheirClimateAndCommonUndeadAlwaysAppear() {
		assertTrue(SoulFogRules.weight(Climate.WATER, FogMob.DROWNED) >= 50);
		assertTrue(SoulFogRules.weight(Climate.HOT, FogMob.HUSK) >= 50);
		assertTrue(SoulFogRules.weight(Climate.COLD, FogMob.STRAY) >= 50);
		assertEquals(0, SoulFogRules.weight(Climate.TEMPERATE, FogMob.DROWNED));
		assertEquals(0, SoulFogRules.weight(Climate.HOT, FogMob.STRAY));
		for (Climate c : Climate.values()) {
			assertTrue(SoulFogRules.weight(c, FogMob.ZOMBIE) > 0);
			assertTrue(SoulFogRules.weight(c, FogMob.SKELETON) > 0);
		}
	}

	@Test public void outOfRangeRollsAreClamped() {
		assertEquals(SoulFogRules.choose(Climate.TEMPERATE, 0), SoulFogRules.choose(Climate.TEMPERATE, -50));
		assertEquals(SoulFogRules.choose(Climate.TEMPERATE, 99), SoulFogRules.choose(Climate.TEMPERATE, 500));
	}
}
