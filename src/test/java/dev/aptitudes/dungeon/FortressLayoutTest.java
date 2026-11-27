package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.FortressLayout.Cell;
import dev.aptitudes.world.StructureLocateRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FortressLayoutTest {
	private static boolean floor(Cell cell) {
		return cell == Cell.FLOOR || cell == Cell.TRIM || cell == Cell.WOOL_MARKER;
	}

	private static boolean walkable(int x, int z) {
		Cell feet = FortressLayout.cell(x, 1, z), head = FortressLayout.cell(x, 2, z);
		return floor(FortressLayout.cell(x, 0, z))
				&& (feet == Cell.AIR || feet == Cell.MOSS)
				&& head == Cell.AIR;
	}

	private static Set<Long> reach() {
		Set<Long> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[] {0, 62});
		seen.add(key(0, 62));
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[1] + d[1];
				if (FortressLayout.inside(x, z) && walkable(x, z) && seen.add(key(x, z))) queue.addLast(new int[] {x, z});
			}
		}
		return seen;
	}

	private static long key(int x, int z) {
		return ((long) x << 32) ^ (z & 0xffffffffL);
	}

	private static boolean free(int x, int y, int z) {
		Cell c = FortressLayout.cell(x, y, z);
		return c == Cell.AIR || c == Cell.SOUL_LIGHT || c == Cell.MOSS || c == Cell.CHAIN;
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

	@Test void allRequiredRoomsAndAllThreeTowersAreReachableWithoutASeal() {
		Set<Long> open = reach();
		assertTrue(open.contains(key(FortressLayout.WEST.cx(), FortressLayout.WEST.cz())));
		assertTrue(open.contains(key(FortressLayout.EAST.cx(), FortressLayout.EAST.cz())));
		assertTrue(open.contains(key(-38, -13)), "monasterio");
		assertTrue(open.contains(key(-39, 17)), "estudio");
		assertTrue(open.contains(key(-34, 42)), "dormitorios");
		assertTrue(open.contains(key(34, 15)), "alquimia y encantamiento");
		assertTrue(open.contains(key(FortressLayout.MARSHAL.cx(), FortressLayout.MARSHAL.cz())));
		for (int[] chestMarker : new int[][] {{-50, -4}, {-49, 23}, {-48, 49}, {23, 24}})
			assertTrue(open.contains(key(chestMarker[0], chestMarker[1])), "cofre oculto " + chestMarker[0] + "," + chestMarker[1]);
	}

	@Test void marshalArenaAndTreasuryHaveTheExpectedClearanceWithoutGates() {
		var m = FortressLayout.MARSHAL;
		assertEquals(39, m.half() * 2 + 1);
		for (int x = m.cx() - 17; x <= m.cx() + 17; x++) {
			for (int z = m.cz() - 17; z <= m.cz() + 17; z++) {
				for (int y = 25; y <= 38; y++) assertEquals(Cell.AIR, FortressLayout.cell(x, y, z), x + "," + y + "," + z);
			}
		}
		assertEquals(Cell.AIR, FortressLayout.cell(28, 2, -17));
		assertEquals(Cell.AIR, FortressLayout.cell(28, 26, -55));
		assertEquals(Cell.AIR, FortressLayout.cell(FortressLayout.REWARD[0], FortressLayout.REWARD[1], FortressLayout.REWARD[2]));
		assertEquals(Cell.FLOOR, FortressLayout.cell(FortressLayout.REWARD[0], FortressLayout.REWARD[1] - 1, FortressLayout.REWARD[2]));
		assertTrue(m.roofY() > FortressLayout.WEST.roofY());
		assertEquals(Cell.WOOD, FortressLayout.cell(-43, 12, -30), "puente occidental");
		assertEquals(Cell.WOOD, FortressLayout.cell(43, 12, 31), "puente oriental");
	}

	@Test void allThreeTowersHaveAClimbableRouteToTheirBossRoom() {
		for (var tower : FortressLayout.TOWERS) assertTrue(canClimbToBoss(tower), tower.role().toString());
	}

	@Test void treasureIsReachableFromTheColossusFloorWithoutAGate() {
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		Set<Long> seen = new HashSet<>();
		queue.add(new int[] {FortressLayout.COLOSSUS[0], FortressLayout.COLOSSUS[2]});
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			if (!seen.add(key(p[0], p[1]))) continue;
			if (p[0] == FortressLayout.REWARD[0] && p[1] == FortressLayout.REWARD[2]) return;
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[1] + d[1];
				if (FortressLayout.inside(x, z) && standable(x, 25, z) && !seen.contains(key(x, z)))
					queue.addLast(new int[] {x, z});
			}
		}
		fail("El tesoro no es alcanzable a pie desde el Coloso");
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
				if (c == Cell.SUPPLY_CHEST) {
					assertTrue(floor(FortressLayout.cell(x, 0, z)));
					chests++;
				}
				if (FortressLayout.cell(x, -1, z) == Cell.HIDDEN_CHEST) {
					assertEquals(Cell.WOOL_MARKER, FortressLayout.cell(x, 0, z));
					assertEquals(Cell.STONE, FortressLayout.cell(x, -2, z));
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
		int[] perChunk = new int[64];
		for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x++) {
			for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z++) {
				if (!FortressLayout.occupied(x, z)) continue;
				columns++;
				for (int y = FortressLayout.BOTTOM_Y; y <= FortressLayout.TOP_Y; y++) {
					if (FortressLayout.cell(x, y, z) != null) {
						writes++;
						perChunk[((z - FortressLayout.MIN) / 16) * 8 + (x - FortressLayout.MIN) / 16]++;
					}
				}
			}
		}
		assertTrue(columns <= 19_000, "columnas ocupadas: " + columns);
		assertTrue(writes <= 359_626, "bloques escritos antes del cimiento: " + writes);
		for (int count : perChunk) assertTrue(count <= 23_306, "celdas en un chunk: " + count);
		System.out.println("V91 fortaleza: " + columns + " columnas, " + writes + " celdas; 64 chunks");
	}

	@Test void gothicDetailsAndRuinCausesAreVisibleInThePurePlan() {
		assertEquals(Cell.TRIM, FortressLayout.cell(-51, 0, -47), "contrafuerte de vigía");
		assertEquals(Cell.WOOL_MARKER, FortressLayout.cell(-50, 0, -4), "marca del cofre oculto");
		assertEquals(Cell.HIDDEN_CHEST, FortressLayout.cell(-50, -1, -4));
		assertEquals(Cell.BENCH, FortressLayout.cell(-47, 1, -20), "banco de la capilla");
		assertEquals(Cell.CAULDRON, FortressLayout.cell(24, 1, 7), "ala de pociones");
		assertEquals(Cell.RUBBLE, FortressLayout.cell(-58, 0, 10), "brecha y escombro");
		for (Cell c : Cell.values()) assertFalse(c.name().contains("GATE"));
		assertEquals(Cell.ROOF, FortressLayout.cell(-40, 16, -12), "cumbrera inclinada del monasterio");
		assertEquals(Cell.ROOF, FortressLayout.cell(28, 50, -28), "cubierta escalonada del Mariscal");
		assertEquals(Cell.STATUE_HEAD, FortressLayout.cell(-8, 4, 46), "figura sin rostro del patio");
		assertEquals(Cell.CORNICE, FortressLayout.cell(12, 6, -7), "bóveda de la arcada");
		assertEquals(Cell.RUBBLE, FortressLayout.cell(-12, 2, 8), "tramo hundido de la arcada");
	}

	@Test void layoutDoesNotDependOnTheOrderOfChunkQueries() {
		List<int[]> positions = new ArrayList<>();
		List<Cell> expected = new ArrayList<>();
		for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x += 4) {
			for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z += 4) {
				for (int y = FortressLayout.BOTTOM_Y; y <= FortressLayout.TOP_Y; y += 3) {
					positions.add(new int[] {x, y, z});
					expected.add(FortressLayout.cell(x, y, z));
				}
			}
		}
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < positions.size(); i++) order.add(i);
		Collections.shuffle(order, new Random(91));
		for (int i : order) {
			int[] p = positions.get(i);
			assertEquals(expected.get(i), FortressLayout.cell(p[0], p[1], p[2]));
		}
	}
}
