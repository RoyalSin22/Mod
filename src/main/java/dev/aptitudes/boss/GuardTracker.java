package dev.aptitudes.boss;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Fase B3: registra desde qué tick del servidor cada jugador tiene el escudo levantado, para la guardia perfecta de
 * {@link GuardRules}. Estado en memoria: se borra al parar el servidor y al dejar de bloquear o desconectarse.
 */
public final class GuardTracker {
	private static final Map<UUID, Integer> RAISED_AT = new HashMap<>();

	private GuardTracker() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(GuardTracker::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> RAISED_AT.clear());
	}

	private static void tick(MinecraftServer server) {
		int now = server.getTicks();
		Set<UUID> online = new HashSet<>();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			online.add(p.getUuid());
			if (p.isBlocking()) RAISED_AT.putIfAbsent(p.getUuid(), now);
			else RAISED_AT.remove(p.getUuid());
		}
		RAISED_AT.keySet().removeIf(id -> !online.contains(id));
	}

	/** Ticks desde que el jugador levantó el escudo, o {@link GuardRules#NOT_BLOCKING}. */
	public static int ticksSinceRaised(UUID player, int now) {
		Integer raised = RAISED_AT.get(player);
		return raised == null ? GuardRules.NOT_BLOCKING : Math.max(0, now - raised);
	}
}
