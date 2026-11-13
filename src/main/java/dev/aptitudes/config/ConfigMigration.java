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
	public static final int CURRENT = 67;
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

	/** Valores por defecto anterior y nuevo de los puntos de evolución (v67, decisión del autor). */
	public static final int OLD_MASTER_BONUS = 10, NEW_MASTER_BONUS = 15;
	public static final int OLD_ULTIMATE_BONUS = 5, NEW_ULTIMATE_BONUS = 10;

	/** v67: los puntos al evolucionar a Maestro pasan de 10 a 15; un valor elegido a mano se respeta. */
	public static int masterBonusPoints(int fromRevision, int value) {
		return fromRevision < 67 && value == OLD_MASTER_BONUS ? NEW_MASTER_BONUS : value;
	}

	/** v67: los puntos al evolucionar a Definitivo pasan de 5 a 10; un valor elegido a mano se respeta. */
	public static int ultimateBonusPoints(int fromRevision, int value) {
		return fromRevision < 67 && value == OLD_ULTIMATE_BONUS ? NEW_ULTIMATE_BONUS : value;
	}
}
