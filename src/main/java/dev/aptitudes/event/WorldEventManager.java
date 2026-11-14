package dev.aptitudes.event;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.mob.ErranteEntity;
import dev.aptitudes.network.EclipsePayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * v41: rotación de eventos del mundo. Cada {@code eclipseIntervalDays} días (desde {@code eclipseFirstDay}) toca un
 * evento elegido al azar entre el Eclipse, la Niebla de almas y la Lluvia de brasas, sin repetir el anterior
 * ({@link WorldEventRules#choose}, reproducible con la semilla del mundo). Este gestor decide qué evento toca,
 * da el aviso del día anterior y lleva los dos eventos nuevos; el Eclipse sigue en {@link EclipseManager}.
 *
 * <p>T1 de la cadena narrativa: el calendario solo programa el Eclipse. La Niebla de almas la inicia un jugador
 * hablando con el aldeano del gremio y la Lluvia de brasas, al encender un portal al Nether ({@link EventTriggerRules}).</p>
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


	private record Meteor(ServerWorld world, Vec3d from, Vec3d to, long impactTick, StrongEventRules.MeteorSize size) {}

	private static final Set<UUID> ALIVE = new HashSet<>();
	private static final Map<UUID, ErranteEntity> FOG_ERRANTES = new HashMap<>();
	private static final PriorityQueue<ErranteEntity> FOG_RETIREMENTS =
			new PriorityQueue<>(Comparator.comparingLong(ErranteEntity::retireAt).thenComparing(ErranteEntity::getUuid));
	private static final Set<UUID> LEFTOVERS = new HashSet<>();
	private static final List<Meteor> METEORS = new ArrayList<>();
	/** T1: comprobaciones pendientes de si un jugador acaba de abrir un portal al Nether (solo en memoria). */
	private record PortalCheck(ServerWorld world, BlockPos pos, long checkTick, int portalBlocksBefore) {}

	private static final List<PortalCheck> PORTAL_CHECKS = new ArrayList<>();
	private static long nextWaveTick;

	private WorldEventManager() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> resetFogErrantes());
		ServerLifecycleEvents.SERVER_STARTED.register(server -> reset());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			reset();
			resetFogErrantes();
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickMeteors(server);
			tickPortalChecks(server);
			if (server.getTicks() % 20 == 15) tick(server);
			tickFogRetirements(server.getOverworld().getTime());
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ErranteEntity errante) {
				if (errante.isAlive() && FogErranteRules.eventOwned(errante.fogSession(), errante.getCommandTags().contains(TAG))) {
					FOG_ERRANTES.put(errante.getUuid(), errante);
					WorldEventState state = WorldEventState.get(world.getServer().getOverworld());
					if (!FogErranteRules.belongsToSession(errante.fogSession(), activeFogSession(state), world.getRegistryKey() == World.OVERWORLD)
							|| errante.isRetiring()) {
						long now = world.getServer().getOverworld().getTime();
						long closedAt = errante.fogSession() == null ? now
								: errante.fogSession().equals(state.lastClosedFogSession) ? state.lastFogCloseTick
								: Math.max(0, now - FogErranteRules.MAX_DELAY);
						queueFogRetirement(errante, closedAt);
					}
				}
				return;
			}
			if (!entity.getCommandTags().contains(TAG)) return;
			if (world.getRegistryKey() == World.OVERWORLD && !WorldEventState.get(world).active.isEmpty()) ALIVE.add(entity.getUuid());
			else LEFTOVERS.add(entity.getUuid());
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			ALIVE.remove(entity.getUuid());
			if (entity instanceof ErranteEntity errante) untrackFogErrante(errante);
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ErranteEntity errante) {
				untrackFogErrante(errante);
				if (errante.isRetiring()) return;
			}
			if (entity instanceof MobEntity mob && mob.getCommandTags().contains(TAG) && mob.getWorld() instanceof ServerWorld w) {
				ALIVE.remove(mob.getUuid());
				// Niebla de almas: las almas que se liberan dejan Fragmentos de alma.
				int shards = 1 + (w.random.nextFloat() < 0.25f ? 1 : 0);
				mob.dropStack(new ItemStack(ModItems.SOUL_SHARD, shards));
			}
		});
		// T1: la Niebla la inicia el aldeano del gremio y la Lluvia de brasas, un portal encendido por un jugador.
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> talkToGuildmaster(player, world, hand, entity));
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> watchPortalLighting(player, world, hand, hit));
	}

	private static void resetFogErrantes() {
		FOG_ERRANTES.clear();
		FOG_RETIREMENTS.clear();
	}

	private static void reset() {
		ALIVE.clear();
		LEFTOVERS.clear();
		METEORS.clear();
		PORTAL_CHECKS.clear();
		nextWaveTick = 0;
	}

	private static UUID activeFogSession(WorldEventState state) {
		return WorldEventKind.SOUL_FOG.id().equals(state.active) ? state.fogSession : null;
	}

	public static boolean canDropVeil(ErranteEntity errante, ServerWorld world) {
		WorldEventState state = WorldEventState.get(world.getServer().getOverworld());
		return FogErranteRules.eligibleLoot(errante.fogSession(), activeFogSession(state),
				world.getRegistryKey() == World.OVERWORLD, errante.isRetiring());
	}

	private static void queueFogRetirement(ErranteEntity errante, long closedAt) {
		errante.beginFogRetirement(FogErranteRules.removalAt(errante.getUuid(), closedAt));
		FOG_RETIREMENTS.add(errante);
	}

	private static void untrackFogErrante(ErranteEntity errante) {
		FOG_ERRANTES.remove(errante.getUuid(), errante);
		if (errante.isRetiring()) FOG_RETIREMENTS.remove(errante);
	}

	private static void tickFogRetirements(long now) {
		for (int count = 0; count < FogErranteRules.MAX_REMOVALS_PER_TICK; count++) {
			ErranteEntity next = FOG_RETIREMENTS.peek();
			if (next == null || next.retireAt() > now) break;
			FOG_RETIREMENTS.remove();
			if (FOG_ERRANTES.get(next.getUuid()) == next && next.getWorld() instanceof ServerWorld world) {
				FOG_ERRANTES.remove(next.getUuid());
				next.finishFogRetirement(world);
			}
		}
	}

	// ------------------------------------------------------------------ Calendario

	private static int[] weights(AptitudesConfig cfg) {
		return new int[] {cfg.eclipseEnabled ? cfg.worldEventWeightEclipse : 0, cfg.worldEventWeightSoulFog,
				cfg.worldEventWeightEmberRain};
	}

	/**
	 * T1: el calendario solo programa el Eclipse (hasta que el Behelit lo sustituya). La Niebla y la Lluvia las activa el
	 * jugador; sus pesos solo siguen valiendo para desactivarlas (0), ver {@link #weights}.
	 */
	private static int[] calendarWeights(AptitudesConfig cfg) {
		return new int[] {cfg.eclipseEnabled ? cfg.worldEventWeightEclipse : 0, 0, 0};
	}

	/** Duración de un evento nuevo: la de la Niebla es configurable; la de la Lluvia de brasas sigue siendo medio día. */
	static long duration(WorldEventKind kind) {
		return kind == WorldEventKind.SOUL_FOG ? EventTriggerRules.fogDuration(Aptitudes.config().fogDurationTicks) : DURATION;
	}

	/** Evento que toca en el número de evento {@code index} (null si no hay ninguno activado). Fija la elección. */
	static WorldEventKind kindFor(ServerWorld world, long index) {
		if (index < 0) return null;
		WorldEventState state = WorldEventState.get(world);
		if (index == state.lastIndex) return WorldEventKind.byId(state.lastKind);
		if (index == state.nextIndex) return WorldEventKind.byId(state.nextKind);
		WorldEventKind previous = index - 1 == state.lastIndex ? WorldEventKind.byId(state.lastKind) : null;
		int chosen = WorldEventRules.choose(world.getSeed(), index, calendarWeights(Aptitudes.config()),
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

		long duration = duration(active);
		long elapsed = world.getTime() - state.startTick;
		long eventTime = state.forced ? elapsed : inDay - (active == WorldEventKind.SOUL_FOG ? FOG_START : EMBER_START);
		boolean wrongDay = !state.forced && day != state.activeDay;
		if (eventTime < 0 || eventTime >= duration || wrongDay || elapsed > duration + 200 || weights(cfg)[active.ordinal()] <= 0) {
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
			emberMobs(world);
		}
	}

	/** Empieza un evento nuevo ({@code forced}: por comando, con su propio reloj). El Eclipse lo lleva EclipseManager. */
	public static void start(MinecraftServer server, ServerWorld world, WorldEventKind kind, boolean forced, long day) {
		if (kind == WorldEventKind.ECLIPSE) return;
		WorldEventState state = WorldEventState.get(world);
		state.active = kind.id();
		if (kind == WorldEventKind.SOUL_FOG) state.fogSession = UUID.randomUUID();
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
		if (kind == WorldEventKind.SOUL_FOG) {
			state.lastClosedFogSession = state.fogSession;
			state.lastFogCloseTick = world.getTime();
			state.fogSession = null;
			for (ErranteEntity errante : FOG_ERRANTES.values()) {
				if (errante.isAlive() && !errante.isRetiring()) queueFogRetirement(errante, state.lastFogCloseTick);
			}
		}
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
			dev.aptitudes.achievement.Achievements.survived(player, kind); // v31
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
		WorldEventState state = WorldEventState.get(world);
		// Una partida guardada antes de introducir sesiones puede tener una Niebla activa.
		if (state.fogSession == null) {
			state.fogSession = UUID.randomUUID();
			state.markDirty();
		}
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
						mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)
								&& (!(mob instanceof ErranteEntity errante) || !errante.isRetiring())).forEach(mob -> alive.add(mob.getUuid()));
			}
			int groupSize = group.size();
			int cap = Math.min(Integer.MAX_VALUE, baseCap * groupSize);
			int count = EclipseRules.allowedSpawns(alive.size(), cap, 2 * groupSize);
			for (int i = 0; i < count; i++) {
				ServerPlayerEntity target = group.get(i % groupSize);
				BlockPos pos = EclipseManager.findSpawn(world, target.getBlockPos(), 14, 28);
				if (pos == null) continue;
				EntityType<? extends MobEntity> type = switch (dev.aptitudes.mob.FogMobRules.choose(world.random.nextInt(100))) {
					case OSARIO -> dev.aptitudes.mob.OsarioEntities.OSARIO;
					case ERRANTE -> dev.aptitudes.mob.NewMobs.ERRANTE;
					case VANILLA -> fogType(world, pos);
				};
				MobEntity mob = type.create(world);
				if (mob == null) continue;
				mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
				mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
				mob.addCommandTag(TAG);
				mob.addCommandTag(Elites.CHECKED);
				if (world.random.nextFloat() < 0.15f) Elites.makeElite(mob, false, world.random);
				if (mob instanceof ErranteEntity errante) {
					errante.calculateDimensions();
					if (!world.isSpaceEmpty(errante, errante.getBoundingBox())) continue;
					errante.markFogOrigin(state.fogSession);
				}
				mob.setPersistent();
				world.spawnEntityAndPassengers(mob);
				world.spawnParticles(ParticleTypes.SOUL, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
				if (!(mob instanceof ErranteEntity)) ALIVE.add(mob.getUuid());
			}
		}
	}

	/** v41.1: solo no muertos a pie; la variante depende del clima del bioma ({@link SoulFogRules}). */
	private static EntityType<? extends MobEntity> fogType(ServerWorld world, BlockPos pos) {
		var biome = world.getBiome(pos);
		boolean water = world.getFluidState(pos).isIn(net.minecraft.registry.tag.FluidTags.WATER)
				|| biome.isIn(net.minecraft.registry.tag.BiomeTags.IS_OCEAN)
				|| biome.isIn(net.minecraft.registry.tag.BiomeTags.IS_RIVER)
				|| biome.isIn(net.minecraft.registry.tag.BiomeTags.IS_BEACH)
				|| biome.matchesKey(net.minecraft.world.biome.BiomeKeys.SWAMP)
				|| biome.matchesKey(net.minecraft.world.biome.BiomeKeys.MANGROVE_SWAMP);
		SoulFogRules.Climate climate = SoulFogRules.climate(water, biome.value().getTemperature());
		return switch (SoulFogRules.choose(climate, world.random.nextInt(100))) {
			case ZOMBIE -> EntityType.ZOMBIE;
			case ZOMBIE_VILLAGER -> EntityType.ZOMBIE_VILLAGER;
			case DROWNED -> EntityType.DROWNED;
			case HUSK -> EntityType.HUSK;
			case SKELETON -> EntityType.SKELETON;
			case STRAY -> EntityType.STRAY;
			case BOGGED -> EntityType.BOGGED;
		};
	}

	// ------------------------------------------------------------------ Lluvia de brasas

	/** Cada segundo, cada jugador tiene un 4 % de probabilidad de ver caer un meteorito cerca (≈ uno cada 25 s). */
	/** Criaturas del Nether de la Lluvia de brasas: una oportunidad por segundo y jugador, con tope cerca. */
	private static void emberMobs(ServerWorld world) {
		if (world.getTime() % 20 != 0) return;
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (!player.isAlive() || player.isSpectator() || player.isCreative() || world.random.nextFloat() >= 0.03f) continue;
			long nearby = world.getEntitiesByClass(MobEntity.class, player.getBoundingBox().expand(48),
					mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)).size();
			if (nearby >= EmberMobRules.MAX_NEARBY) continue;
			BlockPos pos = EclipseManager.findSpawn(world, player.getBlockPos(), 14, 28);
			if (pos == null) continue;
			MobEntity mob = emberType(EmberMobRules.choose(world.random.nextInt(100))).create(world);
			if (mob == null) continue;
			mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
			mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
			mob.addCommandTag(TAG);
			world.spawnEntity(mob);
		}
	}

	private static EntityType<? extends MobEntity> emberType(EmberMobRules.EmberMob mob) {
		return switch (mob) {
			case ZOMBIFIED_PIGLIN -> EntityType.ZOMBIFIED_PIGLIN;
			case HOGLIN -> EntityType.HOGLIN;
			case PIGLIN_BRUTE -> EntityType.PIGLIN_BRUTE;
			case BLAZE -> EntityType.BLAZE;
			case GHAST -> EntityType.GHAST;
			case MAGMA_CUBE -> EntityType.MAGMA_CUBE;
			case WITHER_SKELETON -> EntityType.WITHER_SKELETON;
			case ESCORIFICADOR -> dev.aptitudes.mob.NewMobs.ESCORIFICADOR;
		};
	}

	private static void emberRain(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			if (!player.isAlive() || player.isSpectator() || world.random.nextFloat() >= 0.04f || METEORS.size() > 32) continue;
			AptitudesConfig cfg = Aptitudes.config();
			StrongEventRules.MeteorSize size = StrongEventRules.meteorSize(world.random.nextDouble(),
					cfg.emberRainLargeChance, cfg.emberRainHugeChance);
			// Los grandes caen más lejos del jugador: se ven venir y la onda no le alcanza de lleno.
			int min = size == StrongEventRules.MeteorSize.SMALL ? 8 : 14;
			BlockPos target = EclipseManager.findSpawn(world, player.getBlockPos(), min, 32);
			if (target == null) continue;
			Vec3d to = Vec3d.ofBottomCenter(target);
			Vec3d from = to.add((world.random.nextDouble() - 0.5) * 40, 45 + 15 * size.ordinal(), (world.random.nextDouble() - 0.5) * 40);
			METEORS.add(new Meteor(world, from, to, world.getTime() + StrongEventRules.fallTicks(size), size));
			world.playSound(null, from.x, from.y, from.z, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.WEATHER,
					2.0f + size.ordinal(), 0.5f - 0.1f * size.ordinal());
			if (size != StrongEventRules.MeteorSize.SMALL) {
				// Aviso: ruido grave hacia el punto de impacto.
				world.playSound(null, to.x, to.y, to.z, SoundEvents.ENTITY_GHAST_WARN, SoundCategory.WEATHER, 1.5f, 0.5f);
			}
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
				double t = 1.0 - left / (double) StrongEventRules.fallTicks(m.size());
				Vec3d p = m.from().lerp(m.to(), Math.max(0, Math.min(1, t)));
				int s = m.size().ordinal(); // 0 pequeño, 1 grande, 2 enorme: estela más ancha y densa
				double spread = 0.25 + 0.35 * s;
				m.world().spawnParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 6 + 8 * s, spread, spread, spread, 0.02);
				m.world().spawnParticles(ParticleTypes.LARGE_SMOKE, p.x, p.y, p.z, 2 + 3 * s, spread, spread, spread, 0.01);
				if (s > 0) m.world().spawnParticles(ParticleTypes.LAVA, p.x, p.y, p.z, s, spread, spread, spread, 0.0);
				continue;
			}
			it.remove();
			impact(m.world(), m.to(), m.size());
		}
	}

	/** Bloques de magma temporales alrededor del impacto (30 s); solo en huecos de aire sobre suelo firme. */
	private static final long CRATER_TICKS = 600;

	private static void impact(ServerWorld world, Vec3d at, StrongEventRules.MeteorSize size) {
		// Explosión sin romper bloques (como antes), más potente según el tamaño.
		world.createExplosion(null, at.x, at.y, at.z, StrongEventRules.explosionPower(size), World.ExplosionSourceType.NONE);
		int s = size.ordinal();
		world.spawnParticles(ParticleTypes.LAVA, at.x, at.y + 0.3, at.z, 20 + 20 * s, 0.6 + s, 0.3, 0.6 + s, 0.1);
		if (s > 0) world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
		BlockPos pos = BlockPos.ofFloored(at);
		crater(world, pos, StrongEventRules.craterRadius(size));
		for (int i = 0; i < StrongEventRules.lootRolls(size); i++) Block.dropStack(world, pos, meteorLoot(world, size));
		netherMobs(world, at, pos, size);
	}

	private static ItemStack meteorLoot(ServerWorld world, StrongEventRules.MeteorSize size) {
		float roll = world.random.nextFloat();
		if (size == StrongEventRules.MeteorSize.HUGE && roll < 0.08f) return new ItemStack(Items.NETHERITE_SCRAP);
		return roll < 0.05f ? new ItemStack(Items.DIAMOND)
				: roll < 0.20f ? new ItemStack(Items.GOLD_INGOT, 1 + world.random.nextInt(2))
				: roll < 0.50f ? new ItemStack(Items.COPPER_INGOT, 2 + world.random.nextInt(3))
				: new ItemStack(Items.IRON_INGOT, 1 + world.random.nextInt(3));
	}

	/** Restos ardientes: magma temporal (vuelve a ser aire). Nunca sustituye bloques existentes. */
	private static void crater(ServerWorld world, BlockPos center, int radius) {
		if (radius <= 0) return;
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius || world.random.nextFloat() < 0.35f) continue;
				for (int dy = 1; dy >= -2; dy--) {
					BlockPos p = center.add(dx, dy, dz);
					if (world.getBlockState(p).isAir() && world.getBlockState(p.down()).isSolidBlock(world, p.down())) {
						dev.aptitudes.world.TemporaryBlocks.place(world, p, net.minecraft.block.Blocks.MAGMA_BLOCK.getDefaultState(), CRATER_TICKS);
						break;
					}
				}
			}
		}
	}

	/** Criaturas del Nether del impacto ({@link StrongEventRules#netherMob}), con el tope de la Niebla por zona. */
	private static void netherMobs(ServerWorld world, Vec3d at, BlockPos pos, StrongEventRules.MeteorSize size) {
		if (world.getDifficulty() == Difficulty.PEACEFUL) return;
		AptitudesConfig cfg = Aptitudes.config();
		int count = StrongEventRules.netherMobCount(size, world.random.nextDouble(), cfg.emberRainMobChance);
		if (count <= 0) return;
		int alive = world.getEntitiesByClass(MobEntity.class, new net.minecraft.util.math.Box(pos).expand(48),
				mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)).size();
		count = EclipseRules.allowedSpawns(alive, Math.max(0, cfg.eclipseMaxMobsPerPlayer / 2), count);
		for (int i = 0; i < count; i++) {
			EntityType<? extends MobEntity> type = switch (StrongEventRules.netherMob(size, world.random.nextInt(100))) {
				case MAGMA_CUBE -> EntityType.MAGMA_CUBE;
				case BLAZE -> EntityType.BLAZE;
				case PIGLIN_BRUTE -> EntityType.PIGLIN_BRUTE;
				case HOGLIN -> EntityType.HOGLIN;
				case WITHER_SKELETON -> EntityType.WITHER_SKELETON;
			};
			MobEntity mob = type.create(world);
			if (mob == null) continue;
			double ox = (world.random.nextDouble() - 0.5) * 3, oz = (world.random.nextDouble() - 0.5) * 3;
			mob.refreshPositionAndAngles(at.x + ox, at.y, at.z + oz, world.random.nextFloat() * 360f, 0);
			mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
			// Fuera del Nether, piglins y hoglins se convertirían en zombis (y dejarían de ser del evento).
			if (mob instanceof net.minecraft.entity.mob.AbstractPiglinEntity piglin) piglin.setImmuneToZombification(true);
			if (mob instanceof net.minecraft.entity.mob.HoglinEntity hoglin) hoglin.setImmuneToZombification(true);
			mob.addCommandTag(TAG);
			mob.addCommandTag(Elites.CHECKED);
			if (size == StrongEventRules.MeteorSize.HUGE && world.random.nextFloat() < 0.25f) Elites.makeElite(mob, false, world.random);
			mob.setPersistent();
			world.spawnEntityAndPassengers(mob);
			world.spawnParticles(ParticleTypes.FLAME, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 16, 0.3, 0.6, 0.3, 0.02);
			ALIVE.add(mob.getUuid());
		}
	}

	// ------------------------------------------------------------------ Eventos por activación (T1)

	/**
	 * Empieza un evento por la acción de un jugador y anota el reloj de su espera. Los comandos de administración
	 * ({@link #forceStart}) no consumen la espera. False si ya hay otro evento en curso.
	 */
	static boolean startByPlayer(MinecraftServer server, ServerWorld world, WorldEventKind kind) {
		if (active(server) || EclipseState.get(world).active) return false;
		start(server, world, kind, true, EclipseRules.dayOf(world.getTimeOfDay()));
		WorldEventState state = WorldEventState.get(world);
		if (kind == WorldEventKind.SOUL_FOG) state.lastFogStart = world.getTime();
		else if (kind == WorldEventKind.EMBER_RAIN) state.lastEmberStart = world.getTime();
		state.markDirty();
		return true;
	}

	/**
	 * Mayús + clic derecho sobre el aldeano del gremio: la Niebla de almas empieza para todo el mundo, o el aldeano
	 * dice que está en espera. Sin Mayús se abre el comercio como siempre. Todo se decide en el servidor.
	 */
	private static ActionResult talkToGuildmaster(PlayerEntity player, World world, Hand hand, Entity entity) {
		if (world.isClient() || hand != Hand.MAIN_HAND || !player.isSneaking()) return ActionResult.PASS;
		if (!(entity instanceof VillagerEntity villager) || !villager.isAlive()
				|| villager.getVillagerData().getProfession() != dev.aptitudes.world.GuildVillagers.GUILDMASTER) return ActionResult.PASS;
		if (world.getRegistryKey() != World.OVERWORLD || !(player instanceof ServerPlayerEntity speaker)) return ActionResult.PASS;
		MinecraftServer server = speaker.getServer();
		AptitudesConfig cfg = Aptitudes.config();
		// Evento desactivado (peso 0): el aldeano solo comercia.
		if (server == null || cfg.worldEventWeightSoulFog <= 0) return ActionResult.PASS;
		ServerWorld overworld = server.getOverworld();
		WorldEventState state = WorldEventState.get(overworld);
		boolean busy = active(server) || EclipseState.get(overworld).active;
		EventTriggerRules.Reply reply = EventTriggerRules.villagerReply(busy,
				EventTriggerRules.ready(state.lastFogStart, overworld.getTime(), cfg.fogCooldownDays));
		speaker.sendMessage(Text.translatable("event.aptitudes.soul_fog.villager_" + reply.name().toLowerCase(java.util.Locale.ROOT))
				.formatted(Formatting.GRAY, Formatting.ITALIC), false);
		world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
				reply == EventTriggerRules.Reply.START ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO,
				SoundCategory.NEUTRAL, 1.0f, 1.0f);
		if (reply == EventTriggerRules.Reply.START) startByPlayer(server, overworld, WorldEventKind.SOUL_FOG);
		return ActionResult.SUCCESS;
	}

	/**
	 * Un jugador usa yesca y pedernal o una carga de fuego sobre obsidiana en el Overworld: se anota cuántos bloques de
	 * portal había y, dos ticks después, se mira si ha aparecido alguno. Sin mixin: el uso sigue su curso normal.
	 */
	private static ActionResult watchPortalLighting(PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
		if (world.isClient() || !(world instanceof ServerWorld serverWorld) || serverWorld.getRegistryKey() != World.OVERWORLD
				|| !(player instanceof ServerPlayerEntity lighter) || lighter.isSpectator()) return ActionResult.PASS;
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(Items.FLINT_AND_STEEL) && !stack.isOf(Items.FIRE_CHARGE)) return ActionResult.PASS;
		BlockPos pos = hit.getBlockPos();
		if (!serverWorld.getBlockState(pos).isOf(Blocks.OBSIDIAN)
				|| !EventTriggerRules.canQueuePortalCheck(PORTAL_CHECKS.size())) return ActionResult.PASS;
		PORTAL_CHECKS.add(new PortalCheck(serverWorld, pos, serverWorld.getTime() + 2, portalBlocks(serverWorld, pos)));
		return ActionResult.PASS;
	}

	/** Bloques de portal al Nether en un cubo pequeño alrededor de {@code center}. */
	private static int portalBlocks(ServerWorld world, BlockPos center) {
		int radius = EventTriggerRules.PORTAL_SCAN_RADIUS;
		int count = 0;
		BlockPos.Mutable cursor = new BlockPos.Mutable();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					if (world.getBlockState(cursor).isOf(Blocks.NETHER_PORTAL)) count++;
				}
			}
		}
		return count;
	}

	/** Resuelve las comprobaciones de portal vencidas: si se abrió uno y la espera lo permite, empieza la Lluvia de brasas. */
	private static void tickPortalChecks(MinecraftServer server) {
		if (PORTAL_CHECKS.isEmpty()) return;
		Iterator<PortalCheck> it = PORTAL_CHECKS.iterator();
		while (it.hasNext()) {
			PortalCheck check = it.next();
			if (check.world().getTime() < check.checkTick()) continue;
			it.remove();
			if (!EventTriggerRules.portalOpened(check.portalBlocksBefore(), portalBlocks(check.world(), check.pos()))) continue;
			AptitudesConfig cfg = Aptitudes.config();
			ServerWorld overworld = server.getOverworld();
			if (cfg.worldEventWeightEmberRain <= 0 || !EventTriggerRules.ready(
					WorldEventState.get(overworld).lastEmberStart, overworld.getTime(), cfg.emberRainCooldownDays)) continue;
			startByPlayer(server, overworld, WorldEventKind.EMBER_RAIN);
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
		if (kind == null || eventTime < 0 || eventTime >= duration(kind)) {
			ServerPlayNetworking.send(player, new EclipsePayload(0, 0f, 0f, 0));
			return;
		}
		long duration = duration(kind);
		float intensity = WorldEventRules.ramp(eventTime, duration, RAMP);
		ServerPlayNetworking.send(player, new EclipsePayload(1, intensity, eventTime / (float) duration, kind.ordinal()));
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
