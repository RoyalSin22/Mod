package dev.aptitudes.client;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.network.CooldownPayload;
import dev.aptitudes.network.EnergyPayload;
import dev.aptitudes.network.RoleSyncPayload;
import org.jetbrains.annotations.Nullable;

/**
 * Copia en el cliente del estado de roles. Los enfriamientos se descuentan
 * localmente cada tick a partir del último valor recibido (el servidor sigue
 * siendo quien decide si una habilidad puede usarse).
 */
public final class ClientRoleState {
	private static RoleSyncPayload data;
	private static float energy;
	private static float maxEnergy = 100f;
	private static float resourcePrimary;
	private static float resourceSecondary;
	private static final int[] cooldowns = new int[AbilityManager.SLOTS];
	private static final int[] cooldownTotals = new int[AbilityManager.SLOTS];

	private ClientRoleState() {
	}

	static void onSync(RoleSyncPayload payload) {
		data = payload;
		for (int i = 0; i < cooldowns.length; i++) {
			int remaining = i < payload.slotCooldowns().length ? payload.slotCooldowns()[i] : 0;
			cooldowns[i] = remaining;
			cooldownTotals[i] = Math.max(remaining, 1);
		}
	}

	static void onEnergy(EnergyPayload payload) {
		energy = payload.energy();
		maxEnergy = Math.max(1f, payload.maxEnergy());
	}

	static void onResources(dev.aptitudes.network.ResourcePayload payload) {
		resourcePrimary = payload.primary();
		resourceSecondary = payload.secondary();
	}

	public static float resource(boolean primary) {
		return primary ? resourcePrimary : resourceSecondary;
	}

	static void onCooldown(CooldownPayload payload) {
		if (payload.slot() < 0 || payload.slot() >= cooldowns.length) return;
		cooldowns[payload.slot()] = payload.ticks();
		cooldownTotals[payload.slot()] = Math.max(1, payload.ticks());
	}

	static void tick() {
		for (int i = 0; i < cooldowns.length; i++) {
			if (cooldowns[i] > 0) cooldowns[i]--;
		}
	}

	static void clear() {
		data = null;
		energy = 0;
		maxEnergy = 100f;
		resourcePrimary = 0;
		resourceSecondary = 0;
		java.util.Arrays.fill(cooldowns, 0);
	}

	@Nullable
	public static RoleSyncPayload data() {
		return data;
	}

	public static float energy() {
		return energy;
	}

	public static float maxEnergy() {
		return maxEnergy;
	}

	public static int cooldown(int slot) {
		return cooldowns[slot];
	}

	public static float cooldownFraction(int slot) {
		return cooldowns[slot] / (float) cooldownTotals[slot];
	}
}
