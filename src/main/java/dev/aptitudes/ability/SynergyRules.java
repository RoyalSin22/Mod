package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.rune.RuneKind;
import dev.aptitudes.rune.RuneSocketing;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * v65 (C5 Y1+Y2+Y3+Y4): Sinergias entre sendas, activas, runas y armaduras.
 *
 * <p>Puro: reglas de bonificación sin invocar APIs de Minecraft directamente. Los cálculos se validan
 * con JUnit.</p>
 */
public final class SynergyRules {
	private SynergyRules() {
	}

	// Mapeo: runa (id) → elemento que potencia (lazy initialized)
	private static Map<String, Element> RUNE_ELEMENT_MAP;

	private static Map<String, Element> getRuneElementMap() {
		if (RUNE_ELEMENT_MAP == null) {
			RUNE_ELEMENT_MAP = new HashMap<>();
			// v65 (C5 Y3): Mapeo de runas a elementos para sinergia
			RUNE_ELEMENT_MAP.put("flame", Element.FIRE);
			RUNE_ELEMENT_MAP.put("frost", Element.ICE);
			RUNE_ELEMENT_MAP.put("thunder", Element.LIGHTNING);
			// Runas sin elemento específico: LEECH, SWIFT, FORCE, ECLIPSE, GUARD
		}
		return RUNE_ELEMENT_MAP;
	}

	// =====================================================================
	// Y1: Senda ↔ Activas
	// =====================================================================

	/**
	 * Contexto de sinergia para una habilidad: combina todos los bonificadores aplicables (v65 C5).
	 * Se llama ANTES de ejecutar la habilidad.
	 */
	public static AbilitySynergyContext contextForAbility(ServerPlayerEntity player, Role role, String abilityId) {
		if (role == null || abilityId == null) {
			return AbilitySynergyContext.NONE;
		}

		// v65 C5 Y1: Calcular bonificadores reales usando PathSynergyRules.
		float radiusBonus = PathSynergyRules.radiusBonus(player, role, abilityId);
		float damageBonus = PathSynergyRules.damageBonus(player, role, abilityId);
		float energyCostBonus = energyCostModifierForPath(player, role);

		// Si todos son 1.0, retornar NONE para no crear contextos innecesarios.
		if (radiusBonus == 1.0f && damageBonus == 1.0f && energyCostBonus == 1.0f) {
			return AbilitySynergyContext.NONE;
		}
		return new AbilitySynergyContext(radiusBonus, damageBonus, energyCostBonus);
	}

	/**
	 * Bonificador de radio para activas basado en la senda actual.
	 * Delega a PathSynergyRules (v65 C5 Y1).
	 */
	public static float radiusModifierForPath(ServerPlayerEntity player, Role role) {
		// Nota: se necesita el abilityNodeId para cálculos reales.
		// Por ahora, retorna valor por defecto; contextForAbility() lo usa.
		return 1.0f;
	}

	/**
	 * Bonificador de daño para activas basado en la senda actual.
	 * Delega a PathSynergyRules (v65 C5 Y1).
	 */
	public static float damageModifierForPath(ServerPlayerEntity player, Role role) {
		// Nota: se necesita el abilityNodeId para cálculos reales.
		return 1.0f;
	}

	/**
	 * Bonificador de coste energético para activas.
	 * Integra PathSynergyRules (v65 C5 Y1).
	 */
	public static float energyCostModifierForPath(ServerPlayerEntity player, Role role) {
		// Defensivo: evitar NPE en tests.
		if (player == null || role == null) return 1.0f;
		return PathSynergyRules.energyCostBonus(player, role);
	}

	// =====================================================================
	// Y3: Elementos ↔ Runas
	// =====================================================================

	/**
	 * Bonificador de fusión si una runa del mismo elemento potencia el activo (v65 C5 Y3).
	 * Ejemplo: Elementalista usa FIRE + tiene runa FLAME → +20% daño de fusión.
	 *
	 * @param player Elementalista
	 * @param activeElement Elemento activo de la habilidad
	 * @param weapon Arma con runas insertadas
	 * @return Multiplicador: 1.20 (fusión potenciada) o 1.0 (sin sinergia)
	 */
	public static float elementRuneSynergyBonus(ServerPlayerEntity player, Element activeElement, ItemStack weapon) {
		if (player == null || activeElement == null || weapon == null || weapon.isEmpty()) {
			return 1.0f;
		}

		return checkElementRuneMatch(activeElement, weapon) ? 1.20f : 1.0f;
	}

	/**
	 * Verificar si el arma tiene una runa que coincida con el elemento activo.
	 */
	private static boolean checkElementRuneMatch(Element activeElement, ItemStack weapon) {
		try {
			// Obtener runas del arma.
			var runeIds = RuneSocketing.runes(weapon);
			if (runeIds.isEmpty()) return false;

			// Obtener el mapeo runa → elemento.
			var runeElementMap = getRuneElementMap();

			// Buscar coincidencia.
			for (String runeId : runeIds) {
				Element runeElement = runeElementMap.get(runeId);
				if (runeElement != null && runeElement == activeElement) {
					return true; // Encontró coincidencia.
				}
			}

			return false;
		} catch (Exception e) {
			// Defensivo: en caso de error, retornar false.
			return false;
		}
	}

	// =====================================================================
	// Y2: Resonancia de grupo
	// =====================================================================

	/**
	 * Bonificador de XP si dos o más miembros del grupo comparten plantilla de piedra angular (v65 C5 Y2).
	 * Retorna 1.15 (+15% XP) si hay resonancia, 1.0 si no.
	 */
	public static float groupResonanceBonus(ServerPlayerEntity player) {
		if (player == null) return 1.0f;
		return checkKeystoneResonance(player) ? 1.15f : 1.0f;
	}

	/**
	 * Verificar si hay resonancia de piedras angulares en el grupo:
	 * dos o más miembros comparten la misma plantilla.
	 */
	private static boolean checkKeystoneResonance(ServerPlayerEntity player) {
		try {
			// Obtener todos los miembros online del grupo del jugador.
			var partyMembers = dev.aptitudes.party.PartyManager.onlineMembers(player);
			if (partyMembers.size() < 2) return false; // Sin grupo o grupo de 1.

			// Mapeo: template → cantidad de miembros que la tienen.
			java.util.Map<String, Integer> templateCount = new java.util.HashMap<>();
			for (ServerPlayerEntity member : partyMembers) {
				// Obtener keystones activos del miembro.
				var keystones = Keystones.active(member);
				for (var template : keystones) {
					String templateKey = template.toString();
					templateCount.put(templateKey, templateCount.getOrDefault(templateKey, 0) + 1);
				}
			}

			// Si alguna template es compartida por 2+ miembros → resonancia.
			for (int count : templateCount.values()) {
				if (count >= 2) return true;
			}

			return false;
		} catch (Exception e) {
			// Defensivo: en caso de error (no bootstrapped, etc.), retornar false.
			return false;
		}
	}

	// =====================================================================
	// Y5: Combos ↔ Piedras angulares
	// =====================================================================

	/**
	 * Verificar si un remate de combo puede disparar una piedra angular.
	 * El remate debe tener espera configurable (cooldown).
	 *
	 * @param player Jugador que ejecuta combo
	 * @param comboFinisher ID de la habilidad que remata el combo
	 * @return true si se puede ejecutar el remate de piedra angular
	 */
	public static boolean canTriggerKeystoneOnCombo(ServerPlayerEntity player, String comboFinisher) {
		// Placeholder: sin lógica de cooldown aún.
		// TODO: Verificar si existe cooldown de piedra angular y si ha expirado.
		return false;
	}

	/**
	 * Cooldown de espera para disparar piedra angular desde remate de combo.
	 *
	 * @return Ticks (segundos * 20) de espera antes de poder disparar de nuevo
	 */
	public static int keystoneComboTrigerCooldown() {
		// Placeholder: 20 segundos (400 ticks)
		return 20 * 20;
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
