package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.TowersLayout.Cell;
import dev.aptitudes.dungeon.TowersLayout.Result;
import dev.aptitudes.dungeon.TowersLayout.Role;
import dev.aptitudes.dungeon.TowersLayout.Tower;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static dev.aptitudes.dungeon.TowersLayout.*;
import static org.junit.jupiter.api.Assertions.*;

/** V86 · fase 1: «Las Tres Torres Señoriales» es recorrible, cabe en el radio 15 y deja sitio a sus jefes. */
class TowersLayoutTest {
	private static final Result L = build();

	// ------------------------------------------------------------------------------------------ Recorrido (BFS)

	private static boolean passable(Cell c, boolean sealedOpen) {
		return c == null || c == Cell.AIR || c == Cell.LANTERN || (sealedOpen && c == Cell.SEALED);
	}

	private static boolean solid(Cell c, boolean sealedOpen) {
		return c != null && !passable(c, sealedOpen);
	}

	/** De pie: pies y cabeza libres y algo sólido debajo (un peldaño también vale). */
	private static boolean standable(Result r, int x, int y, int z, boolean sealedOpen) {
		return passable(r.get(x, y, z), sealedOpen) && passable(r.get(x, y + 1, z), sealedOpen) && solid(r.get(x, y - 1, z), sealedOpen);
	}

	/** Celdas donde se puede estar de pie, partiendo de la entrada. Subir exige un peldaño que mire en esa dirección. */
	private static Set<String> reach(boolean sealedOpen) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(L.entrance.clone());
		seen.add(key(L.entrance));
		int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		while (!queue.isEmpty()) {
			int[] p = queue.poll();
			for (int[] d : dirs) {
				int nx = p[0] + d[0], nz = p[2] + d[1];
				if (!Result.inside(nx, p[1], nz)) continue;
				int[] next = null;
				if (standable(L, nx, p[1], nz, sealedOpen)) {
					next = new int[] {nx, p[1], nz};
				} else {
					Cell step = L.get(nx, p[1], nz);
					boolean climbs = (step == Cell.STAIR_EAST && d[0] == 1) || (step == Cell.STAIR_WEST && d[0] == -1);
					if (climbs && standable(L, nx, p[1] + 1, nz, sealedOpen) && passable(L.get(p[0], p[1] + 2, p[2]), sealedOpen)) {
						next = new int[] {nx, p[1] + 1, nz};
					}
				}
				if (next != null && seen.add(key(next))) queue.add(next);
			}
			// Bajar: un peldaño bajo el siguiente paso.
			for (int[] d : dirs) {
				int nx = p[0] + d[0], nz = p[2] + d[1];
				if (!Result.inside(nx, p[1] - 1, nz)) continue;
				Cell below = L.get(nx, p[1] - 2, nz);
				boolean descends = (below == Cell.STAIR_EAST && d[0] == -1) || (below == Cell.STAIR_WEST && d[0] == 1);
				if (descends && standable(L, nx, p[1] - 1, nz, sealedOpen) && seen.add(key(nx, p[1] - 1, nz))) {
					queue.add(new int[] {nx, p[1] - 1, nz});
				}
			}
		}
		return seen;
	}

	private static String key(int x, int y, int z) {
		return x + "," + y + "," + z;
	}

	private static String key(int[] p) {
		return key(p[0], p[1], p[2]);
	}

	@Test void bothVassalTowersAreReachableToTheirBossRoomWithTheLordDoorSealed() {
		Set<String> reach = reach(false);
		assertTrue(reach.contains(key(L.guardianWest)), "Guardián oeste");
		assertTrue(reach.contains(key(L.guardianEast)), "Guardián este");
	}

	@Test void theLordTowerIsClosedWhileSealedAndOpenAfterwards() {
		assertFalse(reach(false).contains(key(L.colossus)), "sellada: el Coloso no se alcanza");
		Set<String> open = reach(true);
		assertTrue(open.contains(key(L.colossus)), "abierta: se llega a la sala del Coloso");
		assertTrue(open.contains(key(L.rewardChest)), "y a la cámara de la recompensa");
	}

	@Test void everyCombatSpawnIsWalkable() {
		Set<String> open = reach(true);
		for (var s : L.spawns) {
			assertTrue(open.contains(key(s.x(), s.y(), s.z())), "aparición inalcanzable: " + s);
		}
	}

	// ------------------------------------------------------------------------------------------ Forma

	@Test void nothingIsWrittenOutsideTheSafeRadius() {
		assertEquals(0, L.outOfBounds);
		for (Tower t : TOWERS) {
			int reachXZ = t.half() + 1; // voladizo
			assertTrue(Math.abs(t.cx()) + reachXZ <= R, t.role() + " en x");
			assertTrue(Math.abs(t.cz()) + reachXZ <= R, t.role() + " en z");
		}
		assertEquals(Cell.CONTROLLER, L.get(0, 0, 0));
	}

	@Test void towersDoNotOverlapAndLeaveAGap() {
		for (int i = 0; i < TOWERS.size(); i++) {
			for (int j = i + 1; j < TOWERS.size(); j++) {
				Tower a = TOWERS.get(i), b = TOWERS.get(j);
				int gapX = Math.abs(a.cx() - b.cx()) - a.half() - b.half() - 2; // con voladizos
				int gapZ = Math.abs(a.cz() - b.cz()) - a.half() - b.half() - 2;
				assertTrue(gapX >= 0 || gapZ >= 0, a.role() + " y " + b.role() + " se solapan");
			}
		}
	}

	@Test void theLordTowerIsTheTallestAndEachOneHasItsHilt() {
		assertTrue(LORD.roofY() > WEST.roofY() && LORD.roofY() > EAST.roofY());
		for (Tower t : TOWERS) {
			assertEquals(Cell.POMMEL, L.get(t.cx(), t.roofY() + 8, t.cz()), t.role() + " pomo");
			assertEquals(Cell.GUARD, L.get(t.cx() + 3, t.roofY() + 6, t.cz()), t.role() + " guarda");
			assertEquals(Cell.GRIP, L.get(t.cx(), t.roofY() + 3, t.cz()), t.role() + " empuñadura");
			assertTrue(t.roofY() + 8 <= TOP_Y);
		}
	}

	@Test void parapetsAreCrenellated() {
		for (Tower t : TOWERS) {
			int merlons = 0, gaps = 0;
			int dz = t.half() + 1;
			for (int dx = -dz; dx <= dz; dx++) {
				Cell c = L.get(t.cx() + dx, t.roofY() + 1, t.cz() - dz);
				if (c == Cell.MERLON) merlons++;
				if (c == Cell.AIR) gaps++;
			}
			assertTrue(merlons > 3 && gaps > 3, t.role() + ": almenas alternadas");
		}
	}

	// ------------------------------------------------------------------------------------------ Salas de los jefes

	private static boolean volumeFree(int x0, int x1, int y0, int y1, int z0, int z1) {
		for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) {
			if (!passable(L.get(x, y, z), true)) return false;
		}
		return true;
	}

	@Test void bossRoomsFitTheirBosses() {
		// Guardián (1,2 × 3,0): 7×7 libres y 5 de alto en el centro de su sala.
		for (int[] g : new int[][] {L.guardianWest, L.guardianEast}) {
			assertTrue(volumeFree(g[0] - 3, g[0] + 3, g[1], g[1] + 4, g[2] - 3, g[2] + 3), "sala del Guardián");
		}
		// Coloso (1,6 × 3,8 y ataques de área): 9×9 libres y 8 de alto.
		assertTrue(volumeFree(L.colossus[0] - 4, L.colossus[0] + 4, L.colossus[1], L.colossus[1] + 7, L.colossus[2] - 4, L.colossus[2] + 4), "sala del Coloso");
		// El techo de la sala del Coloso está a 9 de su suelo.
		assertEquals(Cell.ROOF, L.get(LORD.cx(), LORD.roofY(), LORD.cz()));
		assertEquals(9, LORD.roofY() - LORD.bossFloorY() - 1);
	}

	@Test void theRewardChestSpotIsLeftEmptyForTheController() {
		assertTrue(passable(L.get(L.rewardChest[0], L.rewardChest[1], L.rewardChest[2]), true));
		assertEquals(Cell.FLOOR, L.get(L.rewardChest[0], L.rewardChest[1] - 1, L.rewardChest[2]));
	}

	// ------------------------------------------------------------------------------------------ Aperturas

	@Test void theLordDoorIsASealed3x3OnTheSouthFaceAndTheVassalDoorsAreOpen() {
		assertEquals(9, L.sealed.size());
		for (int[] c : L.sealed) {
			assertEquals(LORD.cz() + LORD.half(), c[2]);
			assertEquals(Cell.SEALED, L.get(c[0], c[1], c[2]));
			assertTrue(c[1] >= 1 && c[1] <= 3 && Math.abs(c[0] - LORD.cx()) <= 1);
		}
		for (Tower t : new Tower[] {WEST, EAST}) {
			for (int dx = -1; dx <= 1; dx++) assertEquals(Cell.AIR, L.get(t.cx() + dx, 2, t.cz() + t.half()), t.role() + " puerta abierta");
		}
	}

	@Test void everyLevelHasWindowsAndLanterns() {
		for (Tower t : TOWERS) {
			for (int floor = 0; floor <= t.bossFloorY(); floor += FLOOR_H) {
				assertEquals(Cell.WINDOW, L.get(t.cx() + 3, floor + 3, t.cz() - t.half()), t.role() + " ventana en " + floor);
				assertEquals(Cell.LANTERN, L.get(t.cx() - (t.half() - 1), floor + 4, t.cz()), t.role() + " farol en " + floor);
			}
		}
	}

	@Test void spawnsAreFourPerCombatLevel() {
		assertEquals(4 * (WEST.combatLevels() + EAST.combatLevels() + LORD.combatLevels()), L.spawns.size());
		assertEquals(3, WEST.combatLevels());
		assertEquals(4, LORD.combatLevels());
		for (Role role : Role.values()) assertTrue(L.spawns.stream().anyMatch(s -> s.role() == role));
	}
}
