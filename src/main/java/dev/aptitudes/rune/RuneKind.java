package dev.aptitudes.rune;

/** Runas disponibles para las armas de rol. */
public enum RuneKind {
	FLAME("flame"), LEECH("leech"), SWIFT("swift"), FORCE("force"),
	/** v34: +daño contra élites, campeones y jefes. Añadida al final. */
	ECLIPSE("eclipse"),
	/** v35: Escarcha (lentitud), Trueno (descarga en cadena) y Guardia (absorción). Añadidas al final. */
	FROST("frost"), THUNDER("thunder"), GUARD("guard");
	private final String id;
	RuneKind(String id) { this.id = id; }
	public String id() { return id; }
	public static RuneKind byId(String id) {
		for (RuneKind kind : values()) if (kind.id.equals(id)) return kind;
		return null;
	}
}
