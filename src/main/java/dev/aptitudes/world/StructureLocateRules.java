package dev.aptitudes.world;

import java.util.List;
import java.util.Optional;

/** V77: reglas puras para localizar las estructuras del mod (que son {@code Feature}s y no aparecen en {@code /locate}). */
public final class StructureLocateRules {
	private StructureLocateRules() {
	}

	/** Tipos de estructura del mod, con el id que usan los comandos y el índice. */
	public static final List<String> KINDS = List.of("custodian_temple", "temple", "tower", "bonfire_shrine", "camp");

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
