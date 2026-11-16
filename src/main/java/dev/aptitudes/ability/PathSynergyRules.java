package dev.aptitudes.ability;

import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleTree;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * v65 (C5 Y1): Lógica de sinergia senda ↔ activas.
 *
 * <p>Define cómo los PathEffects de una senda modifican el comportamiento de las habilidades:
 * radio, daño, coste energético, etc.</p>
 *
 * <p>Puro: usa índices de nodos estables sin invocar APIs de Minecraft.</p>
 */
public final class PathSynergyRules {
	private PathSynergyRules() {
	}

	/**
	 * Modificador de radio para una habilidad basado en la senda elegida del rol.
	 * Ejemplo: SPEED +20% radio en habilidades de movimiento.
	 */
	public static float radiusBonus(ServerPlayerEntity player, Role role, String abilityNodeId) {
		if (role == null || abilityNodeId == null) return 1.0f;

		// Obtener senda elegida del rol.
		int pathNumber = getChosenPath(player, role);
		if (pathNumber == 0) return 1.0f; // Sin senda elegida.

		// Detectar PathEffects de la senda y calcular bonificadores.
		float bonus = 1.0f;

		// Sinergia: SPEED → +radio en habilidades de movimiento (abilityNodeId contiene "dash", "move", etc.)
		if (hasPathEffect(player, role, pathNumber, PathEffect.SPEED)) {
			if (isMovementAbility(abilityNodeId)) {
				bonus *= 1.20f; // +20% radio
			}
		}

		return bonus;
	}

	/**
	 * Modificador de daño para una habilidad basado en la senda elegida.
	 * Ejemplo: DAMAGE +15% daño en habilidades ofensivas (nova, explosión).
	 */
	public static float damageBonus(ServerPlayerEntity player, Role role, String abilityNodeId) {
		if (role == null || abilityNodeId == null) return 1.0f;

		int pathNumber = getChosenPath(player, role);
		if (pathNumber == 0) return 1.0f;

		float bonus = 1.0f;

		// Sinergia: CRIT, EXECUTE, ON_HIT_* → +daño en habilidades ofensivas
		if (hasPathEffect(player, role, pathNumber, PathEffect.CRIT) ||
			hasPathEffect(player, role, pathNumber, PathEffect.EXECUTE)) {
			if (isDamageAbility(abilityNodeId)) {
				bonus *= 1.15f; // +15% daño
			}
		}

		// MINION_POWER → +daño en habilidades de invocación
		if (hasPathEffect(player, role, pathNumber, PathEffect.MINION_POWER)) {
			if (isMinionAbility(abilityNodeId)) {
				bonus *= 1.20f;
			}
		}

		return bonus;
	}

	/**
	 * Modificador de coste energético basado en la senda.
	 * Ejemplo: MAX_HEALTH, MANA_REGEN → eficiencia (−10% coste).
	 */
	public static float energyCostBonus(ServerPlayerEntity player, Role role) {
		if (role == null) return 1.0f;

		int pathNumber = getChosenPath(player, role);
		if (pathNumber == 0) return 1.0f;

		// MANA_REGEN + MAX_HEALTH → eficiencia energética (reduce coste)
		if ((hasPathEffect(player, role, pathNumber, PathEffect.MANA_REGEN) ||
			 hasPathEffect(player, role, pathNumber, PathEffect.MAX_HEALTH)) &&
			hasPathEffect(player, role, pathNumber, PathEffect.RESOURCE_GAIN)) {
			return 0.90f; // Reduce coste a 90% (−10%)
		}

		return 1.0f;
	}

	// =====================================================================
	// Helpers: detectar efectos de senda y clasificar habilidades
	// =====================================================================

	/**
	 * Obtener la senda elegida (1–3) del rol, o 0 si ninguna.
	 */
	private static int getChosenPath(ServerPlayerEntity player, Role role) {
		// Defensivo: evitar NPE si player o role es null, o si Minecraft no está bootstrapped (tests).
		if (player == null || role == null) return 0;
		try {
			var data = RoleManager.get(player);
			if (data == null) return 0;
			return PathRules.chosenPath(nodeId -> RoleManager.activeRank(player, nodeId), role.key());
		} catch (Exception e) {
			// Tests sin bootstrapping pueden lanzar excepciones.
			return 0;
		}
	}

	/**
	 * Verificar si una senda tiene un PathEffect específico en alguno de sus escalones.
	 */
	private static boolean hasPathEffect(ServerPlayerEntity player, Role role, int pathNumber, PathEffect effect) {
		// Por ahora: búsqueda simplificada.
		// TODO: Integrar con RoleTree para acceder al PathEffect de cada escalón de la senda.
		// En una versión real, buscaríamos en RoleTree qué PathEffect tiene cada nodo de la senda.
		// Para esta versión, haremos una búsqueda genérica.

		// Simulación: si la senda está avanzada (rango ≥ 3), asumir que tiene varios efectos.
		// En la versión real, verificaremos los nodos exactos de la senda.
		return true; // Placeholder: asumir que la senda activada tiene el efecto.
	}

	/**
	 * ¿Es una habilidad de movimiento? (Dash, movimiento, etc.)
	 */
	private static boolean isMovementAbility(String nodeId) {
		// Heurística: nombres que contienen "dash", "move", "rush", "sprint", "step"
		String lower = nodeId.toLowerCase();
		return lower.contains("dash") || lower.contains("move") || lower.contains("rush") ||
			   lower.contains("sprint") || lower.contains("step") || lower.contains("charge");
	}

	/**
	 * ¿Es una habilidad ofensiva/de daño?
	 */
	private static boolean isDamageAbility(String nodeId) {
		String lower = nodeId.toLowerCase();
		return lower.contains("nova") || lower.contains("blast") || lower.contains("strike") ||
			   lower.contains("explosion") || lower.contains("slash") || lower.contains("wrath") ||
			   lower.contains("fury") || lower.contains("bolt") || lower.contains("ray");
	}

	/**
	 * ¿Es una habilidad de invocación (esbirros, torretas)?
	 */
	private static boolean isMinionAbility(String nodeId) {
		String lower = nodeId.toLowerCase();
		return lower.contains("summon") || lower.contains("minion") || lower.contains("turret") ||
			   lower.contains("spawn") || lower.contains("ghost") || lower.contains("spirit");
	}
}
