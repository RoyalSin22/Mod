package dev.aptitudes.event;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.boss.BossManager;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.Skill;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.event.EclipseRules.Phase;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.network.EclipsePayload;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * El Eclipse (v34): evento del mundo cada {@code eclipseIntervalDays} días. Eclipse solar de 6:00 a 18:00 en el
 * Mundo Normal con oleadas de criaturas reforzadas, élites, campeones y el Heraldo del Eclipse a mediodía.
 * Todo lo decide el servidor; el cliente solo recibe la fase y la intensidad para pintar el cielo.
 * Ver docs/V34_ECLIPSE.md.
 */
public final class EclipseManager {
	/** Etiqueta de las criaturas del eclipse (se disuelven al terminar o al cargarse fuera del evento). */
	public static final String TAG = Aptitudes.MOD_ID + ".eclipse";
	private static final Identifier HEALTH_ID = Aptitudes.id("eclipse/health");
	private static final Identifier DAMAGE_ID = Aptitudes.id("eclipse/damage");
	private static final Identifier HERALD_HEALTH_ID = Aptitudes.id("eclipse/herald_health");
	private static final int HELMET_COLOR = 0x2A0707;
	private static final double GROUP_RADIUS = 64.0;
	private static final double COUNT_RADIUS = 48.0;

	private static final List<EntityType<? extends MobEntity>> WAVE_TYPES = List.of(EntityType.ZOMBIE,
			EntityType.SKELETON, EntityType.SPIDER, EntityType.CREEPER, EntityType.VINDICATOR, EntityType.WITCH);

	/** Criaturas del eclipse vivas y cargadas (para disolverlas al terminar). */
	private static final Set<UUID> ALIVE = new HashSet<>();
	/** Criaturas del eclipse cargadas fuera del evento: se disuelven en el siguiente tick (no durante la carga). */
	private static final Set<UUID> LEFTOVERS = new HashSet<>();
	private static Phase lastPhase = Phase.NONE;
	private static long nextWaveTick;

	private EclipseManager() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> reset());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 10) tick(server);
		});
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (!entity.getCommandTags().contains(TAG)) return;
			if (world.getRegistryKey() == World.OVERWORLD && EclipseState.get(world).active) ALIVE.add(entity.getUuid());
			else LEFTOVERS.add(entity.getUuid());
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> ALIVE.remove(entity.getUuid()));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(entity.getWorld() instanceof ServerWorld world)) return;
			if (entity instanceof ServerPlayerEntity player) {
				if (world.getRegistryKey() == World.OVERWORLD) {
					EclipseState state = EclipseState.get(world);
					if (state.active && state.fallen.add(player.getUuid())) state.markDirty();
				}
			} else if (entity instanceof MobEntity mob && mob.getCommandTags().contains(TAG)) {
				ALIVE.remove(mob.getUuid());
				boolean herald = mob.getCommandTags().contains(BossKind.ECLIPSE_HERALD.tag());
				int count = EclipseRules.fragments(herald, Elites.isChampion(mob), Elites.isElite(mob), world.random.nextDouble());
				if (count > 0) mob.dropStack(new ItemStack(ModItems.ECLIPSE_FRAGMENT, count));
			}
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sync(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> sync(player));
	}

	private static void reset() {
		ALIVE.clear();
		LEFTOVERS.clear();
		lastPhase = Phase.NONE;
		nextWaveTick = 0;
	}

	// ------------------------------------------------------------------ Bucle

	private static void tick(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		if (world == null) return;
		removeLeftovers(world);
		EclipseState state = EclipseState.get(world);
		AptitudesConfig cfg = Aptitudes.config();
		long timeOfDay = world.getTimeOfDay();
		long day = EclipseRules.dayOf(timeOfDay);
		long inDay = EclipseRules.timeInDay(timeOfDay);

		if (!state.active) {
			if (!cfg.eclipseEnabled) return;
			// v41: el día de evento solo hay Eclipse si la rotación lo eligió (el aviso lo da WorldEventManager).
			if (inDay < EclipseRules.END && state.lastDay != day
					&& WorldEventManager.kindForDay(world, day) == WorldEventKind.ECLIPSE && !WorldEventManager.active(server)) {
				start(server, world, state, false, day);
				WorldEventManager.markStarted(world, day, WorldEventKind.ECLIPSE);
			}
			return;
		}

		if (!cfg.eclipseEnabled) {
			end(server, world, state, false);
			return;
		}
		long elapsed = world.getTime() - state.startTick;
		long eventTime = state.forced ? elapsed : inDay;
		Phase phase = EclipseRules.phase(eventTime);
		boolean wrongDay = !state.forced && day != state.lastDay;
		if (phase == Phase.NONE || wrongDay || elapsed < 0 || elapsed > EclipseRules.END + 200) {
			end(server, world, state, phase == Phase.NONE && !wrongDay);
			return;
		}

		if (phase != lastPhase) {
			if (lastPhase != Phase.NONE || phase != Phase.PENUMBRA) announcePhase(server, world, phase);
			lastPhase = phase;
			syncAll(server, eventTime);
		} else if (server.getTicks() % 100 == 10) {
			syncAll(server, eventTime);
		}

		boolean hostile = world.getDifficulty() != Difficulty.PEACEFUL;
		if (hostile && server.getTicks() >= nextWaveTick) {
			nextWaveTick = server.getTicks() + EclipseRules.waveIntervalTicks(cfg.eclipseWaveIntervalSeconds);
			waves(world, phase, cfg);
		}
		if (hostile && cfg.eclipseHeraldEnabled && !state.heraldSpawned && phase == Phase.TOTALITY
				&& eventTime >= EclipseRules.HERALD_TIME) {
			state.heraldSpawned = true;
			state.markDirty();
			spawnHeralds(server, world);
		}
		if (phase == Phase.TOTALITY && world.random.nextFloat() < 0.15f) cosmeticLightning(world);
	}

	// ------------------------------------------------------------------ Inicio y fin

	/** Empieza un eclipse. {@code forced}: por comando, con su propio reloj de 12 000 ticks. */
	public static void start(MinecraftServer server, ServerWorld world, EclipseState state, boolean forced, long day) {
		state.active = true;
		state.forced = forced;
		state.startTick = world.getTime();
		state.lastDay = day;
		state.heraldSpawned = false;
		state.fallen.clear();
		state.markDirty();
		lastPhase = Phase.PENUMBRA;
		nextWaveTick = server.getTicks() + 100;
		broadcast(server, Text.translatable("eclipse.aptitudes.start").formatted(Formatting.DARK_RED, Formatting.BOLD));
		playToAll(world, dev.aptitudes.sound.ModSounds.ECLIPSE_START, 1.0f);
		long eventTime = forced ? 0 : EclipseRules.timeInDay(world.getTimeOfDay());
		syncAll(server, eventTime);
	}

	/** Termina el eclipse; si {@code completed}, premia a los supervivientes del Mundo Normal. */
	public static void end(MinecraftServer server, ServerWorld world, EclipseState state, boolean completed) {
		state.active = false;
		state.forced = false;
		state.heraldSpawned = false;
		Set<UUID> fallen = new HashSet<>(state.fallen);
		state.fallen.clear();
		state.markDirty();
		lastPhase = Phase.NONE;
		for (UUID id : List.copyOf(ALIVE)) {
			Entity entity = world.getEntity(id);
			if (entity != null) dissolve(world, entity);
		}
		ALIVE.clear();
		broadcast(server, Text.translatable(completed ? "eclipse.aptitudes.end" : "eclipse.aptitudes.stopped")
				.formatted(Formatting.GOLD));
		if (completed) {
			AptitudesConfig cfg = Aptitudes.config();
			for (ServerPlayerEntity player : world.getPlayers()) {
				if (player.isSpectator() || fallen.contains(player.getUuid())) continue;
				Role primary = RoleManager.get(player).primary();
				if (primary != null && cfg.eclipseSurvivorRoleXp > 0) RoleManager.addRoleXp(player, primary, cfg.eclipseSurvivorRoleXp);
				if (cfg.eclipseSurvivorCombatXp > 0) ProgressManager.addXp(player, Skill.COMBAT, cfg.eclipseSurvivorCombatXp);
				player.sendMessage(Text.translatable("eclipse.aptitudes.survivor").formatted(Formatting.GOLD), false);
			}
		}
		syncAll(server, -1L);
	}

	static void dissolve(ServerWorld world, Entity entity) {
		world.spawnParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getBodyY(0.5), entity.getZ(), 12, 0.3, 0.5, 0.3, 0.02);
		entity.discard();
	}

	private static void removeLeftovers(ServerWorld world) {
		if (LEFTOVERS.isEmpty()) return;
		for (UUID id : List.copyOf(LEFTOVERS)) {
			for (ServerWorld w : world.getServer().getWorlds()) {
				Entity entity = w.getEntity(id);
				if (entity != null) {
					dissolve(w, entity);
					break;
				}
			}
		}
		LEFTOVERS.clear();
	}

	// ------------------------------------------------------------------ Oleadas y Heraldo

	private static void waves(ServerWorld world, Phase phase, AptitudesConfig cfg) {
		int wave = EclipseRules.waveSize(phase);
		if (wave <= 0) return;
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
				world.getEntitiesByClass(MobEntity.class, member.getBoundingBox().expand(COUNT_RADIUS),
						mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)).forEach(mob -> alive.add(mob.getUuid()));
			}
			int groupSize = group.size();
			int cap = cfg.eclipseMaxMobsPerPlayer * groupSize;
			int count = EclipseRules.allowedSpawns(alive.size(), cap, wave * groupSize);
			for (int i = 0; i < count; i++) {
				ServerPlayerEntity target = group.get(i % groupSize);
				BlockPos pos = findSpawn(world, target.getBlockPos(), 16, 32);
				if (pos == null) continue;
				spawnMob(world, WAVE_TYPES.get(world.random.nextInt(WAVE_TYPES.size())), pos, cfg);
			}
		}
	}

	private static void spawnMob(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos, AptitudesConfig cfg) {
		MobEntity mob = type.create(world);
		if (mob == null) return;
		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
		mob.addCommandTag(TAG);
		mob.addCommandTag(Elites.CHECKED); // la tirada de élite la hace el eclipse, no la aparición normal
		modifier(mob, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_ID,
				EclipseRules.boundedMultiplier(cfg.eclipseMobHealthMultiplier, 1.0, 5.0) - 1.0);
		modifier(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_ID,
				EclipseRules.boundedMultiplier(cfg.eclipseMobDamageMultiplier, 1.0, 5.0) - 1.0);
		mob.setHealth(mob.getMaxHealth());
		darkHelmet(mob);
		float roll = world.random.nextFloat();
		if (roll < cfg.eclipseChampionChance) Elites.makeElite(mob, true, world.random);
		else if (roll < cfg.eclipseChampionChance + cfg.eclipseEliteChance) Elites.makeElite(mob, false, world.random);
		mob.setPersistent();
		world.spawnEntityAndPassengers(mob);
		world.spawnParticles(ParticleTypes.SMOKE, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 16, 0.3, 0.6, 0.3, 0.01);
		ALIVE.add(mob.getUuid());
	}

	/** Casco oscuro e irrompible: los no muertos del eclipse no arden con la luz del día ni lo sueltan. */
	static void darkHelmet(MobEntity mob) {
		if (!mob.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) return;
		ItemStack helmet = new ItemStack(Items.LEATHER_HELMET);
		helmet.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(HELMET_COLOR, false));
		helmet.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
		mob.equipStack(EquipmentSlot.HEAD, helmet);
		mob.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
	}

	private static void modifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute, Identifier id, double value) {
		EntityAttributeInstance instance = mob.getAttributeInstance(attribute);
		if (instance == null) return;
		instance.removeModifier(id);
		if (value > 0) instance.addPersistentModifier(new EntityAttributeModifier(id, value,
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
	}

	/** Un Heraldo por grupo de jugadores (a menos de 64 bloques entre sí); su vida escala con el grupo. */
	private static void spawnHeralds(MinecraftServer server, ServerWorld world) {
		List<ServerPlayerEntity> pending = new ArrayList<>();
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (p.isAlive() && !p.isSpectator() && !p.isCreative()) pending.add(p);
		}
		while (!pending.isEmpty()) {
			ServerPlayerEntity leader = pending.remove(0);
			int group = 1;
			boolean hasParty = dev.aptitudes.party.PartyManager.hasParty(leader);
			if (hasParty) {
				group = (int) dev.aptitudes.party.PartyManager.onlineMembers(leader).stream()
						.filter(p -> p.isAlive() && !p.isSpectator() && !p.isCreative()).count();
				for (int i = pending.size() - 1; i >= 0; i--) {
					if (dev.aptitudes.party.PartyManager.inSameParty(leader, pending.get(i))) pending.remove(i);
				}
			} else {
				for (int i = pending.size() - 1; i >= 0; i--) {
					if (pending.get(i).squaredDistanceTo(leader) <= GROUP_RADIUS * GROUP_RADIUS) {
						pending.remove(i);
						group++;
					}
				}
			}
			BlockPos pos = findSpawn(world, leader.getBlockPos(), 12, 20);
			if (pos == null) pos = leader.getBlockPos();
			MobEntity herald = BossManager.spawn(world, EntityType.WITHER_SKELETON, pos, BossKind.ECLIPSE_HERALD);
			if (herald == null) continue;
			herald.addCommandTag(TAG);
			modifier(herald, EntityAttributes.GENERIC_MAX_HEALTH, HERALD_HEALTH_ID,
					EclipseRules.heraldHealthMultiplier(group) - 1.0);
			herald.setHealth(herald.getMaxHealth());
			herald.setCustomName(Text.translatable(BossKind.ECLIPSE_HERALD.nameKey()).formatted(Formatting.DARK_RED, Formatting.BOLD));
			Elites.setChampionBarName(herald, herald.getDisplayName());
			ALIVE.add(herald.getUuid());
			world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, herald.getX(), herald.getBodyY(0.5), herald.getZ(), 60, 0.6, 1.2, 0.6, 0.05);
		}
		broadcast(server, Text.translatable("eclipse.aptitudes.herald").formatted(Formatting.DARK_RED, Formatting.BOLD));
		playToAll(world, dev.aptitudes.sound.ModSounds.HERALD_SPAWN, 1.0f);
	}

	/** Busca suelo firme con 2 bloques libres a {@code min}–{@code max} bloques del centro (6 intentos). */
	static BlockPos findSpawn(ServerWorld world, BlockPos center, int min, int max) {
		Random random = world.random;
		for (int attempt = 0; attempt < 6; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			int dist = min + random.nextInt(Math.max(1, max - min + 1));
			int x = center.getX() + (int) Math.round(Math.cos(angle) * dist);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
			if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			BlockPos below = pos.down();
			if (Math.abs(y - center.getY()) > 12) continue;
			if (!world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP)) continue;
			if (!world.getFluidState(below).isEmpty() || !world.isAir(pos) || !world.isAir(pos.up())) continue;
			return pos;
		}
		return null;
	}

	private static void cosmeticLightning(ServerWorld world) {
		List<ServerPlayerEntity> players = world.getPlayers(p -> p.isAlive() && !p.isSpectator());
		if (players.isEmpty()) return;
		ServerPlayerEntity player = players.get(world.random.nextInt(players.size()));
		BlockPos pos = findSpawn(world, player.getBlockPos(), 24, 48);
		if (pos == null) return;
		LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
		if (bolt == null) return;
		bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		bolt.setCosmetic(true);
		world.spawnEntity(bolt);
	}

	// ------------------------------------------------------------------ Mensajes y sincronización

	private static void announcePhase(MinecraftServer server, ServerWorld world, Phase phase) {
		String key = switch (phase) {
			case PENUMBRA -> "eclipse.aptitudes.phase.penumbra";
			case TOTALITY -> "eclipse.aptitudes.phase.totality";
			case RETREAT -> "eclipse.aptitudes.phase.retreat";
			case NONE -> null;
		};
		if (key == null) return;
		broadcast(server, Text.translatable(key).formatted(Formatting.RED));
		if (phase == Phase.TOTALITY) playToAll(world, dev.aptitudes.sound.ModSounds.ECLIPSE_TOTALITY, 1.0f);
	}

	static void broadcast(MinecraftServer server, Text text) {
		for (ServerPlayerEntity player : server.getOverworld().getPlayers()) player.sendMessage(text, false);
	}

	static void playToAll(ServerWorld world, SoundEvent sound, float pitch) {
		for (ServerPlayerEntity p : world.getPlayers()) {
			world.playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.HOSTILE, 1.0f, pitch);
		}
	}

	/** Envía el estado a todos; {@code eventTime} negativo = sin eclipse. */
	private static void syncAll(MinecraftServer server, long eventTime) {
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			boolean overworld = player.getWorld().getRegistryKey() == World.OVERWORLD;
			send(player, overworld ? eventTime : -1L);
		}
	}

	/** Estado actual para un jugador concreto (al entrar, reaparecer o cambiar de dimensión). */
	private static void sync(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) return;
		ServerWorld world = server.getOverworld();
		EclipseState state = EclipseState.get(world);
		if (!state.active) {
			WorldEventManager.sync(player); // v41: si no hay Eclipse, el estado lo da el evento en curso (o ninguno)
			return;
		}
		if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
			send(player, -1L);
			return;
		}
		send(player, state.forced ? world.getTime() - state.startTick : EclipseRules.timeInDay(world.getTimeOfDay()));
	}

	private static void send(ServerPlayerEntity player, long eventTime) {
		if (!ServerPlayNetworking.canSend(player, EclipsePayload.ID)) return;
		Phase phase = EclipseRules.phase(eventTime);
		float progress = phase == Phase.NONE ? 0f : Math.min(1f, Math.max(0f, eventTime / (float) EclipseRules.END));
		ServerPlayNetworking.send(player, new EclipsePayload(phase.ordinal(), EclipseRules.intensity(eventTime), progress, WorldEventKind.ECLIPSE.ordinal()));
	}

	// ------------------------------------------------------------------ Comando

	/** Estado legible para {@code /aptitudes eclipse status}. */
	public static Text status(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		EclipseState state = EclipseState.get(world);
		AptitudesConfig cfg = Aptitudes.config();
		if (state.active) {
			long eventTime = state.forced ? world.getTime() - state.startTick : EclipseRules.timeInDay(world.getTimeOfDay());
			return Text.translatable("eclipse.aptitudes.status.active",
					Text.translatable("eclipse.aptitudes.phase_name." + EclipseRules.phase(eventTime).name().toLowerCase(java.util.Locale.ROOT)));
		}
		// v41: sin Eclipse en curso, informa del próximo evento de la rotación (puede no ser un Eclipse).
		return WorldEventManager.status(server);
	}

	/** {@code /aptitudes eclipse start}: devuelve false si ya hay uno en curso. */
	public static boolean forceStart(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		EclipseState state = EclipseState.get(world);
		if (state.active || WorldEventManager.active(server)) return false; // v41: un evento a la vez
		start(server, world, state, true, EclipseRules.dayOf(world.getTimeOfDay()));
		return true;
	}

	/** {@code /aptitudes eclipse stop}: devuelve false si no hay ninguno en curso. */
	public static boolean forceStop(MinecraftServer server) {
		ServerWorld world = server.getOverworld();
		EclipseState state = EclipseState.get(world);
		if (!state.active) return false;
		end(server, world, state, false);
		return true;
	}
}
