package dev.aptitudes.config;

import java.util.Locale;

/**
 * v67: reglas puras de los tooltips de la pantalla de configuración. Cada opción editable tiene un texto de descripción
 * en los archivos de idioma ({@code config.aptitudes.field.<campo>}); aquí solo se calcula la clave y se decide si una
 * opción coincide con lo que el jugador busca.
 */
public final class ConfigTooltipRules {
	public static final String KEY_PREFIX = "config.aptitudes.field.";
	/** Ancho máximo, en píxeles, del texto de un tooltip. */
	public static final int MAX_TOOLTIP_WIDTH = 220;

	private ConfigTooltipRules() {
	}

	/** Clave de idioma de la descripción de un campo. */
	public static String descriptionKey(String fieldName) {
		return KEY_PREFIX + fieldName;
	}

	/** ¿La opción coincide con la búsqueda? Se mira el nombre del campo y el texto de su descripción. */
	public static boolean matches(String query, String fieldName, String description) {
		String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		if (q.isEmpty()) return true;
		return fieldName.toLowerCase(Locale.ROOT).contains(q)
				|| (description != null && description.toLowerCase(Locale.ROOT).contains(q));
	}
}
