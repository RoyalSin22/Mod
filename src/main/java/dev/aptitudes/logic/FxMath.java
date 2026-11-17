package dev.aptitudes.logic;

import java.util.random.RandomGenerator;

/**
 * V77 (efectos de batalla): matemáticas puras de los efectos visuales (curvas de cámara, atenuación por distancia,
 * ruido suave y geometría de ráfagas). Sin Minecraft, para poder probarlas.
 */
public final class FxMath {
	private FxMath() {
	}

	/**
	 * Envolvente 0–1: sube en {@code attackMs}, se mantiene {@code holdMs} y cae en {@code decayMs} con una curva
	 * cuadrática suave (rápida al principio, lenta al final). Fuera del intervalo vale 0.
	 */
	public static float envelope(long ageMs, long attackMs, long holdMs, long decayMs) {
		if (ageMs < 0) return 0f;
		if (ageMs < attackMs) return attackMs <= 0 ? 1f : easeOut(ageMs / (float) attackMs);
		long afterAttack = ageMs - attackMs;
		if (afterAttack < holdMs) return 1f;
		long decaying = afterAttack - holdMs;
		if (decaying >= decayMs) return 0f;
		float t = decaying / (float) decayMs;
		return (1f - t) * (1f - t);
	}

	/** Duración total de {@link #envelope}. */
	public static long envelopeLength(long attackMs, long holdMs, long decayMs) {
		return Math.max(0, attackMs) + Math.max(0, holdMs) + Math.max(0, decayMs);
	}

	/** Frena al final: 0 → 0, 1 → 1, con pendiente máxima al inicio. */
	public static float easeOut(float t) {
		float c = Math.max(0f, Math.min(1f, t));
		return 1f - (1f - c) * (1f - c);
	}

	/** Intensidad según la distancia: 1 en el centro y 0 a {@code radius} o más (curva suave). */
	public static float falloff(double distance, double radius) {
		if (radius <= 0 || distance >= radius) return 0f;
		if (distance <= 0) return 1f;
		double t = 1.0 - distance / radius;
		return (float) (t * t * (3 - 2 * t));
	}

	/** Radio de una onda expansiva en el tick {@code tick} de {@code totalTicks}: sale rápido y se frena. */
	public static double ringRadius(int tick, int totalTicks, double maxRadius) {
		if (totalTicks <= 0) return maxRadius;
		return maxRadius * easeOut(tick / (float) totalTicks);
	}

	/** Ruido de valor suave y determinista en [-1, 1], con interpolación cúbica entre enteros. */
	public static float smoothNoise(float x, int seed) {
		int i = (int) Math.floor(x);
		float f = x - i;
		float u = f * f * (3 - 2 * f);
		return lerp(hash(i, seed), hash(i + 1, seed), u);
	}

	private static float hash(int n, int seed) {
		int h = n * 374761393 + seed * 668265263;
		h = (h ^ (h >>> 13)) * 1274126177;
		h ^= h >>> 16;
		return ((h & 0xFFFF) / 32767.5f) - 1f;
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

	/**
	 * Dirección unitaria dentro de un cono: {@code yawDeg} es el rumbo (0 = +Z, como Minecraft) y {@code spreadDeg}
	 * la apertura lateral; la elevación se sortea entre {@code elevMinDeg} y {@code elevMaxDeg}.
	 */
	public static double[] coneDirection(RandomGenerator rng, double yawDeg, double spreadDeg, double elevMinDeg, double elevMaxDeg) {
		double yaw = Math.toRadians(yawDeg + (rng.nextDouble() * 2 - 1) * spreadDeg);
		double elev = Math.toRadians(elevMinDeg + rng.nextDouble() * (elevMaxDeg - elevMinDeg));
		double horizontal = Math.cos(elev);
		return new double[] {-Math.sin(yaw) * horizontal, Math.sin(elev), Math.cos(yaw) * horizontal};
	}

	/** Punto de un anillo de radio {@code radius} en el ángulo {@code angleRad}, con rumbo (x, z). */
	public static double[] ringPoint(double angleRad, double radius) {
		return new double[] {Math.cos(angleRad) * radius, Math.sin(angleRad) * radius};
	}

	/**
	 * Cantidad de partículas tras aplicar la intensidad de efectos y el ajuste de partículas del jugador
	 * ({@code particleFactor}: 1 = todas, menos = reducidas). Nunca baja de 1 si había alguna y la intensidad es positiva.
	 */
	public static int scaledCount(int base, float intensity, float particleFactor) {
		if (base <= 0 || intensity <= 0 || particleFactor <= 0) return 0;
		return Math.max(1, Math.round(base * intensity * particleFactor));
	}
}
