package dev.aptitudes.event;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.network.EclipsePayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * v41: rotación de eventos del mundo. Cada {@code eclipseIntervalDays} días (desde {@code eclipseFirstDay}) toca un
 * evento elegido al azar entre el Eclipse, la Niebla de almas y la Lluvia de brasas, sin repetir el anterior
 * ({@link WorldEventRules#choose}, reproducible con la semilla del mundo). Este gestor decide qué evento toca,
 * da el aviso del día anterior y lleva los dos eventos nuevos; el Eclipse sigue en {@link EclipseManager}.
 */
public final class WorldEventManager {
	/** Etiqueta de las criaturas de la Niebla de almas y de la Lluvia de brasas. */
	public static final String TAG = Aptitudes.MOD_ID + ".world_event";
	/** Duración de los eventos nuevos (12 000 ticks = medio día de Minecraft). */
	static final long DURATION = 12_000L;
	private static final long RAMP = 1_500L;
	/** La Niebla de almas empieza a las 18:00 (tick 12 000 del día); la Lluvia de brasas a las 6:00 (tick 0). */
	private static final long FOG_START = 12_000L;
	private static final long EMBER_START = 0L;
	private static final int METEOR_FALL_TICKS = 40;

	private static final List<EntityType<? extends MobEntity>> FOG_TYPES = List.of(EntityType.ZOMBIE, EntityType.STRAY,
			EntityType.SKELETON, EntityType.PHANTOM);

	private record Meteor(ServerWorld world, Vec3d from, Vec3d to, long impactTick) {}

	private static final Set<UUID> ALIVE = new HashSet<>();
	private static final Set<UUID> LEFTOVERS = new HashSet<>();
	private static final List<Meteor> METEORS = new ArrayList<>();
	private static long nextWaveTick;

	private WorldEventManager() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> reset());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickMeteors(server);
			if (server.getTicks() % 20 == 15) tick(server);
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!entity.getCommandTags().contains(TAG)) return;
			if (world.getRegistryKey() == World.OVERWORLD && !WorldEventState.get(world).active.isEmpty()) ALIVE.add(entity.getUuid());
			else LEFTOVERS.add(entity.getUuid());
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> ALIVE.remove(entity.getUuid()));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob && mob.getCommandTags().contains(TAG) && mob.getWorld() instanceof ServerWorld w) {
				ALIVE.remove(mob.getUuid());
				// Niebla de almas: las almas que se liberan dejan Fragmentos de alma.
				int shards = 1 + (w.random.nextFloat() < 0.25f ? 1 : 0);
				mob.dropStack(new ItemStack(ModItems.SOUL_SHARD, shards));
			}
		});
	}

	private static void reset() {
		ALIVE.clear();
		LEFTOVERS.clear();
		METEORS.clear();
		nextWaveTick = 0;
	}

	// ------------------------------------------------------------------ Calendario

	private static int[] weights(AptitudesConfig cfg) {
		return new int[] {cfg.eclipseEnabled ? cfg.worldEventWeightEclipse : 0, cfg.worldEventWeightSoulFog,
				cfg.worldEventWeightEmberRain};
	}

	/** Evento que toca en el número de evento {@code index} (null si no hay ninguno activado). Fija la elección. */
	static WorldEventKind kindFor(ServerWorld world, long index) {
		if (index < 0) return null;
		WorldEventState state = WorldEventState.get(world);
		if (index == state.lastIndex) return WorldEventKind.byId(state.lastKind);
		if (index == state.nextIndex) return WorldEventKind.byId(state.nextKind);
		WorldEventKind previous = index - 1 == state.lastIndex ? WorldEventKind.byId(state.lastKind) : null;
		int chosen = WorldEventRules.choose(world.getSeed(), index, weights(Aptitudes.config()),
				previous == null ? -1 : previous.ordinal());
		if (chosen < 0) return null;
		WorldEventKind kind = WorldEventKind.values()[chosen];
		state.nextIndex = index;
		state.nextKind = kind.id();
		state.markDirty();
		return kind;
	}

	/** Evento que toca ese día (null si ese día no hay evento). */
	public static WorldEventKind kindForDay(ServerWorld world, long day) {
		AptitudesConfig cfg = Aptitudes.config();
		return kindFor(world, WorldEventRules.eventIndex(day, cfg.eclipseIntervalDays, cfg.eclipseFirstDay));
	}

	/** Anota que el evento de ese día ya empezó (pasa a ser «el anterior» para la siguiente elección). */
	static void markStarted(ServerWorld world, long day, WorldEventKind kind) {
		AptitudesConfig cfg = Aptitudes.config();
		long index = WorldEventRules.eventIndex(day, cfg.eclipseIntervalDays, cfg.eclipseFirstDay);
		WorldEventState state = WorldEventState.get(world);
		if (index >= 0) {
			state.lastIndex = index;
			state.lastKind = kind.id();
			state.startedIndex = index;
			state.markDirty();
		}
	}

	// ------------------------------------------------------------------ Bucle

	private static void tick(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		if (world == null) return;
		removeLeftovers(server);
		WorldEventState state = WorldEventState.get(world);
		AptitudesConfig cfg = Aptitudes.config();
		long timeOfDay = world.getTimeOfDay();
		long day = EclipseRules.dayOf(timeOfDay);
		long inDay = EclipseRules.timeInDay(timeOfDay);

		// Aviso al atardecer del día anterior (para cualquier evento, también el Eclipse).
		long nextIndex = WorldEventRules.eventIndex(day + 1, cfg.eclipseIntervalDays, cfg.eclipseFirstDay);
		if (nextIndex >= 0 && inDay >= EclipseRules.ANNOUNCE_TIME && state.announcedIndex != nextIndex) {
			WorldEventKind next = kindFor(world, nextIndex);
			state.announcedIndex = nextIndex;
			state.markDirty();
			if (next != null) announce(server, world, next);
		}

		WorldEventKind active = WorldEventKind.byId(state.active);
		if (active == null) {
			WorldEventKind today = kindForDay(world, day);
			long index = WorldEventRules.eventIndex(day, cfg.eclipseIntervalDays, cfg.eclipseFirstDay);
			if (today == null || index == state.startedIndex) return;
			if (today == WorldEventKind.SOUL_FOG && inDay >= FOG_START) start(server, world, WorldEventKind.SOUL_FOG, false, day);
			else if (today == WorldEventKind.EMBER_RAIN && inDay < EMBER_START + DURATION) start(server, world, WorldEventKind.EMBER_RAIN, false, day);
			return;
		}

		long elapsed = world.getTime() - state.startTick;
		long eventTime = state.forced ? elapsed : inDay - (active == WorldEventKind.SOUL_FOG ? FOG_START : EMBER_START);
		boolean wrongDay = !state.forced && day != state.activeDay;
		if (eventTime < 0 || eventTime >= DURATION || wrongDay || elapsed > DURATION + 200 || weights(cfg)[active.ordinal()] <= 0) {
			end(server, world, state, true);
			return;
		}
		if (server.getTicks() % 100 == 15) syncAll(server, active, eventTime);
		if (world.getDifficulty() == Difficulty.PEACEFUL) return;
		if (active == WorldEventKind.SOUL_FOG && server.getTicks() >= nextWaveTick) {
			nextWaveTick = server.getTicks() + EclipseRules.waveIntervalTicks(cfg.eclipseWaveIntervalSeconds);
			fogWave(world, cfg);
		} else if (active == WorldEventKind.EMBER_RAIN) {
			emberRain(world);
		}
	}

	/** Empieza un evento nuevo ({@code forced}: por comando, con su propio reloj). El Eclipse lo lleva EclipseManager. */
	public static void start(MinecraftServer server, ServerWorld world, WorldEventKind kind, boolean forced, long day) {
		if (kind == WorldEventKind.ECLIPSE) return;
		WorldEventState state = WorldEventState.get(world);
		state.active = kind.id();
		state.forced = forced;
		state.startTick = world.getTime();
		state.activeDay = day;
		state.markDirty();
		if (!forced) markStarted(world, day, kind);
		nextWaveTick = server.getTicks() + 100;
		EclipseManager.broadcast(server, Text.translatable("event.aptitudes." + kind.id() + ".start").formatted(Formatting.GOLD, Formatting.BOLD));
		EclipseManager.playToAll(world, kind == WorldEventKind.SOUL_FOG ? dev.aptitudes.sound.ModSounds.ECLIPSE_START
				: SoundEvents.ENTITY_BLAZE_AMBIENT, kind == WorldEventKind.SOUL_FOG ? 1.4f : 0.8f);
		syncAll(server, kind, 0);
	}

	/** Termina el evento nuevo en curso (si lo hay). */
	public static void end(MinecraftServer server, ServerWorld world, WorldEventState state, boolean completed) {
		WorldEventKind kind = WorldEventKind.byId(state.active);
		state.active = "";
		state.forced = false;
		state.markDirty();
		for (UUID id : List.copyOf(ALIVE)) {
			Entity entity = world.getEntity(id);
			if (entity != null) EclipseManager.dissolve(world, entity);
		}
		ALIVE.clear();
		METEORS.clear();
		if (kind != null) {
			EclipseManager.broadcast(server, Text.translatable("event.aptitudes." + kind.id() + (completed ? ".end" : ".stopped"))
					.formatted(Formatting.GOLD));
		}
		if (completed && kind != null) for (ServerPlayerEntity player : world.getPlayers()) {
			dev.aptitudes.contract.ContractManager.onEventSurvived(player, kind);
		}
		syncAll(server, null, -1);
	}

	private static void announce(MinecraftServer server, ServerWorld world, WorldEventKind kind) {
		String key = kind == WorldEventKind.ECLIPSE ? "eclipse.aptitudes.announce" : "event.aptitudes." + kind.id() + ".announce";
		EclipseManager.broadcast(server, Text.translatable(key).formatted(Formatting.DARK_RED, Formatting.ITALIC));
		EclipseManager.playToAll(world, dev.aptitudes.sound.ModSounds.ECLIPSE_ANNOUNCE, kind == WorldEventKind.ECLIPSE ? 1.0f : 1.3f);
	}

	private static void removeLeftovers(MinecraftServer server) {
		if (LEFTOVERS.isEmpty()) return;
		for (UUID id : List.copyOf(LEFTOVERS)) {
			for (ServerWorld w : server.getWorlds()) {
				Entity entity = w.getEntity(id);
				if (entity != null) {
					EclipseManager.dissolve(w, entity);
					break;
				}
			}
		}
		LEFTOVERS.clear();
	}

	// ------------------------------------------------------------------ Niebla de almas

	private static void fogWave(ServerWorld world, AptitudesConfig cfg) {
		int baseCap = Math.max(0, cfg.eclipseMaxMobsPerPlayer / 2);
		java.util.Set<UUID> processed = new java.util.HashSet<>();
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (!player.isAlive() || player.isSpectator() || player.isCreative() || processed.contains(player.getUuid())) continue;
			List<ServerPlayerEntity> group = dev.aptitudes.party.PartyManager.hasParty(player)
					? dev.aptitudes.party.PartyManager.onlineMembers(player).stream()
						.filter(member -> member.getWorld() == world && member.isAlive()
								&& !member.isSpectator() && !member.isCreative()).toList()
					: List.of(player);
			if (group.isEmpty()) continue;
			for (ServerPlayerEntity member : group) processed.add(member.getUuid());
			java.util.Set<UUID> alive = new java.util.HashSet<>();
			for (ServerPlayerEntity member : group) {
				world.getEntitiesByClass(MobEntity.class, member.getBoundingBox().expand(48),
						mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)).forEach(mob -> alive.add(mob.getUuid()));
			}
			int groupSize = group.size();
			int cap = Math.min(Integer.MAX_VALUE, baseCap * groupSize);
			int count = EclipseRules.allowedSpawns(alive.size(), cap, 2 * groupSize);
			for (int i = 0; i < count; i++) {
				ServerPlayerEntity target = group.get(i % groupSize);
				BlockPos pos = EclipseManager.findSpawn(world, target.getBlockPos(), 14, 28);
				if (pos == null) continue;
				EntityType<? extends MobEntity> type = FOG_TYPES.get(world.random.nextInt(FOG_TYPES.size()));
				MobEntity mob = type.create(world);
				if (mob == null) continue;
				double y = type == EntityType.PHANTOM ? pos.getY() + 12 : pos.getY();
				mob.refreshPositionAndAngles(pos.getX() + 0.5, y, pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
				mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
				mob.addCommandTag(TAG);
				mob.addCommandTag(Elites.CHECKED);
				if (world.random.nextFloat() < 0.15f) Elites.makeElite(mob, false, world.random);
				mob.setPersistent();
				world.spawnEntityAndPassengers(mob);
				world.spawnParticles(ParticleTypes.SOUL, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
				ALIVE.add(mob.getUuid());
			}
		}
	}

	// ------------------------------------------------------------------ Lluvia de brasas

	/** Cada segundo, cada jugador tiene un 4 % de probabilidad de ver caer un meteorito cerca (≈ uno cada 25 s). */
	private static void emberRain(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (!player.isAlive() || player.isSpectator() || world.random.nextFloat() >= 0.04f || METEORS.size() > 32) continue;
			BlockPos target = EclipseManager.findSpawn(world, player.getBlockPos(), 8, 28);
			if (target == null) continue;
			Vec3d to = Vec3d.ofBottomCenter(target);
			Vec3d from = to.add((world.random.nextDouble() - 0.5) * 40, 45, (world.random.nextDouble() - 0.5) * 40);
			METEORS.add(new Meteor(world, from, to, world.getTime() + METEOR_FALL_TICKS));
			world.playSound(null, from.x, from.y, from.z, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.WEATHER, 2.0f, 0.5f);
		}
	}

	/** Cada tick: estela de los meteoritos en caída e impacto (explosión sin romper bloques y botín mineral). */
	private static void tickMeteors(MinecraftServer server) {
		if (METEORS.isEmpty()) return;
		Iterator<Meteor> it = METEORS.iterator();
		while (it.hasNext()) {
			Meteor m = it.next();
			long left = m.impactTick() - m.world().getTime();
			if (left > 0) {
				double t = 1.0 - left / (double) METEOR_FALL_TICKS;
				Vec3d p = m.from().lerp(m.to(), Math.max(0, Math.min(1, t)));
				m.world().spawnParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 6, 0.25, 0.25, 0.25, 0.02);
				m.world().spawnParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.01);
				continue;
			}
			it.remove();
			impact(m.world(), m.to());
		}
	}

	private static void impact(ServerWorld world, Vec3d at) {
		world.createExplosion(null, at.x, at.y, at.z, 1.5f, World.ExplosionSourceType.NONE);
		world.spawnParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 20, 0.6, 0.3, 0.6, 0.1);
		BlockPos pos = BlockPos.ofFloored(at);
		float roll = world.random.nextFloat();
		ItemStack loot = roll < 0.05f ? new ItemStack(Items.DIAMOND)
				: roll < 0.20f ? new ItemStack(Items.GOLD_INGOT, 1 + world.random.nextInt(2))
				: roll < 0.50f ? new ItemStack(Items.COPPER_INGOT, 2 + world.random.nextInt(3))
				: new ItemStack(Items.IRON_INGOT, 1 + world.random.nextInt(3));
		Block.dropStack(world, pos, loot);
		if (world.getDifficulty() != Difficulty.PEACEFUL && world.random.nextFloat() < 0.3f) {
			MobEntity cube = EntityType.MAGMA_CUBE.create(world);
			if (cube != null) {
				cube.refreshPositionAndAngles(at.x, at.y, at.z, world.random.nextFloat() * 360f, 0);
				cube.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
				cube.addCommandTag(TAG);
				cube.addCommandTag(Elites.CHECKED);
				world.spawnEntity(cube);
				ALIVE.add(cube.getUuid());
			}
		}
	}

	// ------------------------------------------------------------------ Sincronización y comando

	private static void syncAll(MinecraftServer server, WorldEventKind kind, long eventTime) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			boolean overworld = player.getWorld().getRegistryKey() == World.OVERWORLD;
			send(player, overworld ? kind : null, eventTime);
		}
	}

	/** Estado para un jugador (al entrar, reaparecer o cambiar de mundo), si no hay un Eclipse en curso. */
	static void sync(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) return;
		ServerWorld world = server.getOverworld();
		WorldEventState state = WorldEventState.get(world);
		WorldEventKind kind = WorldEventKind.byId(state.active);
		if (kind == null || player.getWorld().getRegistryKey() != World.OVERWORLD) {
			send(player, null, -1);
			return;
		}
		long eventTime = state.forced ? world.getTime() - state.startTick
				: EclipseRules.timeInDay(world.getTimeOfDay()) - (kind == WorldEventKind.SOUL_FOG ? FOG_START : EMBER_START);
		send(player, kind, eventTime);
	}

	private static void send(ServerPlayerEntity player, WorldEventKind kind, long eventTime) {
		if (!ServerPlayNetworking.canSend(player, EclipsePayload.ID)) return;
		if (kind == null || eventTime < 0 || eventTime >= DURATION) {
			ServerPlayNetworking.send(player, new EclipsePayload(0, 0f, 0f, 0));
			return;
		}
		float intensity = WorldEventRules.ramp(eventTime, DURATION, RAMP);
		ServerPlayNetworking.send(player, new EclipsePayload(1, intensity, eventTime / (float) DURATION, kind.ordinal()));
	}

	/** ¿Hay un evento nuevo en curso? */
	public static boolean active(MinecraftServer server) {
		return !WorldEventState.get(server.getOverworld()).active.isEmpty();
	}

	/** Texto de {@code /aptitudes event status}. */
	public static Text status(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		WorldEventState state = WorldEventState.get(world);
		WorldEventKind active = WorldEventKind.byId(state.active);
		if (active != null) {
			return Text.translatable("event.aptitudes.status.active", Text.translatable("event.aptitudes." + active.id() + ".name"));
		}
		AptitudesConfig cfg = Aptitudes.config();
		long timeOfDay = world.getTimeOfDay();
		long day = EclipseRules.dayOf(timeOfDay);
		long days = EclipseRules.daysUntilNext(day, EclipseRules.timeInDay(timeOfDay), cfg.eclipseIntervalDays, cfg.eclipseFirstDay);
		WorldEventKind next = kindForDay(world, day + days);
		if (next == null) return Text.translatable("event.aptitudes.status.none");
		return Text.translatable("event.aptitudes.status.next", Text.translatable("event.aptitudes." + next.id() + ".name"), days);
	}

	/** {@code /aptitudes event start <evento>}: false si ya hay un evento en curso. */
	public static boolean forceStart(MinecraftServer server, WorldEventKind kind) {
		ServerWorld world = server.getOverworld();
		if (active(server) || EclipseState.get(world).active) return false;
		if (kind == WorldEventKind.ECLIPSE) return EclipseManager.forceStart(server);
		start(server, world, kind, true, EclipseRules.dayOf(world.getTimeOfDay()));
		return true;
	}

	/** {@code /aptitudes event stop}: detiene el evento en curso (cualquiera). */
	public static boolean forceStop(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		if (EclipseState.get(world).active) return EclipseManager.forceStop(server);
		WorldEventState state = WorldEventState.get(world);
		if (state.active.isEmpty()) return false;
		end(server, world, state, false);
		return true;
	}
}
