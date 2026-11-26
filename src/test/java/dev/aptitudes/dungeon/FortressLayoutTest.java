package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.FortressLayout.Cell;
import dev.aptitudes.world.StructureLocateRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FortressLayoutTest {
	private static boolean walkable(int x, int z, boolean gateOpen) {
		Cell feet = FortressLayout.cell(x, 1, z), head = FortressLayout.cell(x, 2, z);
		return FortressLayout.cell(x, 0, z) == Cell.FLOOR
				&& (feet == Cell.AIR || gateOpen && feet == Cell.GATE)
				&& (head == Cell.AIR || gateOpen && head == Cell.GATE);
	}

	private static Set<Long> reach(boolean gateOpen) {
		Set<Long> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[] {0, 62});
		seen.add(key(0, 62));
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[1] + d[1];
				if (FortressLayout.inside(x, z) && walkable(x, z, gateOpen) && seen.add(key(x, z))) queue.addLast(new int[] {x, z});
			}
		}
		return seen;
	}

	private static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xffffffffL);
	}

	private static boolean free(int x, int y, int z) {
		Cell c = FortressLayout.cell(x, y, z);
		return c == Cell.AIR || c == Cell.SOUL_LIGHT || c == Cell.GATE || c == Cell.REWARD_GATE;
	}

	private static boolean standable(int x, int y, int z) {
		return free(x, y, z) && free(x, y + 1, z) && FortressLayout.cell(x, y - 1, z) != null
				&& !free(x, y - 1, z);
	}

	private static boolean canClimbToBoss(TowersLayout.Tower tower) {
		int startZ = tower.cz() + (tower.role() == TowersLayout.Role.VASSAL_EAST ? -tower.half() + 1 : tower.half() - 1);
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		Set<String> seen = new HashSet<>();
		queue.add(new int[] {tower.cx(), 1, startZ});
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			if (!seen.add(p[0] + "," + p[1] + "," + p[2])) continue;
			if (p[0] == tower.cx() && p[1] == tower.bossFloorY() + 1 && p[2] == tower.cz()) return true;
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[2] + d[1];
				if (Math.abs(x - tower.cx()) >= tower.half() || Math.abs(z - tower.cz()) >= tower.half()) continue;
				if (standable(x, p[1], z)) queue.add(new int[] {x, p[1], z});
				Cell step = FortressLayout.cell(x, p[1], z);
				if ((step == Cell.STAIR_EAST && d[0] == 1 || step == Cell.STAIR_WEST && d[0] == -1)
						&& standable(x, p[1] + 1, z) && free(p[0], p[1] + 2, p[2])) queue.add(new int[] {x, p[1] + 1, z});
			}
		}
		return false;
	}

	@Test void allRequiredRoomsAndBothWatchtowersAreReachableBeforeTheMarshalGate() {
		Set<Long> open = reach(false);
		assertTrue(open.contains(key(FortressLayout.WEST.cx(), FortressLayout.WEST.cz())));
		assertTrue(open.contains(key(FortressLayout.EAST.cx(), FortressLayout.EAST.cz())));
		assertTrue(open.contains(key(-38, -13)), "monasterio");
		assertTrue(open.contains(key(-39, 17)), "estudio");
		assertTrue(open.contains(key(-34, 42)), "dormitorios");
		assertTrue(open.contains(key(34, 15)), "alquimia y encantamiento");
		assertFalse(open.contains(key(FortressLayout.MARSHAL.cx(), FortressLayout.MARSHAL.cz())));
		assertTrue(reach(true).contains(key(FortressLayout.MARSHAL.cx(), FortressLayout.MARSHAL.cz())));
	}

	@Test void marshalArenaAndTreasuryHaveTheExpectedClearanceAndGates() {
		var m = FortressLayout.MARSHAL;
		assertEquals(39, m.half() * 2 + 1);
		for (int x = m.cx() - 17; x <= m.cx() + 17; x++) {
			for (int z = m.cz() - 17; z <= m.cz() + 17; z++) {
				for (int y = 25; y <= 38; y++) assertEquals(Cell.AIR, FortressLayout.cell(x, y, z), x + "," + y + "," + z);
			}
		}
		assertEquals(Cell.GATE, FortressLayout.cell(28, 2, -17));
		assertEquals(Cell.REWARD_GATE, FortressLayout.cell(28, 26, -55));
		assertEquals(Cell.AIR, FortressLayout.cell(FortressLayout.REWARD[0], FortressLayout.REWARD[1], FortressLayout.REWARD[2]));
		assertEquals(Cell.FLOOR, FortressLayout.cell(FortressLayout.REWARD[0], FortressLayout.REWARD[1] - 1, FortressLayout.REWARD[2]));
		assertTrue(m.roofY() > FortressLayout.WEST.roofY());
		assertEquals(Cell.WOOD, FortressLayout.cell(-43, 12, -30), "puente occidental");
		assertEquals(Cell.WOOD, FortressLayout.cell(43, 12, 31), "puente oriental");
	}

	@Test void allThreeTowersHaveAClimbableRouteToTheirBossRoom() {
		for (var tower : FortressLayout.TOWERS) assertTrue(canClimbToBoss(tower), tower.role().toString());
	}

	@Test void thePlanIsExactlyEightByEightChunksAndHasNoOutsideCells() {
		assertEquals(128, FortressLayout.SIZE);
		assertEquals(-64, FortressLayout.MIN);
		assertEquals(63, FortressLayout.MAX);
		assertNull(FortressLayout.cell(-65, 0, 0));
		assertNull(FortressLayout.cell(64, 0, 0));
		int[] anchor = StructureLocateRules.designatedChunk(1234L, "three_towers", 0, 0, 24);
		for (int cx = anchor[0] - 5; cx <= anchor[0] + 4; cx++) {
			for (int cz = anchor[1] - 5; cz <= anchor[1] + 4; cz++) {
				int[] origin = StructureLocateRules.fortressOrigin(1234L, cx, cz);
				boolean expected = cx >= anchor[0] - 4 && cx <= anchor[0] + 3 && cz >= anchor[1] - 4 && cz <= anchor[1] + 3;
				assertEquals(expected, origin != null, cx + "," + cz);
			}
		}
	}

	@Test void everyChestAndCombatSpawnHasAStableFloor() {
		int chests = 0;
		for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x++) {
			for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z++) {
				Cell c = FortressLayout.cell(x, 1, z);
				if (c == Cell.HIDDEN_CHEST || c == Cell.SUPPLY_CHEST) {
					assertEquals(Cell.FLOOR, FortressLayout.cell(x, 0, z));
					chests++;
				}
			}
		}
		assertTrue(chests >= 6, "cofres repartidos y ocultos");
		for (var spawn : FortressLayout.SPAWNS) {
			assertEquals(Cell.AIR, FortressLayout.cell(spawn.x(), spawn.y(), spawn.z()), spawn.toString());
			assertEquals(Cell.FLOOR, FortressLayout.cell(spawn.x(), spawn.y() - 1, spawn.z()), spawn.toString());
		}
	}

	@Test void constructionBudgetIsBoundedBeforeTerrainFoundations() {
		int columns = 0, writes = 0;
		for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x++) {
			for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z++) {
				if (!FortressLayout.occupied(x, z)) continue;
				columns++;
				for (int y = FortressLayout.BOTTOM_Y; y <= FortressLayout.TOP_Y; y++) {
					if (FortressLayout.cell(x, y, z) != null) writes++;
				}
			}
		}
		assertTrue(columns < 10_000, "columnas ocupadas: " + columns);
		assertTrue(writes < 250_000, "bloques escritos antes del cimiento: " + writes);
		System.out.println("V88 fortaleza: " + columns + " columnas, " + writes + " celdas; 64 chunks");
	}
}
