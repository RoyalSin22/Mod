package dev.aptitudes.temple;

import dev.aptitudes.temple.CustodianTempleLayout.Cell;
import dev.aptitudes.temple.CustodianTempleLayout.Result;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static dev.aptitudes.temple.CustodianTempleLayout.*;
import static org.junit.jupiter.api.Assertions.*;

/** V69: el plano de la Cripta de la Niebla (layout 2) y que el de siempre (layout 1) no cambia. */
class CryptLayoutTest {
	private static final boolean[] TARGET = {true, false, true, true};
	private static final int[] ORDER = {2, 0, 3, 1};

	private static Result without() {
		return build(TARGET, 4, ORDER, false);
	}

	private static Result with() {
		return build(TARGET, 4, ORDER, true);
	}

	@Test void flagsAndLayoutValues() {
		assertFalse(without().hasCrypt());
		assertTrue(with().hasCrypt());
		assertEquals(2, TempleFlowRules.LAYOUT_CRYPT);
		assertEquals(TempleFlowRules.LAYOUT_CRYPT, TempleFlowRules.clampLayout(2));
		assertEquals(TempleFlowRules.LAYOUT_OLD, TempleFlowRules.clampLayout(3));
		assertTrue(TempleFlowRules.isCustodian(1));
		assertTrue(TempleFlowRules.isCustodian(2));
		assertFalse(TempleFlowRules.isCustodian(0));
		assertTrue(TempleFlowRules.hasCrypt(2));
		assertFalse(TempleFlowRules.hasCrypt(1));
	}

	@Test void layoutOneIsIdenticalWithAndWithoutTheCryptFlagOffAndTheOldOverload() {
		Result a = build(TARGET, 4, ORDER);
		Result b = without();
		for (int x = -R; x <= R; x++) for (int y = HULL_BOTTOM_Y; y <= ROOF_Y; y++) for (int z = -R; z <= R; z++) {
			assertEquals(a.get(x, y, z), b.get(x, y, z), x + "," + y + "," + z);
		}
		assertNull(a.get(0, CRYPT_BOTTOM_Y, 10), "el plano sin cripta no llega al fondo de la cripta");
		assertTrue(a.cryptHatch.isEmpty());
		assertTrue(a.cryptSpawns.isEmpty());
	}

	@Test void cryptOnlyChangesTheHatchAndTheShaftAboveY10() {
		Result a = without(), b = with();
		Set<String> changed = new HashSet<>();
		for (int x = -R; x <= R; x++) for (int y = HULL_BOTTOM_Y; y <= ROOF_Y; y++) for (int z = -R; z <= R; z++) {
			if (a.get(x, y, z) != b.get(x, y, z)) changed.add(x + "," + y + "," + z);
		}
		Set<String> expected = new HashSet<>();
		for (int x = -1; x <= 1; x++) for (int z = 10; z <= 12; z++) {
			expected.add(x + ",-8," + z);   // trampilla
			expected.add(x + ",-9," + z);   // primer tramo del pozo
		}
		assertEquals(expected, changed);
	}

	@Test void hatchIsAThreeByThreeInTheChamberFloorAndDoesNotTouchTheReliquaryOrTheCustodianSpawn() {
		Result r = with();
		assertEquals(9, r.cryptHatch.size());
		for (int[] c : r.cryptHatch) {
			assertEquals(Cell.CRYPT_HATCH, r.get(c[0], c[1], c[2]));
			assertEquals(UNDER_FLOOR_Y, c[1]);
			assertFalse(c[0] == r.reliquary[0] && c[2] == r.reliquary[2], "no pisa el Relicario");
			assertFalse(c[0] == r.custodianSpawn[0] && c[2] == r.custodianSpawn[2], "no pisa al Custodio");
		}
	}

	@Test void everyCryptCellStaysInsideTheSafeRadius() {
		Result r = with();
		assertTrue(R <= 16);
		for (int x = -R - 1; x <= R + 1; x++) for (int y = CRYPT_BOTTOM_Y - 1; y <= ROOF_Y + 1; y++) for (int z = -R - 1; z <= R + 1; z++) {
			if (Math.abs(x) > R || Math.abs(z) > R || y < CRYPT_BOTTOM_Y || y > ROOF_Y) assertNull(r.get(x, y, z), x + "," + y + "," + z);
		}
		assertNotNull(r.get(0, CRYPT_BOTTOM_Y, 10));
	}

	@Test void theHallIsWrappedByTheHullOnEverySide() {
		Result r = with();
		int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
		for (int x = -R; x <= R; x++) for (int y = CRYPT_BOTTOM_Y; y <= -9; y++) for (int z = -R; z <= R; z++) {
			Cell c = r.get(x, y, z);
			if (c == Cell.AIR || c == Cell.LADDER_EAST || c == Cell.LANTERN) {
				for (int[] d : dirs) {
					assertNotNull(r.get(x + d[0], y + d[1], z + d[2]), "hueco abierto al exterior en " + x + "," + y + "," + z);
				}
			}
		}
	}

	@Test void spawnPointsClimaxAndChestAreStandableInsideTheRoom() {
		Result r = with();
		assertFalse(r.cryptSpawns.isEmpty());
		Set<int[]> points = new HashSet<>(r.cryptSpawns);
		points.add(r.cryptClimax);
		points.add(r.cryptChest);
		for (int[] p : points) {
			assertEquals(Cell.AIR, r.get(p[0], p[1], p[2]), "pies " + p[0] + "," + p[1] + "," + p[2]);
			assertEquals(Cell.AIR, r.get(p[0], p[1] + 1, p[2]), "cabeza");
			Cell floor = r.get(p[0], p[1] - 1, p[2]);
			assertTrue(floor == Cell.FLOOR || floor == Cell.ACCENT, "suelo " + floor);
			assertTrue(p[0] >= r.cryptMin[0] && p[0] <= r.cryptMax[0] && p[1] >= r.cryptMin[1] && p[1] <= r.cryptMax[1]
					&& p[2] >= r.cryptMin[2] && p[2] <= r.cryptMax[2], "dentro de la sala");
		}
		for (int[] p : r.cryptSpawns) {
			assertFalse(p[0] >= -1 && p[0] <= 1 && p[2] >= 10 && p[2] <= 12, "no aparecen bajo el pozo");
		}
	}

	@Test void theRoomBoxCoversTheWholeHallAndNothingElse() {
		Result r = with();
		for (int x = -R; x <= R; x++) for (int y = CRYPT_BOTTOM_Y; y <= -9; y++) for (int z = -R; z <= R; z++) {
			boolean inBox = x >= r.cryptMin[0] && x <= r.cryptMax[0] && y >= r.cryptMin[1] && y <= r.cryptMax[1]
					&& z >= r.cryptMin[2] && z <= r.cryptMax[2];
			if (inBox) assertTrue(r.get(x, y, z) != Cell.HULL, "la sala no incluye casco: " + x + "," + y + "," + z);
		}
	}

	// ----------------------------------------------------------------------------- Alcanzabilidad

	private static boolean passable(Cell c, boolean hatchOpen) {
		if (c == null) return false;
		return c == Cell.AIR || c == Cell.LADDER_EAST || (c == Cell.CRYPT_HATCH && hatchOpen);
	}

	private static Set<String> reach(Result r, int[] start, boolean hatchOpen) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(start);
		seen.add(start[0] + "," + start[1] + "," + start[2]);
		int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
		while (!queue.isEmpty()) {
			int[] p = queue.poll();
			for (int[] s : steps) {
				int x = p[0] + s[0], y = p[1] + s[1], z = p[2] + s[2];
				if (!passable(r.get(x, y, z), hatchOpen) || !seen.add(x + "," + y + "," + z)) continue;
				queue.add(new int[] {x, y, z});
			}
		}
		return seen;
	}

	private static String key(int[] p) {
		return p[0] + "," + p[1] + "," + p[2];
	}

	@Test void theHallIsOnlyReachableFromTheCustodianChamberThroughTheOpenHatch() {
		Result r = with();
		Set<String> closed = reach(r, r.custodianSpawn, false);
		Set<String> open = reach(r, r.custodianSpawn, true);
		for (int[] p : r.cryptSpawns) {
			assertFalse(closed.contains(key(p)), "cerrada: no se debe poder bajar a " + key(p));
			assertTrue(open.contains(key(p)), "abierta: se debe poder bajar a " + key(p));
		}
		assertFalse(closed.contains(key(r.cryptChest)));
		assertTrue(open.contains(key(r.cryptChest)));
		assertTrue(open.contains(key(r.cryptClimax)));
	}

	@Test void theLadderRunsFromTheHatchDownToTheHallFloor() {
		Result r = with();
		for (int y = CRYPT_FLOOR_Y + 1; y <= HULL_BOTTOM_Y; y++) {
			assertEquals(Cell.LADDER_EAST, r.get(-1, y, 11), "escalera en y=" + y);
		}
		for (int y = CRYPT_FLOOR_Y + 1; y <= -11; y++) {
			assertEquals(Cell.WALL, r.get(-2, y, 11), "pilar tras la escalera en y=" + y);
		}
		assertTrue(r.get(-2, -10, 11) == Cell.HULL && r.get(-2, -9, 11) == Cell.HULL, "tras los dos tramos del pozo hay casco");
	}
}
