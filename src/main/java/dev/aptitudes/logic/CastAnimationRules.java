package dev.aptitudes.logic;

/** Curvas de los remates de lanzamiento. Sus medidas se usan solo para dibujar. */
public final class CastAnimationRules {
	public static final int MAX_VISIBLE = 16;
	public static final double RANGE_SQUARED = 40.0 * 40.0;

	public enum Kind {
		ACTIVE(440, 1.0f), SIGNATURE(620, 1.18f), ULTIMATE(840, 1.38f), UNIQUE(720, 1.25f);

		final int durationMs;
		final float scale;

		Kind(int durationMs, float scale) {
			this.durationMs = durationMs;
			this.scale = scale;
		}

		public static Kind byIndex(int index) {
			return index < 0 || index >= BY_INDEX.length ? null : BY_INDEX[index];
		}

		private static final Kind[] BY_INDEX = values();
	}

	public record Frame(float progress, float opacity, float scale) {}

	private CastAnimationRules() {}

	/** Un fotograma transparente al comenzar y al caducar, con una expansión breve al inicio. */
	public static Frame frame(Kind kind, long ageMs) {
		float progress = (float) Math.clamp(ageMs / (double) kind.durationMs, 0.0, 1.0);
		float opacity = Math.min(smooth(progress / 0.16f), smooth((1f - progress) / 0.45f));
		float scale = kind.scale * (0.72f + 0.28f * smooth(progress / 0.46f));
		return new Frame(progress, opacity, scale);
	}

	private static float smooth(float t) {
		float bounded = Math.clamp(t, 0f, 1f);
		return bounded * bounded * (3f - 2f * bounded);
	}
}
