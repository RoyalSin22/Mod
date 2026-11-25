package dev.aptitudes.role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** C2: la modificación de lo que leen {@code PathEffects.all} y {@code Keystones.active} sube la versión. */
class RoleDataVersionTest {
	@Test void everyRelevantSetterBumpsTheVersion() {
		RoleData d = new RoleData();
		int v = d.version();
		d.setPrimary(Role.BERSERKER);
		assertTrue(d.version() > v);
		v = d.version();
		d.setSecondary(Role.MAGE);
		assertTrue(d.version() > v);
		v = d.version();
		d.setTier(Role.BERSERKER, 1);
		assertTrue(d.version() > v);
		// setRank y clearRanks también suben la versión, pero usan RoleTree (registros de Minecraft): no se prueban aquí.
	}

	@Test void readsDoNotChangeTheVersion() {
		RoleData d = new RoleData();
		d.setPrimary(Role.BERSERKER);
		int v = d.version();
		d.rank("x");
		d.tier(Role.BERSERKER);
		d.isActive(Role.BERSERKER);
		d.ranks();
		assertEquals(v, d.version());
	}

	@Test void ranksViewCannotBeUsedToBypassTheVersion() {
		RoleData d = new RoleData();
		assertThrows(UnsupportedOperationException.class, () -> d.ranks().put("x", 1));
	}
}
