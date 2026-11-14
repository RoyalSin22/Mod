package dev.aptitudes.temple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * T3b: distribución del Templo del Custodio, calculada sin Minecraft para poder probarla con JUnit
 * ({@code CustodianTempleLayoutTest}). {@code world/CustodianTempleFeature} traduce cada {@link Cell} a un bloque real.
 *
 * <p>Coordenadas relativas al origen: {@code y = 0} es el suelo de la superficie, {@code z} positiva es el sur (la
 * entrada) y todo cabe en un cuadrado de radio {@link #R} (31×31), el máximo seguro para un {@code Feature}.</p>
 *
 * <pre>
 * Superficie (y 0..8)                        Subsuelo (y -9..-1, casco de piedra)
 *   z -15  muro norte, palancas y luces        z -7..-1  escalera (desciende al sur, bajo la sala)
 *   z -7..-5  trampilla de piedra sellada      z 0..4    antesala
 *   z 5..14 (y 1..7) atrio con el Autómata     z 5       Puerta del Custodio (3×3)
 *   z 4  tabique con arco                      z 6..14   cámara del Custodio, Relicario y cofres
 * Cámaras de recompensa (T4): cobre y diamante a los lados de la cámara del Custodio (x ∓12..14) y hierro y oro tras su
 * muro norte (z 1..4), cada una tras una puerta que solo abre la llave de su metal.
 * </pre>
 */
public final class CustodianTempleLayout {
	/** Radio horizontal: el cuadrado completo mide 2R+1 = 31. */
	public static final int R = 15;
	public static final int SIZE = 2 * R + 1;
	public static final int ROOF_Y = 8;
	/** Suelo de la superficie. */
	public static final int FLOOR_Y = 0;
	/** Suelo del subsuelo. */
	public static final int UNDER_FLOOR_Y = -8;
	/** Fondo del casco. */
	public static final int HULL_BOTTOM_Y = -9;
	public static final int HEIGHT = ROOF_Y - HULL_BOTTOM_Y + 1;

	public static final int LEVER_Z = -14;
	public static final int[] LEVER_X_3 = {-9, -5, 5};
	public static final int[] LEVER_X_4 = {-9, -5, 5, 9};
	/** Posiciones posibles de placas (x, z); se usan las primeras {@code plateCount} en el orden indicado. */
	public static final int[][] PLATE_SPOTS = {{-6, -9}, {6, -9}, {-3, -11}, {3, -11}};

	/** Qué va en cada celda. {@code null} = no se toca (fuera de la estructura). */
	public enum Cell {
		AIR, HULL, WALL, FLOOR, ACCENT, ROOF, PILLAR,
		/** Escalón que asciende hacia el norte (se baja hacia el sur). */
		STAIR_NORTH,
		/** Piedra sellada de la trampilla: desaparece al resolver el puzle. */
		SEALED,
		/** Puerta del Custodio: solo cede al Ancla del Velo. */
		DOOR,
		LEVER, LIGHT_ON, LIGHT_OFF, PLATE, CANDLE_1, CANDLE_2, CANDLE_3, CANDLE_4,
		SEAL, RELIQUARY, LANTERN,
		/** T4: puertas de las cuatro cámaras de recompensa (1 de ancho × 2 de alto), una por metal. */
		CHAMBER_DOOR_COPPER, CHAMBER_DOOR_IRON, CHAMBER_DOOR_GOLD, CHAMBER_DOOR_DIAMOND,
		/** T4: cofre de cada cámara, con la tabla de botín de su metal. */
		CHEST_COPPER, CHEST_IRON, CHEST_GOLD, CHEST_DIAMOND
	}

	/** Celdas de puerta y de cofre por metal, en el orden de {@link TempleKeyRules.Metal}. */
	public static final Cell[] CHAMBER_DOORS = {Cell.CHAMBER_DOOR_COPPER, Cell.CHAMBER_DOOR_IRON, Cell.CHAMBER_DOOR_GOLD, Cell.CHAMBER_DOOR_DIAMOND};
	public static final Cell[] CHESTS = {Cell.CHEST_COPPER, Cell.CHEST_IRON, Cell.CHEST_GOLD, Cell.CHEST_DIAMOND};

	/** Resultado: las celdas y las posiciones que necesita el controlador (todas relativas al origen). */
	public static final class Result {
		private final Cell[] cells = new Cell[SIZE * HEIGHT * SIZE];
		public final List<int[]> levers = new ArrayList<>();
		public final List<int[]> plates = new ArrayList<>();
		public final List<int[]> hatch = new ArrayList<>();
		public final List<int[]> custodianDoor = new ArrayList<>();
		public final int[] seal = {0, 0, -3};
		public final int[] automatonSpawn = {0, 1, 9};
		public final int[] triggerCenter = {0, 1, 10};
		public final int[] custodianSpawn = {0, -7, 9};
		public final int[] reliquary = {0, -7, 13};
		/** T4: celdas de las puertas de cámara como {x, y, z, metal} (el metal es el índice de {@link TempleKeyRules.Metal}). */
		public final List<int[]> chamberDoors = new ArrayList<>();
		/** T4: cofres de las cámaras como {x, y, z, metal}. */
		public final List<int[]> chests = new ArrayList<>();

		private static int index(int x, int y, int z) {
			return ((y - HULL_BOTTOM_Y) * SIZE + (z + R)) * SIZE + (x + R);
		}

		public static boolean inside(int x, int y, int z) {
			return Math.abs(x) <= R && Math.abs(z) <= R && y >= HULL_BOTTOM_Y && y <= ROOF_Y;
		}

		/** Celda en esa posición, o {@code null} si es exterior o no se toca. */
		public Cell get(int x, int y, int z) {
			return inside(x, y, z) ? cells[index(x, y, z)] : null;
		}

		void set(int x, int y, int z, Cell cell) {
			if (!inside(x, y, z)) throw new IllegalArgumentException("fuera de la estructura: " + x + "," + y + "," + z);
			cells[index(x, y, z)] = cell;
		}

		void fill(Cell cell, int x0, int x1, int y0, int y1, int z0, int z1) {
			for (int x = x0; x <= x1; x++) for (int y = y0; y <= y1; y++) for (int z = z0; z <= z1; z++) set(x, y, z, cell);
		}
	}

	private CustodianTempleLayout() {}

	/** Número de palancas del anillo (mismas reglas que el templo antiguo). */
	public static int[] leverXs(int leverCount) {
		return leverCount >= 4 ? LEVER_X_4 : LEVER_X_3;
	}

	/**
	 * Construye la distribución.
	 *
	 * @param target     patrón de las luces: una por palanca (true = luz encendida)
	 * @param plateCount número de placas (0, 3 o 4)
	 * @param plateOrder orden de las posiciones de placa ({@code plateCount} índices distintos de {@link #PLATE_SPOTS})
	 */
	public static Result build(boolean[] target, int plateCount, int[] plateOrder) {
		if (target == null || target.length < TempleRules.MIN_LEVERS || target.length > TempleRules.MAX_LEVERS) {
			throw new IllegalArgumentException("palancas: " + (target == null ? "null" : target.length));
		}
		if (plateCount < 0 || plateCount > TempleRules.MAX_PLATES || plateOrder == null || plateOrder.length < plateCount) {
			throw new IllegalArgumentException("placas: " + plateCount);
		}
		Result r = new Result();

		// 1) Casco macizo del subsuelo: ninguna cueva, agua o lava puede abrirlo.
		r.fill(Cell.HULL, -R, R, HULL_BOTTOM_Y, -1, -R, R);

		// 2) Cascarón de la superficie: suelo, techo, muros exteriores y vacío interior.
		r.fill(Cell.FLOOR, -R, R, FLOOR_Y, FLOOR_Y, -R, R);
		r.fill(Cell.ROOF, -R, R, ROOF_Y, ROOF_Y, -R, R);
		r.fill(Cell.AIR, -R + 1, R - 1, 1, ROOF_Y - 1, -R + 1, R - 1);
		r.fill(Cell.WALL, -R, R, 1, ROOF_Y - 1, -R, -R);
		r.fill(Cell.WALL, -R, R, 1, ROOF_Y - 1, R, R);
		r.fill(Cell.WALL, -R, -R, 1, ROOF_Y - 1, -R, R);
		r.fill(Cell.WALL, R, R, 1, ROOF_Y - 1, -R, R);
		// Tabique entre el atrio y la sala del puzle, con un arco.
		r.fill(Cell.WALL, -R + 1, R - 1, 1, ROOF_Y - 1, 4, 4);
		r.fill(Cell.AIR, -2, 2, 1, 4, 4, 4);
		// Entrada al sur.
		r.fill(Cell.AIR, -1, 1, 1, 3, R, R);

		// 3) Columnas decorativas (fuera de los pasillos del puzle).
		for (int sx : new int[] {-1, 1}) {
			for (int[] p : new int[][] {{8, -4}, {8, 1}, {12, -4}, {12, 1}, {5, 8}, {5, 12}, {10, 8}, {10, 12}}) {
				r.fill(Cell.PILLAR, sx * p[0], sx * p[0], 1, ROOF_Y - 1, p[1], p[1]);
			}
		}

		// 4) Puzle: palancas en el muro norte con las luces encima, placas con velas y la trampilla sellada.
		int[] leverXs = leverXs(target.length);
		for (int i = 0; i < target.length; i++) {
			r.set(leverXs[i], 2, LEVER_Z, Cell.LEVER);
			r.levers.add(new int[] {leverXs[i], 2, LEVER_Z});
			r.set(leverXs[i], 4, -R, target[i] ? Cell.LIGHT_ON : Cell.LIGHT_OFF);
		}
		Cell[] candles = {Cell.CANDLE_1, Cell.CANDLE_2, Cell.CANDLE_3, Cell.CANDLE_4};
		for (int i = 0; i < plateCount; i++) {
			int[] spot = PLATE_SPOTS[plateOrder[i]];
			r.set(spot[0], 1, spot[1], Cell.PLATE);
			r.plates.add(new int[] {spot[0], 1, spot[1]});
			r.set(spot[0] + (spot[0] < 0 ? -1 : 1), 1, spot[1], candles[i]);
		}
		for (int x = -1; x <= 1; x++) {
			for (int z = -7; z <= -5; z++) {
				r.set(x, FLOOR_Y, z, Cell.SEALED);
				r.hatch.add(new int[] {x, FLOOR_Y, z});
			}
		}
		r.set(r.seal[0], r.seal[1], r.seal[2], Cell.SEAL);

		// 5) Escalera: desciende hacia el sur bajo el suelo de la sala (8 escalones, de y=-1 a y=-8).
		for (int i = 0; i < 8; i++) {
			int z = -7 + i;
			for (int x = -1; x <= 1; x++) {
				r.set(x, -1 - i, z, Cell.STAIR_NORTH);
				if (i >= 1) r.fill(Cell.AIR, x, x, -i, -1, z, z);
			}
		}

		// 6) Antesala y cámara del Custodio.
		r.fill(Cell.AIR, -4, 4, -7, -2, 0, 4);
		r.fill(Cell.FLOOR, -4, 4, UNDER_FLOOR_Y, UNDER_FLOOR_Y, 0, 4);
		r.fill(Cell.AIR, -10, 10, -7, -2, 6, 14);
		r.fill(Cell.FLOOR, -10, 10, UNDER_FLOOR_Y, UNDER_FLOOR_Y, 6, 14);
		r.fill(Cell.ACCENT, -1, 1, UNDER_FLOOR_Y, UNDER_FLOOR_Y, 6, 14);
		// Escalón final: sustituye al suelo de la antesala en la boca de la escalera.
		for (int x = -1; x <= 1; x++) r.set(x, UNDER_FLOOR_Y, 0, Cell.STAIR_NORTH);
		// Puerta del Custodio (3×3) en el muro entre la antesala y la cámara.
		for (int x = -1; x <= 1; x++) {
			for (int y = -7; y <= -5; y++) {
				r.set(x, y, 5, Cell.DOOR);
				r.custodianDoor.add(new int[] {x, y, 5});
			}
		}
		// Muro de la antesala con la cámara cerrado salvo la puerta; el suelo de la cámara sigue bajo ella.
		r.fill(Cell.FLOOR, -1, 1, UNDER_FLOOR_Y, UNDER_FLOOR_Y, 5, 5);

		// 7) Relicario (Vaso de vida) y linternas de la cámara del Custodio.
		r.set(r.reliquary[0], r.reliquary[1], r.reliquary[2], Cell.RELIQUARY);
		for (int[] c : new int[][] {{-10, 6}, {10, 6}, {-10, 14}, {10, 14}}) r.set(c[0], -7, c[1], Cell.LANTERN);

		// 8) T4: cuatro cámaras de recompensa. Cobre y diamante a los lados de la cámara del Custodio (3×7) y hierro y oro
		//    tras su muro norte (5×4). Cada puerta mide 1×2 y vive en un muro macizo; el cofre queda al fondo.
		//    {x0, x1, z0, z1, puerta x, puerta z, cofre x, cofre z, linterna x, linterna z}
		int[][] rooms = {
				{-14, -12, 7, 13, -11, 10, -13, 10, -14, 13},   // cobre
				{-10, -6, 1, 4, -8, 5, -8, 2, -10, 4},         // hierro
				{6, 10, 1, 4, 8, 5, 8, 2, 10, 4},              // oro
				{12, 14, 7, 13, 11, 10, 13, 10, 14, 13}};      // diamante
		for (int metal = 0; metal < rooms.length; metal++) {
			int[] k = rooms[metal];
			r.fill(Cell.AIR, k[0], k[1], -7, -2, k[2], k[3]);
			r.fill(metal == 3 ? Cell.ACCENT : Cell.FLOOR, k[0], k[1], UNDER_FLOOR_Y, UNDER_FLOOR_Y, k[2], k[3]);
			for (int y = -7; y <= -6; y++) {
				r.set(k[4], y, k[5], CHAMBER_DOORS[metal]);
				r.chamberDoors.add(new int[] {k[4], y, k[5], metal});
			}
			r.set(k[6], -7, k[7], CHESTS[metal]);
			r.chests.add(new int[] {k[6], -7, k[7], metal});
			r.set(k[8], -7, k[9], Cell.LANTERN);
		}
		return r;
	}

	/** Orden de placas barajado con una semilla (permutación de 0..n-1). */
	public static int[] shuffledPlateOrder(int plateCount, long seed) {
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < PLATE_SPOTS.length; i++) order.add(i);
		Collections.shuffle(order, new java.util.Random(seed));
		int[] out = new int[Math.max(0, plateCount)];
		for (int i = 0; i < out.length; i++) out[i] = order.get(i);
		return out;
	}
}
