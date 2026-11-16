package dev.aptitudes.ability;

import dev.aptitudes.achievement.Achievement;
import dev.aptitudes.achievement.Achievements;
import dev.aptitudes.network.SynergyPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * v65 C5: registra qué sinergias ha activado cada jugador en la sesión, avisa al HUD (como mucho una vez por segundo
 * por sinergia) y concede «Sinergista» al completar las cinco. Estado en memoria: se pierde al desconectar.
 */
public final class SynergyTracker {
	private static final long NOTIFY_INTERVAL_TICKS = 20;
	private static final net.minecraft.particle.SimpleParticleType[] PARTICLES = {ParticleTypes.ENCHANT, ParticleTypes.HAPPY_VILLAGER,
			ParticleTypes.FLAME, ParticleTypes.END_ROD, ParticleTypes.ELECTRIC_SPARK};
	private static final Map<UUID, Integer> MASKS = new HashMap<>();
	private static final Map<UUID, long[]> LAST_NOTIFY = new HashMap<>();

	private SynergyTracker() {
	}

	public static void register() {
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			MASKS.remove(handler.getPlayer().getUuid());
			LAST_NOTIFY.remove(handler.getPlayer().getUuid());
		});
	}

	/** Una sinergia (0 = Y1 … 4 = Y5) se ha aplicado de verdad a este jugador. */
	public static void mark(ServerPlayerEntity player, int index) {
		if (player == null || index < 0 || index >= SynergyMask.COUNT || player.getServer() == null) return;
		UUID id = player.getUuid();
		int before = MASKS.getOrDefault(id, 0);
		int after = SynergyMask.add(before, index);
		MASKS.put(id, after);
		long now = player.getServer().getTicks();
		long[] last = LAST_NOTIFY.computeIfAbsent(id, k -> new long[SynergyMask.COUNT]);
		if (now - last[index] >= NOTIFY_INTERVAL_TICKS || last[index] == 0) {
			last[index] = now;
			player.getServerWorld().spawnParticles(PARTICLES[index], player.getX(), player.getBodyY(0.6), player.getZ(),
					8, 0.35, 0.4, 0.35, 0.03);
			if (ServerPlayNetworking.canSend(player, SynergyPayload.ID)) {
				ServerPlayNetworking.send(player, new SynergyPayload(index));
			}
		}
		if (after != before && SynergyMask.complete(after)) Achievements.grant(player, Achievement.SYNERGIST);
	}

	public static int mask(ServerPlayerEntity player) {
		return MASKS.getOrDefault(player.getUuid(), 0);
	}
}
