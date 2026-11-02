package dev.aptitudes.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Filtro de objetivos para mobs con IA de tipo "Brain" (piglins, piglins brutos, hoglins, zoglins,
 * warden…). Estos mobs no eligen objetivo con {@code MobEntity#setTarget} (donde actúa
 * {@code MobEntityMixin}), sino con la memoria {@code ATTACK_TARGET} de su cerebro.
 *
 * <p>Cada 5 ticks se revisan los mobs con cerebro cercanos a cada jugador: si su objetivo es alguien a
 * quien {@link AbilityState#shouldBlockTarget} protege (Soborno, camuflaje del Cambiaformas, Heraldo de
 * la Muerte, invocaciones), se borran su objetivo y su enfado contra él. Sin mixins: no depende de
 * nombres internos del cerebro. Limitación: entre dos revisiones el mob puede empezar un ataque.</p>
 */
public final class BrainTargetFilter {
	private static final int INTERVAL_TICKS = 5;
	private static final double RADIUS = 32.0;

	private BrainTargetFilter() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(BrainTargetFilter::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTicks() % INTERVAL_TICKS != 0) return;
		Set<UUID> checked = new HashSet<>();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (player.isSpectator()) continue;
			for (MobEntity mob : player.getWorld().getEntitiesByClass(MobEntity.class,
					player.getBoundingBox().expand(RADIUS), m -> m.isAlive() && m.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET))) {
				if (checked.add(mob.getUuid())) filter(mob);
			}
		}
	}

	private static void filter(MobEntity mob) {
		Brain<?> brain = mob.getBrain();
		LivingEntity target = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
		if (target == null || !AbilityState.shouldBlockTarget(mob, target)) return;
		brain.forget(MemoryModuleType.ATTACK_TARGET);
		// Piglins y similares: el enfado (ANGRY_AT) volvería a fijar el objetivo enseguida.
		if (brain.hasMemoryModule(MemoryModuleType.ANGRY_AT)
				&& brain.getOptionalRegisteredMemory(MemoryModuleType.ANGRY_AT).map(target.getUuid()::equals).orElse(false)) {
			brain.forget(MemoryModuleType.ANGRY_AT);
		}
		if (mob.getTarget() == target) mob.setTarget(null);
	}
}
