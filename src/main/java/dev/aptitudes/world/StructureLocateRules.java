package dev.aptitudes.world;

import java.util.List;
import java.util.Optional;

/** V77: reglas puras para localizar las estructuras del mod (que son {@code Feature}s y no aparecen en {@code /locate}). */
public final class StructureLocateRules {
	private StructureLocateRules() {
	}

	/** Tipos de estructura del mod, con el id que usan los comandos y el índice. */
	public static final List<String> KINDS = List.of("custodian_temple", "tower", "bonfire_shrine", "camp",
			"three_towers");

	/**
	 * Estructuras con colocación por cuadrícula de regiones (en lugar de «un chunk de cada N»): en cada región de {@code size}×{@code size}
	 * chunks solo un chunk, el «designado», intenta generarla. Así se sabe dónde mirar sin generar todo el terreno. 0 = sin cuadrícula.
	 */
	public static int regionSize(String kind) {
		// V86: Las Tres Torres Señoriales: región de 24×24 chunks, más frecuentes que el Templo del Custodio (32×32): por decisión del
		// autor el Custodio es aún más raro que las torres.
		if ("three_towers".equals(kind)) return 24;
		return "custodian_temple".equals(kind) ? 32 : 0;
	}

	/** Chunk designado de la región ({@code rx}, {@code rz}): siempre dentro de ella, determinista con la semilla del mundo. */
	public static int[] designatedChunk(long seed, String kind, int rx, int rz, int size) {
		int s = Math.max(1, size);
		java.util.SplittableRandom random = new java.util.SplittableRandom(seed * 341873128712L + rx * 132897987541L + rz * 42317861L + kind.hashCode() * 31L);
		// La fortaleza ocupa 8×8 chunks; un margen de cuatro evita que alcance la región vecina.
		if ("three_towers".equals(kind) && s >= 12) return new int[] {rx * s + 4 + random.nextInt(s - 8), rz * s + 4 + random.nextInt(s - 8)};
		return new int[] {rx * s + random.nextInt(s), rz * s + random.nextInt(s)};
	}

	/** Origen en bloques de la fortaleza que cubre el chunk, o null si no pertenece a su cuadrícula. */
	public static int[] fortressOrigin(long seed, int chunkX, int chunkZ) {
		int size = regionSize("three_towers");
		int[] designated = designatedChunk(seed, "three_towers", regionOf(chunkX, size), regionOf(chunkZ, size), size);
		if (chunkX < designated[0] - 4 || chunkX > designated[0] + 3
				|| chunkZ < designated[1] - 4 || chunkZ > designated[1] + 3) return null;
		return new int[] {designated[0] << 4, designated[1] << 4};
	}

	/** Región (índice) a la que pertenece un chunk. */
	public static int regionOf(int chunk, int size) {
		return Math.floorDiv(chunk, Math.max(1, size));
	}

	/** ¿Es este chunk el designado de su región? */
	public static boolean isDesignated(long seed, String kind, int chunkX, int chunkZ, int size) {
		int[] d = designatedChunk(seed, kind, regionOf(chunkX, size), regionOf(chunkZ, size), size);
		return d[0] == chunkX && d[1] == chunkZ;
	}

	/** Todas las estructuras del mod se pueden buscar explorando. */
	public static boolean searchable(String kind) {
		return isKind(kind);
	}

	public static boolean isKind(String kind) {
		return kind != null && KINDS.contains(kind);
	}

	/** Posición más cercana en horizontal, con su distancia. */
	public record Hit(long pos, int x, int z, int distance) {
	}

	/**
	 * La posición más cercana (distancia horizontal) a {@code (x, z)}. Las posiciones son {@code BlockPos.asLong()}: se decodifican
	 * aquí con la misma disposición (x en 26 bits, z en 26 y y en 12) para que la clase no dependa de Minecraft.
	 */
	public static Optional<Hit> nearest(long[] positions, int x, int z) {
		Hit best = null;
		long bestSq = Long.MAX_VALUE;
		for (long p : positions) {
			int px = (int) (p >> 38);
			int pz = (int) (p << 26 >> 38);
			long dx = (long) px - x, dz = (long) pz - z;
			long sq = dx * dx + dz * dz;
			if (sq < bestSq) {
				bestSq = sq;
				best = new Hit(p, px, pz, (int) Math.min(Integer.MAX_VALUE, Math.round(Math.sqrt((double) sq))));
			}
		}
		return Optional.ofNullable(best);
	}

	/** Codifica x, y, z con la disposición de {@code BlockPos.asLong()} (para las pruebas y para quien no tenga BlockPos). */
	public static long pack(int x, int y, int z) {
		return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFFL);
	}

	/** Chunks del anillo cuadrado de radio {@code r} (en chunks) alrededor de (0, 0), como pares {dx, dz}; r = 0 es el propio chunk. */
	public static int[][] ring(int r) {
		if (r <= 0) return new int[][] {{0, 0}};
		int[][] out = new int[8 * r][];
		int i = 0;
		for (int d = -r; d <= r; d++) {
			out[i++] = new int[] {d, -r};
			out[i++] = new int[] {d, r};
		}
		for (int d = -r + 1; d <= r - 1; d++) {
			out[i++] = new int[] {-r, d};
			out[i++] = new int[] {r, d};
		}
		return out;
	}

	/** ¿Basta lo ya conocido o hay que buscar en terreno sin explorar? Se busca si no hay nada o lo conocido está lejos. */
	public static boolean shouldSearch(java.util.Optional<Hit> known, int blocksIfFar) {
		return known.isEmpty() || known.get().distance() > blocksIfFar;
	}

	/** Intentos de colocación de {@code /aptitudes place}: desplazamientos (x, z) en anillo alrededor del jugador, de cerca a lejos. */
	public static int[][] placementOffsets(int attempts, int minRadius, int maxRadius, long seed) {
		java.util.Random random = new java.util.Random(seed);
		int n = Math.max(1, Math.min(200, attempts));
		int lo = Math.max(0, Math.min(minRadius, maxRadius)), hi = Math.max(lo + 1, maxRadius);
		int[][] out = new int[n][2];
		for (int i = 0; i < n; i++) {
			double angle = random.nextDouble() * Math.PI * 2;
			double radius = lo + (hi - lo) * (i + 1) / (double) n;
			out[i][0] = (int) Math.round(Math.cos(angle) * radius);
			out[i][1] = (int) Math.round(Math.sin(angle) * radius);
		}
		return out;
	}
}
