package dev.aptitudes.ability;

import dev.aptitudes.role.Role;
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
	 * Contexto de sinergia para una habilidad: combina todos los bonificadores aplicables.
	 * Se llama ANTES de ejecutar la habilidad.
	 */
	public static AbilitySynergyContext contextForAbility(ServerPlayerEntity player, Role role, String abilityId) {
		// Placeholder: sin mapeos reales de senda ↔ activa aún.
		// Retorna contexto sin bonificadores (1.0 para todos).
		float radiusBonus = radiusModifierForPath(player, role);
		float damageBonus = damageModifierForPath(player, role);
		float energyCostBonus = energyCostModifierForPath(player, role);

		// Si todos son 1.0, retornar NONE para no crear contextos innecesarios.
		if (radiusBonus == 1.0f && damageBonus == 1.0f && energyCostBonus == 1.0f) {
			return AbilitySynergyContext.NONE;
		}
		return new AbilitySynergyContext(radiusBonus, damageBonus, energyCostBonus);
	}

	/**
	 * Bonificador de radio para activas basado en la senda actual.
	 * Ejemplo: senda Velocidad → radio +20%.
	 */
	public static float radiusModifierForPath(ServerPlayerEntity player, Role role) {
		// Placeholder: sin sendas específicas mapeadas aún.
		// Retorna 1.0 (sin cambio) por defecto.
		return 1.0f;
	}

	/**
	 * Bonificador de daño para activas basado en la senda actual.
	 */
	public static float damageModifierForPath(ServerPlayerEntity player, Role role) {
		return 1.0f; // Placeholder
	}

	/**
	 * Bonificador de coste energético para activas.
	 * Ejemplo: senda Eficiencia → coste −10%.
	 */
	public static float energyCostModifierForPath(ServerPlayerEntity player, Role role) {
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
