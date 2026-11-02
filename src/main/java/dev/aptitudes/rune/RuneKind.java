package dev.aptitudes.rune;

/** Runas disponibles para las armas de rol. */
public enum RuneKind {
	FLAME("flame"), LEECH("leech"), SWIFT("swift"), FORCE("force");
	private final String id;
	RuneKind(String id) { this.id = id; }
	public String id() { return id; }
	public static RuneKind byId(String id) {
		for (RuneKind kind : values()) if (kind.id.equals(id)) return kind;
		return null;
	}
}
