package dev.aptitudes.unique;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.Abilities;
import dev.aptitudes.ability.Ability;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.ability.Targeting;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.morph.MorphManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pasivas y activas de las habilidades únicas, y los contadores de sus gestas.
 * Todo el estado temporal vive en memoria (servidor) y se limpia al desconectar.
 */
public final class UniqueEffects {
	private static final Identifier KING_HEALTH = Aptitudes.id("unique/chosen_king_health");
	private static final Identifier PENDRAGON_ARMOR = Aptitudes.id("unique/pendragon_armor");
	private static final String FROZEN_TAG = "aptitudes.frozen";
	private static final List<EntityType<? extends AnimalEntity>> MUTATIONS = List.of(
			EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN, EntityType.RABBIT,
			EntityType.GOAT, EntityType.MOOSHROOM, EntityType.FOX, EntityType.PANDA);

	/** Rueda de la Adaptación: tipo de daño → [cargas, tick de la última]. */
	private static final Map<UUID, Map<String, int[]>> ADAPTATION = new HashMap<>();
	private static final Map<UUID, String> LAST_DAMAGE_TYPE = new HashMap<>();
	private static final Map<UUID, Object[]> ADAPT_IMMUNITY = new HashMap<>(); // {String tipo, Integer hasta}
	/** Archivo del Vacío: última habilidad de rol vista cerca. */
	private static final Map<UUID, String> ARCHIVED = new HashMap<>();
	/** Señor del Espacio-Tiempo: historial de posiciones (1 por segundo, 6 s). */
	private record Snapshot(RegistryKey<World> world, Vec3d pos, float health) {}
	private static final Map<UUID, Deque<Snapshot>> HISTORY = new HashMap<>();
	/** Dominio de la Negación activo: dueño → zona. */
	private record Zone(RegistryKey<World> world, Vec3d center, int until) {}
	private static final Map<UUID, Zone> NEGATION_ZONES = new HashMap<>();
	/** Explotador: ventana de botín doble (jugador → tick fin). */
	private static final Map<UUID, Integer> LOOT_WINDOW = new HashMap<>();

	private UniqueEffects() {
	}

	public static void register() {
		// Explotador (pasiva): 25 % de mineral doble.
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayerEntity sp && state.isIn(ConventionalBlockTags.ORES)
					&& UniqueManager.has(sp, UniqueSkill.EXPLOITER) && world.random.nextFloat() < 0.25f) {
				Block.dropStacks(state, world, pos, blockEntity, sp, sp.getMainHandStack());
			}
		});
		// Quietud Eterna: si el servidor se cerró con mobs congelados, se descongelan al cargar.
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity.getCommandTags().contains(FROZEN_TAG)) unfreeze(entity);
		});
	}

	public static void forget(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		ADAPTATION.remove(id);
		LAST_DAMAGE_TYPE.remove(id);
		ADAPT_IMMUNITY.remove(id);
		ARCHIVED.remove(id);
		HISTORY.remove(id);
		NEGATION_ZONES.remove(id);
		LOOT_WINDOW.remove(id);
	}

	// ------------------------------------------------------------- Tick (1 s)

	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		NEGATION_ZONES.values().removeIf(z -> z.until() < now);
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (!p.isAlive()) continue;
			if (p.isInLava()) UniqueManager.progressData(p).lavaSeconds++;

			UniqueSkill skill = UniqueManager.skillOf(p);
			if (skill == null) continue;
			switch (skill) {
				case HELLFIRE -> quiet(p, StatusEffects.FIRE_RESISTANCE, 60, 0);
				case CHOSEN_KING -> {
					for (PlayerEntity ally : Targeting.playersAround(p, 12)) {
						quiet(ally, StatusEffects.STRENGTH, 40, 0);
						quiet(ally, StatusEffects.REGENERATION, 40, 0);
					}
				}
				case SPACETIME -> {
					Deque<Snapshot> history = HISTORY.computeIfAbsent(p.getUuid(), k -> new ArrayDeque<>());
					history.addLast(new Snapshot(p.getWorld().getRegistryKey(), p.getPos(), p.getHealth()));
					while (history.size() > 6) history.removeFirst();
				}
				case NEGATION -> p.getStatusEffects().stream()
						.filter(e -> !e.getEffectType().value().isBeneficial())
						.map(StatusEffectInstance::getEffectType).toList().forEach(p::removeStatusEffect);
				case GENETICIST -> {
					for (PassiveEntity baby : p.getWorld().getEntitiesByClass(PassiveEntity.class,
							p.getBoundingBox().expand(16), PassiveEntity::isBaby)) {
						baby.growUp(10, false);
					}
				}
				case PENDRAGON -> AttributeApplier.set(p, EntityAttributes.GENERIC_ARMOR, PENDRAGON_ARMOR,
						p.getMainHandStack().getItem() instanceof SwordItem ? 4.0 : 0.0, Operation.ADD_VALUE);
				case STILLNESS -> {
					if (p.isSneaking() && p.getVelocity().horizontalLengthSquared() < 1.0e-3) {
						quiet(p, StatusEffects.RESISTANCE, 40, 0);
					}
				}
				case WORLD_TREE -> {
					var ground = p.getWorld().getBlockState(p.getBlockPos().down());
					if (ground.isIn(net.minecraft.registry.tag.BlockTags.DIRT) || ground.isIn(net.minecraft.registry.tag.BlockTags.LEAVES)) {
						quiet(p, StatusEffects.REGENERATION, 40, 0);
					}
				}
				case ADAPTATION -> {
					Map<String, int[]> stacks = ADAPTATION.get(p.getUuid());
					if (stacks != null) stacks.values().removeIf(s -> now - s[1] > 60 * 20);
				}
				default -> {
				}
			}
		}
	}

	public static void applyAttributes(ServerPlayerEntity player, UniqueSkill skill) {
		// Atravesacielos: +1 de alcance contra entidades.
		AttributeApplier.set(player, EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE,
				dev.aptitudes.Aptitudes.id("unique/skypiercer"), skill == UniqueSkill.SKYPIERCER ? 1.0 : 0.0,
				Operation.ADD_VALUE);
		AttributeApplier.set(player, EntityAttributes.GENERIC_MAX_HEALTH, KING_HEALTH,
				skill == UniqueSkill.CHOSEN_KING ? 8.0 : 0.0, Operation.ADD_VALUE);
		if (skill != UniqueSkill.PENDRAGON) {
			AttributeApplier.set(player, EntityAttributes.GENERIC_ARMOR, PENDRAGON_ARMOR, 0.0, Operation.ADD_VALUE);
		}
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	// ---------------------------------------------------------------- Daño

	/** Registro de tipos de daño (gesta de Adaptación), inmunidades y caídas. @return false = cancelar. */
	public static boolean allowIncomingDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		String type = source.getName();
		UniqueManager.progressData(player).addDamageType(type);
		LAST_DAMAGE_TYPE.put(player.getUuid(), type);

		UniqueSkill skill = UniqueManager.skillOf(player);
		if (skill == UniqueSkill.SKY_LORD && source.isOf(DamageTypes.FALL)) return false;
		if (skill == UniqueSkill.ADAPTATION) {
			Object[] immunity = ADAPT_IMMUNITY.get(player.getUuid());
			if (immunity != null && type.equals(immunity[0]) && (Integer) immunity[1] >= player.getServer().getTicks()) {
				return false;
			}
		}
		return true;
	}

	/** Modificación de daño (desde el mixin de {@code LivingEntity#damage}): entrante y saliente. */
	public static float modifyDamage(LivingEntity target, DamageSource source, float amount) {
		if (amount <= 0 || target.getWorld().isClient()) return amount;

		// Entrante – Rueda de la Adaptación: −10 % por golpe del mismo tipo en 60 s (máx. −80 %).
		if (target instanceof ServerPlayerEntity victim && UniqueManager.has(victim, UniqueSkill.ADAPTATION)) {
			int now = victim.getServer().getTicks();
			int[] stack = ADAPTATION.computeIfAbsent(victim.getUuid(), k -> new HashMap<>())
					.computeIfAbsent(source.getName(), k -> new int[]{0, now});
			amount *= 1.0f - Math.min(0.8f, 0.1f * stack[0]);
			stack[0]++;
			stack[1] = now;
		}

		// Saliente
		if (source.getAttacker() instanceof ServerPlayerEntity attacker && attacker != target) {
			UniqueSkill skill = UniqueManager.skillOf(attacker);
			if (skill == null) return amount;
			boolean melee = source.getSource() == attacker;
			switch (skill) {
				case PARASITE -> attacker.heal(amount * 0.10f);
				case FUNGAL -> {
					if (melee) {
						target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 80, 0));
						if (target instanceof PlayerEntity) {
							target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
						}
					}
				}
				case HELLFIRE -> {
					if (melee) target.setOnFireFor(5);
				}
				case PENDRAGON -> {
					if (melee && attacker.getMainHandStack().getItem() instanceof SwordItem) amount *= 1.10f;
				}
				case LAST_BULLET -> {
					if (!melee) amount *= 1.10f;
				}
				default -> {
				}
			}
		}
		return amount;
	}

	// --------------------------------------------------------------- Muertes

	public static void onKill(ServerPlayerEntity killer, LivingEntity victim, DamageSource source) {
		UniqueProgress progress = UniqueManager.progressData(killer);
		BlockPos below = killer.getBlockPos().down();
		if (killer.getWorld().getBlockState(below).isOf(Blocks.MYCELIUM)
				|| killer.getWorld().getBiome(killer.getBlockPos()).matchesKey(BiomeKeys.MUSHROOM_FIELDS)) {
			progress.myceliumKills++;
		}
		if (killer.getHealth() <= 6.0f) progress.lowHpKills++;
		if (victim.getType() == EntityType.WITHER) progress.wither = true;
		if (victim.getType() == EntityType.ENDER_DRAGON) progress.dragon = true;

		UniqueSkill skill = UniqueManager.skillOf(killer);
		if (skill == null) return;
		ServerWorld world = killer.getServerWorld();
		switch (skill) {
			case GREED -> {
				if (world.random.nextFloat() < 0.15f) {
					victim.dropStack(new net.minecraft.item.ItemStack(net.minecraft.item.Items.EMERALD));
				}
			}
			case FUNGAL -> {
				if (victim.hasStatusEffect(StatusEffects.POISON)) {
					for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, victim.getBoundingBox().expand(4),
							e -> Targeting.isHostileTo(killer, e))) {
						e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
					}
					world.spawnParticles(ParticleTypes.MYCELIUM, victim.getX(), victim.getBodyY(0.5), victim.getZ(),
							40, 1.5, 0.5, 1.5, 0.05);
				}
			}
			case EXPLOITER -> {
				Integer until = LOOT_WINDOW.get(killer.getUuid());
				if (until != null && until >= killer.getServer().getTicks()) {
					Box area = victim.getBoundingBox().expand(1.5);
					// El botín aparece en este mismo tick; se duplica en el siguiente.
					AbilityState.schedule(killer.getServer(), 1, () -> {
						for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, area, i -> i.getItemAge() < 5)) {
							world.spawnEntity(new ItemEntity(world, item.getX(), item.getY(), item.getZ(), item.getStack().copy()));
						}
					});
				}
			}
			default -> {
			}
		}
	}

	// ----------------------------------------------------- Interacciones

	/** Archivo del Vacío: guarda la última habilidad de rol usada a menos de 16 bloques. */
	public static void recordAbility(ServerPlayerEntity user, String nodeId) {
		if (RoleTree.get(nodeId) == null) return;
		for (PlayerEntity p : Targeting.playersAround(user, 16)) {
			if (p instanceof ServerPlayerEntity sp && UniqueManager.has(sp, UniqueSkill.VOID_ARCHIVE)) {
				ARCHIVED.put(sp.getUuid(), nodeId);
			}
		}
	}

	/** {@code true} si el jugador está dentro del Dominio de la Negación de OTRO jugador. */
	public static boolean isNegated(ServerPlayerEntity player) {
		for (Map.Entry<UUID, Zone> e : NEGATION_ZONES.entrySet()) {
			Zone z = e.getValue();
			if (!e.getKey().equals(player.getUuid()) && z.world() == player.getWorld().getRegistryKey()
					&& z.center().squaredDistanceTo(player.getPos()) <= 64) {
				return true;
			}
		}
		return false;
	}

	// ------------------------------------------------------------- Activas

	public static boolean activate(ServerPlayerEntity p, UniqueSkill skill) {
		ServerWorld world = p.getServerWorld();
		MinecraftServer server = p.getServer();
		int now = server.getTicks();
		switch (skill) {
			case ADAPTATION -> {
				String last = LAST_DAMAGE_TYPE.get(p.getUuid());
				if (last == null) return false;
				ADAPT_IMMUNITY.put(p.getUuid(), new Object[]{last, now + 200});
				effect(p, StatusEffects.REGENERATION, 200, 1);
				burst(p, ParticleTypes.ENCHANTED_HIT, 40);
				sound(p, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE);
				return true;
			}
			case GREED -> {
				int stolen = 0;
				for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(8),
						e -> e != p && e.isAlive())) {
					for (StatusEffectInstance effect : List.copyOf(e.getStatusEffects())) {
						if (!effect.getEffectType().value().isBeneficial()) continue;
						p.addStatusEffect(new StatusEffectInstance(effect));
						e.removeStatusEffect(effect.getEffectType());
						stolen++;
					}
				}
				sound(p, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
				return stolen > 0;
			}
			case VOID_ARCHIVE -> {
				String nodeId = ARCHIVED.get(p.getUuid());
				Ability ability = nodeId == null ? null : Abilities.get(nodeId);
				RoleNode node = nodeId == null ? null : RoleTree.get(nodeId);
				if (ability == null || node == null || !node.active()) return false;
				burst(p, ParticleTypes.PORTAL, 40);
				return ability.use(p, 3);
			}
			case FUNGAL -> {
				Vec3d center = p.getPos();
				AbilityState.repeat(server, 6, 20, i -> {
					world.spawnParticles(ParticleTypes.MYCELIUM, center.x, center.y + 1, center.z, 80, 3, 1, 3, 0.02);
					for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(6),
							e -> Targeting.isHostileTo(p, e))) {
						effect(e, StatusEffects.POISON, 60, 1);
						effect(e, StatusEffects.BLINDNESS, 60, 0);
					}
				});
				sound(p, SoundEvents.BLOCK_FUNGUS_BREAK);
				return true;
			}
			case PARASITE -> {
				LivingEntity target = Targeting.raycastLiving(p, 8);
				if (target == null) return false;
				effect(p, StatusEffects.INVISIBILITY, 100, 0);
				AbilityState.repeat(server, 5, 20, i -> {
					if (!p.isAlive() || !target.isAlive()) return;
					target.damage(p.getDamageSources().indirectMagic(p, p), 2.0f);
					p.heal(2.0f);
					Targeting.particleLine(world, ParticleTypes.DAMAGE_INDICATOR, target.getEyePos(), p.getEyePos());
				});
				return true;
			}
			case HELLFIRE -> {
				for (LivingEntity e : Targeting.hostilesAround(p, 7)) {
					e.damage(p.getDamageSources().onFire(), 10.0f);
					e.setOnFireFor(8);
				}
				world.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 0.5, p.getZ(), 200, 4, 0.5, 4, 0.05);
				if (Aptitudes.config().uniqueGriefing) {
					for (int i = 0; i < 20; i++) {
						BlockPos pos = p.getBlockPos().add(world.random.nextInt(11) - 5, 0, world.random.nextInt(11) - 5);
						if (world.getBlockState(pos).isAir() && world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
							world.setBlockState(pos, Blocks.FIRE.getDefaultState());
						}
					}
				}
				sound(p, SoundEvents.ENTITY_BLAZE_SHOOT);
				return true;
			}
			case CHOSEN_KING -> {
				for (LivingEntity e : Targeting.hostilesAround(p, 16)) {
					effect(e, StatusEffects.SLOWNESS, 120, 4);
					effect(e, StatusEffects.WEAKNESS, 120, 1);
				}
				burst(p, ParticleTypes.TOTEM_OF_UNDYING, 60);
				sound(p, SoundEvents.BLOCK_BELL_USE);
				return true;
			}
			case SPACETIME -> {
				Deque<Snapshot> history = HISTORY.get(p.getUuid());
				if (history == null || history.isEmpty()) return false;
				Snapshot target = history.peekFirst();
				if (target.world() != p.getWorld().getRegistryKey()) return false;
				p.requestTeleport(target.pos().x, target.pos().y, target.pos().z);
				p.setHealth(Math.max(p.getHealth(), Math.min(target.health(), p.getMaxHealth())));
				p.fallDistance = 0;
				history.clear();
				burst(p, ParticleTypes.REVERSE_PORTAL, 60);
				sound(p, SoundEvents.ENTITY_ENDERMAN_TELEPORT);
				return true;
			}
			case NEGATION -> {
				NEGATION_ZONES.put(p.getUuid(), new Zone(p.getWorld().getRegistryKey(), p.getPos(), now + 8 * 20));
				Vec3d center = p.getPos();
				AbilityState.repeat(server, 8, 20, i -> {
					world.spawnParticles(ParticleTypes.WHITE_ASH, center.x, center.y + 1, center.z, 120, 5, 1, 5, 0);
					for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, new Box(center, center).expand(8), MobEntity::isAlive)) {
						mob.clearStatusEffects();
						effect(mob, StatusEffects.SLOWNESS, 30, 1);
					}
				});
				sound(p, SoundEvents.BLOCK_BEACON_DEACTIVATE);
				return true;
			}
			case GENETICIST -> {
				int mutated = 0;
				for (AnimalEntity animal : world.getEntitiesByClass(AnimalEntity.class, p.getBoundingBox().expand(6),
						a -> a.isAlive() && !AbilityState.isSummon(a))) {
					EntityType<? extends AnimalEntity> next = MUTATIONS.get(world.random.nextInt(MUTATIONS.size()));
					if (next == animal.getType()) continue;
					if (animal.convertTo(next, false) != null) mutated++;
				}
				burst(p, ParticleTypes.HAPPY_VILLAGER, 40);
				sound(p, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE);
				return mutated > 0;
			}
			case MIRROR -> {
				LivingEntity target = Targeting.raycastLiving(p, 16);
				if (target == null || target instanceof PlayerEntity) return false;
				MorphManager.morph(p, target.getType(), 30 * 20, Math.min(20.0f, target.getMaxHealth() / 2));
				return true;
			}
			case EXPLOITER -> {
				LOOT_WINDOW.put(p.getUuid(), now + 15 * 20);
				effect(p, StatusEffects.LUCK, 300, 2);
				sound(p, SoundEvents.BLOCK_CHEST_OPEN);
				return true;
			}
			case PENDRAGON -> {
				Vec3d start = p.getEyePos();
				Vec3d dir = p.getRotationVec(1.0f);
				Vec3d end = start.add(dir.multiply(30));
				Targeting.particleLine(world, ParticleTypes.END_ROD, start, end);
				for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class, new Box(start, end).expand(1.5),
						e -> Targeting.isHostileTo(p, e) && distanceToRay(e.getPos().add(0, e.getHeight() / 2, 0), start, dir) < 1.8)) {
					float damage = e.getType().isIn(EntityTypeTags.UNDEAD) ? 32.0f : 16.0f;
					e.damage(p.getDamageSources().indirectMagic(p, p), damage);
				}
				sound(p, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER);
				return true;
			}
			case SKY_LORD -> {
				if (p.isCreative() || p.isSpectator()) return false;
				p.getAbilities().allowFlying = true;
				p.getAbilities().flying = true;
				p.sendAbilitiesUpdate();
				AbilityState.schedule(server, 12 * 20, () -> {
					if (p.isRemoved() || p.isCreative() || p.isSpectator()) return;
					p.getAbilities().allowFlying = false;
					p.getAbilities().flying = false;
					p.sendAbilitiesUpdate();
					effect(p, StatusEffects.SLOW_FALLING, 200, 0);
				});
				sound(p, SoundEvents.ITEM_ELYTRA_FLYING);
				return true;
			}
			case STILLNESS -> {
				List<Entity> frozen = new ArrayList<>();
				for (Entity e : world.getOtherEntities(p, p.getBoundingBox().expand(12))) {
					if (e instanceof MobEntity mob && !mob.isAiDisabled() && !AbilityState.isSummon(mob)) {
						mob.setAiDisabled(true);
						mob.setVelocity(Vec3d.ZERO);
						mob.addCommandTag(FROZEN_TAG);
						frozen.add(mob);
					} else if (e instanceof ProjectileEntity proj && !proj.hasNoGravity()) {
						proj.setVelocity(Vec3d.ZERO);
						proj.setNoGravity(true);
						proj.addCommandTag(FROZEN_TAG);
						frozen.add(proj);
					}
				}
				AbilityState.schedule(server, 5 * 20, () -> frozen.forEach(UniqueEffects::unfreeze));
				burst(p, ParticleTypes.SNOWFLAKE, 80);
				sound(p, SoundEvents.BLOCK_BEACON_DEACTIVATE);
				return !frozen.isEmpty();
			}
			case SKYPIERCER -> {
				Vec3d point = Targeting.lookEnd(p, 30);
				Targeting.particleLine(world, ParticleTypes.END_ROD, point.add(0, 20, 0), point);
				AbilityState.schedule(server, 15, () -> {
					for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
							new net.minecraft.util.math.Box(point, point).expand(4), e -> Targeting.isHostileTo(p, e))) {
						e.damage(p.getDamageSources().indirectMagic(p, p), 14);
						e.addVelocity(0, 0.8, 0);
						e.velocityModified = true;
					}
					world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, point.x, point.y, point.z, 1, 0, 0, 0, 0);
					world.playSound(null, point.x, point.y, point.z, SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.0f, 1.0f);
				});
				return true;
			}
			case WORLD_TREE -> {
				Vec3d center = p.getPos();
				AbilityState.repeat(server, 8, 20, i -> {
					for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
							new net.minecraft.util.math.Box(center, center).expand(8), LivingEntity::isAlive)) {
						if (e instanceof PlayerEntity) e.heal(2.0f);
						else if (Targeting.isHostileTo(p, e)) effect(e, StatusEffects.SLOWNESS, 30, 3);
					}
					world.spawnParticles(ParticleTypes.CHERRY_LEAVES, center.x, center.y + 3, center.z, 60, 6, 1, 6, 0);
				});
				burst(p, ParticleTypes.HAPPY_VILLAGER, 60);
				sound(p, SoundEvents.BLOCK_AZALEA_PLACE);
				return true;
			}
			case LAST_BULLET -> {
				LivingEntity target = Targeting.raycastLiving(p, 40);
				Vec3d end = target != null ? target.getEyePos() : Targeting.lookEnd(p, 40);
				Targeting.particleLine(world, ParticleTypes.ELECTRIC_SPARK, p.getEyePos(), end);
				sound(p, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST);
				if (target == null) return false;
				float damage = target.getHealth() < target.getMaxHealth() * 0.5f ? 60f : 30f;
				target.damage(p.getDamageSources().indirectMagic(p, p), damage);
				return true;
			}
			case GATE_OF_ARMS -> {
				AbilityState.repeat(server, 30, 2, i -> {
					double ang = p.getRandom().nextDouble() * Math.PI * 2;
					double dist = p.getRandom().nextDouble() * 10;
					Vec3d hit = p.getPos().add(Math.cos(ang) * dist, 0, Math.sin(ang) * dist);
					Targeting.particleLine(world, ParticleTypes.ENCHANTED_HIT, hit.add(0, 8, 0), hit);
					for (LivingEntity e : world.getEntitiesByClass(LivingEntity.class,
							new net.minecraft.util.math.Box(hit, hit).expand(2), e -> Targeting.isHostileTo(p, e))) {
						e.timeUntilRegen = 0;
						e.damage(p.getDamageSources().indirectMagic(p, p), 4);
					}
				});
				sound(p, SoundEvents.BLOCK_ANVIL_LAND);
				return true;
			}
			case HARBINGER -> {
				LivingEntity target = Targeting.raycastLiving(p, 20);
				if (target == null) return false;
				effect(target, StatusEffects.GLOWING, 60, 0);
				sound(p, SoundEvents.ENTITY_WITHER_AMBIENT);
				AbilityState.schedule(server, 60, () -> {
					if (!target.isAlive()) return;
					boolean boss = target.getType() == EntityType.WITHER || target.getType() == EntityType.ENDER_DRAGON
							|| target.getType() == EntityType.WARDEN;
					float damage = Math.max(4.0f, target.getMaxHealth() * (boss ? 0.10f : 0.50f));
					target.damage(p.getDamageSources().indirectMagic(p, p), damage);
					world.spawnParticles(ParticleTypes.SOUL, target.getX(), target.getBodyY(0.5), target.getZ(),
							30, 0.4, 0.6, 0.4, 0.05);
				});
				return true;
			}
		}
		return false;
	}

	private static void unfreeze(Entity e) {
		e.removeCommandTag(FROZEN_TAG);
		if (e instanceof MobEntity mob) mob.setAiDisabled(false);
		if (e instanceof ProjectileEntity) e.setNoGravity(false);
	}

	private static double distanceToRay(Vec3d point, Vec3d origin, Vec3d dir) {
		Vec3d rel = point.subtract(origin);
		double t = Math.max(0, rel.dotProduct(dir));
		return rel.subtract(dir.multiply(t)).length();
	}

	// ------------------------------------------------------------ Utilidades

	private static void effect(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, Math.max(0, amplifier)));
	}

	private static void quiet(LivingEntity e, RegistryEntry<StatusEffect> effect, int ticks, int amplifier) {
		e.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, true, false));
	}

	private static void burst(ServerPlayerEntity p, net.minecraft.particle.ParticleEffect particle, int count) {
		p.getServerWorld().spawnParticles(particle, p.getX(), p.getBodyY(0.5), p.getZ(), count, 0.8, 0.8, 0.8, 0.05);
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}

	private static void sound(ServerPlayerEntity p, RegistryEntry<SoundEvent> sound) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}
}
