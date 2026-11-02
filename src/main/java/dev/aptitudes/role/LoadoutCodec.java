package dev.aptitudes.role;

/**
 * Codificación de las 2 activas equipadas de un rol en un entero (guardado y red).
 *
 * <p>Hasta la v22 cada ranura usaba 2 bits (índices 0-3). Desde la v23 hay 8 activas por rol, así que se
 * usan 3 bits por ranura y un <b>bit de formato</b> ({@link #V3}) que distingue el formato nuevo: un valor
 * guardado con el formato antiguo (sin ese bit) se sigue leyendo exactamente igual. Clase pura y probada.</p>
 */
public final class LoadoutCodec {
	/** Marca del formato de 3 bits por ranura. */
	static final int V3 = 1 << 8;
	/** Índice máximo de activa (8 activas: 0-7). */
	public static final int MAX_INDEX = 7;

	private LoadoutCodec() {
	}

	public static int encode(int first, int second) {
		return V3 | (clamp(first) & 7) | ((clamp(second) & 7) << 3);
	}

	/** @param which 0 = primera ranura, 1 = segunda */
	public static int decode(int packed, int which) {
		if ((packed & V3) != 0) return which == 0 ? packed & 7 : (packed >> 3) & 7;
		return which == 0 ? packed & 3 : (packed >> 2) & 3; // formato antiguo (v22 y anteriores)
	}

	private static int clamp(int index) {
		return Math.max(0, Math.min(MAX_INDEX, index));
	}
}
