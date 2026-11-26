package dev.aptitudes.dungeon;

/** Cuatro plantas de campamento V90, combinables con siete temáticas. Coordenadas relativas al origen. */
public final class CampLayout {
	public static final int R = 14, BOTTOM = -2, TOP = 9;
	public enum Shape { CIRCLE, PALISADE, ROAD, TERRACE }
	/** El orden también identifica los siete objetivos de contrato; solo se añaden tipos al final. */
	public enum Kind { HUNTER, WARLOCK, BANDIT, RAIDED_MERCHANT, DESERTER, VEIL_CULTIST, RELIC_SEEKER }
	public enum Cell { AIR, FLOOR, FOUNDATION, FENCE, WALL, WOOD, WOOL, RAMPART, STAIR, LADDER, TENT,
		CAMPFIRE, ALTAR, WORKBENCH, CARGO, CHEST, HIDDEN_CHEST, CLUE, LANTERN, RUBBLE,
		TARGET, SMOKER, BREWING, CAGE, BARRICADE, TRENCH, SCAFFOLD, CANDLE, ARMORY }
	private final Shape shape;
	private final Kind kind;
	private final long seed;

	public CampLayout(Shape shape, Kind kind, long seed) {
		this.shape = shape;
		this.kind = kind;
		this.seed = seed;
	}
	public Shape shape() { return shape; }
	public Kind kind() { return kind; }
	public static boolean inside(int x, int z) { return Math.abs(x) <= R && Math.abs(z) <= R; }
	public int[] entrance() { return new int[] {0, 0, 13}; }
	public int[] captain() { return shape == Shape.TERRACE ? new int[] {0, 3, -8} : new int[] {0, 0, -8}; }
	public int[] visibleChest() { return new int[] {-8, 0, 0}; }
	public int[] hiddenChest() { return new int[] {8, -1, 5}; }
	private int elevation(int x, int z) { return shape == Shape.TERRACE && z < -2 ? 3 : 0; }
	private boolean ground(int x, int z) {
		return switch (shape) {
			case CIRCLE -> x * x + z * z <= 14 * 14;
			case PALISADE, TERRACE -> true;
			case ROAD -> Math.abs(x) <= 10 || Math.abs(z) <= 3;
		};
	}
	private boolean boundary(int x, int z) {
		return switch (shape) {
			case CIRCLE -> x * x + z * z >= 12 * 12;
			case PALISADE -> Math.abs(x) == R || Math.abs(z) == R;
			case ROAD -> false;
			case TERRACE -> Math.abs(x) == R || z == -14;
		};
	}
	private boolean entranceGap(int x, int z) { return z >= 12 && Math.abs(x) <= 1; }
	private boolean tent(int x, int z) {
		return Math.abs(x - 8) <= 2 && Math.abs(z + 5) <= 2
				|| Math.abs(x + 8) <= 2 && Math.abs(z + 5) <= 2
				|| shape == Shape.CIRCLE && Math.abs(x) <= 2 && Math.abs(z - 8) <= 2;
	}
	private boolean cart(int x, int z) {
		return (shape == Shape.ROAD || kind == Kind.RAIDED_MERCHANT) && ((Math.abs(x - 6) <= 2 && Math.abs(z - 4) <= 1)
				|| (Math.abs(x + 6) <= 2 && Math.abs(z + 4) <= 1));
	}
	private boolean watchtower(int x, int z) { return shape == Shape.PALISADE && x >= 9 && x <= 13 && z >= -13 && z <= -9; }
	private long hash(int x, int z) {
		long h = seed ^ (x * 0x9e3779b97f4a7c15L) ^ (z * 0xc2b2ae3d27d4eb4fL);
		h ^= h >>> 30; h *= 0xbf58476d1ce4e5b9L; h ^= h >>> 27;
		return h;
	}
	/** null deja el terreno intacto; los marcadores de cofre quedan en el suelo o sobre él. */
	public Cell cell(int x, int y, int z) {
		if (!inside(x, z) || y < BOTTOM || y > TOP || !ground(x, z)) return null;
		int level = elevation(x, z);
		boolean excavation = (kind == Kind.DESERTER && x == -4 && z == 9)
				|| (kind == Kind.RELIC_SEEKER && x == 4 && z == 9);
		if (excavation && y == level - 1) return Cell.AIR;
		if (shape == Shape.TERRACE && z >= -4 && z <= -2 && Math.abs(x) <= 1) {
			int step = -z - 2;
			if (y == step - 1) return Cell.STAIR;
			if (y >= step && y <= step + 3) return Cell.AIR;
		}
		if (y < level - 1) return Cell.FOUNDATION;
		if (y == level - 1) {
			if (x == 8 && z == 5) return Cell.HIDDEN_CHEST;
			return Cell.FLOOR;
		}
		if (y == level && x == 8 && z == 5) return Cell.CLUE;
		if (y == level && x == visibleChest()[0] && z == visibleChest()[2]) return Cell.CHEST;
		if (y == level && x == -4 && z == 9) {
			return switch (kind) {
				case HUNTER -> Cell.TARGET;
				case WARLOCK -> Cell.BREWING;
				case BANDIT -> Cell.BARRICADE;
				case RAIDED_MERCHANT -> Cell.CARGO;
				case DESERTER -> Cell.TRENCH;
				case VEIL_CULTIST -> Cell.CANDLE;
				case RELIC_SEEKER -> Cell.SCAFFOLD;
			};
		}
		if (y == level && x == 4 && z == 9) {
			return switch (kind) {
				case HUNTER -> Cell.SMOKER;
				case WARLOCK -> Cell.CANDLE;
				case BANDIT -> Cell.CAGE;
				case RAIDED_MERCHANT -> Cell.CARGO;
				case DESERTER -> Cell.BARRICADE;
				case VEIL_CULTIST -> Cell.ALTAR;
				case RELIC_SEEKER -> Cell.TRENCH;
			};
		}
		if (y == level && x == 7 && z == 8 && kind == Kind.DESERTER) return Cell.ARMORY;
		if (watchtower(x, z)) {
			if (x == 10 && z == -12 && y <= 7) return Cell.LADDER;
			boolean wall = x == 9 || x == 13 || z == -13 || z == -9;
			if (wall && !(x == 11 && z == -9 && y <= 2)) return y <= 7 ? Cell.WALL : y == 8 ? Cell.RAMPART : null;
			if (y == 3 || y == 6) return Cell.WOOD;
			return y <= 7 ? Cell.AIR : null;
		}
		if (boundary(x, z) && !entranceGap(x, z)) {
			if (shape == Shape.PALISADE && Math.floorMod(hash(x, z), 11) == 0 && y >= level + 2) return Cell.RUBBLE;
			if (y <= level + (shape == Shape.PALISADE ? 3 : 1)) return Cell.FENCE;
		}
		if (tent(x, z)) {
			int dx = Math.min(Math.abs(x - 8), Math.abs(x + 8));
			if (shape == Shape.CIRCLE) dx = Math.min(dx, Math.abs(x));
			if (y == level && dx == 2 || y == level + 1 && dx == 1 || y == level + 2 && dx == 0) return Cell.TENT;
		}
		if (cart(x, z) && y == level) return Cell.CARGO;
		if (y == level && x == 0 && z == 0) return kind == Kind.WARLOCK || kind == Kind.VEIL_CULTIST ? Cell.ALTAR : Cell.CAMPFIRE;
		if (y == level && x == -4 && z == 5) return kind == Kind.RELIC_SEEKER ? Cell.WORKBENCH : Cell.CARGO;
		if (y == level && x == 4 && z == -4) return Cell.LANTERN;
		return y <= Math.max(level + 3, 4) ? Cell.AIR : null;
	}
}
