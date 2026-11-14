package dev.aptitudes.temple;

import dev.aptitudes.temple.CustodianTempleLayout.Cell;
import dev.aptitudes.temple.CustodianTempleLayout.Result;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static dev.aptitudes.temple.CustodianTempleLayout.*;
import static org.junit.jupiter.api.Assertions.*;

class CustodianTempleLayoutTest {
	private static final boolean[] TARGET_3 = {true, false, true};
	private static final boolean[] TARGET_4 = {true, true, false, true};

	private static Result plain() {
		return build(TARGET_3, 0, new int[0]);
	}

	private static Result full() {
		return build(TARGET_4, 4, new int[] {2, 0, 3, 1});
	}

	@Test void everyRingVariantBuildsInsideTheSafeRadius() {
		for (int levers = 3; levers <= 4; levers++) {
			for (int plates : new int[] {0, 3, 4}) {
				boolean[] target = levers == 3 ? TARGET_3 : TARGET_4;
				Result r = build(target, plates, shuffledPlateOrder(plates, 42));
				assertEquals(levers, r.levers.size());
				assertEquals(plates, r.plates.size());
			}
		}
		assertEquals(15, R);
		assertTrue(R <= 16, "un Feature no puede escribir a más de 16 bloques del origen");
	}

	@Test void nothingIsWrittenOutsideTheFootprint() {
		assertFalse(Result.inside(R + 1, 0, 0));
		assertFalse(Result.inside(0, 0, -R - 1));
		assertFalse(Result.inside(0, ROOF_Y + 1, 0));
		assertFalse(Result.inside(0, HULL_BOTTOM_Y - 1, 0));
		assertNull(plain().get(R + 1, 0, 0));
	}

	@Test void undergroundIsAClosedHullWithEveryCellDecided() {
		Result r = plain();
		for (int x = -R; x <= R; x++) for (int z = -R; z <= R; z++) {
			for (int y = HULL_BOTTOM_Y; y <= -1; y++) assertNotNull(r.get(x, y, z), x + "," + y + "," + z);
		}
		// El contorno del subsuelo es todo macizo: ninguna celda de aire toca el borde.
		for (int y = HULL_BOTTOM_Y; y <= -1; y++) {
			for (int i = -R; i <= R; i++) {
				for (int[] edge : new int[][] {{i, -R}, {i, R}, {-R, i}, {R, i}}) {
					assertEquals(Cell.HULL, r.get(edge[0], y, edge[1]), edge[0] + "," + y + "," + edge[1]);
				}
			}
		}
		for (int x = -R; x <= R; x++) for (int z = -R; z <= R; z++) assertEquals(Cell.HULL, r.get(x, HULL_BOTTOM_Y, z));
	}

	@Test void onlyTheEntranceTouchesTheOutsideOnTheSurface() {
		Result r = plain();
		Set<String> openOnBoundary = new HashSet<>();
		for (int y = 1; y <= ROOF_Y - 1; y++) {
			for (int i = -R; i <= R; i++) {
				for (int[] edge : new int[][] {{i, -R}, {i, R}, {-R, i}, {R, i}}) {
					if (r.get(edge[0], y, edge[1]) == Cell.AIR) openOnBoundary.add(edge[0] + "," + y + "," + edge[1]);
				}
			}
		}
		assertEquals(Set.of("-1,1,15", "0,1,15", "1,1,15", "-1,2,15", "0,2,15", "1,2,15", "-1,3,15", "0,3,15", "1,3,15"),
				openOnBoundary);
	}

	@Test void hatchAndDoorAreThreeByThree() {
		Result r = full();
		assertEquals(9, r.hatch.size());
		for (int[] p : r.hatch) assertEquals(Cell.SEALED, r.get(p[0], p[1], p[2]));
		assertEquals(9, r.custodianDoor.size());
		for (int[] p : r.custodianDoor) assertEquals(Cell.DOOR, r.get(p[0], p[1], p[2]));
		assertEquals(Cell.SEAL, r.get(r.seal[0], r.seal[1], r.seal[2]));
	}

	@Test void lightsFollowThePatternAndCandlesMarkThePlateTurn() {
		Result r = full();
		int on = 0;
		for (int i = 0; i < TARGET_4.length; i++) {
			int[] lever = r.levers.get(i);
			assertEquals(Cell.LEVER, r.get(lever[0], lever[1], lever[2]));
			Cell light = r.get(lever[0], 4, -R);
			assertEquals(TARGET_4[i] ? Cell.LIGHT_ON : Cell.LIGHT_OFF, light);
			if (TARGET_4[i]) on++;
		}
		assertEquals(3, on);
		Cell[] candles = {Cell.CANDLE_1, Cell.CANDLE_2, Cell.CANDLE_3, Cell.CANDLE_4};
		for (int i = 0; i < 4; i++) {
			int[] plate = r.plates.get(i);
			assertEquals(Cell.PLATE, r.get(plate[0], plate[1], plate[2]));
			int side = plate[0] < 0 ? -1 : 1;
			assertEquals(candles[i], r.get(plate[0] + side, plate[1], plate[2]));
		}
	}

	@Test void spawnPointsAreOpenAir() {
		Result r = plain();
		assertEquals(Cell.AIR, r.get(r.automatonSpawn[0], r.automatonSpawn[1], r.automatonSpawn[2]));
		assertEquals(Cell.AIR, r.get(r.automatonSpawn[0], r.automatonSpawn[1] + 1, r.automatonSpawn[2]));
		assertEquals(Cell.AIR, r.get(r.custodianSpawn[0], r.custodianSpawn[1], r.custodianSpawn[2]));
		assertEquals(Cell.AIR, r.get(r.custodianSpawn[0], r.custodianSpawn[1] + 1, r.custodianSpawn[2]));
		assertEquals(Cell.RELIQUARY, r.get(r.reliquary[0], r.reliquary[1], r.reliquary[2]));
	}

	@Test void routeIsClosedUntilEachMechanismOpens() {
		Result r = full();
		int[] entrance = {0, 1, R};
		int[] antechamber = {0, -7, 2};
		int[] chamber = r.custodianSpawn;

		Set<String> closed = reach(r, entrance, false, false, false);
		assertTrue(closed.contains(key(0, 1, 9)), "el atrio es accesible");
		assertTrue(closed.contains(key(0, 1, -2)), "la sala del puzle es accesible");
		assertFalse(closed.contains(key(antechamber)), "sin resolver el puzle no se baja");

		Set<String> stairs = reach(r, entrance, true, false, false);
		assertTrue(stairs.contains(key(antechamber)), "con la trampilla abierta se llega a la antesala");
		assertFalse(stairs.contains(key(chamber)), "la cámara sigue cerrada");

		Set<String> open = reach(r, entrance, true, true, false);
		assertTrue(open.contains(key(chamber)), "con la puerta abierta se llega al Custodio");
		assertTrue(open.contains(key(0, -7, 12)), "se llega junto al Relicario");
	}

	@Test void doorAloneIsNotEnoughWithoutTheHatch() {
		Result r = full();
		Set<String> doorOnly = reach(r, new int[] {0, 1, R}, false, true, false);
		assertFalse(doorOnly.contains(key(r.custodianSpawn)));
	}

	@Test void stairHasHeadroomOnceTheHatchIsOpen() {
		Result r = plain();
		for (int i = 0; i < 8; i++) {
			int z = -7 + i;
			for (int x = -1; x <= 1; x++) {
				assertEquals(Cell.STAIR_NORTH, r.get(x, -1 - i, z), "escalón " + i);
				assertTrue(open(r.get(x, -i, z), true, false, false), "pies del escalón " + i);
				assertTrue(open(r.get(x, -i + 1, z), true, false, false), "cabeza del escalón " + i);
			}
		}
	}

	@Test void fourChambersHaveOneDoorAndOneChestEach() {
		Result r = plain();
		assertEquals(8, r.chamberDoors.size(), "cada puerta mide 1×2");
		assertEquals(4, r.chests.size());
		for (int metal = 0; metal < 4; metal++) {
			final int m = metal;
			assertEquals(2, r.chamberDoors.stream().filter(d -> d[3] == m).count(), "puerta del metal " + m);
			assertEquals(1, r.chests.stream().filter(c -> c[3] == m).count(), "cofre del metal " + m);
		}
		for (int[] d : r.chamberDoors) assertEquals(CHAMBER_DOORS[d[3]], r.get(d[0], d[1], d[2]));
		for (int[] c : r.chests) assertEquals(CHESTS[c[3]], r.get(c[0], c[1], c[2]));
	}

	@Test void everyChamberDoorJoinsTheCustodianRoomWithItsChamber() {
		Result r = plain();
		for (int[] d : r.chamberDoors) {
			boolean eastWest = Math.abs(d[0]) == 11;
			int[] a = eastWest ? new int[] {d[0] - 1, d[1], d[2]} : new int[] {d[0], d[1], d[2] + 1};
			int[] b = eastWest ? new int[] {d[0] + 1, d[1], d[2]} : new int[] {d[0], d[1], d[2] - 1};
			Cell first = r.get(a[0], a[1], a[2]);
			Cell second = r.get(b[0], b[1], b[2]);
			assertEquals(Cell.AIR, first, "un lado de la puerta del metal " + d[3]);
			assertEquals(Cell.AIR, second, "el otro lado de la puerta del metal " + d[3]);
		}
	}

	@Test void chambersStayClosedUntilTheirDoorsOpen() {
		Result r = full();
		int[] entrance = {0, 1, R};
		Set<String> doorsClosed = reach(r, entrance, true, true, false);
		Set<String> allOpen = reach(r, entrance, true, true, true);
		for (int[] c : r.chests) {
			// La celda delante del cofre, hacia la puerta, está dentro de la cámara.
			int inside = Math.abs(c[0]) > 11 ? (c[0] < 0 ? c[0] + 1 : c[0] - 1) : c[0];
			int z = Math.abs(c[0]) > 11 ? c[2] : c[2] + 1;
			assertFalse(doorsClosed.contains(key(inside, c[1], z)), "cámara " + c[3] + " accesible sin abrir su puerta");
			assertTrue(allOpen.contains(key(inside, c[1], z)), "cámara " + c[3] + " inaccesible con su puerta abierta");
		}
	}

	@Test void chambersStayInsideTheHullAndLeaveTheOutsideSealed() {
		Result r = plain();
		for (int y = HULL_BOTTOM_Y; y <= -1; y++) {
			for (int i = -R; i <= R; i++) {
				for (int[] edge : new int[][] {{i, -R}, {i, R}, {-R, i}, {R, i}}) {
					assertEquals(Cell.HULL, r.get(edge[0], y, edge[1]), "borde del casco con las cámaras");
				}
			}
		}
	}

	@Test void buildIsDeterministicAndValidatesItsArguments() {
		Result a = full();
		Result b = full();
		for (int x = -R; x <= R; x++) for (int y = HULL_BOTTOM_Y; y <= ROOF_Y; y++) for (int z = -R; z <= R; z++) {
			assertEquals(a.get(x, y, z), b.get(x, y, z));
		}
		assertThrows(IllegalArgumentException.class, () -> build(null, 0, new int[0]));
		assertThrows(IllegalArgumentException.class, () -> build(new boolean[2], 0, new int[0]));
		assertThrows(IllegalArgumentException.class, () -> build(new boolean[5], 0, new int[0]));
		assertThrows(IllegalArgumentException.class, () -> build(TARGET_3, 5, new int[5]));
		assertThrows(IllegalArgumentException.class, () -> build(TARGET_3, 3, new int[1]));
	}

	@Test void plateOrderIsAPermutation() {
		for (long seed = 0; seed < 50; seed++) {
			int[] order = shuffledPlateOrder(4, seed);
			assertEquals(4, order.length);
			Set<Integer> seen = new HashSet<>();
			for (int i : order) {
				assertTrue(i >= 0 && i < PLATE_SPOTS.length);
				seen.add(i);
			}
			assertEquals(4, seen.size());
		}
		assertEquals(0, shuffledPlateOrder(0, 1).length);
		assertEquals(0, shuffledPlateOrder(-3, 1).length);
		assertEquals(3, shuffledPlateOrder(3, 7).length);
	}

	// ------------------------------------------------------------------ BFS

	private static String key(int x, int y, int z) {
		return x + "," + y + "," + z;
	}

	private static String key(int[] p) {
		return key(p[0], p[1], p[2]);
	}

	/** ¿Se puede estar en esa celda? Las trampillas y puertas cuentan solo si están abiertas. */
	private static boolean open(Cell cell, boolean hatchOpen, boolean doorOpen, boolean chambersOpen) {
		if (cell == null) return false;
		return switch (cell) {
			case AIR, PLATE, CANDLE_1, CANDLE_2, CANDLE_3, CANDLE_4, LEVER, STAIR_NORTH -> true;
			case SEALED -> hatchOpen;
			case DOOR -> doorOpen;
			case CHAMBER_DOOR_COPPER, CHAMBER_DOOR_IRON, CHAMBER_DOOR_GOLD, CHAMBER_DOOR_DIAMOND -> chambersOpen;
			default -> false;
		};
	}

	private static Set<String> reach(Result r, int[] start, boolean hatchOpen, boolean doorOpen, boolean chambersOpen) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(start);
		seen.add(key(start));
		int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
		while (!queue.isEmpty()) {
			int[] p = queue.poll();
			for (int[] s : steps) {
				int x = p[0] + s[0], y = p[1] + s[1], z = p[2] + s[2];
				if (!Result.inside(x, y, z) || !open(r.get(x, y, z), hatchOpen, doorOpen, chambersOpen) || !seen.add(key(x, y, z))) continue;
				queue.add(new int[] {x, y, z});
			}
		}
		return seen;
	}
}
