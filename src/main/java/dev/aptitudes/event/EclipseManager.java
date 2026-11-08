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
import dev.aptitudes.logic.AttributeApplier;
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
	private static final Identifier ARMOR_ID = Aptitudes.id("eclipse/armor");
	private static final Identifier KNOCKBACK_ID = Aptitudes.id("eclipse/knockback");
	private static final Identifier FOLLOW_ID = Aptitudes.id("eclipse/follow_range");
	private static final int HELMET_COLOR = 0x2A0707;
	private static final double GROUP_RADIUS = 64.0;
	private static final double COUNT_RADIUS = 48.0;

	/** Criaturas que muestran armadura puesta (las demás solo reciben los puntos de armadura). */
	private static final Set<EntityType<?>> ARMORED_TYPES = Set.of(EntityType.ZOMBIE, EntityType.SKELETON,
			EntityType.WITHER_SKELETON, EntityType.VINDICATOR);

	/** Criaturas del eclipse vivas y cargadas (para disolverlas al terminar). */
	private static final Set<UUID> ALIVE = new HashSet<>();
	/** Criaturas del eclipse cargadas fuera del evento: se disuelven en el siguiente tick (no durante la carga). */
	private static final Set<UUID> LEFTOVERS = new HashSet<>();
	private static Phase lastPhase = Phase.NONE;
	private static long nextWaveTick;
	private static int waveCursor;

	private EclipseManager() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			reset();
			// Las entidades de los chunks iniciales pueden cargarse antes de SERVER_STARTED.
			for (ServerWorld world : server.getWorlds()) {
				for (Entity entity : world.iterateEntities()) trackLoaded(entity, world);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 10) tick(server);
			if (ALIVE.isEmpty() || !Aptitudes.config().eclipseEnabled) return;
			ServerWorld world = server.getOverworld();
			if (world != null && world.getDifficulty() != Difficulty.PEACEFUL && EclipseState.get(world).active) {
				EclipsePursuit.tick(world, ALIVE);
			}
		});
		ServerEntityEvents.ENTITY_LOAD.register(EclipseManager::trackLoaded);
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (ALIVE.remove(entity.getUuid())) EclipsePursuit.forget(entity.getUuid());
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!(entity.getWorld() instanceof ServerWorld world)) return;
			if (entity instanceof ServerPlayerEntity player) {
				EclipsePursuit.forgetPlayer(world.getServer().getOverworld(), player);
				if (world.getRegistryKey() == World.OVERWORLD) {
					EclipseState state = EclipseState.get(world);
					if (state.active && state.fallen.add(player.getUuid())) state.markDirty();
				}
			} else if (entity instanceof MobEntity mob && mob.getCommandTags().contains(TAG)) {
				ALIVE.remove(mob.getUuid());
				EclipsePursuit.forget(mob.getUuid());
				boolean herald = mob.getCommandTags().contains(BossKind.ECLIPSE_HERALD.tag());
				int count = EclipseRules.fragments(herald, Elites.isChampion(mob), Elites.isElite(mob), world.random.nextDouble());
				if (count > 0) mob.dropStack(new ItemStack(ModItems.ECLIPSE_FRAGMENT, count));
			}
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> sync(newPlayer));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
				EclipsePursuit.forgetPlayer(server.getOverworld(), handler.getPlayer()));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			EclipsePursuit.forgetPlayer(origin.getServer().getOverworld(), player);
			sync(player);
		});
	}

	private static void trackLoaded(Entity entity, ServerWorld world) {
		if (!entity.getCommandTags().contains(TAG)) return;
		if (world.getRegistryKey() == World.OVERWORLD && EclipseState.get(world).active && entity.isAlive()) {
			ALIVE.add(entity.getUuid());
		} else LEFTOVERS.add(entity.getUuid());
	}

	private static void reset() {
		ALIVE.clear();
		EclipsePursuit.clear();
		LEFTOVERS.clear();
		lastPhase = Phase.NONE;
		nextWaveTick = 0;
		waveCursor = 0;
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
		if (hostile && world.getTime() >= nextWaveTick) {
			nextWaveTick = world.getTime() + EclipseRules.waveIntervalTicks(cfg.eclipseWaveIntervalSeconds);
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
		nextWaveTick = world.getTime() + 100;
		broadcast(server, Text.translatable("eclipse.aptitudes.start").formatted(Formatting.DARK_RED, Formatting.BOLD));
		playToAll(world, dev.aptitudes.sound.ModSounds.ECLIPSE_START, 1.0f);
		long eventTime = forced ? 0 : EclipseRules.timeInDay(world.getTimeOfDay());
		syncAll(server, eventTime);
	}

	/** Termina el eclipse; si {@code completed}, premia a los supervivientes del Mundo Normal. */
	public static void end(MinecraftServer server, ServerWorld world, EclipseState state, boolean completed) {
		if (!state.active) return; // una segunda finalización no vuelve a conceder XP, contratos ni logros
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
		EclipsePursuit.clear();
		broadcast(server, Text.translatable(completed ? "eclipse.aptitudes.end" : "eclipse.aptitudes.stopped")
				.formatted(Formatting.GOLD));
		if (completed) {
			AptitudesConfig cfg = Aptitudes.config();
			for (ServerPlayerEntity player : world.getPlayers()) {
				if (!eligible(player) || fallen.contains(player.getUuid())) continue;
				dev.aptitudes.contract.ContractManager.onEventSurvived(player, WorldEventKind.ECLIPSE);
				dev.aptitudes.achievement.Achievements.survived(player, WorldEventKind.ECLIPSE); // v31
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
		int wave = EclipseAssaultRules.waveSize(phase, cfg.eclipsePenumbraWaveSize, cfg.eclipseTotalityWaveSize);
		if (wave <= 0) return;
		List<ServerPlayerEntity> players = world.getPlayers(EclipseManager::eligible);
		if (players.isEmpty()) return;
		int first = Math.floorMod(waveCursor, players.size());
		waveCursor = (first + 1) % players.size();
		for (int offset = 0; offset < players.size(); offset++) {
			ServerPlayerEntity target = players.get((first + offset) % players.size());
			var area = target.getBoundingBox().expand(COUNT_RADIUS);
			int alive = world.getEntitiesByClass(MobEntity.class, area,
					mob -> mob.isAlive() && mob.getCommandTags().contains(TAG)).size();
			int nearby = (int) players.stream().filter(p -> area.contains(p.getPos())).count();
			int cap = EclipseAssaultRules.localCap(cfg.eclipseMaxMobsPerPlayer, nearby, cfg.eclipseZoneMobCap);
			int count = EclipseAssaultRules.spawnBudget(alive, cap, ALIVE.size(), cfg.eclipseWorldMobCap, wave);
			int spawned = 0;
			for (int attempt = 0; attempt < count; attempt++) {
				BlockPos pos = findSpawn(world, target.getBlockPos(), 16, 32, 3);
				if (pos == null) continue;
				var grade = EclipseAssaultRules.grade(phase, spawned, cfg.eclipseMinElitesPerWave,
						cfg.eclipseMinChampionsTotality, cfg.eclipseEliteChance, cfg.eclipseChampionChance, world.random.nextDouble());
				var type = EclipseAssaultRules.mob(phase, grade, cfg.eclipseGuaranteedEliteArmor, world.random.nextInt(100));
				if (spawnMob(world, waveType(type), pos, cfg, phase, grade, target)) spawned++;
			}
		}
	}

	static boolean eligible(ServerPlayerEntity player) {
		return EclipseAssaultRules.eligible(player.isAlive(), player.isCreative(), player.isSpectator());
	}

	/** v41.2: tipo de criatura por fase ({@link StrongEventRules#eclipseMob}); la Totalidad trae esqueletos wither. */
	private static EntityType<? extends MobEntity> waveType(StrongEventRules.EclipseMob type) {
		return switch (type) {
			case ZOMBIE -> EntityType.ZOMBIE;
			case SKELETON -> EntityType.SKELETON;
			case SPIDER -> EntityType.SPIDER;
			case CREEPER -> EntityType.CREEPER;
			case VINDICATOR -> EntityType.VINDICATOR;
			case WITCH -> EntityType.WITCH;
			case WITHER_SKELETON -> EntityType.WITHER_SKELETON;
		};
	}

	private static boolean spawnMob(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos, AptitudesConfig cfg,
			Phase phase, EclipseAssaultRules.Grade grade, ServerPlayerEntity target) {
		MobEntity mob = type.create(world);
		if (mob == null) return false;
		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
		// Sin pasajeros adicionales fuera del presupuesto (p. ej., un jinete generado al inicializar una araña).
		for (Entity passenger : List.copyOf(mob.getPassengerList())) passenger.discard();
		mob.removeAllPassengers();
		if (!world.getWorldBorder().contains(pos) || !world.isSpaceEmpty(mob)) return false;
		mob.addCommandTag(TAG);
		mob.addCommandTag(Elites.CHECKED); // la tirada de élite la hace el eclipse, no la aparición normal
		// v41.2: en la Totalidad, vida y daño extra sobre los multiplicadores de siempre; armadura y aguante.
		double phaseBonus = 1.0 + StrongEventRules.phaseBonus(phase, cfg.eclipseTotalityBonus);
		modifier(mob, EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_ID,
				EclipseRules.boundedMultiplier(cfg.eclipseMobHealthMultiplier, 1.0, 5.0) * phaseBonus - 1.0);
		modifier(mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, DAMAGE_ID,
				EclipseRules.boundedMultiplier(cfg.eclipseMobDamageMultiplier, 1.0, 5.0) * phaseBonus - 1.0);
		flatModifier(mob, EntityAttributes.GENERIC_ARMOR, ARMOR_ID, StrongEventRules.armorPoints(cfg.eclipseMobArmor));
		flatModifier(mob, EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, KNOCKBACK_ID, phase == Phase.TOTALITY ? 0.25 : 0.0);
		flatModifier(mob, EntityAttributes.GENERIC_FOLLOW_RANGE, FOLLOW_ID, 16.0);
		mob.setHealth(mob.getMaxHealth());
		darkHelmet(mob);
		boolean champion = grade == EclipseAssaultRules.Grade.CHAMPION;
		boolean elite = grade == EclipseAssaultRules.Grade.ELITE;
		double armorChance = EclipseAssaultRules.armorChance(grade, cfg.eclipseGuaranteedEliteArmor, cfg.eclipseArmorChance);
		equipArmor(mob, StrongEventRules.armorTier(phase, champion), armorChance,
				cfg.eclipseGuaranteedEliteArmor && (champion || elite), world.random);
		if (champion) Elites.makeElite(mob, true, world.random);
		else if (elite) Elites.makeElite(mob, false, world.random);
		if (!world.isSpaceEmpty(mob)) return false; // el modificador Gigante también debe caber
		for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0.0f);
		mob.setPersistent();
		if (!world.spawnEntity(mob)) return false;
		EclipsePursuit.seed(world, mob, target);
		world.spawnParticles(ParticleTypes.SMOKE, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 16, 0.3, 0.6, 0.3, 0.01);
		ALIVE.add(mob.getUuid());
		return true;
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

	/** Equipo del evento: piezas completas en élites; las unidades normales conservan su probabilidad. */
	private static void equipArmor(MobEntity mob, StrongEventRules.ArmorTier tier, double chance, boolean full, Random random) {
		if (!ARMORED_TYPES.contains(mob.getType())) return;
		EquipmentSlot[] slots = {EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD};
		net.minecraft.item.Item[][] items = {
				{Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, Items.CHAINMAIL_HELMET},
				{Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_HELMET},
				{Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.DIAMOND_HELMET},
		};
		for (int i = 0; i < slots.length; i++) {
			if (!full && (slots[i] == EquipmentSlot.HEAD || !mob.getEquippedStack(slots[i]).isEmpty()
					|| random.nextDouble() >= chance)) continue;
			ItemStack piece = new ItemStack(items[tier.ordinal()][i]);
			piece.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(false));
			mob.equipStack(slots[i], piece);
			mob.setEquipmentDropChance(slots[i], 0.0f);
		}
	}

	/** Suma un valor fijo a un atributo (quita antes el modificador anterior con el mismo id). */
	private static void flatModifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute, Identifier id, double value) {
		AttributeApplier.set(mob.getAttributeInstance(attribute), id, Math.max(0, value), EntityAttributeModifier.Operation.ADD_VALUE);
	}

	private static void modifier(MobEntity mob, RegistryEntry<EntityAttribute> attribute, Identifier id, double value) {
		AttributeApplier.set(mob.getAttributeInstance(attribute), id, Math.max(0, value),
				EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	/** Un Heraldo por grupo de jugadores (a menos de 64 bloques entre sí); su vida escala con el grupo. */
	private static void spawnHeralds(MinecraftServer server, ServerWorld world) {
		boolean spawned = false;
		List<ServerPlayerEntity> pending = new ArrayList<>();
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (eligible(p)) pending.add(p);
		}
		while (!pending.isEmpty()) {
			ServerPlayerEntity leader = pending.remove(0);
			int group = 1;
			for (int i = pending.size() - 1; i >= 0; i--) {
				if (pending.get(i).squaredDistanceTo(leader) <= GROUP_RADIUS * GROUP_RADIUS) {
					pending.remove(i);
					group++;
				}
			}
			BlockPos pos = findSpawn(world, leader.getBlockPos(), 12, 20, 4);
			if (pos == null) continue;
			if (!reserveHeraldSlot(world, Aptitudes.config().eclipseWorldMobCap)) continue;
			MobEntity herald = BossManager.spawn(world, EntityType.WITHER_SKELETON, pos, BossKind.ECLIPSE_HERALD);
			if (herald == null) continue;
			herald.addCommandTag(TAG);
			modifier(herald, EntityAttributes.GENERIC_MAX_HEALTH, HERALD_HEALTH_ID,
					EclipseRules.heraldHealthMultiplier(group) - 1.0);
			herald.setHealth(herald.getMaxHealth());
			herald.setCustomName(Text.translatable(BossKind.ECLIPSE_HERALD.nameKey()).formatted(Formatting.DARK_RED, Formatting.BOLD));
			Elites.setChampionBarName(herald, herald.getDisplayName());
			ALIVE.add(herald.getUuid());
			EclipsePursuit.seed(world, herald, leader);
			spawned = true;
			world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, herald.getX(), herald.getBodyY(0.5), herald.getZ(), 60, 0.6, 1.2, 0.6, 0.05);
		}
		if (spawned) {
			broadcast(server, Text.translatable("eclipse.aptitudes.herald").formatted(Formatting.DARK_RED, Formatting.BOLD));
			playToAll(world, dev.aptitudes.sound.ModSounds.HERALD_SPAWN, 1.0f);
		}
	}

	/** Al llegar al tope, el Heraldo sustituye una unidad ordinaria; nunca elimina otro jefe ni da botín. */
	private static boolean reserveHeraldSlot(ServerWorld world, int configuredCap) {
		int cap = Math.max(0, Math.min(512, configuredCap));
		if (cap == 0 || ALIVE.size() > cap) return false;
		if (ALIVE.size() < cap) return true;
		for (UUID id : List.copyOf(ALIVE)) {
			Entity entity = world.getEntity(id);
			if (entity instanceof MobEntity mob && mob.isAlive()
					&& !mob.getCommandTags().contains(BossKind.ECLIPSE_HERALD.tag())) {
				ALIVE.remove(id);
				EclipsePursuit.forget(id);
				dissolve(world, mob);
				return true;
			}
		}
		return false;
	}

	/** Búsqueda compartida histórica: dos bloques libres, también para los otros eventos. */
	static BlockPos findSpawn(ServerWorld world, BlockPos center, int min, int max) {
		return findSpawn(world, center, min, max, 2);
	}

	/** El asalto reserva más altura; el Heraldo puede tener el modificador Gigante. */
	private static BlockPos findSpawn(ServerWorld world, BlockPos center, int min, int max, int clearance) {
		Random random = world.random;
		for (int attempt = 0; attempt < 6; attempt++) {
			double angle = random.nextDouble() * Math.PI * 2;
			int dist = min + random.nextInt(Math.max(1, max - min + 1));
			int x = center.getX() + (int) Math.round(Math.cos(angle) * dist);
			int z = center.getZ() + (int) Math.round(Math.sin(angle) * dist);
			if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
			int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			BlockPos pos = new BlockPos(x, y, z);
			if (!world.getWorldBorder().contains(pos)) continue;
			BlockPos below = pos.down();
			if (Math.abs(y - center.getY()) > 12) continue;
			if (!world.getBlockState(below).isSideSolidFullSquare(world, below, Direction.UP)) continue;
			if (!world.getFluidState(below).isEmpty()) continue;
			boolean clear = true;
			for (int height = 0; height < clearance; height++) {
				if (!world.isAir(pos.up(height))) { clear = false; break; }
			}
			if (!clear) continue;
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
