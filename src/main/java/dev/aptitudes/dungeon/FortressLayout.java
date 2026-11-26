package dev.aptitudes.dungeon;

import java.util.List;

/**
 * V88: plano puro de la fortaleza de las Tres Torres. Cada consulta depende solo de coordenadas relativas;
 * un chunk puede construir sus columnas sin consultar ni escribir en los vecinos.
 */
public final class FortressLayout {
	public static final int MIN = -64, MAX = 63, SIZE = 128;
	public static final int BOTTOM_Y = -3, TOP_Y = 62;
	public static final int MAX_SLOPE = 12, MAX_FOUNDATION = 12;
	public static final TowersLayout.Tower WEST = new TowersLayout.Tower(TowersLayout.Role.VASSAL_WEST, -43, -40, 7, 18, 27);
	public static final TowersLayout.Tower EAST = new TowersLayout.Tower(TowersLayout.Role.VASSAL_EAST, 43, 41, 7, 18, 27);
	public static final TowersLayout.Tower MARSHAL = new TowersLayout.Tower(TowersLayout.Role.LORD, 28, -36, 19, 24, 40);
	public static final List<TowersLayout.Tower> TOWERS = List.of(WEST, EAST, MARSHAL);
	public static final int[] CONTROLLER = {0, 0, 0};
	public static final int[] ENTRANCE = {0, 1, 62};
	public static final int[] WEST_BOSS = {WEST.cx(), 19, WEST.cz()};
	public static final int[] EAST_BOSS = {EAST.cx(), 19, EAST.cz()};
	public static final int[] COLOSSUS = {MARSHAL.cx(), 25, MARSHAL.cz()};
	public static final int[] REWARD = {28, 25, -58};
	public static final int[] MONASTERY_ELITE = {-39, 1, -13};

	public enum Cell {
		AIR, STONE, FLOOR, WOOD, ROOF, RUBBLE, MERLON, WINDOW, SOUL_LIGHT,
		STAIR_EAST, STAIR_WEST, GATE, REWARD_GATE, CONTROLLER, BOOKSHELF,
		BED_FOOT, BED_HEAD, BREWING, ENCHANTING, ALTAR, HIDDEN_CHEST, SUPPLY_CHEST
	}

	public record Spawn(TowersLayout.Role role, int level, int x, int y, int z) {}

	public static final List<Spawn> SPAWNS = List.of(
		new Spawn(TowersLayout.Role.VASSAL_WEST, 0, -45, 1, -40),
		new Spawn(TowersLayout.Role.VASSAL_WEST, 1, -45, 7, -40),
		new Spawn(TowersLayout.Role.VASSAL_WEST, 2, -45, 13, -40),
		new Spawn(TowersLayout.Role.VASSAL_EAST, 0, 41, 1, 41),
		new Spawn(TowersLayout.Role.VASSAL_EAST, 1, 41, 7, 41),
		new Spawn(TowersLayout.Role.VASSAL_EAST, 2, 41, 13, 41),
		new Spawn(TowersLayout.Role.LORD, 0, 24, 1, -35),
		new Spawn(TowersLayout.Role.LORD, 1, 24, 7, -35),
		new Spawn(TowersLayout.Role.LORD, 2, 24, 13, -35),
		new Spawn(TowersLayout.Role.LORD, 3, 24, 19, -35));

	private FortressLayout() {}

	public static boolean inside(int x, int z) {
		return x >= MIN && x <= MAX && z >= MIN && z <= MAX;
	}

	public static TowersLayout.Tower towerAt(int x, int z) {
		for (var tower : TOWERS) {
			if (Math.abs(x - tower.cx()) <= tower.half() && Math.abs(z - tower.cz()) <= tower.half()) return tower;
		}
		return null;
	}

	/** Solo estas columnas sustituyen el terreno; los jardines y patios rotos conservan vegetación natural. */
	public static boolean occupied(int x, int z) {
		return cell(x, 0, z) != null;
	}

	/** Devuelve null si el bloque del mundo debe permanecer intacto. */
	public static Cell cell(int x, int y, int z) {
		if (!inside(x, z) || y < BOTTOM_Y || y > TOP_Y) return null;
		if (x == 0 && y == 0 && z == 0) return Cell.CONTROLLER;
		TowersLayout.Tower tower = towerAt(x, z);
		if (tower != null) return tower(tower, x, y, z);
		if (x >= 20 && x <= 36 && z >= -61 && z <= -55) return treasury(x, y, z);
		if (rect(x, z, -53, -25, -25, -1)) return room(x, y, z, -53, -25, -25, -1, -25, -13, 0);
		if (rect(x, z, -52, -27, 7, 26)) return room(x, y, z, -52, -27, 7, 26, -27, 17, 1);
		if (rect(x, z, -51, -17, 32, 52)) return room(x, y, z, -51, -17, 32, 52, -28, 32, 2);
		if (rect(x, z, 20, 51, 3, 27)) return room(x, y, z, 20, 51, 3, 27, 20, 15, 3);
		if (rect(x, z, -10, 10, 51, 63)) return barbican(x, y, z);
		if (bridge(x, z)) {
			if (y == 12) return Cell.WOOD;
			if (y >= 13 && y <= 15) return Cell.AIR;
		}
		if (Math.abs(x) >= 58 && Math.abs(x) <= 61 && Math.abs(z) <= 61
				|| Math.abs(z) >= 58 && Math.abs(z) <= 61 && Math.abs(x) <= 61) return curtain(x, y, z);
		if (path(x, z)) {
			if (y < 0) return Cell.STONE;
			if (y == 0) return Cell.FLOOR;
			return y <= 6 ? Cell.AIR : null;
		}
		return null;
	}

	private static boolean rect(int x, int z, int x0, int x1, int z0, int z1) {
		return x >= x0 && x <= x1 && z >= z0 && z <= z1;
	}

	private static boolean bridge(int x, int z) {
		return rect(x, z, -45, -41, -32, -26) || rect(x, z, 41, 45, 28, 33);
	}

	private static boolean path(int x, int z) {
		return rect(x, z, -14, 14, -14, 50)
				|| rect(x, z, -43, -13, -33, -27) // vigía occidental
				|| rect(x, z, -23, -19, -33, -11) // enlace entre vigía y patio
				|| rect(x, z, -29, -13, -15, -11) // monasterio
				|| rect(x, z, -31, -13, 15, 19) // estudio
				|| rect(x, z, -26, -22, 18, 32) // enlace estudio y dormitorios
				|| rect(x, z, -28, -22, 28, 34) // dormitorios
				|| rect(x, z, 12, 22, 13, 17) // alquimia
				|| rect(x, z, 12, 43, 28, 34) // vigía oriental
				|| rect(x, z, 12, 32, -14, -10) // enlace del Mariscal
				|| rect(x, z, 24, 32, -18, -12); // puerta del Mariscal
	}

	private static Cell tower(TowersLayout.Tower t, int x, int y, int z) {
		int dx = x - t.cx(), dz = z - t.cz(), h = t.half();
		if (y < 0) return Cell.STONE;
		if (y == 0) return Cell.FLOOR;
		if (y == t.roofY()) return Cell.ROOF;
		if (y > t.roofY()) {
			int high = t.role() == TowersLayout.Role.LORD ? 60 : 39;
			int narrow = t.role() == TowersLayout.Role.LORD ? 6 : 2;
			if (y <= high && Math.max(Math.abs(dx), Math.abs(dz)) == narrow) return Cell.STONE;
			if (y == high + 1 && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) return Cell.ROOF;
			return null;
		}
		boolean wall = Math.abs(dx) == h || Math.abs(dz) == h;
		if (wall) {
			if (t.role() == TowersLayout.Role.LORD && dz == -h && Math.abs(dx) <= 1 && y >= 25 && y <= 27) return Cell.REWARD_GATE;
			if (t.role() == TowersLayout.Role.VASSAL_WEST && dz == h && Math.abs(dx) <= 1 && y >= 13 && y <= 15) return Cell.AIR;
			if (t.role() == TowersLayout.Role.VASSAL_EAST && dz == -h && Math.abs(dx) <= 1 && y >= 13 && y <= 15) return Cell.AIR;
			int entryZ = t.role() == TowersLayout.Role.VASSAL_EAST ? -h : h;
			if (dz == entryZ && Math.abs(dx) <= 1 && y <= 3) return t.role() == TowersLayout.Role.LORD ? Cell.GATE : Cell.AIR;
			if (y >= 3 && y <= t.bossFloorY() + 5 && y % 6 >= 3 && Math.abs(dx + dz) % 7 == 0) return Cell.WINDOW;
			return Cell.STONE;
		}
		if (y == t.bossFloorY() && x == t.cx() && z == t.cz()) return Cell.FLOOR;
		if (y <= t.bossFloorY() && y % TowersLayout.FLOOR_H == 0) {
			if (y > 0) {
				Cell landing = staircase(t, x, y, z, y - TowersLayout.FLOOR_H, false);
				if (landing != null) return landing;
			}
			// Hueco sobre el tramo alto de cada escalera.
			if (y > 0 && staircase(t, x, y, z, y - TowersLayout.FLOOR_H, true) == Cell.AIR) return Cell.AIR;
			return Cell.FLOOR;
		}
		for (int floor = 0; floor < t.bossFloorY(); floor += TowersLayout.FLOOR_H) {
			Cell step = staircase(t, x, y, z, floor, false);
			if (step != null) return step;
		}
		if (y == t.bossFloorY() + 1 && Math.abs(dx) == h - 1 && dz == 0) return Cell.SOUL_LIGHT;
		if (y == 1 && t.role() != TowersLayout.Role.LORD && dx == -h + 2 && dz == 0) return Cell.SUPPLY_CHEST;
		return Cell.AIR;
	}

	private static Cell staircase(TowersLayout.Tower t, int x, int y, int z, int floor, boolean headroom) {
		int level = floor / TowersLayout.FLOOR_H;
		boolean east = level % 2 == 0;
		int sz = t.cz() + (east ? -t.half() + 1 : t.half() - 1);
		if (z != sz) return null;
		int start = t.cx() + (east ? -t.half() + 1 : t.half() - 1);
		for (int i = 1; i <= TowersLayout.FLOOR_H; i++) {
			int sx = start + (east ? i : -i);
			if (x != sx) continue;
			if (headroom && y == floor + TowersLayout.FLOOR_H && i >= 3 && i <= 5) return Cell.AIR;
			if (!headroom && y == floor + i) return east ? Cell.STAIR_EAST : Cell.STAIR_WEST;
		}
		return null;
	}

	private static Cell treasury(int x, int y, int z) {
		if (y < 24) return Cell.STONE;
		if (y == 24) return Cell.FLOOR;
		if (y == 33) return Cell.ROOF;
		if (y > 33) return null;
		return x == 20 || x == 36 || z == -61 ? Cell.STONE : Cell.AIR;
	}

	private static Cell room(int x, int y, int z, int x0, int x1, int z0, int z1, int doorX, int doorZ, int kind) {
		if (y < 0) return Cell.STONE;
		if (y == 0) return Cell.FLOOR;
		if (y == 10) return ((x + z) % 11 == 0 || (x - z) % 13 == 0) ? Cell.AIR : Cell.ROOF;
		if (y > 10) return null;
		boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
		if (wall) {
			if (Math.abs(x - doorX) + Math.abs(z - doorZ) <= 1 && y <= 3) return Cell.AIR;
			if (y >= 4 && y <= 6 && (x + z) % 7 == 0) return Cell.WINDOW;
			return Cell.STONE;
		}
		if (y > 1) return Cell.AIR;
		if (x == x0 + 3 && z == z0 + 3) return Cell.SOUL_LIGHT;
		if (kind == 0 && x == -39 && z == -13) return Cell.ALTAR;
		if (kind == 1 && (x == -49 || x == -30) && z % 3 == 0) return Cell.BOOKSHELF;
		if (kind == 2 && x % 5 == 0 && (z == 36 || z == 43)) return Cell.BED_FOOT;
		if (kind == 2 && x % 5 == 0 && (z == 37 || z == 44)) return Cell.BED_HEAD;
		if (kind == 3 && x == 36 && z == 14) return Cell.ENCHANTING;
		if (kind == 3 && x == 29 && z == 14) return Cell.BREWING;
		if (x == x0 + 2 && z == z1 - 2) return Cell.HIDDEN_CHEST;
		return Cell.AIR;
	}

	private static Cell barbican(int x, int y, int z) {
		if (y < 0) return Cell.STONE;
		if (y == 0) return Cell.FLOOR;
		if (y > 12) return null;
		if (y == 1 && Math.abs(x) == 5 && z == 59) return Cell.SOUL_LIGHT;
		if (Math.abs(x) <= 3 && y <= 5) return Cell.AIR;
		if (Math.abs(x) >= 7 || z == 51 || z == 63) return y == 12 ? Cell.MERLON : Cell.STONE;
		return y == 9 ? Cell.WOOD : Cell.AIR;
	}

	private static Cell curtain(int x, int y, int z) {
		if (y < 0) return Cell.STONE;
		if (y == 0) return Cell.FLOOR;
		boolean breach = (x <= -58 && z >= 8 && z <= 16) || (z <= -58 && x >= -17 && x <= -10)
				|| (x >= 58 && z >= -8 && z <= -3);
		if (breach) return y <= 2 ? Cell.RUBBLE : null;
		if (y <= 8) return Cell.STONE;
		if (y == 9) return Cell.FLOOR;
		if (y <= 11 && ((x + z) & 3) < 2) return Cell.MERLON;
		return null;
	}
}
