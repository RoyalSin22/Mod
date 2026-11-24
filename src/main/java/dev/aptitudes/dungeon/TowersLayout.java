package dev.aptitudes.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * V86 · fase 1: distribución de «Las Tres Torres Señoriales», calculada sin Minecraft para poder probarla con JUnit
 * ({@code TowersLayoutTest}). Una {@code Feature} posterior traducirá cada {@link Cell} a un bloque real.
 *
 * <p>Coordenadas relativas al origen: {@code y = 0} es el suelo del patio, {@code z} positiva es el sur (la entrada) y todo
 * cabe en un cuadrado de radio {@link #R} (31×31), el máximo seguro para un {@code Feature}.</p>
 *
 * <pre>
 *        z -14 ┌──────────────┐
 *              │  TORRE DEL   │   13×13, 4 salas de combate (y 0..23) y sala del Coloso (y 25..33)
 *              │    SEÑOR     │   puerta sellada al sur (3×3) hasta vencer a los dos Guardianes
 *        z  -2 └──────────────┘
 *                  patio                     controlador en (0,0,0), entrada por el sur (0,1,14)
 *        z   3 ┌───────┐  ┌───────┐
 *              │VASALLA│  │VASALLA│          11×11, 3 salas de combate y sala del Guardián (y 19..25)
 *        z  13 └───────┘  └───────┘
 * </pre>
 *
 * <p>Cada planta mide {@link #FLOOR_H} bloques. Entre plantas hay una escalera recta de {@link #FLOOR_H} peldaños pegada a un
 * muro: en las plantas pares junto al muro norte y subiendo hacia el este; en las impares, junto al sur y subiendo hacia el
 * oeste. El forjado queda abierto sobre los peldaños que chocarían con la cabeza. Cada torre se remata con un parapeto en
 * voladizo con almenas y una empuñadura de espada gigante (guarda en cruz y pomo), guiño a las espadas hincadas de la
 * referencia del autor.</p>
 */
public final class TowersLayout {
	public static final int R = 15;
	public static final int SIZE = 2 * R + 1;
	/** Altura entre forjados. */
	public static final int FLOOR_H = 6;
	/** Fondo del cimiento bajo el patio. */
	public static final int BOTTOM_Y = -2;
	/** Cota máxima: la empuñadura de la Torre del Señor. */
	public static final int TOP_Y = 44;

	/** Tipos de torre. */
	public enum Role { VASSAL_WEST, VASSAL_EAST, LORD }

	/** Torre: centro, semianchura de los muros, forjado de la sala final y cota del techo. */
	public record Tower(Role role, int cx, int cz, int half, int bossFloorY, int roofY) {
		/** Plantas de combate (sin contar la sala del jefe). */
		public int combatLevels() {
			return bossFloorY / FLOOR_H;
		}
	}

	public static final Tower WEST = new Tower(Role.VASSAL_WEST, -8, 8, 5, 18, 26);
	public static final Tower EAST = new Tower(Role.VASSAL_EAST, 8, 8, 5, 18, 26);
	public static final Tower LORD = new Tower(Role.LORD, 0, -8, 6, 24, 34);
	public static final List<Tower> TOWERS = List.of(WEST, EAST, LORD);

	/** Qué va en cada celda. {@code null} = no se toca (fuera de la estructura). */
	public enum Cell {
		AIR, WALL, PLINTH, FLOOR, ROOF, CORBEL, MERLON, WINDOW, LANTERN,
		/** Peldaño que asciende hacia el este (+x). */
		STAIR_EAST,
		/** Peldaño que asciende hacia el oeste (−x). */
		STAIR_WEST,
		/** Puerta de la Torre del Señor: desaparece al vencer a los dos Guardianes. */
		SEALED,
		/** Empuñadura gigante que remata cada torre. */
		GRIP, GUARD, POMMEL,
		/** Bloque controlador, en el centro del patio. */
		CONTROLLER
	}

	/** Un punto de aparición de combate: torre, planta (0 = baja) y posición. */
	public record Spawn(Role role, int level, int x, int y, int z) {}

	/** Resultado: celdas y posiciones que necesita el controlador (todas relativas al origen). */
	public static final class Result {
		private final Cell[] cells = new Cell[SIZE * (TOP_Y - BOTTOM_Y + 1) * SIZE];
		/** Intentos de escribir fuera de la estructura (debe ser 0: lo comprueba una prueba). */
		public int outOfBounds;
		public final List<Spawn> spawns = new ArrayList<>();
		public final int[] controller = {0, 0, 0};
		public final int[] entrance = {0, 1, 14};
		public final int[] guardianWest = {WEST.cx(), WEST.bossFloorY() + 1, WEST.cz()};
		public final int[] guardianEast = {EAST.cx(), EAST.bossFloorY() + 1, EAST.cz()};
		public final int[] colossus = {LORD.cx(), LORD.bossFloorY() + 1, LORD.cz()};
		/** Donde el controlador coloca el cofre de la recompensa al vencer al Coloso (queda vacío antes). */
		public final int[] rewardChest = {LORD.cx(), LORD.bossFloorY() + 1, LORD.cz() - (LORD.half() - 1)};
		/** Celdas de la puerta sellada de la Torre del Señor como {x, y, z}. */
		public final List<int[]> sealed = new ArrayList<>();

		private static int index(int x, int y, int z) {
			return ((y - BOTTOM_Y) * SIZE + (z + R)) * SIZE + (x + R);
		}

		public static boolean inside(int x, int y, int z) {
			return Math.abs(x) <= R && Math.abs(z) <= R && y >= BOTTOM_Y && y <= TOP_Y;
		}

		public Cell get(int x, int y, int z) {
			return inside(x, y, z) ? cells[index(x, y, z)] : null;
		}

		void set(int x, int y, int z, Cell cell) {
			if (!inside(x, y, z)) {
				outOfBounds++;
				return;
			}
			cells[index(x, y, z)] = cell;
		}
	}

	private TowersLayout() {}

	private static Result cached;

	/** Layout compartido y de solo lectura (la entidad de bloque lo consulta en cada comprobación; construirlo cuesta ~45 000 celdas). */
	public static synchronized Result build0() {
		if (cached == null) cached = build();
		return cached;
	}

	public static Result build() {
		Result r = new Result();
		// Patio con cimiento: la Feature nivelará el terreno hasta aquí.
		for (int x = -14; x <= 14; x++) {
			for (int z = -14; z <= 14; z++) {
				r.set(x, 0, z, Cell.FLOOR);
				r.set(x, -1, z, Cell.PLINTH);
				r.set(x, BOTTOM_Y, z, Cell.PLINTH);
			}
		}
		r.set(0, 0, 0, Cell.CONTROLLER);
		for (Tower t : TOWERS) tower(r, t);
		return r;
	}

	// ------------------------------------------------------------------------------------------ Una torre

	private static void tower(Result r, Tower t) {
		int h = t.half();
		int cx = t.cx(), cz = t.cz();
		// Cuerpo hueco: muros, aire interior y forjados.
		for (int y = 1; y < t.roofY(); y++) {
			for (int dx = -h; dx <= h; dx++) {
				for (int dz = -h; dz <= h; dz++) {
					boolean ring = Math.abs(dx) == h || Math.abs(dz) == h;
					r.set(cx + dx, y, cz + dz, ring ? Cell.WALL : Cell.AIR);
				}
			}
		}
		for (int dx = -h; dx <= h; dx++) {
			for (int dz = -h; dz <= h; dz++) {
				r.set(cx + dx, -1, cz + dz, Cell.PLINTH);
				r.set(cx + dx, BOTTOM_Y, cz + dz, Cell.PLINTH);
				r.set(cx + dx, 0, cz + dz, Cell.FLOOR);
			}
		}
		for (int floor = FLOOR_H; floor <= t.bossFloorY(); floor += FLOOR_H) {
			for (int dx = -h + 1; dx <= h - 1; dx++) for (int dz = -h + 1; dz <= h - 1; dz++) r.set(cx + dx, floor, cz + dz, Cell.FLOOR);
		}
		// Escaleras: una por forjado, alternando muro norte (sube al este) y muro sur (sube al oeste).
		for (int level = 0, floor = 0; floor < t.bossFloorY(); level++, floor += FLOOR_H) stair(r, t, level, floor);
		// Techo, voladizo y parapeto.
		int ry = t.roofY();
		for (int dx = -h - 1; dx <= h + 1; dx++) {
			for (int dz = -h - 1; dz <= h + 1; dz++) {
				boolean over = Math.abs(dx) == h + 1 || Math.abs(dz) == h + 1;
				r.set(cx + dx, ry, cz + dz, over ? Cell.CORBEL : Cell.ROOF);
				if (over) {
					boolean merlon = ((dx - dz) & 1) == 0;
					r.set(cx + dx, ry + 1, cz + dz, merlon ? Cell.MERLON : Cell.AIR);
					r.set(cx + dx, ry + 2, cz + dz, merlon ? Cell.MERLON : Cell.AIR);
				}
			}
		}
		finial(r, t);
		openings(r, t);
		spawns(r, t);
	}

	/** Escalera de la planta {@code level} (suelo en {@code floor}) a la siguiente. */
	private static void stair(Result r, Tower t, int level, int floor) {
		int h = t.half();
		int cx = t.cx(), cz = t.cz();
		boolean east = level % 2 == 0;
		int z = east ? cz - (h - 1) : cz + (h - 1);
		int start = east ? cx - (h - 1) : cx + (h - 1);
		int dir = east ? 1 : -1;
		for (int i = 1; i <= FLOOR_H; i++) {
			r.set(start + dir * i, floor + i, z, east ? Cell.STAIR_EAST : Cell.STAIR_WEST);
		}
		// El forjado sobre los peldaños 3..5 queda abierto: sin él la cabeza chocaría. El 6.º peldaño ocupa la fila del forjado.
		int slab = floor + FLOOR_H;
		for (int i = 3; i <= 5; i++) r.set(start + dir * i, slab, z, Cell.AIR);
		r.set(start + dir * FLOOR_H, slab, z, east ? Cell.STAIR_EAST : Cell.STAIR_WEST);
	}

	/** Puerta, ventanas y faroles. */
	private static void openings(Result r, Tower t) {
		int h = t.half();
		int cx = t.cx(), cz = t.cz();
		// Puerta al sur, 3 de ancho × 3 de alto. La del Señor está sellada.
		for (int dx = -1; dx <= 1; dx++) {
			for (int y = 1; y <= 3; y++) {
				if (t.role() == Role.LORD) {
					r.set(cx + dx, y, cz + h, Cell.SEALED);
					r.sealed.add(new int[] {cx + dx, y, cz + h});
				} else {
					r.set(cx + dx, y, cz + h, Cell.AIR);
				}
			}
		}
		for (int floor = 0; floor <= t.bossFloorY(); floor += FLOOR_H) {
			int wy = floor + 3;
			for (int off : new int[] {-3, 3}) {
				for (int dy = 0; dy < 2; dy++) {
					r.set(cx + off, wy + dy, cz - h, Cell.WINDOW);
					r.set(cx - h, wy + dy, cz + off, Cell.WINDOW);
					r.set(cx + h, wy + dy, cz + off, Cell.WINDOW);
					if (floor > 0) r.set(cx + off, wy + dy, cz + h, Cell.WINDOW); // en la planta baja la puerta manda
				}
			}
			r.set(cx - (h - 1), floor + 4, cz, Cell.LANTERN);
			r.set(cx + (h - 1), floor + 4, cz, Cell.LANTERN);
		}
	}

	/** Empuñadura gigante: pomo, empuñadura y guarda en cruz. */
	private static void finial(Result r, Tower t) {
		int y0 = t.roofY();
		for (int y = y0 + 1; y <= y0 + 7; y++) r.set(t.cx(), y, t.cz(), Cell.GRIP);
		for (int dx = -3; dx <= 3; dx++) if (dx != 0) r.set(t.cx() + dx, y0 + 6, t.cz(), Cell.GUARD);
		r.set(t.cx(), y0 + 8, t.cz(), Cell.POMMEL);
	}

	/** Cuatro puntos de aparición por planta de combate, alejados de las escaleras. */
	private static void spawns(Result r, Tower t) {
		int off = t.half() - 3;
		for (int level = 0; level < t.combatLevels(); level++) {
			for (int sx : new int[] {-off, off}) {
				for (int sz : new int[] {-off, off}) {
					r.spawns.add(new Spawn(t.role(), level, t.cx() + sx, level * FLOOR_H + 1, t.cz() + sz));
				}
			}
		}
	}

	/** Torre a la que pertenece la columna {@code (x, z)} (muros incluidos), o {@code null} si es patio. */
	public static Tower towerAt(int x, int z) {
		for (Tower t : TOWERS) if (Math.abs(x - t.cx()) <= t.half() && Math.abs(z - t.cz()) <= t.half()) return t;
		return null;
	}

	/** Lista inmodificable de las celdas selladas, para quien solo quiera leerlas. */
	public static List<int[]> sealedCells(Result r) {
		return Collections.unmodifiableList(r.sealed);
	}
}
