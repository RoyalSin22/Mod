package dev.aptitudes.temple;

import dev.aptitudes.temple.CustodianTempleLayout.Cell;
import dev.aptitudes.temple.CustodianTempleLayout.Result;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static dev.aptitudes.temple.CustodianTempleLayout.*;
import static org.junit.jupiter.api.Assertions.*;

/** V70: el Templo hondo (layout 3 y 4): subterráneo, tres pisos, sin puzle y con sus pasos sellados. */
class DeepTempleLayoutTest {
	private static Result deep() {
		return buildDeep(false);
	}

	private static Result deepCrypt() {
		return buildDeep(true);
	}

	private static String key(int x, int y, int z) {
		return x + "," + y + "," + z;
	}

	private static String key(int[] p) {
		return key(p[0], p[1], p[2]);
	}

	// ------------------------------------------------------------------------------------------ Estructura

	@Test void layoutValuesAndFlow() {
		assertEquals(3, TempleFlowRules.LAYOUT_DEEP);
		assertEquals(4, TempleFlowRules.LAYOUT_DEEP_CRYPT);
		assertTrue(TempleFlowRules.isDeep(3) && TempleFlowRules.isDeep(4));
		assertFalse(TempleFlowRules.isDeep(1) || TempleFlowRules.isDeep(2) || TempleFlowRules.isDeep(0));
		for (int l = 1; l <= 4; l++) assertTrue(TempleFlowRules.isCustodian(l), "layout " + l);
		assertEquals(3, TempleFlowRules.clampLayout(3));
		assertEquals(4, TempleFlowRules.clampLayout(4));
		assertEquals(5, TempleFlowRules.clampLayout(5)); // V89 añade los layouts 5 y 6 sin mover 0–4.
		assertEquals(6, TempleFlowRules.clampLayout(6));
		assertEquals(0, TempleFlowRules.clampLayout(7));
		assertTrue(TempleFlowRules.hasCrypt(4) && TempleFlowRules.hasCrypt(2));
		assertFalse(TempleFlowRules.hasCrypt(3));
	}

	@Test void flowGoesThroughTheTrialAndKeepsTheOldOrdinals() {
		assertEquals(0, TempleFlowRules.Flow.DORMANT.ordinal());
		assertEquals(5, TempleFlowRules.Flow.CLEARED.ordinal());
		assertEquals(6, TempleFlowRules.Flow.TRIAL.ordinal());
		assertEquals(TempleFlowRules.Flow.TRIAL, TempleFlowRules.flowFromOrdinal(6));
		assertEquals(TempleFlowRules.Flow.DORMANT, TempleFlowRules.flowFromOrdinal(7));
		assertEquals(TempleFlowRules.Flow.TRIAL, TempleFlowRules.afterAutomatonDown(TempleFlowRules.Flow.AUTOMATON, 3));
		assertEquals(TempleFlowRules.Flow.TRIAL, TempleFlowRules.afterAutomatonDown(TempleFlowRules.Flow.AUTOMATON, 4));
		assertEquals(TempleFlowRules.Flow.PUZZLE, TempleFlowRules.afterAutomatonDown(TempleFlowRules.Flow.AUTOMATON, 1));
		assertEquals(TempleFlowRules.Flow.DORMANT, TempleFlowRules.afterAutomatonDown(TempleFlowRules.Flow.DORMANT, 3));
		assertEquals(TempleFlowRules.Flow.DOOR_LOCKED, TempleFlowRules.afterTrialCleared(TempleFlowRules.Flow.TRIAL));
		assertEquals(TempleFlowRules.Flow.CUSTODIAN, TempleFlowRules.afterTrialCleared(TempleFlowRules.Flow.CUSTODIAN));
		assertEquals(TempleFlowRules.DoorUse.NOT_READY, TempleFlowRules.useDoor(TempleFlowRules.Flow.TRIAL, true), "la puerta espera a la prueba");
	}

	@Test void everyCellStaysInsideTheSafeRadiusAndTheDepthIsTheDeclaredOne() {
		for (boolean crypt : new boolean[] {false, true}) {
			Result r = buildDeep(crypt);
			assertEquals(crypt ? DEEP_CRYPT_BOTTOM_Y : DEEP_BOTTOM_Y, r.bottomY());
			for (int x = -R - 1; x <= R + 1; x++) for (int y = r.bottomY() - 1; y <= ROOF_Y + 1; y++) for (int z = -R - 1; z <= R + 1; z++) {
				if (Math.abs(x) > R || Math.abs(z) > R || y < r.bottomY() || y > ROOF_Y) assertNull(r.get(x, y, z), key(x, y, z));
			}
		}
		assertTrue(R <= 16);
	}

	@Test void aboveTheSurfaceOnlyTheCraterRampAndRuinsStandOut() {
		Result r = deep();
		for (int x = -R; x <= R; x++) for (int y = 0; y <= ROOF_Y; y++) for (int z = -R; z <= R; z++) {
			Cell c = r.get(x, y, z);
			if (c == null) continue;
			boolean inCrater = x >= -7 && x <= 7 && z >= -2 && z <= 5 || x >= -2 && x <= 2 && z >= 6;
			boolean ruin = c == Cell.BRAZIER || c == Cell.PILLAR;
			assertTrue(inCrater || ruin, "fuera del foso: " + key(x, y, z) + " " + c);
		}
	}

	@Test void everyRoomLiesUnderGround() {
		Result r = deep();
		for (int[] p : new int[][] {r.automatonSpawn, r.custodianSpawn, r.reliquary}) assertTrue(p[1] < -3, key(p));
		for (int[] p : r.trialSpawns) assertTrue(p[1] < -10, key(p));
		for (int[] c : r.chests) assertTrue(c[1] < -17, "los cofres están en el Piso 3");
		// Entre el Piso 1 y la superficie hay al menos tres capas macizas (techo, tierra y césped).
		for (int x = -13; x <= 13; x++) for (int z = -13; z <= -4; z++) {
			assertEquals(Cell.ROOF == r.get(x, -3, z) || r.get(x, -3, z) == Cell.LIGHT_ON, true, key(x, -3, z));
			assertEquals(Cell.SOIL, r.get(x, -2, z));
			assertEquals(Cell.GRASS, r.get(x, -1, z));
		}
	}

	@Test void thereIsNoPuzzleInTheDeepTemple() {
		Result r = deep();
		assertTrue(r.levers.isEmpty());
		assertTrue(r.plates.isEmpty());
		for (int x = -R; x <= R; x++) for (int y = r.bottomY(); y <= ROOF_Y; y++) for (int z = -R; z <= R; z++) {
			Cell c = r.get(x, y, z);
			assertFalse(c == Cell.LEVER || c == Cell.PLATE || c == Cell.LIGHT_OFF || c == Cell.CANDLE_1, key(x, y, z));
		}
	}

	@Test void sealsAndDoorsAreWhereTheControllerExpectsThem() {
		Result r = deep();
		assertEquals(9, r.gateOne.size());
		assertEquals(9, r.hatch.size());
		for (int[] p : r.gateOne) assertEquals(Cell.GATE, r.get(p[0], p[1], p[2]));
		for (int[] p : r.hatch) assertEquals(Cell.GATE, r.get(p[0], p[1], p[2]));
		assertEquals(Cell.CONTROLLER, r.get(r.seal[0], r.seal[1], r.seal[2]));
		assertEquals(9, r.custodianDoor.size());
		for (int[] p : r.custodianDoor) assertEquals(Cell.DOOR, r.get(p[0], p[1], p[2]));
		assertEquals(8, r.chamberDoors.size(), "cuatro puertas de dos bloques");
		assertEquals(4, r.chests.size());
		assertEquals(Cell.RELIQUARY, r.get(r.reliquary[0], r.reliquary[1], r.reliquary[2]));
	}

	@Test void trialRoomCoversTheSpawnsAndTheSpawnsAreStandable() {
		Result r = deep();
		assertTrue(r.trialSpawns.size() >= 6);
		for (int[] p : r.trialSpawns) {
			assertEquals(Cell.AIR, r.get(p[0], p[1], p[2]), "pies " + key(p));
			assertEquals(Cell.AIR, r.get(p[0], p[1] + 1, p[2]), "cabeza " + key(p));
			Cell floor = r.get(p[0], p[1] - 1, p[2]);
			assertTrue(floor == Cell.FLOOR || floor == Cell.ACCENT, "suelo " + floor + " en " + key(p));
			assertTrue(p[0] >= r.trialMin[0] && p[0] <= r.trialMax[0] && p[1] >= r.trialMin[1] && p[1] <= r.trialMax[1]
					&& p[2] >= r.trialMin[2] && p[2] <= r.trialMax[2], "dentro de la sala " + key(p));
		}
	}

	@Test void cryptIsTheFourthLevelAndKeepsItsRulesDeeper() {
		Result r = deepCrypt();
		assertTrue(r.hasCrypt());
		assertFalse(deep().hasCrypt());
		assertEquals(9, r.cryptHatch.size());
		for (int[] c : r.cryptHatch) {
			assertEquals(Cell.CRYPT_HATCH, r.get(c[0], c[1], c[2]));
			assertEquals(UNDER_FLOOR_Y + DEEP_DY, c[1]);
		}
		for (int[] p : r.cryptSpawns) {
			assertEquals(Cell.AIR, r.get(p[0], p[1], p[2]), key(p));
			assertTrue(p[1] >= r.cryptMin[1] && p[1] <= r.cryptMax[1]);
		}
		assertEquals(Cell.AIR, r.get(r.cryptChest[0], r.cryptChest[1], r.cryptChest[2]));
		assertTrue(r.cryptMin[1] > DEEP_CRYPT_BOTTOM_Y);
	}

	// ------------------------------------------------------------------------------------- Alcanzabilidad

	private static boolean passable(Cell c, Set<Cell> open) {
		return c != null && (c == Cell.AIR || c == Cell.LADDER_EAST || c == Cell.LANTERN || c == Cell.BRAZIER
				|| c == Cell.STAIR_NORTH || c == Cell.STAIR_SOUTH || c == Cell.CONTROLLER || open.contains(c));
	}

	/** Alcanzable a pie desde {@code start}: los escalones y los huecos cuentan como transitables (el plano no modela medios bloques). */
	private static Set<String> reach(Result r, int[] start, Set<Cell> open) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(start);
		seen.add(key(start));
		int[][] steps = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
		while (!queue.isEmpty()) {
			int[] p = queue.poll();
			for (int[] s : steps) {
				int x = p[0] + s[0], y = p[1] + s[1], z = p[2] + s[2];
				if (!passable(r.get(x, y, z), open) || !seen.add(key(x, y, z))) continue;
				queue.add(new int[] {x, y, z});
			}
		}
		return seen;
	}

	private static final int[] SURFACE_RAMP_TOP = {0, 0, 15};

	@Test void fromTheSurfaceYouReachTheAtriumButNotTheDeeperFloorsUntilTheSealsFall() {
		Result r = deep();
		Set<String> start = reach(r, SURFACE_RAMP_TOP, Set.of());
		assertTrue(start.contains(key(r.automatonSpawn)), "el atrio es accesible desde la rampa");
		assertFalse(start.contains(key(r.trialSpawns.get(0))), "la sala de pruebas está sellada");
		assertFalse(start.contains(key(r.custodianSpawn)));

		Set<String> afterAutomaton = reach(r, SURFACE_RAMP_TOP, Set.of(Cell.GATE));
		// Con GATE abierto todo cae a la vez: se comprueba por pasos con una copia que solo abre la primera escalera.
		assertTrue(afterAutomaton.contains(key(r.trialSpawns.get(0))));
	}

	@Test void theFirstSealOpensOnlyTheTrialRoom() {
		Result r = deep();
		// Se abre solo la escalera del Piso 1: se rellenan las celdas selladas del Piso 2 con casco (cerradas).
		Result closed = deep();
		for (int[] p : r.hatch) closed.set(p[0], p[1], p[2], Cell.HULL);
		for (int[] p : closed.gateOne) closed.set(p[0], p[1], p[2], Cell.AIR);
		Set<String> seen = reach(closed, SURFACE_RAMP_TOP, Set.of());
		for (int[] p : closed.trialSpawns) assertTrue(seen.contains(key(p)), "alcanza la prueba " + key(p));
		assertFalse(seen.contains(key(closed.custodianSpawn)), "aún no se llega al Custodio");
	}

	@Test void afterTheTrialTheChamberIsReachedButTheInnerDoorStillBlocks() {
		Result r = deep();
		for (int[] p : r.gateOne) r.set(p[0], p[1], p[2], Cell.AIR);
		for (int[] p : r.hatch) r.set(p[0], p[1], p[2], Cell.AIR);
		Set<String> seen = reach(r, SURFACE_RAMP_TOP, Set.of());
		int[] antechamber = {0, UNDER_FLOOR_Y + DEEP_DY + 1, 2};
		assertTrue(seen.contains(key(antechamber)), "se llega a la antesala");
		assertFalse(seen.contains(key(r.custodianSpawn)), "la Puerta del Custodio sigue cerrada");
		Set<String> withDoor = reach(r, SURFACE_RAMP_TOP, Set.of(Cell.DOOR));
		assertTrue(withDoor.contains(key(r.custodianSpawn)), "abierta la puerta, la cámara es accesible");
	}

	@Test void cryptNeedsTheHatchOpen() {
		Result r = deepCrypt();
		for (int[] p : r.gateOne) r.set(p[0], p[1], p[2], Cell.AIR);
		for (int[] p : r.hatch) r.set(p[0], p[1], p[2], Cell.AIR);
		Set<String> closed = reach(r, SURFACE_RAMP_TOP, Set.of(Cell.DOOR));
		for (int[] p : r.cryptSpawns) assertFalse(closed.contains(key(p)), "cerrada " + key(p));
		Set<String> open = reach(r, SURFACE_RAMP_TOP, Set.of(Cell.DOOR, Cell.CRYPT_HATCH));
		for (int[] p : r.cryptSpawns) assertTrue(open.contains(key(p)), "abierta " + key(p));
	}

	@Test void oldLayoutsAreUnchangedByTheRefactor() {
		boolean[] target = {true, false, true};
		Result a = build(target, 0, new int[0]);
		assertEquals(Cell.STAIR_NORTH, a.get(0, -1, -7));
		assertEquals(Cell.DOOR, a.get(0, -6, 5));
		assertEquals(HULL_BOTTOM_Y, a.bottomY());
		assertFalse(a.hasCrypt());
		assertTrue(a.gateOne.isEmpty() && a.trialSpawns.isEmpty());
		assertEquals(-7, a.custodianSpawn[1]);
	}
}
