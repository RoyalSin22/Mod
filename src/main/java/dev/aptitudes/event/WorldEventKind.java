package dev.aptitudes.event;

import java.util.Locale;

/**
 * v41: eventos del mundo que rotan en el calendario (uno cada {@code eclipseIntervalDays} días). Se guardan por
 * nombre ({@link #id()}); el orden solo se usa en red y en los pesos: añadir al final es seguro.
 */
public enum WorldEventKind {
	ECLIPSE, SOUL_FOG, EMBER_RAIN;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static WorldEventKind byId(String id) {
		for (WorldEventKind k : values()) if (k.id().equals(id)) return k;
		return null;
	}
}
