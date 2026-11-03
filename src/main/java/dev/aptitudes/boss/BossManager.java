package dev.aptitudes.boss;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Jefes en estructuras: fases telegráficas, barra de campeón y núcleos para armas de evolución. */
public final class BossManager {
	private static final String PHASE_TWO = Aptitudes.MOD_ID + ".boss_phase_2";
	private static final String PHASE_THREE = Aptitudes.MOD_ID + ".boss_phase_3";
	private static final String UPGRADE_DATA = "aptitudes_boss_cores";
	private static final Map<UUID, BossKind> LOADED = new HashMap<>();
	private static final Map<UUID, Long> ABILITY_READY = new HashMap<>();
	private BossManager() {}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof MobEntity mob) kind(mob).ifPresent(k -> LOADED.put(mob.getUuid(), k));
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			LOADED.remove(entity.getUuid());
			ABILITY_READY.remove(entity.getUuid());
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof MobEntity mob) onDeath(mob, source.getAttacker());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 10 == 0) tick(server);
		});
	}

	/** Crea un campeón nombrado; las fases se guardan en etiquetas del mob vanilla. Devuelve el jefe o null. */
	public static MobEntity spawn(ServerWorld world, EntityType<? extends MobEntity> type, BlockPos pos, BossKind kind) {
		MobEntity mob = type.create(world);
		if (mob == null) return null;
		mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
		Elites.makeElite(mob, true, world.random); // reutiliza el escalado, las barras y recompensas de campeón
		mob.addCommandTag(kind.tag());
		mob.setCustomName(Text.translatable(kind.nameKey()).formatted(Formatting.GOLD));
		mob.setCustomNameVisible(true);
		Elites.setChampionBarName(mob, mob.getDisplayName());
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
		world.spawnEntityAndPassengers(mob);
		LOADED.put(mob.getUuid(), kind);
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
		Iterator<Map.Entry<UUID, BossKind>> iterator = LOADED.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, BossKind> entry = iterator.next();
			MobEntity boss = find(server, entry.getKey());
			if (boss == null || !boss.isAlive() || !(boss.getWorld() instanceof ServerWorld world)) {
				iterator.remove();
				ABILITY_READY.remove(entry.getKey());
				continue;
			}
			int phase = BossRules.phase(boss.getHealth() / boss.getMaxHealth());
			if (phase >= 2 && !boss.getCommandTags().contains(PHASE_TWO)) transition(world, boss, entry.getValue(), 2, PHASE_TWO);
			if (phase >= 3 && !boss.getCommandTags().contains(PHASE_THREE)) transition(world, boss, entry.getValue(), 3, PHASE_THREE);
			if (phase == 3 && ABILITY_READY.getOrDefault(boss.getUuid(), 0L) <= world.getTime()) {
				ABILITY_READY.put(boss.getUuid(), world.getTime() + (entry.getValue() == BossKind.SHRINE_ORACLE ? 80 : 60));
				ability(world, boss, entry.getValue());
			}
		}
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

	/** Golpe de área al entrar a fase final, repetido cada 3–4 s y limitado a jugadores cercanos. */
	private static void ability(ServerWorld world, MobEntity boss, BossKind kind) {
		Box area = boss.getBoundingBox().expand(kind == BossKind.TOWER_SENTINEL ? 4.5 : 6.0);
		List<ServerPlayerEntity> targets = world.getEntitiesByClass(ServerPlayerEntity.class, area, PlayerEntity::isAlive);
		world.spawnParticles(kind == BossKind.SHRINE_ORACLE ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.SWEEP_ATTACK,
				boss.getX(), boss.getY() + 0.1, boss.getZ(), 36, 2.5, 0.15, 2.5, 0.02);
		world.playSound(null, boss.getX(), boss.getY(), boss.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
				SoundCategory.HOSTILE, 0.55f, kind == BossKind.TOWER_SENTINEL ? 0.7f : 1.3f);
		for (ServerPlayerEntity target : targets) {
			float damage = kind == BossKind.TOWER_SENTINEL ? 5.0f : kind == BossKind.SHRINE_ORACLE ? 4.0f : 4.5f;
			target.damage(world.getDamageSources().mobAttack(boss), damage);
			if (kind == BossKind.SHRINE_ORACLE) target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 1));
			Vec3d away = target.getPos().subtract(boss.getPos()).normalize();
			target.takeKnockback(0.9, -away.x, -away.z);
		}
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
			world.getServer().getPlayerManager().broadcast(Text.translatable("boss.aptitudes.defeated",
					killer.getDisplayName(), mob.getDisplayName()).formatted(Formatting.GOLD), false);
		}
		LOADED.remove(mob.getUuid());
		ABILITY_READY.remove(mob.getUuid());
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
