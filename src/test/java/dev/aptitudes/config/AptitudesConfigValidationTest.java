package dev.aptitudes.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Validación de la configuración al cargar: ningún valor del archivo puede dejar el juego en un estado inválido. */
public class AptitudesConfigValidationTest {

	@Test
	public void defaultsSurviveValidationUnchanged() {
		AptitudesConfig d = new AptitudesConfig();
		AptitudesConfig v = AptitudesConfig.validated(new AptitudesConfig());
		assertEquals(d.maxSkillLevel, v.maxSkillLevel);
		assertEquals(d.xpDiminishingThreshold, v.xpDiminishingThreshold);
		assertEquals(d.attributeScaling, v.attributeScaling);
		assertEquals(d.fishingDoubleCatchPerLevel, v.fishingDoubleCatchPerLevel, 0);
	}

	@Test
	public void negativeNanAndInfiniteFallBackToDefaults() {
		AptitudesConfig d = new AptitudesConfig();
		AptitudesConfig c = new AptitudesConfig();
		c.miningXpOre = -5;
		c.combatXpPerMaxHealth = Double.NaN;
		c.xpBase = Double.POSITIVE_INFINITY;
		c.fishingLuckPerLevel = -1;
		AptitudesConfig v = AptitudesConfig.validated(c);
		assertEquals(d.miningXpOre, v.miningXpOre);
		assertEquals(d.combatXpPerMaxHealth, v.combatXpPerMaxHealth, 0);
		assertEquals(d.xpBase, v.xpBase, 0);
		assertEquals(d.fishingLuckPerLevel, v.fishingLuckPerLevel, 0);
	}

	@Test
	public void outOfRangeValuesAreClamped() {
		AptitudesConfig c = new AptitudesConfig();
		c.eliteChance = 7;
		c.fishingDoubleCatchPerLevel = 3;
		c.xpDiminishingFloor = 2;
		c.xpDiminishingWindowSeconds = 0;
		c.xpDiminishingThreshold = 0;
		c.levelCostDivisor = 0;
		c.maxSkillLevel = 50_000;
		AptitudesConfig v = AptitudesConfig.validated(c);
		assertEquals(1.0, v.eliteChance, 0);
		assertEquals(1.0, v.fishingDoubleCatchPerLevel, 0);
		assertEquals(1.0, v.xpDiminishingFloor, 0);
		assertEquals(1, v.xpDiminishingWindowSeconds);
		assertEquals(1, v.xpDiminishingThreshold);
		assertEquals(1, v.levelCostDivisor);
		assertEquals(1000, v.maxSkillLevel);
	}

	@Test
	public void attributeScalingIsParsedAndSoftCapsAreOrdered() {
		AptitudesConfig c = new AptitudesConfig();
		c.attributeScaling = "nonsense";
		c.attributeSoftCapFirst = 70;
		c.attributeSoftCapSecond = 10;
		c.attributeSaturationPoints = 0.2;
		AptitudesConfig v = AptitudesConfig.validated(c);
		assertEquals("LINEAR", v.attributeScaling);
		assertTrue(v.attributeSoftCapSecond >= v.attributeSoftCapFirst);
		assertEquals(1.0, v.attributeSaturationPoints, 0);
	}
}
