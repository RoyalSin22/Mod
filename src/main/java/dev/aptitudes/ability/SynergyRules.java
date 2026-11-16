package dev.aptitudes.ability;

import dev.aptitudes.role.RoleManager;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * v65 (C5 Y1+Y4): Sinergias entre sendas, activas, runas y armaduras.
 *
 * <p>Puro: reglas de bonificación sin invocar APIs de Minecraft directamente. Los cálculos se validan
 * con JUnit.</p>
 */
public final class SynergyRules {
	private SynergyRules() {
	}

	// =====================================================================
	// Y1: Senda ↔ Activas
	// =====================================================================

	/**
	 * Bonificador de radio para activas basado en la senda actual.
	 * Ejemplo: senda Velocidad → radio +20%.
	 */
	public static float radiusModifierForPath(ServerPlayerEntity player, String pathId) {
		// Placeholder: sin sendas específicas mapeadas aún.
		// Retorna 1.0 (sin cambio) por defecto.
		return 1.0f;
	}

	/**
	 * Bonificador de daño para activas basado en la senda actual.
	 */
	public static float damageModifierForPath(ServerPlayerEntity player, String pathId) {
		return 1.0f; // Placeholder
	}

	/**
	 * Bonificador de coste energético para activas.
	 * Ejemplo: senda Eficiencia → coste −10%.
	 */
	public static float energyCostModifierForPath(ServerPlayerEntity player, String pathId) {
		return 1.0f; // Placeholder
	}

	// =====================================================================
	// Y4: Armadura ↔ Senda
	// =====================================================================

	/**
	 * Bonificador de atributos si la armadura coincide con la senda del rol.
	 * Ejemplo: Paladin (Defensa) + armadura Paladín → +10% defensa.
	 */
	public static float armorSynergyBonus(ServerPlayerEntity player, String roleId, String pathId) {
		// Placeholder: verificar coincidencia de senda y armadura.
		// Por ahora, retorna 1.0 (sin bonificación).
		return 1.0f;
	}
}
