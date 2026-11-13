package dev.aptitudes.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigMigrationTest {
	@Test public void oldDefaultRoleLevelIsRaisedOnce() {
		assertEquals(50, ConfigMigration.maxRoleLevel(0, 30));
		assertEquals(50, ConfigMigration.maxRoleLevel(45, 30));
		// Ya migrado: si el administrador vuelve a poner 30 a mano, se respeta.
		assertEquals(30, ConfigMigration.maxRoleLevel(ConfigMigration.CURRENT, 30));
	}

	@Test public void customValuesAreKept() {
		assertEquals(40, ConfigMigration.maxRoleLevel(0, 40));
		assertEquals(100, ConfigMigration.maxRoleLevel(0, 100));
		assertEquals(50, ConfigMigration.maxRoleLevel(0, 50));
	}

	@Test public void oldEvolutionBonusDefaultsAreRaisedOnce() {
		assertEquals(15, ConfigMigration.masterBonusPoints(0, 10));
		assertEquals(15, ConfigMigration.masterBonusPoints(46, 10));
		assertEquals(10, ConfigMigration.ultimateBonusPoints(46, 5));
		// Ya migrado: un 10/5 puesto a mano después se respeta.
		assertEquals(10, ConfigMigration.masterBonusPoints(ConfigMigration.CURRENT, 10));
		assertEquals(5, ConfigMigration.ultimateBonusPoints(ConfigMigration.CURRENT, 5));
	}

	@Test public void customEvolutionBonusesAreKept() {
		assertEquals(20, ConfigMigration.masterBonusPoints(46, 20));
		assertEquals(0, ConfigMigration.ultimateBonusPoints(46, 0));
		assertEquals(8, ConfigMigration.ultimateBonusPoints(0, 8));
	}
}
