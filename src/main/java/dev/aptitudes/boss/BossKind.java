package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;

/** Arquetipos propios guardados como etiquetas persistentes sobre entidades vanilla. */
public enum BossKind {
	TOWER_SENTINEL("tower_sentinel"), SHRINE_ORACLE("shrine_oracle"), CAMP_CHAMPION("camp_champion");
	private final String id;
	BossKind(String id) { this.id = id; }
	public String id() { return id; }
	public String tag() { return Aptitudes.MOD_ID + ".boss." + id; }
	public String nameKey() { return "boss." + Aptitudes.MOD_ID + "." + id; }
	public static BossKind fromTag(String tag) {
		for (BossKind kind : values()) if (kind.tag().equals(tag)) return kind;
		return null;
	}
}
