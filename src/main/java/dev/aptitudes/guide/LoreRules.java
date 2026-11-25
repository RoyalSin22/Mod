package dev.aptitudes.guide;

import java.util.Set;

/**
 * Reglas puras de desbloqueo de lore del Grimorio (v67, T7a).
 *
 * <p>Define qué entradas de lore se desbloquean en cada evento: encuentros con NPCs, derrotas de jefes,
 * cambios de mundo, etc. Sin dependencias de Minecraft.
 */
public final class LoreRules {
	private LoreRules() {
	}

	/** Lore desbloqueado al hablar con el aldeano del gremio e iniciar la Niebla de almas (T1). */
	public static final String LORE_SOUL_FOG_TRIGGER = "soul_fog_triggered";

	/** Lore desbloqueado al hablar con el aldeano del gremio e iniciar la Lluvia de brasas (T1). */
	public static final String LORE_EMBER_RAIN_TRIGGER = "ember_rain_triggered";

	/** Lore del Custodio desbloqueado al vencerlo o entrar en su cámara (T4). */
	public static final String LORE_CUSTODIAN_DEFEATED = "custodian_lore_unlocked";

	/** Lore desbloqueado al completar la cámara de diamante y obtener el Damnorath (T4). */
	/** El texto del id se conserva tal cual ("behelit_lore_unlocked"): ya está guardado en los progresos de los jugadores. El objeto se llama Damnorath. */
	public static final String LORE_ARTIFACT_FOUND = "behelit_lore_unlocked";

	/** Lore desbloqueado al sacrificar el Damnorath en la Hoguera (T5). */
	public static final String LORE_ECLIPSE_SACRIFICE = "eclipse_sacrifice_lore";

	/** Lore desbloqueado al sobrevivir o presenciar el final del Eclipse (T1, T5). */
	public static final String LORE_ECLIPSE_AFTERMATH = "eclipse_aftermath_lore";

	/** Lore desbloqueado al superar la Cripta de la Niebla bajo el Templo del Custodio (V69). */
	public static final String LORE_CRYPT_CLEARED = "crypt_cleared_lore";

	/** V72: lore del segundo arco, que se revela al sobrevivir a un segundo Eclipse. */
	public static final String LORE_UNFINISHED_OATH = "unfinished_oath_lore";

	/** V72: marca interna (no revela entradas por sí sola): el jugador ya sobrevivió a un Eclipse. */
	public static final String MARKER_ECLIPSE_SURVIVED = "eclipse_survived_once";

	/** Todas las ids de lore válidas en T7a. */
	private static final Set<String> ALL_VALID_IDS = Set.of(
			LORE_SOUL_FOG_TRIGGER,
			LORE_EMBER_RAIN_TRIGGER,
			LORE_CUSTODIAN_DEFEATED,
			LORE_ARTIFACT_FOUND,
			LORE_ECLIPSE_SACRIFICE,
			LORE_ECLIPSE_AFTERMATH,
			LORE_CRYPT_CLEARED,
			LORE_UNFINISHED_OATH,
			MARKER_ECLIPSE_SURVIVED
	);

	/** Valida que un id de lore sea válido. */
	public static boolean isValidLoreId(String loreId) {
		return ALL_VALID_IDS.contains(loreId);
	}

	/** Retorna todas las ids de lore válidas. */
	public static Set<String> validLoreIds() {
		return Set.copyOf(ALL_VALID_IDS);
	}

	/**
	 * Desbloqueos al hablar con el aldeano del gremio.
	 *
	 * @param whichEvent cuál evento se activó: "fog" para Niebla, "rain" para Lluvia
	 * @return ids de lore a desbloquear
	 */
	public static Set<String> unlocksOnGuildmasterTalk(String whichEvent) {
		return switch (whichEvent) {
			case "fog" -> Set.of(LORE_SOUL_FOG_TRIGGER);
			case "rain" -> Set.of(LORE_EMBER_RAIN_TRIGGER);
			default -> Set.of();
		};
	}

	/** Desbloqueos al derrotar o encontrar al Custodio (T4, presencia en su cámara). */
	public static Set<String> unlocksOnCustodianEncounter() {
		return Set.of(LORE_CUSTODIAN_DEFEATED);
	}

	/** Desbloqueos al completar la cámara de diamante (T4). */
	public static Set<String> unlocksOnDiamondChamberComplete() {
		return Set.of(LORE_ARTIFACT_FOUND);
	}

	/** Desbloqueos al sacrificar el Damnorath en la Hoguera (T5). */
	public static Set<String> unlocksOnDamnorathSacrifice() {
		return Set.of(LORE_ECLIPSE_SACRIFICE);
	}

	/** Desbloqueos al finalizar el Eclipse (evento global). */
	public static Set<String> unlocksOnEclipseEnd() {
		return Set.of(LORE_ECLIPSE_AFTERMATH);
	}

	/** Desbloqueos al derrotar al campeón final de la Cripta de la Niebla (V69). */
	public static Set<String> unlocksOnCryptCleared() {
		return Set.of(LORE_CRYPT_CLEARED);
	}

	/**
	 * V72: desbloqueos al sobrevivir a un Eclipse. La primera vez solo anota la marca; la segunda revela además
	 * «El juramento sin cumplir» (el Santuario de las Dos Verdades, su fuente original, no existe).
	 *
	 * @param has si el jugador ya tiene ese id de lore
	 */
	public static Set<String> unlocksOnEclipseSurvived(java.util.function.Predicate<String> has) {
		return has.test(MARKER_ECLIPSE_SURVIVED) ? Set.of(MARKER_ECLIPSE_SURVIVED, LORE_UNFINISHED_OATH) : Set.of(MARKER_ECLIPSE_SURVIVED);
	}
}
