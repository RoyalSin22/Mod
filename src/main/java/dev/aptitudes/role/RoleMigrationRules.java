package dev.aptitudes.role;

import java.util.Map;

/**
 * Migración de roles (v65): ids antiguos a ids nuevos, decididos por el autor. Puro (sin Minecraft).
 *
 * <p>Regla: un id antiguo nunca se reutiliza para otro rol. Un progreso guardado con un id antiguo se traslada al
 * nuevo mediante esta tabla. Nigromante y Druida conservan su id porque conservan su nombre.</p>
 */
public final class RoleMigrationRules {
	/** Mapeo aprobado por el autor: id antiguo → id nuevo. */
	public static final Map<String, String> LEGACY_TO_NEW = Map.of(
			"mage", "arcanist",
			"elementalist", "shaman",
			"herbalist", "apothecary",
			"swordsman", "duelist",
			"merchant", "tactical_merchant",
			"warlock", "abyssal_warlock");

	private RoleMigrationRules() {}

	/** Id que debe usarse para un rol guardado: el nuevo si el antiguo migra; si no, el mismo. */
	public static String resolve(String key) {
		return key == null ? null : LEGACY_TO_NEW.getOrDefault(key, key);
	}

	/** ¿Es un id antiguo que se traslada a otro? */
	public static boolean isLegacy(String key) {
		return LEGACY_TO_NEW.containsKey(key);
	}
}
