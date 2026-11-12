package dev.aptitudes.config;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** v66 (auditoría): los campos numéricos tienen un máximo, y los valores por defecto no cambian con él. */
public class ConfigUpperBoundsTest {

	@Test
	public void extremeValuesAreCappedInsteadOfPassingThrough() {
		AptitudesConfig c = new AptitudesConfig();
		c.levelCostMax = Integer.MAX_VALUE;
		c.levelCostDivisor = Integer.MAX_VALUE;
		c.levelCostBase = Integer.MAX_VALUE;
		c.soulLevelsPerPoint = Integer.MAX_VALUE;
		c.miningXpStone = Integer.MAX_VALUE;
		c.roleXpPerAction = Integer.MAX_VALUE;
		c.combatDamagePerLevel = 1e300;
		c.mindManaPerPoint = Double.MAX_VALUE;
		c.maxDamageMultiplier = 1e12;
		c.roleXpPerDamage = 1e9;
		c.uniqueReleaseAfterDays = Integer.MAX_VALUE;
		AptitudesConfig v = AptitudesConfig.validated(c);
		assertEquals(10_000, v.levelCostMax);
		assertEquals(100_000, v.levelCostDivisor);
		assertEquals(10_000, v.levelCostBase);
		assertEquals(100, v.soulLevelsPerPoint);
		assertEquals(100_000, v.miningXpStone);
		assertEquals(100_000, v.roleXpPerAction);
		assertEquals(100.0, v.combatDamagePerLevel, 0);
		assertEquals(100.0, v.mindManaPerPoint, 0);
		assertEquals(1000.0, v.maxDamageMultiplier, 0);
		assertEquals(1000.0, v.roleXpPerDamage, 0);
		assertEquals(3650, v.uniqueReleaseAfterDays);
	}

	@Test
	public void theMaximumCostOfASoulLevelPurchaseFitsInAnInt() {
		AptitudesConfig v = AptitudesConfig.validated(new AptitudesConfig());
		// SoulsMath.validRequestTotal admite hasta 1000 puntos; cada punto compra como mucho soulLevelsPerPoint niveles
		// de alma y cada nivel cuesta como mucho levelCostMax: con las cotas máximas es 1000 × 100 × 10 000 = 10⁹.
		AptitudesConfig extreme = new AptitudesConfig();
		extreme.soulLevelsPerPoint = Integer.MAX_VALUE;
		extreme.levelCostMax = Integer.MAX_VALUE;
		extreme = AptitudesConfig.validated(extreme);
		long worst = 1000L * extreme.soulLevelsPerPoint * extreme.levelCostMax;
		assertTrue(worst <= Integer.MAX_VALUE);
		assertTrue(v.levelCostMax >= 1);
	}

	@Test
	public void defaultsAreInsideTheirCaps() {
		AptitudesConfig d = new AptitudesConfig();
		AptitudesConfig v = AptitudesConfig.validated(new AptitudesConfig());
		for (Field f : AptitudesConfig.class.getFields()) {
			if (Modifier.isStatic(f.getModifiers())) continue;
			try {
				if (f.getType() == int.class) assertEquals(f.getName(), f.getInt(d), f.getInt(v));
				else if (f.getType() == double.class) assertEquals(f.getName(), f.getDouble(d), f.getDouble(v), 0);
			} catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
		}
	}
}
