package dev.aptitudes.role;

import dev.aptitudes.role.KeystoneRules.Template;
import org.junit.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class KeystoneRulesTest {
	@Test public void everyRoleHasThreeDistinctKeystonesAndLegionOnlyWithMinions() {
		assertEquals(20, KeystoneRules.roles().size());
		for (String role : KeystoneRules.roles()) {
			Set<Template> seen = EnumSet.noneOf(Template.class);
			for (int path = 1; path <= 3; path++) {
				Template t = KeystoneRules.template(role, path);
				assertTrue(role + " " + path, t != null && seen.add(t));
				if (t == Template.LEGION) assertTrue(role, role.equals("necromancer") || role.equals("tamer"));
			}
		}
		assertEquals(Template.CARNAGE, KeystoneRules.template("berserker", 1));
		assertEquals(Template.TEMPEST, KeystoneRules.template("berserker", 3));
		assertEquals(Template.TEMPEST, KeystoneRules.template("weapon_summoner", 1));
		assertNull(KeystoneRules.template("berserker", 4));
		assertNull(KeystoneRules.template("nadie", 1));
	}

	@Test public void overloadMakesEveryFourthCastFree() {
		int counter = 0;
		int free = 0;
		for (int cast = 1; cast <= 12; cast++) {
			if (KeystoneRules.overloadReady(counter, 4)) free++;
			boolean wasFree = KeystoneRules.overloadReady(counter, 4);
			counter = KeystoneRules.overloadAfterCast(counter, 4);
			assertEquals("lanzamiento " + cast, cast % 4 == 0, wasFree);
		}
		assertEquals(3, free);
		assertEquals(2, KeystoneRules.overloadEvery(0));
		assertEquals(20, KeystoneRules.overloadEvery(999));
	}

	@Test public void bulwarkNeedsLowHealthAndNoCooldown() {
		assertTrue(KeystoneRules.bulwarkTriggers(5f, 20f, 0.30, 100, 100));
		assertFalse(KeystoneRules.bulwarkTriggers(7f, 20f, 0.30, 100, 0));   // 35 %: no
		assertFalse(KeystoneRules.bulwarkTriggers(5f, 20f, 0.30, 99, 100));  // en espera
		assertFalse(KeystoneRules.bulwarkTriggers(0f, 20f, 0.30, 100, 0));   // ya muerto
		assertFalse(KeystoneRules.bulwarkTriggers(5f, 20f, -1, 100, 0));     // umbral acotado a 0
	}

	@Test public void cooldownsAreBoundedAndProgressIsAFraction() {
		assertEquals(1300L, KeystoneRules.readyAt(100, 60));
		assertEquals(100L + 3600 * 20, KeystoneRules.readyAt(100, 1e12));
		assertEquals(100L, KeystoneRules.readyAt(100, Double.NaN));
		assertEquals(1f, KeystoneRules.progress(2000, 1300, 60), 1e-6f);
		assertEquals(0.5f, KeystoneRules.progress(700, 1300, 60), 1e-6f);
	}

	@Test public void valuesAreBounded() {
		assertEquals(2f, KeystoneRules.carnageHeal(20f, 0.10), 1e-6f);
		assertEquals(20f, KeystoneRules.carnageHeal(20f, 5.0), 1e-6f);
		assertEquals(0f, KeystoneRules.carnageHeal(20f, Double.NaN), 1e-6f);
		assertEquals(1, KeystoneRules.absorptionAmplifier(8));   // Absorción II = 8 de vida
		assertEquals(0, KeystoneRules.absorptionAmplifier(4));
		assertEquals(0, KeystoneRules.absorptionAmplifier(2));
		assertEquals(-1, KeystoneRules.absorptionAmplifier(0));
		assertEquals(9, KeystoneRules.absorptionAmplifier(1000));
		assertEquals(1f, KeystoneRules.legionHeal(10f, 0.10), 1e-6f);
		assertEquals(5, KeystoneRules.maxMinions(4, true));
		assertEquals(4, KeystoneRules.maxMinions(4, false));
	}
}
