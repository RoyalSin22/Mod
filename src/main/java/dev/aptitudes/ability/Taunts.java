package dev.aptitudes.ability;

import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.elite.Elites;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Fase F1 cableada: provocación de la Consagración del Paladín. Al usarla, los élites y campeones cercanos (nunca los
 * jefes) cambian su objetivo al Paladín durante {@link TauntRules#DURATION_TICKS} ticks. El estado vive en memoria del
 * servidor; las reglas puras están en {@link TauntRules}.
 */
public final class Taunts {
	/** Cuándo puede volver a provocar cada jugador (tick del mundo). */
	private static final Map<UUID, Long> READY_AT = new HashMap<>();
	/** Provocaciones activas: mob → provocación. */
	private static final Map<UUID, Active> ACTIVE = new HashMap<>();

	private record Active(MobEntity mob, UUID player, long startTick) {}

	private Taunts() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> tick());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			READY_AT.clear();
			ACTIVE.clear();
		});
	}

	/** Provoca a los élites y campeones cercanos. Devuelve cuántos quedaron provocados (0 si aún está en espera). */
	public static int apply(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world)) return 0;
		long now = world.getTime();
		if (!TauntRules.canTaunt(READY_AT.getOrDefault(player.getUuid(), 0L), now)) return 0;
		int count = 0;
		Box box = player.getBoundingBox().expand(TauntRules.RADIUS);
		for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, box, MobEntity::isAlive)) {
			if (!TauntRules.eligible(Elites.isElite(mob), isBoss(mob))) continue;
			if (mob.squaredDistanceTo(player) > TauntRules.RADIUS * TauntRules.RADIUS) continue;
			if (AbilityState.shouldBlockTarget(mob, player)) continue;
			ACTIVE.put(mob.getUuid(), new Active(mob, player.getUuid(), now));
			mob.setTarget(player);
			count++;
		}
		if (count > 0) {
			READY_AT.put(player.getUuid(), TauntRules.readyAfter(now));
			world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 0.8f, 1.2f);
		}
		return count;
	}

	/** Mantiene el objetivo mientras dura la provocación y olvida las que ya no aplican. */
	private static void tick() {
		for (Iterator<Map.Entry<UUID, Active>> it = ACTIVE.entrySet().iterator(); it.hasNext(); ) {
			Active a = it.next().getValue();
			MobEntity mob = a.mob();
			if (!mob.isAlive() || mob.isRemoved() || !(mob.getWorld() instanceof ServerWorld world)) {
				it.remove();
				continue;
			}
			long now = world.getTime();
			if (!TauntRules.isTaunting(a.startTick(), now)) {
				it.remove();
				continue;
			}
			ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(a.player());
			if (player == null || !player.isAlive()) {
				it.remove();
				continue;
			}
			if (TauntRules.shouldRetarget(true, mob.distanceTo(player)) && mob.getTarget() != player) {
				mob.setTarget(player);
			}
		}
	}

	/** Jefe animado o jefe propio (con etiqueta de jefe): nunca se provocan. */
	private static boolean isBoss(MobEntity mob) {
		if (mob instanceof AnimatedBossEntity) return true;
		for (String tag : mob.getCommandTags()) {
			if (BossKind.fromTag(tag) != null) return true;
		}
		return false;
	}
}
