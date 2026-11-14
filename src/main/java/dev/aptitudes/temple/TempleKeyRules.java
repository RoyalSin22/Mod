package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleFlowRules.Flow;

/**
 * T4 de la cadena narrativa: reglas puras de las cuatro llaves del Custodio y de las puertas de sus cámaras. Sin clases de
 * Minecraft: se prueban con JUnit ({@code TempleKeyRulesTest}).
 *
 * <p>El Custodio suelta las cuatro llaves a la vez (cobre, hierro, oro y diamante) al caer. Cada llave abre solo la
 * cámara de su metal y solo en el templo del que procede; la de diamante guarda la recompensa principal.</p>
 */
public final class TempleKeyRules {
	private TempleKeyRules() {}

	/** Metales de las llaves y las cámaras. Se guardan como número: solo se añaden valores al final. */
	public enum Metal { COPPER, IRON, GOLD, DIAMOND }

	/** Resultado de usar una puerta de cámara. */
	public enum KeyUse {
		OPEN,
		/** Todavía no: las llaves las guarda el Custodio (sigue vivo o el templo no está limpio). */
		NOT_READY,
		/** No lleva ninguna llave del Custodio. */
		NO_KEY,
		/** Lleva una llave de otro templo. */
		WRONG_TEMPLE,
		/** Lleva una llave de este templo, pero de otro metal. */
		WRONG_METAL
	}

	/** Metal guardado → metal válido, o {@code null} si el número no existe. */
	public static Metal metalFromOrdinal(int ordinal) {
		Metal[] all = Metal.values();
		return ordinal < 0 || ordinal >= all.length ? null : all[ordinal];
	}

	/**
	 * Llaves que suelta el Custodio de una distribución: las cuatro, en una sola tirada, en el Templo del Custodio; ninguna
	 * en el templo antiguo (que solo suelta la llave del Relicario).
	 */
	public static Metal[] keysToDrop(int layout) {
		return TempleFlowRules.clampLayout(layout) == TempleFlowRules.LAYOUT_CUSTODIAN ? Metal.values() : new Metal[0];
	}

	/**
	 * Qué pasa al usar la puerta de una cámara. {@code keyMetal} es {@code null} si no lleva una llave del Custodio.
	 * Solo se abre con el templo limpio (el Custodio ha caído): antes de eso nadie tiene las llaves.
	 */
	public static KeyUse useChamberDoor(Flow flow, Metal doorMetal, Metal keyMetal, boolean sameTemple) {
		if (flow != Flow.CLEARED) return KeyUse.NOT_READY;
		if (keyMetal == null) return KeyUse.NO_KEY;
		if (!sameTemple) return KeyUse.WRONG_TEMPLE;
		return keyMetal == doorMetal ? KeyUse.OPEN : KeyUse.WRONG_METAL;
	}

	/** Máscara de cámaras abiertas con una más. */
	public static int withOpened(int openMask, Metal metal) {
		return openMask | (1 << metal.ordinal());
	}

	public static boolean isOpened(int openMask, Metal metal) {
		return (openMask & (1 << metal.ordinal())) != 0;
	}

	/** Máscara guardada → máscara válida (solo los bits de los metales que existen). */
	public static int sanitizeMask(int mask) {
		return mask & ((1 << Metal.values().length) - 1);
	}
}
