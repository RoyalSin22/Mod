package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.TowersFlowRules.Boss;
import dev.aptitudes.dungeon.TowersLayout.Role;
import org.junit.jupiter.api.Test;

import static dev.aptitudes.dungeon.TowersFlowRules.*;
import static dev.aptitudes.dungeon.TowersLayout.*;
import static org.junit.jupiter.api.Assertions.*;

class TowersFlowRulesTest {
	@Test void difficultyIsOneRingAboveThePlaceWithAFloorOfThreeAndACapOfFive() {
		assertEquals(3, effectiveRing(0));
		assertEquals(3, effectiveRing(2));
		assertEquals(4, effectiveRing(3));
		assertEquals(5, effectiveRing(4));
		assertEquals(5, effectiveRing(5));
		assertEquals(5, effectiveRing(99));
		assertEquals(3, effectiveRing(-7), "un anillo negativo no baja del mínimo");
		assertEquals(0.75, bossHealthBonus(3), 1e-9);
		assertEquals(1.25, bossHealthBonus(5), 1e-9);
		assertEquals(1.25, bossHealthBonus(50), 1e-9, "acotado");
		assertTrue(minionHealthBonus(4) < bossHealthBonus(4));
	}

	@Test void bossesWakeOnlyOnceAndOnlyWithSomeoneInTheRoom() {
		assertTrue(shouldWake(Boss.ASLEEP, true));
		assertFalse(shouldWake(Boss.ASLEEP, false));
		assertFalse(shouldWake(Boss.AWAKE, true));
		assertFalse(shouldWake(Boss.DEFEATED, true), "una sola vez por estructura");
	}

	@Test void theLordDoorNeedsBothGuardiansDown() {
		assertFalse(lordDoorOpen(Boss.AWAKE, Boss.DEFEATED));
		assertFalse(lordDoorOpen(Boss.DEFEATED, Boss.ASLEEP));
		assertTrue(lordDoorOpen(Boss.DEFEATED, Boss.DEFEATED));
	}

	@Test void theColossusNeedsTheOpenDoorAndSomeoneInside() {
		assertFalse(shouldWakeColossus(false, Boss.ASLEEP, true));
		assertFalse(shouldWakeColossus(true, Boss.ASLEEP, false));
		assertTrue(shouldWakeColossus(true, Boss.ASLEEP, true));
		assertFalse(shouldWakeColossus(true, Boss.DEFEATED, true));
	}

	@Test void aStateNeverGoesBackwards() {
		assertEquals(Boss.DEFEATED, advance(Boss.DEFEATED, Boss.AWAKE));
		assertEquals(Boss.DEFEATED, advance(Boss.AWAKE, Boss.DEFEATED));
		assertEquals(Boss.AWAKE, advance(Boss.ASLEEP, Boss.AWAKE));
		assertEquals(Boss.AWAKE, advance(Boss.AWAKE, Boss.ASLEEP));
	}

	@Test void bossStatesKeepTheirOrdinalsForSavedWorlds() {
		assertEquals(0, Boss.ASLEEP.ordinal());
		assertEquals(1, Boss.AWAKE.ordinal());
		assertEquals(2, Boss.DEFEATED.ordinal());
	}

	@Test void roomsMatchTheLayout() {
		// El centro del patio no está en ninguna torre; el centro de la sala del Coloso, sí.
		assertEquals(-1, combatLevel(LORD, 0, 1, 0));
		assertTrue(inBossRoom(LORD, LORD.cx(), LORD.bossFloorY() + 1, LORD.cz()));
		assertTrue(inBossRoom(WEST, WEST.cx(), WEST.bossFloorY() + 3, WEST.cz()));
		assertFalse(inBossRoom(WEST, WEST.cx(), WEST.bossFloorY() - 1, WEST.cz()), "la planta de debajo no cuenta");
		assertFalse(inBossRoom(WEST, WEST.cx() + WEST.half(), WEST.bossFloorY() + 1, WEST.cz()), "el muro no es sala");
		for (int level = 0; level < LORD.combatLevels(); level++) {
			assertEquals(level, combatLevel(LORD, LORD.cx(), level * FLOOR_H + 1, LORD.cz()));
			assertEquals(level, combatLevel(LORD, LORD.cx(), level * FLOOR_H + 5, LORD.cz()));
		}
		assertEquals(-1, combatLevel(LORD, LORD.cx(), LORD.bossFloorY() + 1, LORD.cz()), "la sala del jefe no es de combate");
	}

	@Test void everyLayoutSpawnLiesInsideItsOwnCombatLevel() {
		for (var s : build().spawns) {
			Tower t = TOWERS.stream().filter(x -> x.role() == s.role()).findFirst().orElseThrow();
			assertEquals(s.level(), combatLevel(t, s.x(), s.y(), s.z()), s.toString());
		}
	}

	@Test void waveBitsAreDistinctAndTracked() {
		int mask = 0;
		java.util.Set<Integer> bits = new java.util.HashSet<>();
		for (Tower t : TOWERS) {
			for (int level = 0; level < t.combatLevels(); level++) {
				assertTrue(bits.add(waveBit(t.role(), level)), "bit repetido");
				assertFalse(waveDone(mask, t.role(), level));
				mask = withWave(mask, t.role(), level);
				assertTrue(waveDone(mask, t.role(), level));
			}
		}
		assertEquals(10, bits.size());
		assertTrue(bits.stream().allMatch(b -> b >= 0 && b < 31), "cabe en un entero");
	}

	@Test void wavesGrowWithHeightAndRingAndMixMeleeWithArchers() {
		assertEquals(1, mobsPerSpawn(0, 3));
		assertEquals(2, mobsPerSpawn(2, 3));
		assertEquals(3, mobsPerSpawn(3, 5));
		boolean melee = false, ranged = false;
		for (int level = 0; level < 4; level++) {
			for (int i = 0; i < 4; i++) {
				var k = mobKind(level, i);
				if (k == TowersFlowRules.MobKind.ZOMBIE || k == TowersFlowRules.MobKind.HUSK) melee = true; else ranged = true;
			}
		}
		assertTrue(melee && ranged);
		assertEquals(TowersFlowRules.MobKind.ZOMBIE, mobKind(0, 0));
		assertEquals(TowersFlowRules.MobKind.HUSK, mobKind(1, 1));
	}
}
