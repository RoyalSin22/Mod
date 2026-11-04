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
}
