package dev.aptitudes.ability;

import dev.aptitudes.role.Element;
import dev.aptitudes.role.PathRules;
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
	/** Lugares ya avisados en el log: se llaman en cada muerte y un fallo repetido inundaría el registro. */
	private static final java.util.Set<String> WARNED = new java.util.HashSet<>();

	private static void logOnce(String where, RuntimeException e) {
		if (WARNED.add(where)) {
			dev.aptitudes.Aptitudes.LOGGER.error("[Aptitudes] La sinergia {} falló y se desactiva para esa llamada; no se repetirá este aviso", where, e);
		}
	}

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
		if (!dev.aptitudes.Aptitudes.config().enableSynergies || role == null || abilityId == null) {
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
		if (!dev.aptitudes.Aptitudes.config().enableSynergies || player == null || activeElement == null || weapon == null || weapon.isEmpty()) {
			return 1.0f;
		}

		return checkElementRuneMatch(activeElement, weapon) ? (float) dev.aptitudes.Aptitudes.config().synergyBonusY3Damage : 1.0f;
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
		} catch (RuntimeException e) {
			// C1: antes se tragaba en silencio y la sinergia dejaba de aplicarse sin que nadie lo supiera.
			logOnce("Y3 (elementos y runas)", e);
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
		if (!dev.aptitudes.Aptitudes.config().enableSynergies || player == null) return 1.0f;
		return checkKeystoneResonance(player) ? (float) dev.aptitudes.Aptitudes.config().synergyBonusY2Xp : 1.0f;
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
		} catch (RuntimeException e) {
			logOnce("Y2 (resonancia de grupo)", e);
			return false;
		}
	}

	// =====================================================================
	// Y5: Combos ↔ Piedras angulares
	// =====================================================================

	/**
	 * Cooldown de espera para disparar piedra angular desde remate de combo.
	 *
	 * @return Ticks (segundos * 20) de espera antes de poder disparar de nuevo
	 */
	public static int keystoneComboTrigerCooldown() {
		// Placeholder: 20 segundos (400 ticks)
		return dev.aptitudes.Aptitudes.config().synergyY5KeystoneCooldownSeconds * 20;
	}

	// =====================================================================
	// Y4: Armadura ↔ Senda
	// =====================================================================

	/**
	 * Bonificador de atributos si la armadura coincide con la senda del rol (v65 C5 Y4).
	 * Ejemplo: Paladin (Defensa) + armadura Paladín → +10% defensa.
	 *
	 * @param player Jugador
	 * @param role Rol activo (principal o secundario)
	 * @return Multiplicador: 1.10 (sinergia activa) o 1.0 (sin sinergia)
	 */
	public static float armorSynergyBonus(ServerPlayerEntity player, Role role) {
		if (!dev.aptitudes.Aptitudes.config().enableSynergies || player == null || role == null) return 1.0f;
		return checkArmorPathMatch(player, role) ? (float) dev.aptitudes.Aptitudes.config().synergyBonusY4Defense : 1.0f;
	}

	/**
	 * Verificar si la armadura equipada coincide con la senda elegida del rol.
	 */
	private static boolean checkArmorPathMatch(ServerPlayerEntity player, Role role) {
		try {
			// Obtener armadura completa que se está usando.
			Role wornSet = dev.aptitudes.ability.RoleArmorSets.wornSet(player);
			if (wornSet == null || !wornSet.equals(role)) {
				return false; // Armadura no coincide con el rol.
			}

			// Verificar si el rol tiene una senda elegida.
			int pathNumber = getChosenPath(player, role);
			return pathNumber > 0; // Sinergia si rol + senda + armadura coinciden.
		} catch (RuntimeException e) {
			logOnce("Y4 (armadura y senda)", e);
			return false;
		}
	}

	/**
	 * Obtener la senda elegida (1–3) del rol, o 0 si ninguna.
	 * (Reutilizado de PathSynergyRules.)
	 */
	private static int getChosenPath(ServerPlayerEntity player, Role role) {
		if (player == null || role == null) return 0;
		try {
			var data = RoleManager.get(player);
			if (data == null) return 0;
			return PathRules.chosenPath(nodeId -> RoleManager.activeRank(player, nodeId), role.key());
		} catch (RuntimeException e) {
			logOnce("Y4 (senda elegida)", e);
			return 0;
		}
	}
}
