package dev.aptitudes.ability;

import dev.aptitudes.ability.MageRules.ConcatTracker;
import dev.aptitudes.ability.MageRules.CrystalMeter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F3): cifras de las habilidades nuevas del Mago, tal como las describen los textos. */
class MageRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void plenitudeNeedsEightyPercentMana() {
		assertEquals(0.04, MageRules.plenitudeBonus(1, 80, 100), EPS);
		assertEquals(0.12, MageRules.plenitudeBonus(3, 100, 100), EPS);
		assertEquals(0, MageRules.plenitudeBonus(3, 79.9, 100), EPS);
		assertEquals(0, MageRules.plenitudeBonus(0, 100, 100), EPS);
		assertEquals(0, MageRules.plenitudeBonus(2, 50, 0), EPS, "sin maná máximo no hay bonificación");
	}

	@Test
	void frostChainAndMirrorFollowTheRanks() {
		assertEquals(40, MageRules.frostTicks(1));
		assertEquals(80, MageRules.frostTicks(3));
		assertEquals(3, MageRules.chainTargets(1));
		assertEquals(5, MageRules.chainTargets(3));
		assertEquals(4.0, MageRules.chainDamage(1, 1), EPS);
		assertEquals(6.0, MageRules.chainDamage(3, 1), EPS);
		assertEquals(3.0, MageRules.chainDamage(3, 0.5), EPS, "escala de la configuración");
		assertEquals(1.5, MageRules.mirrorMultiplier(1), EPS);
		assertEquals(2.5, MageRules.mirrorMultiplier(3), EPS);
	}

	@Test
	void hailAndCrystalDamage() {
		assertEquals(3.0, MageRules.hailDamage(1, 1), EPS);
		assertEquals(4.0, MageRules.hailDamage(2, 1), EPS);
		assertEquals(6.0, MageRules.crystalDamage(1, 1), EPS);
		assertEquals(9.0, MageRules.crystalDamage(2, 1), EPS);
	}

	@Test
	void emergencyReserveTriggersOnceBelowTenPercentAndWaitsItsCooldown() {
		assertTrue(MageRules.emergency(9, 100, 500, 0));
		assertFalse(MageRules.emergency(10, 100, 500, 0));
		assertFalse(MageRules.emergency(5, 100, 500, 600), "todavía en espera");
		assertTrue(MageRules.emergency(5, 100, 600, 600));
		assertFalse(MageRules.emergency(0, 0, 500, 0));
	}

	@Test
	void pureFormLastsAndLeavesTwentyPercentMana() {
		assertEquals(160, MageRules.pureTicks(1));
		assertEquals(200, MageRules.pureTicks(2));
		assertEquals(0, MageRules.pureTicks(0));
		assertEquals(20, MageRules.pureEndEnergy(90, 100), EPS);
		assertEquals(5, MageRules.pureEndEnergy(5, 100), EPS, "si ya tenías menos, no se sube");
	}

	@Test
	void novaRefundIsCapped() {
		assertEquals(0, MageRules.novaRefund(0), EPS);
		assertEquals(15, MageRules.novaRefund(3), EPS);
		assertEquals(40, MageRules.novaRefund(20), EPS);
		assertEquals(0, MageRules.novaRefund(-2), EPS);
	}

	@Test
	void concatenationNeedsThreeDifferentSpellsInEightSeconds() {
		ConcatTracker t = new ConcatTracker();
		t.record("fireball", 0);
		t.record("fireball", 10);
		t.record("fireball", 20);
		assertFalse(t.ready(), "repetir la misma no cuenta");
		t.record("blink", 30);
		assertFalse(t.ready());
		t.record("time_warp", 40);
		assertTrue(t.ready());
		t.take();
		assertFalse(t.ready());
	}

	@Test
	void concatenationWindowExpires() {
		ConcatTracker t = new ConcatTracker();
		t.record("a", 0);
		t.record("b", 100);
		t.record("c", 200); // la primera salió de la ventana de 160 ticks
		assertFalse(t.ready());
		t.record("d", 250);
		assertTrue(t.ready());
	}

	@Test
	void crystalMeterGivesOneChargePerHundredManaUpToThree() {
		CrystalMeter m = new CrystalMeter();
		m.add(60);
		assertEquals(0, m.charges());
		m.add(60);
		assertEquals(1, m.charges());
		m.add(1000);
		assertEquals(3, m.charges());
		assertTrue(m.take());
		assertEquals(2, m.charges());
		m.take();
		m.take();
		assertFalse(m.take());
		m.add(-50);
		assertEquals(0, m.charges());
	}
}
