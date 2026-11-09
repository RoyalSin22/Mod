package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityState;
import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.boss.BossRules.StrikeShape;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.StructureWorldAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** Jefes en estructuras: fases telegráficas, barra de campeón y núcleos para armas de evolución. */
public final class BossManager {
	private static final String PHASE_TWO = Aptitudes.MOD_ID + ".boss_phase_2";
	private static final String PHASE_THREE = Aptitudes.MOD_ID + ".boss_phase_3";
	private static final String UPGRADE_DATA = "aptitudes_boss_cores";
	private static final Map<UUID, BossKind> LOADED = new HashMap<>();
	private static final Map<UUID, Long> ABILITY_READY = new HashMap<>();
	private static final Map<UUID, Strike> STRIKES = new HashMap<>();

	private record Strike(Vec3d center, long impactAt, double heading) {}
	private BossManager() {}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof MobEntity mob) kind(mob).ifPresent(k -> LOADED.put(mob.getUuid(), k));
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> forget(entity.getUuid()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			LOADED.clear();
			ABILITY_READY.clear();
			STRIKES.clear();
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob) onDeath(mob, source.getAttacker());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 10 == 0) tick(server);
		});
	}

	public static MobEntity spawn(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos, BossKind kind) {
		return spawn(world, type, pos, kind, world.random);
	}

	/** Usa la región de generación para inicializar y guardar el jefe junto con su chunk. */
	public static MobEntity spawn(StructureWorldAccess world, EntityType<? extends MobEntity> type, BlockPos pos,
								   BossKind kind, Random random) {
		return spawn(world, type, pos, kind, random, mob -> {});
	}

	/** Permite anotar el origen de una arena antes de que el chunk guarde la entidad. */
	public static MobEntity spawn(StructureWorldAccess world, EntityType<? extends MobEntity> type, BlockPos pos,
								  BossKind kind, Random random, Consumer<MobEntity> prepare) {
		MobEntity mob = type.create(world.toServerWorld());
		if (mob == null) return null;
		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360f, 0);
		// Consultar la dificultad en ServerWorld pediría un chunk completo que todavía se está generando.
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
		Elites.makeElite(mob, true, random);
		mob.addCommandTag(kind.tag());
		mob.setCustomName(Text.translatable(kind.nameKey()).formatted(Formatting.GOLD));
		mob.setCustomNameVisible(true);
		mob.setPersistent();
		if (kind == BossKind.TOWER_SENTINEL) {
			mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.item.Items.NETHERITE_SWORD));
			mob.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.IRON_HELMET));
		} else if (kind == BossKind.SHRINE_ORACLE) {
			mob.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.GOLDEN_HELMET));
		} else if (kind == BossKind.ECLIPSE_HERALD) {
			mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.item.Items.NETHERITE_SWORD));
			mob.equipStack(EquipmentSlot.HEAD, new ItemStack(net.minecraft.item.Items.NETHERITE_HELMET));
			mob.equipStack(EquipmentSlot.CHEST, new ItemStack(net.minecraft.item.Items.NETHERITE_CHESTPLATE));
		} else {
			mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.item.Items.IRON_AXE));
			mob.equipStack(EquipmentSlot.CHEST, new ItemStack(net.minecraft.item.Items.CHAINMAIL_CHESTPLATE));
		}
		for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0.0f);
		prepare.accept(mob);
		world.spawnEntityAndPassengers(mob);
		return mob;
	}

	private static Optional<BossKind> kind(MobEntity mob) {
		for (String tag : mob.getCommandTags()) {
			BossKind kind = BossKind.fromTag(tag);
			if (kind != null) return Optional.of(kind);
		}
		return Optional.empty();
	}

	private static void tick(MinecraftServer server) {
		// El daño devuelto por una víctima puede matar y retirar al jefe en este mismo tick.
		for (UUID id : List.copyOf(LOADED.keySet())) {
			BossKind kind = LOADED.get(id);
			if (kind == null) continue;
			MobEntity boss = find(server, id);
			if (boss == null || !boss.isAlive() || !(boss.getWorld() instanceof ServerWorld world)) {
				forget(id);
				continue;
			}
			int phase = BossRules.phase(boss.getHealth() / boss.getMaxHealth());
			if (phase >= 2 && !boss.getCommandTags().contains(PHASE_TWO)) transition(world, boss, kind, 2, PHASE_TWO);
			if (phase >= 3 && !boss.getCommandTags().contains(PHASE_THREE)) transition(world, boss, kind, 3, PHASE_THREE);
			if (phase != 3) {
				STRIKES.remove(id);
			} else if (kind == BossKind.ECLIPSE_HERALD) {
				if (ABILITY_READY.getOrDefault(id, 0L) <= world.getTime()) {
					ABILITY_READY.put(id, world.getTime() + 60);
					heraldStrike(world, boss);
				}
			} else {
				structureStrike(world, boss, kind, id);
			}
		}
	}

	private static void forget(UUID id) {
		LOADED.remove(id);
		ABILITY_READY.remove(id);
		STRIKES.remove(id);
	}

	private static void structureStrike(ServerWorld world, MobEntity boss, BossKind kind, UUID id) {
		long now = world.getTime();
		StrikeShape shape = switch (kind) {
			case TOWER_SENTINEL -> StrikeShape.CIRCLE;
			case SHRINE_ORACLE -> StrikeShape.RING;
			default -> StrikeShape.CONE;
		};
		Strike pending = STRIKES.get(id);
		if (pending != null) {
			Vec3d center = pending.center();
			if (!world.isChunkLoaded(BlockPos.ofFloored(center))) {
				STRIKES.remove(id);
			} else if (now >= pending.impactAt()) {
				STRIKES.remove(id);
				impact(world, center, kind);
				float damage = switch (kind) {
					case TOWER_SENTINEL -> 5.0f;
					case SHRINE_ORACLE -> 4.0f;
					default -> 4.5f;
				};
				for (ServerPlayerEntity target : strikeTargets(world, boss, center, shape.radius)) {
					if (!boss.isAlive()) break;
					Vec3d offset = target.getPos().subtract(center);
					if (!shape.contains(offset.x, offset.y, offset.z, pending.heading())) continue;
					if (target.damage(world.getDamageSources().mobAttack(boss), damage)) {
						if (kind == BossKind.SHRINE_ORACLE) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
						knockback(target, center);
					}
				}
			} else {
				drawWarning(world, pending, shape, kind);
			}
			return;
		}
		if (ABILITY_READY.getOrDefault(id, 0L) > now) return;
		Vec3d center = boss.getPos();
		List<ServerPlayerEntity> targets = strikeTargets(world, boss, center, shape.radius);
		if (targets.isEmpty()) return;
		double heading = shape == StrikeShape.CONE ? aim(boss, center, targets) : 0;
		Strike strike = new Strike(center, now + BossRules.STRIKE_WARNING_TICKS, heading);
		STRIKES.put(id, strike);
		ABILITY_READY.put(id, now + (kind == BossKind.SHRINE_ORACLE ? 80 : 60));
		drawWarning(world, strike, shape, kind);
		world.playSound(null, center.x, center.y, center.z, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
				SoundCategory.HOSTILE, 0.8f, kind == BossKind.SHRINE_ORACLE ? 1.4f : 0.9f);
	}

	private static List<ServerPlayerEntity> strikeTargets(ServerWorld world, MobEntity boss, Vec3d center, double radius) {
		// La caja es un filtro amplio. inStrike incluye el borde de altura al medir los pies.
		Box area = new Box(center, center).expand(radius, BossRules.STRIKE_HEIGHT + 1, radius);
		Vec3d origin = center.add(0, 0.7, 0);
		return world.getEntitiesByClass(ServerPlayerEntity.class, area, player -> player.isAlive()
				&& !player.isCreative() && !player.isSpectator()
				&& BossRules.inStrike(player.getX() - center.x, player.getY() - center.y, player.getZ() - center.z, radius)
				&& !AbilityState.shouldBlockTarget(boss, player)
				&& world.raycast(new RaycastContext(origin, player.getPos().add(0, 0.7, 0),
						RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, boss)).getType() == HitResult.Type.MISS);
	}

	private static double aim(MobEntity boss, Vec3d center, List<ServerPlayerEntity> targets) {
		ServerPlayerEntity closest = targets.stream()
				.min(Comparator.comparingDouble(player -> player.squaredDistanceTo(center))).orElseThrow();
		Vec3d direction = closest.getPos().subtract(center);
		if (direction.horizontalLengthSquared() < 1e-9) return Math.toRadians(boss.getYaw() + 90.0);
		return Math.atan2(direction.z, direction.x);
	}

	private static void drawWarning(ServerWorld world, Strike strike, StrikeShape shape, BossKind kind) {
		int rgb = switch (kind) {
			case TOWER_SENTINEL -> 0xFF694A;
			case SHRINE_ORACLE -> 0xD78BFF;
			default -> 0xFFC34F;
		};
		var dust = ParticleFx.dust(rgb, 1.25f);
		Vec3d center = strike.center().add(0, 0.15, 0);
		if (shape == StrikeShape.CONE) {
			double start = strike.heading() - shape.halfAngle;
			double end = strike.heading() + shape.halfAngle;
			ParticleFx.line(world, dust, center, strikeEdge(center, shape.radius, start), 6);
			ParticleFx.line(world, dust, center, strikeEdge(center, shape.radius, end), 6);
			for (int i = 1; i < 12; i++) {
				Vec3d point = strikeEdge(center, shape.radius, start + (end - start) * i / 12);
				world.spawnParticles(dust, point.x, point.y, point.z, 1, 0, 0, 0, 0);
			}
		} else {
			ParticleFx.ring(world, center, shape.radius, dust, 24);
			if (shape.safeRadius > 0) ParticleFx.ring(world, center, shape.safeRadius, dust, 12);
		}
	}

	private static Vec3d strikeEdge(Vec3d center, double radius, double angle) {
		return center.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
	}

	private static MobEntity find(MinecraftServer server, UUID uuid) {
		for (ServerWorld world : server.getWorlds()) {
			Entity entity = world.getEntity(uuid);
			if (entity instanceof MobEntity mob) return mob;
		}
		return null;
	}

	private static void transition(ServerWorld world, MobEntity boss, BossKind kind, int phase, String tag) {
		boss.addCommandTag(tag);
		Text message = Text.translatable(phase == 2 ? "boss.aptitudes.phase_two" : "boss.aptitudes.phase_three",
				boss.getDisplayName()).formatted(Formatting.GOLD);
		for (ServerPlayerEntity player : world.getPlayers(p -> p.squaredDistanceTo(boss) <= 48 * 48)) player.sendMessage(message, false);
		world.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.ENTITY_WITHER_SPAWN,
				SoundCategory.HOSTILE, 0.8f, phase == 2 ? 1.25f : 0.8f);
		world.spawnParticles(kind == BossKind.SHRINE_ORACLE ? ParticleTypes.ENCHANT : ParticleTypes.SOUL,
				boss.getX(), boss.getBodyY(0.5), boss.getZ(), 48, 1.2, 1.0, 1.2, 0.08);
		if (kind == BossKind.TOWER_SENTINEL || kind == BossKind.ECLIPSE_HERALD) boss.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 160, phase - 2));
		else if (kind == BossKind.SHRINE_ORACLE) boss.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, phase - 2));
		else boss.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 160, phase - 2));
	}

	private static void heraldStrike(ServerWorld world, MobEntity boss) {
		Box area = boss.getBoundingBox().expand(6.0);
		impact(world, boss.getPos(), BossKind.ECLIPSE_HERALD);
		for (ServerPlayerEntity target : world.getEntitiesByClass(ServerPlayerEntity.class, area, PlayerEntity::isAlive)) {
			target.damage(world.getDamageSources().mobAttack(boss), 4.5f);
			knockback(target, boss.getPos());
		}
	}

	private static void impact(ServerWorld world, Vec3d center, BossKind kind) {
		world.spawnParticles(kind == BossKind.SHRINE_ORACLE ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.SWEEP_ATTACK,
				center.x, center.y + 0.1, center.z, 36, 2.5, 0.15, 2.5, 0.02);
		world.playSound(null, center.x, center.y, center.z, SoundEvents.ENTITY_GENERIC_EXPLODE,
				SoundCategory.HOSTILE, 0.55f, kind == BossKind.TOWER_SENTINEL ? 0.7f : 1.3f);
	}

	private static void knockback(ServerPlayerEntity target, Vec3d center) {
		Vec3d away = target.getPos().subtract(center).normalize();
		target.takeKnockback(0.9, -away.x, -away.z);
	}

	private static void onDeath(MobEntity mob, Entity attacker) {
		Optional<BossKind> kind = kind(mob);
		if (kind.isEmpty() || !(mob.getWorld() instanceof ServerWorld world)) return;
		ItemStack reward = new ItemStack(switch (kind.get()) {
			case TOWER_SENTINEL -> ModItems.TOWER_CORE;
			case SHRINE_ORACLE -> ModItems.SHRINE_SIGIL;
			case CAMP_CHAMPION -> ModItems.WARLORD_MEDALLION;
			case ECLIPSE_HERALD -> ModItems.ECLIPSE_CORE;
		});
		mob.dropStack(reward);
		if (attacker instanceof ServerPlayerEntity killer) {
			dev.aptitudes.contract.ContractManager.onBossKilled(killer, kind.get());
			// v31: el logro es para el que remata y para su grupo conectado (como los contratos de Jefe).
			for (ServerPlayerEntity member : dev.aptitudes.party.PartyManager.onlineMembers(killer)) {
				dev.aptitudes.achievement.Achievements.grant(member, dev.aptitudes.achievement.Achievement.SLAY_BOSS);
				if (kind.get() == BossKind.ECLIPSE_HERALD) {
					dev.aptitudes.achievement.Achievements.grant(member, dev.aptitudes.achievement.Achievement.SLAY_HERALD);
				}
			}
			world.getServer().getPlayerManager().broadcast(Text.translatable("boss.aptitudes.defeated",
					killer.getDisplayName(), mob.getDisplayName()).formatted(Formatting.GOLD), false);
		}
		forget(mob.getUuid());
	}

	/** Engarza un núcleo en una hoja/foco evolucionado: +0,5 daño por núcleo y uno de cada tipo. */
	public static boolean upgrade(ServerPlayerEntity player, ItemStack weapon, ItemStack material) {
		if (!isEvolutionWeapon(weapon) || !(material.getItem() instanceof BossCoreItem core)) return false;
		List<String> applied = upgrades(weapon);
		if (!BossRules.canApplyCore(applied, core.coreId())) {
			player.sendMessage(Text.translatable(applied.size() >= BossRules.MAX_WEAPON_CORES
					? "boss.aptitudes.weapon_full" : "boss.aptitudes.weapon_duplicate").formatted(Formatting.RED), true);
			return true;
		}
		NbtCompound root = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		NbtCompound saved = root.contains(UPGRADE_DATA, NbtCompound.COMPOUND_TYPE) ? root.getCompound(UPGRADE_DATA) : new NbtCompound();
		saved.putBoolean(core.coreId(), true);
		root.put(UPGRADE_DATA, saved);
		weapon.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		Identifier id = Aptitudes.id("boss_core/" + core.coreId());
		AttributeModifiersComponent attributes = weapon.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT)
				.with(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(id, BossRules.damageBonus(1), EntityAttributeModifier.Operation.ADD_VALUE), net.minecraft.component.type.AttributeModifierSlot.MAINHAND);
		weapon.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attributes);
		if (!player.isCreative()) material.decrement(1);
		player.sendMessage(Text.translatable("boss.aptitudes.weapon_upgraded", weapon.getName(), BossRules.damageBonus(applied.size() + 1)).formatted(Formatting.GOLD), true);
		return true;
	}

	public static boolean isEvolutionWeapon(ItemStack stack) { return stack.getItem() instanceof RoleBladeItem || stack.getItem() instanceof RoleFocusItem; }
	public static double damageBonus(ItemStack weapon) { return BossRules.damageBonus(upgrades(weapon).size()); }
	public static void appendTooltip(ItemStack stack, List<Text> tooltip) {
		if (!isEvolutionWeapon(stack)) return;
		List<String> cores = upgrades(stack);
		tooltip.add(Text.translatable("boss.aptitudes.weapon_cores", cores.size(), BossRules.MAX_WEAPON_CORES,
				BossRules.damageBonus(cores.size())).formatted(Formatting.DARK_PURPLE));
	}

	private static List<String> upgrades(ItemStack weapon) {
		NbtCompound root = weapon.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		if (!root.contains(UPGRADE_DATA, NbtCompound.COMPOUND_TYPE)) return List.of();
		NbtCompound saved = root.getCompound(UPGRADE_DATA);
		List<String> ids = new ArrayList<>(BossRules.MAX_WEAPON_CORES);
		for (String id : List.of("tower", "shrine", "camp", "eclipse")) if (saved.getBoolean(id)) ids.add(id);
		return List.copyOf(ids);
	}
}
