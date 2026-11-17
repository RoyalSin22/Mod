package dev.aptitudes.client.fx;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.logic.FxMath;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * V77: dinámica de cámara y pantalla de los efectos de batalla. Pulso de campo de visión, retroceso suave de cámara,
 * sacudida con ruido continuo (sin temblor aleatorio cuadro a cuadro) y viñeteado de color. Todo escala con
 * {@code ClientConfig.fxIntensity} y se apaga con {@code ClientConfig.screenEffects}. Solo cliente y solo visual:
 * no toca hacia dónde mira el jugador.
 */
public final class ScreenDynamics {
	private static final int MAX_EACH = 10;
	private static final float MAX_FOV = 22f;
	private static final float MAX_ANGLE = 14f;

	private record Punch(long start, long attack, long hold, long decay, float degrees) {}
	private record Kick(long start, long attack, long decay, float pitch, float yaw) {}
	private record Shake(long start, long duration, float strength, float hz, int seed) {}
	private record Vignette(long start, long attack, long hold, long decay, int color, float strength, float pulseHz) {}

	private static final List<Punch> PUNCHES = new ArrayList<>();
	private static final List<Kick> KICKS = new ArrayList<>();
	private static final List<Shake> SHAKES = new ArrayList<>();
	private static final List<Vignette> VIGNETTES = new ArrayList<>();
	private static int shakeSeed = 1;

	private ScreenDynamics() {
	}

	/** Multiplicador global: 0 si los efectos de pantalla están apagados. */
	private static float scale() {
		ClientConfig cfg = ClientConfig.get();
		return cfg.screenEffects ? cfg.fxIntensity : 0f;
	}

	private static <T> void add(List<T> list, T value) {
		if (list.size() >= MAX_EACH) list.remove(0);
		list.add(value);
	}

	/** Pulso del campo de visión: positivo ensancha (rugido), negativo aprieta (impacto). */
	public static void fovPunch(float degrees, long attackMs, long holdMs, long decayMs) {
		float s = scale();
		if (s <= 0 || degrees == 0) return;
		add(PUNCHES, new Punch(Util.getMeasuringTimeMs(), attackMs, holdMs, decayMs, degrees * s));
	}

	/** Retroceso de cámara: {@code pitch} positivo mira hacia abajo, {@code yaw} positivo hacia la derecha. */
	public static void kick(float pitch, float yaw, long attackMs, long decayMs) {
		float s = scale();
		if (s <= 0) return;
		add(KICKS, new Kick(Util.getMeasuringTimeMs(), attackMs, decayMs, pitch * s, yaw * s));
	}

	/** Sacudida con ruido suave de {@code hz} oscilaciones por segundo que se apaga durante {@code durationMs}. */
	public static void shake(float strength, long durationMs, float hz) {
		float s = scale();
		if (s <= 0 || strength <= 0) return;
		add(SHAKES, new Shake(Util.getMeasuringTimeMs(), durationMs, strength * s, hz, shakeSeed++ * 7919));
	}

	/** Viñeteado de color; {@code pulseHz} > 0 lo hace latir. */
	public static void vignette(int color, float strength, long attackMs, long holdMs, long decayMs, float pulseHz) {
		float s = scale();
		if (s <= 0 || strength <= 0) return;
		add(VIGNETTES, new Vignette(Util.getMeasuringTimeMs(), attackMs, holdMs, decayMs, color & 0xFFFFFF,
				Math.min(1f, strength * Math.min(1f, s)), pulseHz));
	}

	public static void clear() {
		PUNCHES.clear();
		KICKS.clear();
		SHAKES.clear();
		VIGNETTES.clear();
	}

	// ------------------------------------------------------------------ Lectura por cuadro

	/** Grados que se suman al campo de visión del mundo (no al de la mano). */
	public static float fovOffset() {
		if (PUNCHES.isEmpty()) return 0f;
		long now = Util.getMeasuringTimeMs();
		float sum = 0f;
		for (var it = PUNCHES.iterator(); it.hasNext(); ) {
			Punch p = it.next();
			long age = now - p.start();
			if (age >= FxMath.envelopeLength(p.attack(), p.hold(), p.decay())) {
				it.remove();
				continue;
			}
			sum += p.degrees() * FxMath.envelope(age, p.attack(), p.hold(), p.decay());
		}
		return Math.clamp(sum, -MAX_FOV, MAX_FOV);
	}

	public static float pitchOffset() {
		return cameraOffset(true);
	}

	public static float yawOffset() {
		return cameraOffset(false);
	}

	private static float cameraOffset(boolean pitch) {
		if (KICKS.isEmpty() && SHAKES.isEmpty()) return 0f;
		long now = Util.getMeasuringTimeMs();
		float sum = 0f;
		for (var it = KICKS.iterator(); it.hasNext(); ) {
			Kick k = it.next();
			long age = now - k.start();
			if (age >= k.attack() + k.decay()) {
				it.remove();
				continue;
			}
			sum += (pitch ? k.pitch() : k.yaw()) * FxMath.envelope(age, k.attack(), 0, k.decay());
		}
		for (var it = SHAKES.iterator(); it.hasNext(); ) {
			Shake s = it.next();
			long age = now - s.start();
			if (age >= s.duration()) {
				it.remove();
				continue;
			}
			float amplitude = s.strength() * FxMath.envelope(age, 25, 0, s.duration() - 25);
			float x = age / 1000f * s.hz();
			sum += FxMath.smoothNoise(x, s.seed() + (pitch ? 31 : 0)) * amplitude * (pitch ? 0.7f : 1.0f);
		}
		return Math.clamp(sum, -MAX_ANGLE, MAX_ANGLE);
	}

	// ------------------------------------------------------------------ Viñeteado

	public static void renderVignette(DrawContext ctx, int w, int h) {
		if (VIGNETTES.isEmpty()) return;
		long now = Util.getMeasuringTimeMs();
		for (var it = VIGNETTES.iterator(); it.hasNext(); ) {
			Vignette v = it.next();
			long age = now - v.start();
			if (age >= FxMath.envelopeLength(v.attack(), v.hold(), v.decay())) {
				it.remove();
				continue;
			}
			float level = FxMath.envelope(age, v.attack(), v.hold(), v.decay()) * v.strength();
			if (v.pulseHz() > 0) level *= 0.72f + 0.28f * (float) Math.sin(age / 1000.0 * v.pulseHz() * Math.PI * 2);
			drawEdges(ctx, w, h, v.color(), (int) (190 * level));
		}
	}

	private static void drawEdges(DrawContext ctx, int w, int h, int rgb, int alpha) {
		if (alpha <= 2) return;
		int edge = (Math.min(alpha, 255) << 24) | rgb;
		int clear = rgb;
		int band = Math.max(24, h / 4);
		ctx.fillGradient(0, 0, w, band, edge, clear);
		ctx.fillGradient(0, h - band, w, h, clear, edge);
		int sideBand = Math.max(24, w / 6);
		for (int i = 0; i < sideBand; i += 2) {
			int a = (int) (alpha * (1f - i / (float) sideBand));
			int c = (Math.min(a, 255) << 24) | rgb;
			ctx.fill(i, 0, i + 2, h, c);
			ctx.fill(w - i - 2, 0, w - i, h, c);
		}
	}
}
