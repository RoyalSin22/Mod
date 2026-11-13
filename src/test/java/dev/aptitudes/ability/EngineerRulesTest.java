package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F3): cifras de las habilidades nuevas del Ingeniero, tal como las describen los textos. */
class EngineerRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void insulationAndRecycling() {
		assertEquals(0.1, EngineerRules.insulation(1), EPS);
		assertEquals(0.3, EngineerRules.insulation(3), EPS);
		assertEquals(0, EngineerRules.insulation(-1), EPS);
		assertEquals(4, EngineerRules.recycleMana(1), EPS);
		assertEquals(8, EngineerRules.recycleMana(3), EPS);
		assertEquals(0, EngineerRules.recycleMana(0), EPS);
	}

	@Test
	void gadgetDamageAndDuration() {
		assertEquals(6.0, EngineerRules.mineDamage(1, 1), EPS);
		assertEquals(10.0, EngineerRules.mineDamage(3, 1), EPS);
		assertEquals(5.0, EngineerRules.mineDamage(3, 0.5), EPS);
		assertEquals(300, EngineerRules.droneTicks(1));
		assertEquals(400, EngineerRules.droneTicks(2));
		assertEquals(500, EngineerRules.droneTicks(3));
		assertEquals(6.0, EngineerRules.heatDamage(1, 1), EPS);
		assertEquals(8.0, EngineerRules.heatDamage(2, 1), EPS);
	}

	@Test
	void domeReducesFortyToFiftyPercent() {
		assertEquals(0.4, EngineerRules.domeReduction(1), EPS);
		assertEquals(0.5, EngineerRules.domeReduction(2), EPS);
	}

	@Test
	void thriftOnlyAffectsGadgets() {
		assertTrue(EngineerRules.isGadget("turret"));
		assertTrue(EngineerRules.isGadget("engineer_n10"));
		assertFalse(EngineerRules.isGadget("thrusters"));
		assertFalse(EngineerRules.isGadget("pulse_grenade"));
		assertEquals(0.8, EngineerRules.costMultiplier("engineer_n3", true), EPS);
		assertEquals(1.0, EngineerRules.costMultiplier("engineer_n3", false), EPS);
		assertEquals(1.0, EngineerRules.costMultiplier("thrusters", true), EPS);
		assertEquals(130, EngineerRules.duration(100, true));
		assertEquals(100, EngineerRules.duration(100, false));
	}

	@Test
	void exosuitAndAssemblyLine() {
		assertEquals(200, EngineerRules.exoTicks(1));
		assertEquals(280, EngineerRules.exoTicks(2));
		assertEquals(0, EngineerRules.exoTicks(0));
		assertEquals(20, EngineerRules.assemblyTicks(1));
		assertEquals(40, EngineerRules.assemblyTicks(2));
		assertTrue(EngineerRules.ASSEMBLY.containsAll(EngineerRules.GADGETS), "los artilugios entran en la cadena");
	}
}
