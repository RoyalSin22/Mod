package dev.aptitudes.dungeon;

import java.util.List;

/**
 * V91: plano puro de la fortaleza de las Tres Torres. Cada consulta depende solo de coordenadas relativas;
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
	private static final int[][] WINGS = {{-53, -25, -25, -1}, {-52, -27, 7, 26}, {-51, -17, 32, 52}, {20, 51, 3, 27}};
	public static final int[] CONTROLLER = {0, 0, 0};
	public static final int[] ENTRANCE = {0, 1, 62};
	public static final int[] WEST_BOSS = {WEST.cx(), 19, WEST.cz()};
	public static final int[] EAST_BOSS = {EAST.cx(), 19, EAST.cz()};
	public static final int[] COLOSSUS = {MARSHAL.cx(), 25, MARSHAL.cz()};
	public static final int[] REWARD = {28, 25, -58};
	public static final int[] MONASTERY_ELITE = {-39, 1, -13};

	public enum Cell {
		AIR, STONE, FLOOR, WOOD, ROOF, RUBBLE, MERLON, WINDOW, SOUL_LIGHT,
		STAIR_EAST, STAIR_WEST, CONTROLLER, BOOKSHELF,
		BED_FOOT, BED_HEAD, BREWING, ENCHANTING, ALTAR, HIDDEN_CHEST, SUPPLY_CHEST,
		TRIM, CORNICE, ARCH_NORTH, ARCH_SOUTH, ARCH_EAST, ARCH_WEST,
		IRON_BARS, CHAIN, LANTERN, BENCH, TABLE, LECTERN, CAULDRON, BARREL,
		GRINDSTONE, WOOL_MARKER, MOSS, FENCE, STATUE_BODY, STATUE_HEAD,
		PATH_WORN, PATH_STONE, WALL_PANEL, CARTOGRAPHY, PLANTER, TIMBER, BANNER
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

	/** Columnas de suelo o alero; los jardines y patios rotos conservan vegetación natural. */
	public static boolean occupied(int x, int z) {
		return cell(x, 0, z) != null || wingOverhang(x, z);
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
		Cell depth = facadeDepth(x, y, z);
		if (depth != null) return depth;
		Cell detail = wingExterior(x, y, z);
		if (detail != null) return detail;
		Cell courtyard = courtyard(x, y, z);
		if (courtyard != null) return courtyard;
		if (bridge(x, z)) {
			if (y == 12) return Cell.WOOD;
			if (y == 13 && (x == -45 || x == -41 || x == 41 || x == 45)) return Cell.FENCE;
			if (y >= 13 && y <= 15) return Cell.AIR;
		}
		if (Math.abs(x) >= 58 && Math.abs(x) <= 61 && Math.abs(z) <= 61
				|| Math.abs(z) >= 58 && Math.abs(z) <= 61 && Math.abs(x) <= 61) return curtain(x, y, z);
		if (path(x, z)) {
			if (y < 0) return Cell.STONE;
			if (y == 0) {
				int patch = Math.floorMod(pattern(Math.floorDiv(x, 3), Math.floorDiv(z, 3)), 13);
				int chip = Math.floorMod(pattern(x, z), 17);
				return patch == 0 && chip > 3 ? Cell.PATH_WORN : chip < 2 ? Cell.PATH_STONE : chip == 2 ? Cell.TRIM : Cell.FLOOR;
			}
			if (y == 1 && Math.abs(x) >= 10 && Math.floorMod(z, 17) == 0) return Cell.MOSS;
			return y <= 6 ? Cell.AIR : null;
		}
		return null;
	}

	private static boolean rect(int x, int z, int x0, int x1, int z0, int z1) {
		return x >= x0 && x <= x1 && z >= z0 && z <= z1;
	}

	private static int pattern(int x, int z) {
		int value = x * 0x1f1f1f1f ^ z * 0x5f356495;
		value ^= value >>> 16;
		value *= 0x7feb352d;
		return value ^ value >>> 15;
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

	/** Contrafuertes y escombros exteriores: añaden profundidad sin cerrar los accesos. */
	private static Cell facadeDepth(int x, int y, int z) {
		for (TowersLayout.Tower t : TOWERS) {
			int dx = x - t.cx(), dz = z - t.cz(), h = t.half();
			if (t == MARSHAL && dz < -h) continue; // conserva la entrada al tesoro
			int entryZ = t.role() == TowersLayout.Role.VASSAL_EAST ? -h - 1 : h + 1;
			boolean passage = dz == entryZ && Math.abs(dx) <= 2;
			if (Math.abs(dx) == h + 1 && Math.abs(dz) == h + 1) {
				if (y < 0) return Cell.STONE;
				if (y == 0) return Cell.TRIM;
				if (y < t.roofY() - 1) return y % 6 == 0 ? Cell.CORNICE : Cell.TIMBER;
			}
			boolean flank = (Math.abs(dx) == h + 1 && Math.abs(dz) <= h && Math.abs(dz) >= h - 2)
					|| (Math.abs(dz) == h + 1 && Math.abs(dx) <= h && Math.abs(dx) >= h - 2);
			boolean pier = !passage && ((Math.abs(dx) == h + 1 && Math.abs(dz) <= h - 2 && Math.floorMod(dz + h, 9) == 0)
					|| (Math.abs(dz) == h + 1 && Math.abs(dx) <= h - 2 && Math.floorMod(dx + h, 9) == 0));
			if (flank || pier) {
				if (y < 0) return Cell.STONE;
				if (y == 0) return Cell.TRIM;
				if (y <= (t == MARSHAL ? 34 : 22)) return y % 6 == 5 ? Cell.CORNICE : Cell.STONE;
			}
			boolean outer = (Math.abs(dx) == h + 1 && Math.abs(dz) <= h)
					|| (Math.abs(dz) == h + 1 && Math.abs(dx) <= h);
			if (outer) {
				if (y == t.roofY()) return Cell.ROOF;
				if (y == t.roofY() - 1) return Cell.CORNICE;
				if (passage) continue;
				if (y < 0) return Cell.STONE;
				if (y == 0) return Cell.TRIM;
				if (y <= 3) return Cell.STONE;
			}
		}
		for (int[] r : WINGS) {
			boolean side = (x == r[0] - 1 || x == r[1] + 1) && z > r[2] + 2 && z < r[3] - 2
					&& Math.floorMod(z - r[2], 7) == 0;
			boolean end = (z == r[2] - 1 || z == r[3] + 1) && x > r[0] + 2 && x < r[1] - 2
					&& Math.floorMod(x - r[0], 7) == 0;
			if (side || end) {
				if (y < 0) return Cell.STONE;
				if (y == 0) return Cell.TRIM;
				if (y <= 6) return y == 6 ? Cell.CORNICE : Cell.STONE;
			}
		}
		boolean wallPier = (Math.abs(x) == 57 || Math.abs(x) == 62) && Math.abs(z) < 57 && Math.floorMod(z + 60, 12) == 0
				|| (Math.abs(z) == 57 || Math.abs(z) == 62) && Math.abs(x) < 57 && Math.floorMod(x + 60, 12) == 0;
		if (wallPier) {
			if (y < 0) return Cell.STONE;
			if (y == 0) return Cell.TRIM;
			if (y <= 6) return y == 6 ? Cell.CORNICE : Cell.STONE;
		}
		boolean breachRubble = rect(x, z, -57, -54, 8, 16) || rect(x, z, -17, -10, -57, -55)
				|| rect(x, z, 55, 57, -8, -3);
		if (breachRubble && Math.floorMod(x * 5 + z * 3, 4) != 0) {
			if (y < 0) return Cell.STONE;
			if (y == 0) return Cell.RUBBLE;
			if (y == 1 && Math.floorMod(x + z, 3) == 0) return Cell.RUBBLE;
		}
		return null;
	}

	private static boolean wingOverhang(int x, int z) {
		for (int[] r : WINGS) {
			if (((x == r[0] - 1 || x == r[1] + 1) && z >= r[2] && z <= r[3])
					|| ((z == r[2] - 1 || z == r[3] + 1) && x >= r[0] && x <= r[1])) return true;
		}
		return false;
	}

	/** Alero continuo y sillar saliente bajo las ventanas, un bloque fuera de cada ala. */
	private static Cell wingExterior(int x, int y, int z) {
		for (int[] r : WINGS) {
			boolean side = (x == r[0] - 1 || x == r[1] + 1) && z >= r[2] && z <= r[3];
			boolean end = (z == r[2] - 1 || z == r[3] + 1) && x >= r[0] && x <= r[1];
			if (!side && !end) continue;
			if (y == 10) return Cell.CORNICE;
			int along = side ? z - r[2] : x - r[0];
			if (Math.floorMod(along, 7) == 3 && (y == 2 || y == 7)) return Cell.CORNICE;
		}
		return null;
	}

	/** Arcada procesional rota y dos figuras sin rostro que señalan la entrada desde el patio. */
	private static Cell courtyard(int x, int y, int z) {
		if (Math.abs(x) == 8 && z == 46) {
			if (y == 0 || y == 1) return Cell.TRIM;
			if (y == 2 || y == 3) return Cell.STATUE_BODY;
			if (y == 4) return Cell.STATUE_HEAD;
		}
		if (Math.abs(x) != 12 || z < -8 || z > 40) return null;
		int bay = Math.floorMod(z + 8, 8);
		boolean broken = x < 0 ? z >= 8 && z <= 16 : z >= 24 && z <= 32;
		if (bay == 0 && y >= 1 && y <= (broken ? 2 : 5)) return broken && y == 2 ? Cell.RUBBLE : Cell.TRIM;
		if (broken) return null;
		if (bay != 0 && y == 6) return Cell.CORNICE;
		if (y == 5 && bay == 1) return Cell.ARCH_NORTH;
		if (y == 5 && bay == 7) return Cell.ARCH_SOUTH;
		return null;
	}

	private static Cell tower(TowersLayout.Tower t, int x, int y, int z) {
		int dx = x - t.cx(), dz = z - t.cz(), h = t.half();
		if (y < 0) return Cell.STONE;
		if (y == 0) return Cell.FLOOR;
		if (y == t.roofY()) {
			if (t == MARSHAL && Math.abs(dx) <= 2 && Math.abs(dz) <= 2) return Cell.WINDOW;
			return Math.abs(dx) == h || Math.abs(dz) == h ? Cell.CORNICE : Cell.ROOF;
		}
		if (y > t.roofY()) return towerCrown(t, dx, y, dz);
		boolean wall = Math.abs(dx) == h || Math.abs(dz) == h;
		if (wall) {
			if (t.role() == TowersLayout.Role.LORD && dz == -h && Math.abs(dx) <= 1 && y >= 25 && y <= 27) return Cell.AIR;
			if (t.role() == TowersLayout.Role.VASSAL_WEST && dz == h && Math.abs(dx) <= 1 && y >= 13 && y <= 15) return Cell.AIR;
			if (t.role() == TowersLayout.Role.VASSAL_EAST && dz == -h && Math.abs(dx) <= 1 && y >= 13 && y <= 15) return Cell.AIR;
			int entryZ = t.role() == TowersLayout.Role.VASSAL_EAST ? -h : h;
			if (dz == entryZ && Math.abs(dx) <= 1 && y <= 3) return Cell.AIR;
			if (y % 6 == 5 && y < t.bossFloorY()) return Cell.CORNICE;
			if (y >= t.bossFloorY() && y < t.roofY() && y % 6 == 0) return Cell.CORNICE;
			int along = Math.abs(dx) == h ? dz : dx;
			boolean window = Math.floorMod(along, t == MARSHAL ? 8 : 5) == 0 && Math.abs(along) < h - 2;
			if (window && y >= 2 && y <= t.roofY() - 3) {
				int phase = Math.floorMod(y, 6);
				if (phase == 2 || phase == 3) return t == MARSHAL ? Cell.WINDOW : Cell.IRON_BARS;
				if (phase == 4) return dz == -h ? Cell.ARCH_NORTH : dz == h ? Cell.ARCH_SOUTH
						: dx == -h ? Cell.ARCH_WEST : Cell.ARCH_EAST;
			}
			if (Math.floorMod(along + h, 7) == 0) return Cell.TRIM;
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
			int level = floor / TowersLayout.FLOOR_H;
			if (y == floor + 1 && Math.abs(dx) == h - 2 && Math.abs(dz) == h - 2) return Cell.BARREL;
			if (y == floor + 1 && dz == 0 && dx == h - 2) return Cell.TABLE;
			if (y == floor + 1 && dx == -h + 2 && dz == 2) return level == 1 ? Cell.BOOKSHELF : level == 2 ? Cell.ENCHANTING : Cell.BARREL;
			if (y == floor + 1 && dx == -h + 3 && dz == 2) return level == 1 ? Cell.LECTERN : level == 2 ? Cell.BREWING : Cell.CARTOGRAPHY;
			if (t == MARSHAL && level == 3 && y == floor + 1 && dx == h - 3 && dz == 2) return Cell.GRINDSTONE;
			if (y == floor + 4 && dx == 0 && dz == 0) return Cell.LANTERN;
			if (y == floor + 5 && dx == 0 && dz == 0) return Cell.CHAIN;
		}
		if (y == t.bossFloorY() + 1 && Math.abs(dx) == h - 1 && dz == 0) return Cell.SOUL_LIGHT;
		if (y == 1 && t.role() != TowersLayout.Role.LORD && dx == -h + 2 && dz == 0) return Cell.SUPPLY_CHEST;
		return Cell.AIR;
	}

	/** Atalayas abiertas y linterna octogonal del Mariscal, con almenas sobre la cubierta. */
	private static Cell towerCrown(TowersLayout.Tower t, int dx, int y, int dz) {
		int h = t.half(), roof = t.roofY();
		if (y <= roof + 2 && (Math.abs(dx) == h || Math.abs(dz) == h))
			return Math.floorMod(dx + dz, 3) == 0 ? Cell.MERLON : Cell.AIR;
		if (t == MARSHAL) {
			int distance = Math.max(Math.abs(dx), Math.abs(dz));
			if (y <= 50) return distance == 58 - y ? Cell.ROOF : null;
			int radius = y <= 52 ? 7 : 5;
			if (y == 59 && Math.max(Math.abs(dx), Math.abs(dz)) <= 5) return Cell.ROOF;
			if (y >= 60) return y == 62 && dx == 0 && dz == 0 ? Cell.BANNER
					: Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && y <= 61 ? Cell.TRIM : null;
			if (y == 53 && Math.max(Math.abs(dx), Math.abs(dz)) <= 7) return Cell.FLOOR;
			if (y <= 58 && Math.max(Math.abs(dx), Math.abs(dz)) == radius
					&& Math.abs(dx) + Math.abs(dz) < radius * 2 - 1) {
				boolean opening = (Math.abs(dx) <= 2 || Math.abs(dz) <= 2) && y >= 52 && y <= 56;
				return opening ? Cell.WINDOW : y % 4 == 0 ? Cell.TRIM : Cell.STONE;
			}
			return null;
		}
		int radius = 5;
		if (y == 36 && Math.max(Math.abs(dx), Math.abs(dz)) <= 5) return Cell.ROOF;
		if (y >= 37) {
			int distance = Math.max(Math.abs(dx), Math.abs(dz));
			return y == 41 && dx == 0 && dz == 0 ? Cell.BANNER : y <= 40 && distance == 40 - y ? Cell.ROOF : null;
		}
		if (y <= 35 && Math.max(Math.abs(dx), Math.abs(dz)) == radius && Math.abs(dx) + Math.abs(dz) <= 8) {
			boolean opening = (dx == 0 || dz == 0) && y >= 30 && y <= 33;
			return opening ? Cell.WINDOW : y == 35 ? Cell.CORNICE : Cell.STONE;
		}
		return null;
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
		if (y == 24) return Math.floorMod(x + z, 4) == 0 ? Cell.TRIM : Cell.FLOOR;
		if (y == 33) return Math.abs(x - 28) == 7 ? Cell.TRIM : Cell.ROOF;
		if (y > 33) return null;
		if (x == 20 || x == 36 || z == -61) {
			if (y == 30) return Cell.CORNICE;
			if (y >= 27 && y <= 29 && Math.floorMod(x + z, 5) == 0) return Cell.WINDOW;
			return Cell.STONE;
		}
		if (y >= 25 && y <= 31 && (x == 22 || x == 34) && (z == -59 || z == -57)) return Cell.TRIM;
		if (y == 30 && z == -58 && x == 28) return Cell.LANTERN;
		if (y == 31 && z == -58 && x == 28) return Cell.CHAIN;
		return Cell.AIR;
	}

	private static Cell room(int x, int y, int z, int x0, int x1, int z0, int z1, int doorX, int doorZ, int kind) {
		boolean secret = x == x0 + 3 && z == z1 - 3;
		if (secret && y == -1) return Cell.HIDDEN_CHEST;
		if (y < 0) return Cell.STONE;
		if (y == 0) return secret ? Cell.WOOL_MARKER
				: Math.floorMod(x * 3 + z * 5 + kind, 13) == 0 ? Cell.TRIM : Cell.FLOOR;
		int roofY = 10 + Math.min(6, Math.min(x - x0, x1 - x) / 2);
		if (y >= 10) {
			if (missingRoof(kind, x, z)) return y <= roofY ? Cell.AIR : null;
			if (y == roofY) return Cell.ROOF;
			if ((z == z0 || z == z1) && y < roofY) return y == roofY - 1 && x == (x0 + x1) / 2 ? Cell.WINDOW : Cell.STONE;
			return y <= roofY ? Cell.AIR : null;
		}
		boolean wall = x == x0 || x == x1 || z == z0 || z == z1;
		if (wall) {
			if (Math.abs(x - doorX) + Math.abs(z - doorZ) <= 1 && y <= 3) return Cell.AIR;
			int along = x == x0 || x == x1 ? z - z0 : x - x0;
			if (y == 8) return Cell.CORNICE;
			if (Math.floorMod(along, 7) == 3 && y >= 3 && y <= 6) {
				if (y <= 5) return Cell.WINDOW;
				return z == z0 ? Cell.ARCH_NORTH : z == z1 ? Cell.ARCH_SOUTH
						: x == x0 ? Cell.ARCH_WEST : Cell.ARCH_EAST;
			}
			if (Math.floorMod(along, 7) == 0) return Cell.TRIM;
			return Cell.STONE;
		}
		if (y >= 1 && y <= 3 && kind == 1 && x == x0 + 1 && z > z0 + 2 && z < z1 - 2) return Cell.WALL_PANEL;
		if (y >= 1 && y <= 3 && kind == 2 && z == z1 - 1 && x > x0 + 2 && x < x1 - 2) return Cell.WALL_PANEL;
		if (y == 9 && (Math.floorMod(x - x0, 7) == 0 || Math.floorMod(z - z0, 7) == 0)) return Cell.TRIM;
		if (y == 9 && x == (x0 + x1) / 2 && z == (z0 + z1) / 2) return Cell.TRIM;
		if (y == 7 && x == (x0 + x1) / 2 && z == (z0 + z1) / 2) return Cell.LANTERN;
		if (y == 8 && x == (x0 + x1) / 2 && z == (z0 + z1) / 2) return Cell.CHAIN;
		if (y >= 1 && y <= 8 && (x == x0 + 2 || x == x1 - 2) && (z == z0 + 2 || z == z1 - 2)) return Cell.TRIM;
		if (kind == 1 && y <= 3 && (x == -49 || x == -30) && Math.floorMod(z, 3) == 0) return Cell.BOOKSHELF;
		if (kind == 3 && y <= 3 && x == 48 && Math.floorMod(z, 4) == 0) return Cell.BOOKSHELF;
		if (y > 1) return Cell.AIR;
		if (kind == 0) {
			if (x == -39 && z == -13) return Cell.ALTAR;
			if ((x == -47 || x == -31) && z >= -20 && z <= -5 && Math.floorMod(z + 20, 4) == 0) return Cell.BENCH;
			if (x == -39 && z == -21) return Cell.LECTERN;
		}
		if (kind == 1) {
			if ((x == -44 && z == 12) || (x == -34 && z == 22)) return Cell.LECTERN;
			if (x == -39 && (z == 11 || z == 22)) return Cell.TABLE;
			if (x == -41 && z == 13) return Cell.CARTOGRAPHY;
		}
		if (kind == 2) {
			if (x % 5 == 0 && (z == 36 || z == 43)) return Cell.BED_FOOT;
			if (x % 5 == 0 && (z == 37 || z == 44)) return Cell.BED_HEAD;
			if ((x == -47 || x == -21) && z == 49) return Cell.BARREL;
			if (x == -44 && z == 48) return Cell.BARREL;
		}
		if (kind == 3) {
			if (x == 36 && z == 14) return Cell.ENCHANTING;
			if (x == 29 && z == 14) return Cell.BREWING;
			if (x == 24 && Math.floorMod(z - 7, 5) == 0) return Cell.CAULDRON;
			if (x == 43 && z == 20) return Cell.GRINDSTONE;
			if (x == 39 && z == 7) return Cell.LECTERN;
			if (x == 46 && z == 6) return Cell.BARREL;
		}
		if (x == x0 + 4 && z == z1 - 4) return Cell.PLANTER;
		return Cell.AIR;
	}

	private static boolean missingRoof(int kind, int x, int z) {
		return switch (kind) {
			case 0 -> x < -42 && z < -14;
			case 1 -> x < -43 && z > 17;
			case 2 -> x > -30 && z > 44;
			default -> x > 41 && z < 14;
		};
	}

	private static Cell barbican(int x, int y, int z) {
		if (y < 0) return Cell.STONE;
		if (y == 0) return Math.floorMod(x + z, 5) == 0 ? Cell.TRIM : Cell.FLOOR;
		if (y > 12) return null;
		if (y == 1 && Math.abs(x) == 5 && (z == 54 || z == 60)) return Cell.BARREL;
		if (y == 6 && x == 0 && z == 57) return Cell.LANTERN;
		if (y == 7 && x == 0 && z == 57) return Cell.CHAIN;
		if (Math.abs(x) <= 3 && y <= 5) return Cell.AIR;
		if (z == 63 && Math.abs(x) <= 3 && y == 6 - Math.abs(x) / 2) return Cell.ARCH_SOUTH;
		if (Math.abs(x) >= 7 || z == 51 || z == 63) {
			if (y == 12) return Math.floorMod(x + z, 3) == 0 ? Cell.MERLON : Cell.AIR;
			if (y == 10) return Cell.CORNICE;
			if (Math.abs(x) >= 7 && y >= 3 && y <= 5 && Math.floorMod(z, 5) == 0) return Cell.IRON_BARS;
			return Cell.STONE;
		}
		return y == 9 ? Cell.WOOD : Cell.AIR;
	}

	private static Cell curtain(int x, int y, int z) {
		if (y < 0) return Cell.STONE;
		boolean breach = (x <= -58 && z >= 8 && z <= 16) || (z <= -58 && x >= -17 && x <= -10)
				|| (x >= 58 && z >= -8 && z <= -3);
		if (y == 0) return breach ? Cell.RUBBLE : Cell.FLOOR;
		if (breach) return y <= 2 && Math.floorMod(x * 3 + z * 5 + y, 4) != 0 ? Cell.RUBBLE : Cell.AIR;
		int along = Math.abs(x) >= 58 ? z : x;
		boolean damaged = Math.floorMod(Math.floorDiv(along + 61, 13), 5) == 2;
		if (damaged && y >= 7) return null;
		if (y <= 8) {
			if (y >= 4 && y <= 6 && Math.floorMod(along + 61, 11) == 0) return Cell.IRON_BARS;
			if (y == 7) return Cell.CORNICE;
			return Cell.STONE;
		}
		if (y == 9) return Cell.FLOOR;
		if (y <= 11 && Math.floorMod(along, 4) < 2) return Cell.MERLON;
		return null;
	}
}
