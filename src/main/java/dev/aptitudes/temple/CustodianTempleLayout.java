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
	/** V69: la Cripta de la Niebla vive bajo la cámara del Custodio. Fondo de su casco, suelo y techo de la sala. */
	public static final int CRYPT_BOTTOM_Y = -19;
	public static final int CRYPT_FLOOR_Y = -17;
	public static final int CRYPT_CEILING_Y = -10;

	/**
	 * V70: Templo hondo (layout 3 y 4). Todo bajo tierra: un foso con rampa en el sur, el Piso 1 (atrio del Autómata), el Piso 2
	 * (sala de pruebas) y el Piso 3 (antesala, Puerta y cámara del Custodio, desplazado {@link #DEEP_DY} respecto al templo
	 * antiguo). La Cripta de la Niebla es un cuarto nivel opcional.
	 */
	public static final int DEEP_F1_FLOOR_Y = -10;
	public static final int DEEP_F2_FLOOR_Y = -17;
	public static final int DEEP_DY = -17;
	public static final int DEEP_BOTTOM_Y = HULL_BOTTOM_Y + DEEP_DY;
	public static final int DEEP_CRYPT_BOTTOM_Y = CRYPT_BOTTOM_Y + DEEP_DY;
	public static final int DEEP_CRYPT_CEILING_Y = CRYPT_CEILING_Y + DEEP_DY;

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
		CHEST_COPPER, CHEST_IRON, CHEST_GOLD, CHEST_DIAMOND,
		/** V69: trampilla de la cripta (3×3 en el suelo de la cámara del Custodio): solo cede a la Llave del Velo. */
		CRYPT_HATCH,
		/** V69: escalera de mano del pozo de la cripta, pegada a un pilar al oeste (mira al este). */
		LADDER_EAST,
		/** V70: tierra y césped de la cobertura del templo hondo. */
		SOIL, GRASS,
		/** V70: escalón que asciende hacia el sur (se baja hacia el norte): la rampa del foso y las escaleras del templo hondo. */
		STAIR_SOUTH,
		/** V70: brasero de fuego de almas. */
		BRAZIER
	}

	/** Celdas de puerta y de cofre por metal, en el orden de {@link TempleKeyRules.Metal}. */
	public static final Cell[] CHAMBER_DOORS = {Cell.CHAMBER_DOOR_COPPER, Cell.CHAMBER_DOOR_IRON, Cell.CHAMBER_DOOR_GOLD, Cell.CHAMBER_DOOR_DIAMOND};
	public static final Cell[] CHESTS = {Cell.CHEST_COPPER, Cell.CHEST_IRON, Cell.CHEST_GOLD, Cell.CHEST_DIAMOND};

	/** Resultado: las celdas y las posiciones que necesita el controlador (todas relativas al origen). */
	public static final class Result {
		private final int bottomY;
		private final Cell[] cells;
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
		/** V69: celdas de la trampilla de la cripta como {x, y, z} (vacío si el plano no tiene cripta). */
		public final List<int[]> cryptHatch = new ArrayList<>();
		/** V69: puntos de aparición de las oleadas dentro de la sala de la cripta. */
		public final List<int[]> cryptSpawns = new ArrayList<>();
		/** V69: dónde aparece el campeón del clímax y dónde emerge el cofre final. */
		public final int[] cryptClimax = {6, -16, 10};
		public final int[] cryptChest = {0, -16, 7};
		/** V69: esquinas de la sala (mínima y máxima, inclusivas) para saber qué jugadores están dentro. */
		public final int[] cryptMin = {-10, -16, 6};
		public final int[] cryptMax = {10, -11, 14};
		/** V70: piedra sellada de la escalera del Piso 1 al 2 (cae al vencer al Autómata). */
		public final List<int[]> gateOne = new ArrayList<>();
		/** V70: puntos de aparición y esquinas (mínima y máxima, inclusivas) de la sala de pruebas del Piso 2. */
		public final List<int[]> trialSpawns = new ArrayList<>();
		public final int[] trialMin = {0, 0, 0};
		public final int[] trialMax = {0, 0, 0};

		Result() {
			this(HULL_BOTTOM_Y);
		}

		Result(int bottomY) {
			this.bottomY = bottomY;
			this.cells = new Cell[SIZE * (ROOF_Y - bottomY + 1) * SIZE];
		}

		/** ¿Tiene este plano la Cripta de la Niebla? */
		public boolean hasCrypt() {
			return !cryptHatch.isEmpty();
		}

		/** Fondo (y relativa mínima) de este plano. */
		public int bottomY() {
			return bottomY;
		}

		private int index(int x, int y, int z) {
			return ((y - bottomY) * SIZE + (z + R)) * SIZE + (x + R);
		}

		/** ¿Cae la posición dentro de este plano (con su fondo)? */
		private boolean contains(int x, int y, int z) {
			return Math.abs(x) <= R && Math.abs(z) <= R && y >= bottomY && y <= ROOF_Y;
		}

		public static boolean inside(int x, int y, int z) {
			return Math.abs(x) <= R && Math.abs(z) <= R && y >= HULL_BOTTOM_Y && y <= ROOF_Y;
		}

		/** Celda en esa posición, o {@code null} si es exterior o no se toca. */
		public Cell get(int x, int y, int z) {
			return contains(x, y, z) ? cells[index(x, y, z)] : null;
		}

		void set(int x, int y, int z, Cell cell) {
			if (!contains(x, y, z)) throw new IllegalArgumentException("fuera de la estructura: " + x + "," + y + "," + z);
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
		return build(target, plateCount, plateOrder, false);
	}

	/**
	 * Igual que {@link #build(boolean[], int, int[])}; con {@code crypt} añade la Cripta de la Niebla bajo la cámara del
	 * Custodio (V69). Sin ella el plano es idéntico al de siempre ({@code layout = 1}).
	 */
	public static Result build(boolean[] target, int plateCount, int[] plateOrder, boolean crypt) {
		if (target == null || target.length < TempleRules.MIN_LEVERS || target.length > TempleRules.MAX_LEVERS) {
			throw new IllegalArgumentException("palancas: " + (target == null ? "null" : target.length));
		}
		if (plateCount < 0 || plateCount > TempleRules.MAX_PLATES || plateOrder == null || plateOrder.length < plateCount) {
			throw new IllegalArgumentException("placas: " + plateCount);
		}
		Result r = new Result(crypt ? CRYPT_BOTTOM_Y : HULL_BOTTOM_Y);

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

		buildUnderground(r, 0);
		if (crypt) buildCrypt(r, 0);
		return r;
	}

	/**
	 * V70: Templo hondo, totalmente subterráneo (layout 3, y 4 con cripta).
	 *
	 * <pre>
	 * Superficie  foso (patio x -7..7, z -2..5) y rampa de 5 de ancho (z 6..15) que baja al patio; solo sobresalen braseros y ruinas.
	 * Piso 1      atrio del Autómata (x ±13, z -13..-4, suelo y -10); la escalera al Piso 2 (x 11..13) está sellada hasta vencerlo.
	 * Piso 2      sala de pruebas (mismo hueco, suelo y -17): dos oleadas; la escalera al Piso 3 (x -1..1) está sellada hasta superarlas.
	 * Piso 3      antesala, Puerta del Custodio, cámara del Custodio y cuatro cámaras de recompensa (el subsuelo del templo antiguo,
	 *             desplazado {@link #DEEP_DY}).
	 * Cripta      cuarto nivel opcional bajo la cámara del Custodio (Llave del Velo).
	 * </pre>
	 */
	public static Result buildDeep(boolean crypt) {
		Result r = new Result(crypt ? DEEP_CRYPT_BOTTOM_Y : DEEP_BOTTOM_Y);
		final int f1 = DEEP_F1_FLOOR_Y, f2 = DEEP_F2_FLOOR_Y;

		// 1) Casco macizo hasta el techo del Piso 1 y cobertura de tierra y césped: ninguna cueva puede abrir el templo.
		r.fill(Cell.HULL, -R, R, r.bottomY, -3, -R, R);
		r.fill(Cell.SOIL, -R, R, -2, -2, -R, R);
		r.fill(Cell.GRASS, -R, R, -1, -1, -R, R);

		// 2) Foso: patio al fondo, con rampa (escalones que suben hacia el sur) hasta la superficie.
		r.fill(Cell.AIR, -7, 7, f1 + 1, ROOF_Y, -2, 5);
		r.fill(Cell.FLOOR, -7, 7, f1, f1, -2, 5);
		r.fill(Cell.ACCENT, -1, 1, f1, f1, -2, 5);
		for (int j = 0; j < 10; j++) {
			int z = 6 + j, y = f1 + j;
			r.fill(Cell.AIR, -2, 2, y + 1, ROOF_Y, z, z);
			for (int x = -2; x <= 2; x++) r.set(x, y, z, Cell.STAIR_SOUTH);
		}
		for (int[] p : new int[][] {{-6, -1, 7}, {6, -1, 7}, {-6, 4, 3}, {6, 4, 3}}) r.fill(Cell.PILLAR, p[0], p[0], f1 + 1, f1 + p[2], p[1], p[1]);
		for (int[] p : new int[][] {{-3, 8}, {3, 8}, {-3, 13}, {3, 13}}) r.set(p[0], 0, p[1], Cell.BRAZIER);
		for (int[] p : new int[][] {{-5, 1}, {5, 1}}) r.set(p[0], f1 + 1, p[1], Cell.BRAZIER);

		// 3) Piso 1: atrio del Autómata. Arco de entrada en el muro norte del patio.
		r.fill(Cell.AIR, -13, 13, f1 + 1, f1 + 6, -13, -4);
		r.fill(Cell.FLOOR, -13, 13, f1, f1, -13, -4);
		r.fill(Cell.ROOF, -13, 13, f1 + 7, f1 + 7, -13, -4);
		r.fill(Cell.ACCENT, -1, 1, f1, f1, -13, -4);
		r.fill(Cell.AIR, -2, 2, f1 + 1, f1 + 4, -3, -3);
		r.fill(Cell.FLOOR, -2, 2, f1, f1, -3, -3);
		r.fill(Cell.ACCENT, -3, -3, f1 + 1, f1 + 5, -3, -3);
		r.fill(Cell.ACCENT, 3, 3, f1 + 1, f1 + 5, -3, -3);
		r.fill(Cell.ACCENT, -2, 2, f1 + 5, f1 + 5, -3, -3);
		for (int sx : new int[] {-1, 1}) {
			for (int[] p : new int[][] {{4, -6}, {4, -11}, {9, -6}, {9, -11}}) {
				r.fill(Cell.PILLAR, sx * p[0], sx * p[0], f1 + 1, f1 + 6, p[1], p[1]);
				r.set(sx * p[0], f1 + 7, p[1], Cell.LIGHT_ON);
			}
			r.set(sx * 12, f1 + 1, -13, Cell.BRAZIER);
			r.set(sx * 12, f1 + 1, -4, Cell.BRAZIER);
		}
		for (int x = 11; x <= 13; x++) {
			for (int z = -7; z <= -5; z++) {
				r.set(x, f1, z, Cell.SEALED);
				r.gateOne.add(new int[] {x, f1, z});
			}
		}
		System.arraycopy(new int[] {3, f1 + 1, -4}, 0, r.seal, 0, 3);
		r.set(r.seal[0], r.seal[1], r.seal[2], Cell.SEAL);
		System.arraycopy(new int[] {0, f1 + 1, -9}, 0, r.automatonSpawn, 0, 3);
		System.arraycopy(new int[] {0, f1 + 1, -9}, 0, r.triggerCenter, 0, 3);

		// 4) Piso 2: sala de pruebas bajo el atrio. El suelo del Piso 1 es su techo.
		r.fill(Cell.AIR, -13, 13, f2 + 1, f1 - 1, -13, -4);
		r.fill(Cell.FLOOR, -13, 13, f2, f2, -13, -4);
		for (int x = -6; x <= 6; x++) for (int z : new int[] {-12, -5}) r.set(x, f2, z, Cell.ACCENT);
		for (int z = -12; z <= -5; z++) for (int x : new int[] {-6, 6}) r.set(x, f2, z, Cell.ACCENT);
		for (int sx : new int[] {-1, 1}) {
			for (int[] p : new int[][] {{4, -6}, {4, -11}, {9, -6}, {9, -11}}) {
				r.fill(Cell.PILLAR, sx * p[0], sx * p[0], f2 + 1, f1 - 1, p[1], p[1]);
				r.set(sx * p[0], f1, p[1], Cell.LIGHT_ON);
			}
			r.set(sx * 12, f2 + 1, -13, Cell.BRAZIER);
			r.set(sx * 12, f2 + 1, -4, Cell.BRAZIER);
		}
		for (int x : new int[] {-9, -5, 5, 9}) {
			r.fill(Cell.AIR, x, x, f2 + 1, f2 + 3, -14, -14);
			r.set(x, f2 + 1, -14, Cell.LANTERN);
		}
		// Escalera sellada al Piso 2 (este del atrio, ya dentro del hueco del Piso 2): 7 escalones hacia el norte; los tres primeros bajo la piedra sellada.
		for (int i = 0; i < 7; i++) {
			for (int x = 11; x <= 13; x++) r.set(x, f1 - 1 - i, -5 - i, Cell.STAIR_SOUTH);
		}
		// Escalera sellada al Piso 3 (centro): su tramo hacia el sur sigue el subsuelo del templo antiguo, 17 más abajo.
		for (int x = -1; x <= 1; x++) {
			for (int z = -7; z <= -5; z++) {
				r.set(x, f2, z, Cell.SEALED);
				r.hatch.add(new int[] {x, f2, z});
			}
		}
		for (int[] p : new int[][] {{-11, -12}, {-11, -6}, {-6, -9}, {6, -9}, {0, -12}, {9, -8}, {-3, -12}, {3, -12}}) {
			r.trialSpawns.add(new int[] {p[0], f2 + 1, p[1]});
		}
		System.arraycopy(new int[] {-13, f2 + 1, -13}, 0, r.trialMin, 0, 3);
		System.arraycopy(new int[] {13, f1 - 1, -4}, 0, r.trialMax, 0, 3);

		// 5) Piso 3: subsuelo del templo antiguo (escalera, antesala, Puerta, cámara y cuatro cámaras) más abajo.
		buildUnderground(r, DEEP_DY);
		for (int sx : new int[] {-1, 1}) {
			for (int z : new int[] {8, 12}) r.fill(Cell.PILLAR, sx * 5, sx * 5, UNDER_FLOOR_Y + DEEP_DY + 1, -2 + DEEP_DY, z, z);
			for (int z : new int[] {7, 13}) r.set(sx * 8, UNDER_FLOOR_Y + DEEP_DY + 1, z, Cell.BRAZIER);
		}
		if (crypt) buildCrypt(r, DEEP_DY);
		return r;
	}

	/**
	 * Escalera de bajada, antesala, Puerta del Custodio, cámara del Custodio, Relicario y cuatro cámaras de recompensa.
	 * {@code dy} desplaza todo en vertical: 0 para el templo antiguo (layout 1 y 2) y {@link #DEEP_DY} para el hondo.
	 */
	private static void buildUnderground(Result r, int dy) {
		int floorY = UNDER_FLOOR_Y + dy;
		// 5) Escalera: desciende hacia el sur bajo el suelo de la sala (8 escalones).
		for (int i = 0; i < 8; i++) {
			int z = -7 + i;
			for (int x = -1; x <= 1; x++) {
				r.set(x, -1 - i + dy, z, Cell.STAIR_NORTH);
				if (i >= 1) r.fill(Cell.AIR, x, x, -i + dy, -1 + dy, z, z);
			}
		}

		// 6) Antesala y cámara del Custodio.
		r.fill(Cell.AIR, -4, 4, -7 + dy, -2 + dy, 0, 4);
		r.fill(Cell.FLOOR, -4, 4, floorY, floorY, 0, 4);
		r.fill(Cell.AIR, -10, 10, -7 + dy, -2 + dy, 6, 14);
		r.fill(Cell.FLOOR, -10, 10, floorY, floorY, 6, 14);
		r.fill(Cell.ACCENT, -1, 1, floorY, floorY, 6, 14);
		// Escalón final: sustituye al suelo de la antesala en la boca de la escalera.
		for (int x = -1; x <= 1; x++) r.set(x, floorY, 0, Cell.STAIR_NORTH);
		// Puerta del Custodio (3×3) en el muro entre la antesala y la cámara.
		for (int x = -1; x <= 1; x++) {
			for (int y = -7; y <= -5; y++) {
				r.set(x, y + dy, 5, Cell.DOOR);
				r.custodianDoor.add(new int[] {x, y + dy, 5});
			}
		}
		// Muro de la antesala con la cámara cerrado salvo la puerta; el suelo de la cámara sigue bajo ella.
		r.fill(Cell.FLOOR, -1, 1, floorY, floorY, 5, 5);

		// 7) Relicario (Vaso de vida) y linternas de la cámara del Custodio.
		r.custodianSpawn[1] += dy;
		r.reliquary[1] += dy;
		r.set(r.reliquary[0], r.reliquary[1], r.reliquary[2], Cell.RELIQUARY);
		for (int[] c : new int[][] {{-10, 6}, {10, 6}, {-10, 14}, {10, 14}}) r.set(c[0], -7 + dy, c[1], Cell.LANTERN);

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
			r.fill(Cell.AIR, k[0], k[1], -7 + dy, -2 + dy, k[2], k[3]);
			r.fill(metal == 3 ? Cell.ACCENT : Cell.FLOOR, k[0], k[1], floorY, floorY, k[2], k[3]);
			for (int y = -7; y <= -6; y++) {
				r.set(k[4], y + dy, k[5], CHAMBER_DOORS[metal]);
				r.chamberDoors.add(new int[] {k[4], y + dy, k[5], metal});
			}
			r.set(k[6], -7 + dy, k[7], CHESTS[metal]);
			r.chests.add(new int[] {k[6], -7 + dy, k[7], metal});
			r.set(k[8], -7 + dy, k[9], Cell.LANTERN);
		}
	}

	/**
	 * V69: Cripta de la Niebla. Sala de 21×6 (x −10…10, z 6…14, y −16…−11) con suelo en y −17, bajo la cámara del Custodio y
	 * envuelta por un casco de piedra (x ±11, z 5…15, y −19…−10). Un pozo de 3×3 sube hasta la trampilla del suelo de la
	 * cámara (x −1…1, z 10…12) con una escalera de mano pegada a un pilar al oeste.
	 */
	private static void buildCrypt(Result r, int dy) {
		int bottom = CRYPT_BOTTOM_Y + dy, floor = CRYPT_FLOOR_Y + dy, ceiling = CRYPT_CEILING_Y + dy;
		int chamberFloor = UNDER_FLOOR_Y + dy, hullBottom = HULL_BOTTOM_Y + dy;
		r.fill(Cell.HULL, -11, 11, bottom, ceiling, 5, R);
		r.fill(Cell.AIR, -10, 10, floor + 1, -11 + dy, 6, 14);
		r.fill(Cell.FLOOR, -10, 10, floor, floor, 6, 14);
		r.fill(Cell.ACCENT, -1, 1, floor, floor, 10, 12);
		// Pozo bajo la trampilla: corta las dos capas de casco entre la cámara y el techo de la sala.
		r.fill(Cell.AIR, -1, 1, ceiling, hullBottom, 10, 12);
		for (int x = -1; x <= 1; x++) {
			for (int z = 10; z <= 12; z++) {
				r.set(x, chamberFloor, z, Cell.CRYPT_HATCH);
				r.cryptHatch.add(new int[] {x, chamberFloor, z});
			}
		}
		// Pilar y escalera de mano: del suelo de la sala hasta la trampilla.
		r.fill(Cell.WALL, -2, -2, floor + 1, -11 + dy, 11, 11);
		r.fill(Cell.LADDER_EAST, -1, -1, floor + 1, hullBottom, 11, 11);
		for (int[] c : new int[][] {{-10, 6}, {10, 6}, {-10, 14}, {10, 14}}) r.set(c[0], floor + 1, c[1], Cell.LANTERN);
		for (int[] p : new int[][] {{-8, 8}, {8, 8}, {-8, 12}, {8, 12}, {-4, 7}, {4, 7}, {-4, 13}, {4, 13}}) {
			r.cryptSpawns.add(new int[] {p[0], floor + 1, p[1]});
		}
		r.cryptClimax[1] += dy;
		r.cryptChest[1] += dy;
		r.cryptMin[1] += dy;
		r.cryptMax[1] += dy;
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
