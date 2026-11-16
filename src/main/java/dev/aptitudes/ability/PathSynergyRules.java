package dev.aptitudes.ability;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.Y1Rules.PathProfile;
import dev.aptitudes.role.PathEffect;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleTree;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * v65 C5 Y1: sinergia senda ↔ activas. Lee los efectos reales de los nodos aprendidos de la senda elegida del rol
 * y delega las decisiones en {@link Y1Rules}, que es pura y está probada.
 */
public final class PathSynergyRules {
	private PathSynergyRules() {
	}

	public static float radiusBonus(ServerPlayerEntity player, Role role, String abilityId) {
		if (role == null || abilityId == null) return 1.0f;
		return Y1Rules.radius(profile(player, role), abilityId, Aptitudes.config().synergyBonusY1Radius);
	}

	public static float damageBonus(ServerPlayerEntity player, Role role, String abilityId) {
		if (role == null || abilityId == null) return 1.0f;
		return Y1Rules.damage(profile(player, role), abilityId, Aptitudes.config().synergyBonusY1Damage);
	}

	public static float energyCostBonus(ServerPlayerEntity player, Role role) {
		if (role == null) return 1.0f;
		return Y1Rules.energy(profile(player, role), Aptitudes.config().synergyBonusY1Energy);
	}

	/** Efectos de los nodos con rango de la senda elegida del rol (solo ese rol, no los de otros roles). */
	private static PathProfile profile(ServerPlayerEntity player, Role role) {
		if (player == null) return PathProfile.EMPTY;
		int path = PathRules.chosenPath(id -> RoleManager.activeRank(player, id), role.key());
		if (path <= 0) return PathProfile.EMPTY;
		boolean speed = false, crit = false, execute = false, minion = false, mana = false, health = false, resource = false;
		for (PathRules.Step step : PathRules.Step.values()) {
			String id = PathRules.nodeId(role.key(), path, step);
			RoleTree.PathSpec spec = RoleTree.pathSpecs().get(id);
			if (spec == null || RoleManager.activeRank(player, id) <= 0) continue;
			PathEffect effect = spec.effect();
			switch (effect) {
				case SPEED -> speed = true;
				case CRIT -> crit = true;
				case EXECUTE -> execute = true;
				case MINION_POWER -> minion = true;
				case MANA_REGEN -> mana = true;
				case MAX_HEALTH -> health = true;
				case RESOURCE_GAIN -> resource = true;
				default -> { }
			}
		}
		return new PathProfile(speed, crit, execute, minion, mana, health, resource);
	}
}
