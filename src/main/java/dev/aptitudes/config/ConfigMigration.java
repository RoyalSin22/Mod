package dev.aptitudes.config;

/**
 * Migraciones de {@code config/aptitudes.json} entre versiones (v46). Puro, con pruebas.
 *
 * <p>Gson rellena con el valor por defecto los campos que faltan, pero no cambia los que ya están guardados: un
 * servidor que se creó con {@code maxRoleLevel = 30} lo conservaría aunque el nuevo valor por defecto sea 50. Por
 * eso el archivo guarda {@code configRevision}; los archivos sin ella son anteriores a la v46 (revisión 0).</p>
 */
public final class ConfigMigration {
	/** Revisión actual de la configuración. */
	public static final int CURRENT = 46;
	/** Valor por defecto anterior y nuevo del nivel máximo de rol. */
	public static final int OLD_MAX_ROLE_LEVEL = 30;
	public static final int NEW_MAX_ROLE_LEVEL = 50;

	private ConfigMigration() {}

	/**
	 * v46: el nivel máximo de rol pasa de 30 a 50. Solo se cambia si era el valor por defecto anterior; un valor
	 * elegido a mano por el administrador (p. ej. 40) se respeta.
	 */
	public static int maxRoleLevel(int fromRevision, int value) {
		return fromRevision < 46 && value == OLD_MAX_ROLE_LEVEL ? NEW_MAX_ROLE_LEVEL : value;
	}
}
