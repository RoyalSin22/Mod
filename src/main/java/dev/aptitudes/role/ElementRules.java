package dev.aptitudes.role;

import java.util.Map;
import java.util.Set;

/**
 * Elementos del Arcanista (v65): retirada de la selección y evolución por rango. Puro (sin Minecraft); usa los ids
 * en minúsculas de {@link Element}. Un elemento retirado no se borra del código: el elemento activo se guarda por
 * índice, así que quitar un valor del enum rompería el progreso guardado.
 */
public final class ElementRules {
	/** Rango del nodo del elemento a partir del cual evoluciona. Aprobado por el autor. */
	public static final int EVOLUTION_RANK = 3;

	/** Elementos fuera de la selección: el veneno lo cubre la Planta. */
	public static final Set<String> RETIRED = Set.of("poison");

	/** Evoluciones: elemento base → forma evolucionada. Magma es la evolución del Fuego. */
	public static final Map<String, String> EVOLUTIONS = Map.of("fire", "magma");

	private ElementRules() {}

	/** ¿Puede elegirse este elemento? */
	public static boolean selectable(String elementId) {
		return elementId != null && !RETIRED.contains(elementId);
	}

	/** Forma que tiene el elemento con este rango: la evolucionada si ya llegó al rango de evolución. */
	public static String form(String elementId, int rank) {
		if (elementId == null) return null;
		String evolved = EVOLUTIONS.get(elementId);
		return evolved != null && rank >= EVOLUTION_RANK ? evolved : elementId;
	}
}
