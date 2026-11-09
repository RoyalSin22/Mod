package dev.aptitudes.logic;

/** Anclajes de la barra de habilidades, guardados por id en el cliente. */
public enum HudPreset {
	CLASSIC("classic", 1f),
	CENTERED("centered", 1f),
	COMPACT("compact", 0.75f);

	private final String id;
	private final float scaleFactor;

	HudPreset(String id, float scaleFactor) {
		this.id = id;
		this.scaleFactor = scaleFactor;
	}

	public String id() {
		return id;
	}

	public float scaleFactor() {
		return scaleFactor;
	}

	public static HudPreset fromId(String id) {
		return switch (id) {
			case "centered" -> CENTERED;
			case "compact" -> COMPACT;
			case null, default -> CLASSIC;
		};
	}
}
