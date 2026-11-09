package dev.aptitudes.logic;

/** Transiciones del HUD en tiempo real, independientes de los FPS. */
public final class HudMotion {
	private HudMotion() {}

	private static float unit(float value) {
		return Float.isFinite(value) ? Math.clamp(value, 0f, 1f) : 0f;
	}

	public static float ease(long elapsed, long duration) {
		float t = duration <= 0 ? 1f : unit(elapsed / (float) duration);
		return t * t * (3f - 2f * t);
	}

	public static float notice(long elapsed, long lifetime, boolean animated) {
		if (elapsed < 0 || elapsed >= lifetime) return 0f;
		return animated ? Math.min(ease(elapsed, 180), ease(lifetime - elapsed, 350)) : 1f;
	}

	public static float levelPulse(long elapsed) {
		if (elapsed < 0 || elapsed >= 650) return 0f;
		float t = elapsed / 650f;
		return 4f * t * (1f - t);
	}

	/** Reinicia desde el valor visible si llega un nuevo objetivo a mitad del movimiento. */
	public static final class Fill {
		private float from;
		private float target = -1f;
		private long started;

		public float update(float value, long now, long duration) {
			value = unit(value);
			if (target < 0f || duration <= 0) {
				from = target = value;
				started = now;
				return target;
			}
			float shown = from + (target - from) * ease(now - started, duration);
			if (target != value) {
				from = shown;
				target = value;
				started = now;
			}
			return shown;
		}

		public void clear() {
			target = -1f;
		}
	}
}
