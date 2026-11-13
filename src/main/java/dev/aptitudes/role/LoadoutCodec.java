package dev.aptitudes.role;

/**
 * Codificación de las 2 activas equipadas de un rol en un entero (guardado y red).
 *
 * <p>Hasta la v22 cada ranura usaba 2 bits (índices 0-3). En la v23 pasó a 3 bits por ranura (8 activas) con un
 * <b>bit de formato</b> ({@link #V3}). Desde la v67 (habilidades nuevas N1–N10) se escriben 4 bits por ranura con otro
 * bit de formato ({@link #V4}): hasta 16 activas por rol. Los valores guardados con los formatos anteriores se siguen
 * leyendo exactamente igual. Clase pura y probada.</p>
 */
public final class LoadoutCodec {
	/** Marca del formato de 3 bits por ranura (v23). */
	static final int V3 = 1 << 8;
	/** Marca del formato de 4 bits por ranura (v67). */
	static final int V4 = 1 << 9;
	/** Índice máximo de activa (16 activas: 0-15). */
	public static final int MAX_INDEX = 15;

	private LoadoutCodec() {
	}

	public static int encode(int first, int second) {
		return V4 | (clamp(first) & 15) | ((clamp(second) & 15) << 4);
	}

	/** @param which 0 = primera ranura, 1 = segunda */
	public static int decode(int packed, int which) {
		if ((packed & V4) != 0) return which == 0 ? packed & 15 : (packed >> 4) & 15;
		if ((packed & V3) != 0) return which == 0 ? packed & 7 : (packed >> 3) & 7; // formato de la v23
		return which == 0 ? packed & 3 : (packed >> 2) & 3; // formato antiguo (v22 y anteriores)
	}

	private static int clamp(int index) {
		return Math.max(0, Math.min(MAX_INDEX, index));
	}
}
