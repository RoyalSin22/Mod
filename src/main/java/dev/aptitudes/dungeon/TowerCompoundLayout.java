package dev.aptitudes.dungeon;

/** Plano V90 de la torre menor. No depende de Minecraft ni del orden de consulta. */
public final class TowerCompoundLayout {
	public static final int R = 10, BOTTOM = -6, TOP = 27;
	public static final int CX = -4, CZ = -2;
	public enum Shape { SQUARE, ROUND }
	public enum Cell { AIR, STONE, FLOOR, WOOD, RAMPART, WINDOW, RUBBLE, STAIR_EAST, STAIR_WEST, LADDER,
		DOOR, LANTERN, BED, BOOKSHELF, ARMORY, WORKBENCH, BARS, CHEST, UPPER_CHEST, TOP_CHEST,
		HIDDEN_CHEST, CLUE, SPAWNER, BOSS }
	private final Shape shape;
	private final int damage;
	private final long seed;

	public TowerCompoundLayout(Shape shape, int damage, long seed) {
		this.shape = shape;
		this.damage = Math.max(0, Math.min(2, damage));
		this.seed = seed;
	}
	public Shape shape() { return shape; }
	public int damage() { return damage; }
	public static boolean inside(int x, int z) { return Math.abs(x) <= R && Math.abs(z) <= R; }
	private static long hash(long seed, int x, int y, int z) {
		long h = seed ^ (x * 0x9e3779b97f4a7c15L) ^ (y * 0xc2b2ae3d27d4eb4fL) ^ (z * 0x165667b19e3779f9L);
		h ^= h >>> 30; h *= 0xbf58476d1ce4e5b9L; h ^= h >>> 27; h *= 0x94d049bb133111ebL;
		return h ^ h >>> 31;
	}
	private boolean tower(int x, int z) {
		int dx = x - CX, dz = z - CZ;
		return Math.abs(dx) <= 4 && Math.abs(dz) <= 4
				&& (shape == Shape.SQUARE || dx * dx + dz * dz <= 20);
	}
	private boolean edge(int x, int z) {
		if (!tower(x, z)) return false;
		return !tower(x + 1, z) || !tower(x - 1, z) || !tower(x, z + 1) || !tower(x, z - 1);
	}
	private boolean courtyardWall(int x, int z) {
		return (Math.abs(x) == R && Math.abs(z) <= R || Math.abs(z) == R && Math.abs(x) <= R)
				&& !(z == R && Math.abs(x) <= 1);
	}
	private boolean buttress(int x, int z) {
		if (shape != Shape.SQUARE) return false;
		int dx = x - CX, dz = z - CZ;
		return Math.abs(dx) == 5 && Math.abs(dz) == 3
				|| Math.abs(dz) == 5 && Math.abs(dx) == 3;
	}
	private boolean damageGap(int x, int y, int z) {
		return damage > 0 && y >= (damage == 1 ? 4 : 2)
				&& Math.floorMod(hash(seed, x, y / 2, z), damage == 1 ? 17 : 9) == 0;
	}
	/** null = terreno ajeno; las celdas de aire pertenecen a la estructura. */
	public Cell cell(int x, int y, int z) {
		if (!inside(x, z) || y < BOTTOM || y > TOP) return null;
		if (tower(x, z)) return towerCell(x, y, z);
		if (buttress(x, z)) {
			if (y == -1 || y <= 12 && y >= 0) return Cell.STONE;
			if (y == 13) return Cell.RAMPART;
			return null;
		}
		if (y < -1) return null;
		if (z == 10 && Math.abs(x) <= 1) return y == -1 ? Cell.FLOOR : y <= 4 ? Cell.AIR : null;
		if (courtyardWall(x, z)) {
			if (y == -1) return Cell.STONE;
			if (y <= 3) return damageGap(x, y, z) ? Cell.RUBBLE : Cell.STONE;
			if (y == 4) return damageGap(x, y, z) ? null : Cell.RAMPART;
			return null;
		}
		if (Math.abs(x) > 9 || Math.abs(z) > 9 || y > 5) return null;
		if (y == -1) return Cell.FLOOR;
		if (x >= 4 && x <= 8 && z >= -8 && z <= -4) {
			if (y == 4) return Cell.WOOD;
			if ((x == 4 || x == 8 || z == -8 || z == -4) && !(x == 6 && z == -4 && y <= 2))
				return Cell.STONE;
			if (y == 0 && x == 6 && z == -7) return Cell.CHEST;
			if (y == 0 && x == 5 && z == -6) return Cell.LANTERN;
			return Cell.AIR;
		}
		if (y == 0 && x == 5 && z == 3) return Cell.STONE; // pozo
		if (y == 0 && x == 7 && z == 6) return Cell.WOOD; // establo
		if (y == 0 && x == 2 && z == 6) return Cell.LANTERN;
		return y <= 4 ? Cell.AIR : null;
	}
	private Cell towerCell(int x, int y, int z) {
		int dx = x - CX, dz = z - CZ;
		if (y == -6) return dx == -2 && dz == 2 ? Cell.HIDDEN_CHEST : Cell.STONE;
		if (y == -5 || y == 0 || y == 5 || y == 10 || y == 15 || y == 20 || y == 25) {
			if (y == -5 && dx == -2 && dz == 2) return Cell.CLUE;
			if (y == 0 && dx == 3 && dz == 2) return Cell.LADDER;
			if (y >= 0 && y <= 25 && !edge(x, z)) {
				int start = (y - 5) / 5 * 5;
				if (y > 0 && stair(x, y, z, start) != null) return stair(x, y, z, start);
				if (y > 0 && headOpening(x, y, z, start)) return Cell.AIR;
			}
			return Cell.FLOOR;
		}
		if (y == 26) {
			if (dx == 1 && dz == 0) return Cell.BOSS;
			if (dx == -2 && dz == -1) return Cell.TOP_CHEST;
			return edge(x, z) && (dx + dz & 1) == 0 ? Cell.RAMPART : Cell.AIR;
		}
		if (y == 27) return edge(x, z) && (dx + dz & 1) == 0 ? Cell.RAMPART : null;
		if (edge(x, z)) {
			if (y >= 1 && y <= 3 && dx == 4 && dz == 0) return Cell.DOOR;
			if (y >= -4 && y <= -2 && dx == 4 && dz == 1) return Cell.STONE;
			if (y >= 0 && y % 5 == 3 && (dx == 0 || dz == 0)) return Cell.WINDOW;
			return damageGap(x, y, z) && y > 0 ? Cell.RUBBLE : Cell.STONE;
		}
		if (y >= -4 && y <= -1 && dx == 3 && dz == 2) return Cell.LADDER;
		if (y >= 0 && y <= 25) {
			for (int floor = 0; floor < 25; floor += 5) {
				Cell stair = stair(x, y, z, floor);
				if (stair != null) return stair;
			}
		}
		if (y == -4 && dx == -2 && dz == 2) return Cell.CLUE;
		if (y == -4 && dx == -3 && dz == -1) return Cell.BARS;
		if (y == -4 && dx == 2 && dz == 0) return Cell.CHEST;
		if (y >= 0 && (y - 1) % 5 == 0) {
			int level = (y - 1) / 5;
			if (dx == -2 && dz == -2) return level == 4 ? Cell.UPPER_CHEST : Cell.CHEST;
			if (level == 2 && dx == 1 && dz == -1 || level == 3 && dx == 1 && dz == -1) return Cell.SPAWNER;
			if (dx == 0 && dz == 0) return level == 2 ? Cell.BED : level == 3 ? Cell.BOOKSHELF : level == 1 ? Cell.ARMORY : Cell.AIR;
			if (dx == 2 && dz == 2) return level == 3 ? Cell.WORKBENCH : Cell.LANTERN;
		}
		if ((y == 17 || y == 22) && Math.abs(dz) != 3
				&& (Math.abs(dx) == 3 || Math.abs(dz) == 3)) return Cell.WOOD;
		return Cell.AIR;
	}
	private static Cell stair(int x, int y, int z, int floor) {
		if (floor < 0 || floor >= 25 || y <= floor || y > floor + 5) return null;
		boolean east = (floor / 5 & 1) == 0;
		int row = CZ + (east ? -3 : 3), start = CX + (east ? -3 : 3);
		if (z != row) return null;
		for (int i = 1; i <= 5; i++) if (x == start + (east ? i : -i) && y == floor + i)
			return east ? Cell.STAIR_EAST : Cell.STAIR_WEST;
		return null;
	}
	private static boolean headOpening(int x, int y, int z, int floor) {
		if (floor < 0 || floor >= 25) return false;
		boolean east = (floor / 5 & 1) == 0;
		int row = CZ + (east ? -3 : 3), start = CX + (east ? -3 : 3);
		if (z != row) return false;
		for (int i = 2; i <= 4; i++) if (x == start + (east ? i : -i)) return true;
		return false;
	}
}
