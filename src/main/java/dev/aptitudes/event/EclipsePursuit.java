package dev.aptitudes.event;

import dev.aptitudes.ability.AbilityState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Memoria de persecución del servidor para las criaturas cargadas del Eclipse. */
final class EclipsePursuit {
	private static final int VISIBLE_CANDIDATES = 4;
	private static final double SEARCH_SPEED = 1.0;
	private static final TargetPredicate VISIBLE = TargetPredicate.createAttackable().setBaseMaxDistance(EclipsePursuitRules.RANGE);
	private static final Map<UUID, Sighting> SIGHTINGS = new HashMap<>();

	private record Sighting(UUID player, BlockPos position, long seenAt, @Nullable Path path) {}

	private EclipsePursuit() {
	}

	static void tick(ServerWorld world, Set<UUID> mobs) {
		long now = world.getTime();
		for (UUID id : mobs) {
			if (!EclipsePursuitRules.due(id.hashCode(), now)) continue;
			if (!(world.getEntity(id) instanceof MobEntity mob) || !mob.isAlive()) {
				forget(id);
			} else if (mob.isAiDisabled() || !mob.getCommandTags().contains(EclipseManager.TAG)) {
				stopSearch(mob, SIGHTINGS.remove(id));
			} else {
				update(world, mob, now);
			}
		}
	}

	static void seed(ServerWorld world, MobEntity mob, ServerPlayerEntity target) {
		if (eligible(world, mob, target) && visible(world, mob, target)) {
			follow(mob, target, world.getTime(), null);
		}
	}

	private static void update(ServerWorld world, MobEntity mob, long now) {
		UUID id = mob.getUuid();
		Sighting last = SIGHTINGS.get(id);
		LivingEntity current = mob.getTarget();
		if (current != null && !(current instanceof ServerPlayerEntity)) {
			// Las metas de combate del mob siguen a cargo de sus objetivos ajenos a la persecución.
			stopSearch(mob, last);
			forget(id);
			return;
		}

		ServerPlayerEntity active = (ServerPlayerEntity) current;
		if (active != null && eligible(world, mob, active) && visible(world, mob, active)) {
			follow(mob, active, now, last);
			return;
		}

		ServerPlayerEntity remembered = last != null && world.getEntity(last.player()) instanceof ServerPlayerEntity player
				&& eligible(world, mob, player) ? player : null;
		if (remembered != null && remembered != active && visible(world, mob, remembered)) {
			follow(mob, remembered, now, last);
			return;
		}
		ServerPlayerEntity found = nearestVisible(world, mob, active, remembered);
		if (found != null) {
			follow(mob, found, now, last);
			return;
		}

		clearPlayerTarget(mob);
		if (last != null && remembered != null && search(world, mob, last, now)) return;
		stopSearch(mob, last);
		forget(id);
	}

	private static boolean eligible(ServerWorld world, MobEntity mob, ServerPlayerEntity player) {
		return player.getWorld() == world && !player.isDisconnected() && EclipseManager.eligible(player)
				&& EclipsePursuitRules.inRange(mob.squaredDistanceTo(player))
				&& world.getWorldBorder().contains(player.getBlockPos())
				&& mob.canTarget(player) && !mob.isTeammate(player)
				&& !AbilityState.shouldBlockTarget(mob, player);
	}

	private static boolean visible(ServerWorld world, MobEntity mob, ServerPlayerEntity player) {
		return loadedBetween(world, mob.getBlockPos(), player.getBlockPos()) && VISIBLE.test(mob, player);
	}

	@Nullable
	private static ServerPlayerEntity nearestVisible(ServerWorld world, MobEntity mob,
			@Nullable ServerPlayerEntity active, @Nullable ServerPlayerEntity remembered) {
		var nearby = world.getPlayers(player -> player != active && player != remembered && eligible(world, mob, player));
		nearby.sort(Comparator.comparingDouble(mob::squaredDistanceTo));
		for (int i = 0; i < Math.min(nearby.size(), VISIBLE_CANDIDATES); i++) {
			ServerPlayerEntity player = nearby.get(i);
			if (visible(world, mob, player)) return player;
		}
		return null;
	}

	private static void follow(MobEntity mob, ServerPlayerEntity player, long now, @Nullable Sighting last) {
		stopSearch(mob, last);
		if (mob.getTarget() != player) {
			mob.getNavigation().stop();
			mob.setTarget(player);
		}
		if (mob.getTarget() == player) {
			SIGHTINGS.put(mob.getUuid(), new Sighting(player.getUuid(), player.getBlockPos(), now, null));
		}
	}

	private static boolean search(ServerWorld world, MobEntity mob, Sighting last, long now) {
		BlockPos pos = last.position();
		Vec3d destination = Vec3d.ofBottomCenter(pos);
		if (!EclipsePursuitRules.canSearch(now, last.seenAt(), mob.squaredDistanceTo(destination))
				|| !world.getWorldBorder().contains(pos) || !loadedBetween(world, mob.getBlockPos(), pos)) return false;
		var navigation = mob.getNavigation();
		if (!navigation.isIdle() && navigation.getCurrentPath() == last.path()) return true;
		if (navigation.startMovingTo(destination.x, destination.y, destination.z, SEARCH_SPEED)) {
			SIGHTINGS.put(mob.getUuid(), new Sighting(last.player(), pos, last.seenAt(), navigation.getCurrentPath()));
		}
		return true;
	}

	/** Limita la consulta de visión y destino a chunks que ya están cargados. */
	private static boolean loadedBetween(ServerWorld world, BlockPos from, BlockPos to) {
		int minX = Math.min(from.getX(), to.getX()) >> 4;
		int maxX = Math.max(from.getX(), to.getX()) >> 4;
		int minZ = Math.min(from.getZ(), to.getZ()) >> 4;
		int maxZ = Math.max(from.getZ(), to.getZ()) >> 4;
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if (!world.isChunkLoaded(x, z)) return false;
			}
		}
		return true;
	}

	private static void stopSearch(MobEntity mob, @Nullable Sighting last) {
		if (last != null && last.path() != null && mob.getNavigation().getCurrentPath() == last.path()) {
			mob.getNavigation().stop();
		}
	}

	private static void clearPlayerTarget(MobEntity mob) {
		if (mob.getTarget() instanceof ServerPlayerEntity) {
			mob.setTarget(null);
			mob.getNavigation().stop();
		}
	}

	static void forget(UUID mob) {
		SIGHTINGS.remove(mob);
	}

	static void forgetPlayer(ServerWorld overworld, ServerPlayerEntity player) {
		if (overworld == null) return;
		SIGHTINGS.entrySet().removeIf(entry -> {
			Sighting sighting = entry.getValue();
			if (!sighting.player().equals(player.getUuid())) return false;
			if (overworld.getEntity(entry.getKey()) instanceof MobEntity mob) {
				stopSearch(mob, sighting);
				if (mob.getTarget() == player) clearPlayerTarget(mob);
			}
			return true;
		});
	}

	static void clear() {
		SIGHTINGS.clear();
	}
}
