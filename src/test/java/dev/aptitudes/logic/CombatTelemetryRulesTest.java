package dev.aptitudes.logic;

import dev.aptitudes.logic.CombatTelemetryRules.Kind;
import dev.aptitudes.logic.CombatTelemetryRules.Sample;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatTelemetryRulesTest {
	@Test
	void damageIsSplitBySource() {
		Sample s = new Sample();
		s.addDamage(Kind.ABILITY, 10, false, 0);
		s.addDamage(Kind.DERIVED, 5, false, 10);
		s.addDamage(Kind.VANILLA, 5, false, 20);
		assertEquals(20, s.totalDamage(), 1e-9);
		assertEquals(0.75, s.abilityShare(), 1e-9);
		assertEquals(5.0 / 15.0, s.derivedShare(), 1e-9);
	}

	@Test
	void activeTimeCountsGapsInsideACombatAndOpeningOnlyAfterAnIdlePause() {
		Sample s = new Sample();
		s.addDamage(Kind.VANILLA, 4, false, 100); // abre: 20 ticks
		s.addDamage(Kind.VANILLA, 4, false, 140); // +40
		assertEquals(60, s.activeTicks);
		s.addDamage(Kind.VANILLA, 4, false, 140 + CombatTelemetryRules.IDLE_TICKS + 1); // pausa larga: abre otro, +20
		assertEquals(80, s.activeTicks);
		assertEquals(12.0 / 4.0, s.dps(), 1e-9);
	}

	@Test
	void invalidDamageIsIgnored() {
		Sample s = new Sample();
		s.addDamage(Kind.ABILITY, 0, false, 0);
		s.addDamage(Kind.ABILITY, -3, false, 0);
		s.addDamage(Kind.ABILITY, Double.NaN, false, 0);
		s.addDamage(Kind.ABILITY, Double.POSITIVE_INFINITY, false, 0);
		assertEquals(0, s.totalDamage(), 0);
		assertEquals(0, s.activeTicks);
		assertEquals(0, s.dps(), 0);
	}

	@Test
	void pvpDamageIsAPartOfTheTotal() {
		Sample s = new Sample();
		s.addDamage(Kind.ABILITY, 8, true, 0);
		s.addDamage(Kind.ABILITY, 2, false, 5);
		assertEquals(10, s.totalDamage(), 1e-9);
		assertEquals(8, s.pvpDamage, 1e-9);
	}

	@Test
	void damagePerEnergyAndCasts() {
		Sample s = new Sample();
		s.addCast(20);
		s.addCast(0);
		s.addDamage(Kind.ABILITY, 30, false, 0);
		assertEquals(2, s.casts);
		assertEquals(1.5, s.damagePerEnergy(), 1e-9);
		assertEquals(0, new Sample().damagePerEnergy(), 0);
	}

	@Test
	void timeToKillAndHpPerSecond() {
		Sample s = new Sample();
		s.addDamage(Kind.VANILLA, 20, false, 0); // 20 ticks de combate
		s.addKill(20, 100);
		s.addKill(40, 200);
		assertEquals(2, s.kills);
		assertEquals(7.5, s.averageTimeToKillSeconds(), 1e-9);
		assertEquals(60.0, s.hpKilledPerSecond(), 1e-9);
		s.addKill(10, -1); // sin tiempo medido: cuenta la baja pero no el tiempo
		assertEquals(3, s.kills);
		assertEquals(7.5, s.averageTimeToKillSeconds(), 1e-9);
	}

	@Test
	void autonomousDamageIsCountedSeparately() {
		Sample s = new Sample();
		s.addDamage(Kind.VANILLA, 10, false, 0);
		s.addDamage(Kind.SUMMON, 30, false, 10);
		assertEquals(40, s.totalDamage(), 1e-9);
		assertEquals(0.75, s.summonShare(), 1e-9);
		assertEquals(0, s.abilityShare(), 1e-9); // el daño autónomo no cuenta como habilidad
		Sample other = new Sample();
		other.addDamage(Kind.SUMMON, 10, false, 0);
		s.merge(other);
		assertEquals(40, s.summonDamage, 1e-9);
	}

	@Test
	void mergeAddsEverything() {
		Sample a = new Sample();
		a.addDamage(Kind.ABILITY, 10, false, 0);
		a.addCast(5);
		Sample b = new Sample();
		b.addDamage(Kind.VANILLA, 6, true, 0);
		b.addKill(8, 40);
		a.merge(b);
		assertEquals(16, a.totalDamage(), 1e-9);
		assertEquals(6, a.pvpDamage, 1e-9);
		assertEquals(1, a.kills);
		assertEquals(40, a.activeTicks);
	}
}
