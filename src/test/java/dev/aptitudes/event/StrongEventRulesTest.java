package dev.aptitudes.event;

import dev.aptitudes.event.EclipseRules.Phase;
import dev.aptitudes.event.StrongEventRules.ArmorTier;
import dev.aptitudes.event.StrongEventRules.EclipseMob;
import dev.aptitudes.event.StrongEventRules.MeteorSize;
import dev.aptitudes.event.StrongEventRules.NetherMob;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StrongEventRulesTest {
	@Test public void eclipseWeightsSumToOneHundredAndTotalityIsHarder() {
		for (Phase phase : new Phase[] {Phase.PENUMBRA, Phase.TOTALITY}) {
			int sum = 0;
			for (EclipseMob m : EclipseMob.values()) sum += StrongEventRules.eclipseWeight(phase, m);
			assertEquals(100, sum);
		}
		assertEquals(0, StrongEventRules.eclipseWeight(Phase.PENUMBRA, EclipseMob.WITHER_SKELETON));
		assertTrue(StrongEventRules.eclipseWeight(Phase.TOTALITY, EclipseMob.WITHER_SKELETON) > 0);
		// Todas las criaturas de antes de la v41.2 siguen pudiendo salir en la Penumbra.
		for (EclipseMob m : new EclipseMob[] {EclipseMob.ZOMBIE, EclipseMob.SKELETON, EclipseMob.SPIDER,
				EclipseMob.CREEPER, EclipseMob.VINDICATOR, EclipseMob.WITCH}) {
			assertTrue(m.name(), StrongEventRules.eclipseWeight(Phase.PENUMBRA, m) > 0);
		}
	}

	@Test public void rollsFollowTheWeights() {
		for (Phase phase : new Phase[] {Phase.PENUMBRA, Phase.TOTALITY}) {
			int[] counts = new int[EclipseMob.values().length];
			for (int r = 0; r < 100; r++) counts[StrongEventRules.eclipseMob(phase, r).ordinal()]++;
			for (EclipseMob m : EclipseMob.values()) assertEquals(StrongEventRules.eclipseWeight(phase, m), counts[m.ordinal()]);
		}
		for (MeteorSize s : MeteorSize.values()) {
			int[] counts = new int[NetherMob.values().length];
			int sum = 0;
			for (int r = 0; r < 100; r++) counts[StrongEventRules.netherMob(s, r).ordinal()]++;
			for (NetherMob m : NetherMob.values()) {
				assertEquals(StrongEventRules.netherWeight(s, m), counts[m.ordinal()]);
				sum += StrongEventRules.netherWeight(s, m);
			}
			assertEquals(100, sum);
		}
		assertEquals(StrongEventRules.eclipseMob(Phase.TOTALITY, 0), StrongEventRules.eclipseMob(Phase.TOTALITY, -9));
		assertEquals(StrongEventRules.netherMob(MeteorSize.HUGE, 99), StrongEventRules.netherMob(MeteorSize.HUGE, 900));
	}

	@Test public void phaseBonusAndArmorAreBounded() {
		assertEquals(0.0, StrongEventRules.phaseBonus(Phase.PENUMBRA, 0.25), 1e-9);
		assertEquals(0.25, StrongEventRules.phaseBonus(Phase.TOTALITY, 0.25), 1e-9);
		assertEquals(2.0, StrongEventRules.phaseBonus(Phase.TOTALITY, 50), 1e-9);
		assertEquals(0.0, StrongEventRules.phaseBonus(Phase.TOTALITY, Double.NaN), 1e-9);
		assertEquals(20.0, StrongEventRules.armorPoints(999), 1e-9);
		assertEquals(0.0, StrongEventRules.armorPoints(-3), 1e-9);
		assertEquals(ArmorTier.CHAINMAIL, StrongEventRules.armorTier(Phase.PENUMBRA, false));
		assertEquals(ArmorTier.IRON, StrongEventRules.armorTier(Phase.TOTALITY, false));
		assertEquals(ArmorTier.DIAMOND, StrongEventRules.armorTier(Phase.PENUMBRA, true));
	}

	@Test public void meteorSizesGrowInEveryAspect() {
		assertEquals(MeteorSize.HUGE, StrongEventRules.meteorSize(0.01, 0.25, 0.05));
		assertEquals(MeteorSize.LARGE, StrongEventRules.meteorSize(0.10, 0.25, 0.05));
		assertEquals(MeteorSize.SMALL, StrongEventRules.meteorSize(0.31, 0.25, 0.05));
		assertEquals(MeteorSize.SMALL, StrongEventRules.meteorSize(0.0, 0.0, 0.0));
		assertEquals(MeteorSize.HUGE, StrongEventRules.meteorSize(0.99, 5.0, 5.0)); // acotado a 1
		MeteorSize[] sizes = MeteorSize.values();
		for (int i = 1; i < sizes.length; i++) {
			assertTrue(StrongEventRules.explosionPower(sizes[i]) > StrongEventRules.explosionPower(sizes[i - 1]));
			assertTrue(StrongEventRules.fallTicks(sizes[i]) > StrongEventRules.fallTicks(sizes[i - 1]));
			assertTrue(StrongEventRules.lootRolls(sizes[i]) > StrongEventRules.lootRolls(sizes[i - 1]));
			assertTrue(StrongEventRules.craterRadius(sizes[i]) > StrongEventRules.craterRadius(sizes[i - 1]));
		}
		assertEquals(1.5f, StrongEventRules.explosionPower(MeteorSize.SMALL), 1e-6); // igual que antes de la v41.2
	}

	@Test public void netherMobsScaleWithSizeAndChance() {
		assertEquals(0, StrongEventRules.netherMobCount(MeteorSize.SMALL, 0.3, 0.5));  // pequeño: la mitad (0,25)
		assertEquals(1, StrongEventRules.netherMobCount(MeteorSize.SMALL, 0.2, 0.5));
		assertEquals(2, StrongEventRules.netherMobCount(MeteorSize.LARGE, 0.4, 0.5));
		assertEquals(3, StrongEventRules.netherMobCount(MeteorSize.HUGE, 0.0, 0.5));
		assertEquals(0, StrongEventRules.netherMobCount(MeteorSize.HUGE, 0.0, 0.0));
		assertEquals(0, StrongEventRules.netherMobCount(MeteorSize.HUGE, 0.9, Double.NaN));
	}
}
