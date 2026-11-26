package dev.aptitudes.dungeon;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.achievement.Achievement;
import dev.aptitudes.achievement.Achievements;
import dev.aptitudes.boss.BossEntities;
import dev.aptitudes.dungeon.TowersFlowRules.Boss;
import dev.aptitudes.dungeon.TowersLayout.Role;
import dev.aptitudes.dungeon.TowersLayout.Tower;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.temple.Temples;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.entity.attribute.EntityAttributeInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * V86 · fase 3: el controlador de Las Tres Torres Señoriales, en el centro del patio (la posición del bloque es el origen de
 * {@link TowersLayout}). Despierta a las criaturas de cada planta, a los dos Guardianes y al Coloso cuando un jugador entra en su
 * sala, abre la puerta del Señor al vencer a los dos Guardianes y, al caer el Coloso, coloca los cofres de la recompensa y concede
 * el logro. Todo el estado es persistente y <b>nunca retrocede</b>: una sola vez por estructura. Las reglas viven en
 * {@link TowersFlowRules} (puras y con pruebas). Los textos de los avisos son provisionales (los reescribe Codex).
 */
public final class TowersControllerBlockEntity extends BlockEntity {
	private static final int CHECK_EVERY = 10;
	private static final double RANGE = 48.0;
	private static final Identifier BOSS_HEALTH = Aptitudes.id("towers_boss_health");
	private static final Identifier MINION_HEALTH = Aptitudes.id("towers_minion_health");
	private static final RegistryKey<net.minecraft.loot.LootTable> REWARD = RegistryKey.of(RegistryKeys.LOOT_TABLE, Aptitudes.id("chests/three_towers_reward"));

	private int ring = TowersFlowRules.MIN_RING;
	private Boss west = Boss.ASLEEP, east = Boss.ASLEEP, lord = Boss.ASLEEP;
	private boolean doorOpen;
	private int waves;
	private UUID westId, eastId, lordId;
	private int missingWest, missingEast, missingLord;

	public TowersControllerBlockEntity(BlockPos pos, BlockState state) {
		super(TowersBlocks.CONTROLLER_ENTITY, pos, state);
	}

	/** La llama la generación: fija la dificultad según el anillo de Amenaza del lugar. */
	public void setup(int placeRing) {
		this.ring = TowersFlowRules.effectiveRing(placeRing);
		markDirty();
	}

	public static void tick(World world, BlockPos pos, BlockState state, TowersControllerBlockEntity be) {
		if (world instanceof ServerWorld server && server.getTime() % CHECK_EVERY == 0) be.serverTick(server);
	}

	// ------------------------------------------------------------------------------------------ Bucle

	private List<ServerPlayerEntity> nearby(ServerWorld world, double range) {
		List<ServerPlayerEntity> out = new ArrayList<>();
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (!p.isSpectator() && p.squaredDistanceTo(pos.toCenterPos()) <= range * range) out.add(p);
		}
		return out;
	}

	private void serverTick(ServerWorld world) {
		if (lord == Boss.DEFEATED) return;
		// En pacífico no aparece nada: no se gasta el despertar ni las oleadas.
		if (world.getDifficulty() == Difficulty.PEACEFUL) return;
		List<ServerPlayerEntity> players = nearby(world, RANGE);
		if (players.isEmpty()) return;

		boolean westIn = false, eastIn = false, lordIn = false;
		for (ServerPlayerEntity p : players) {
			int dx = p.getBlockX() - pos.getX(), dy = p.getBlockY() - pos.getY(), dz = p.getBlockZ() - pos.getZ();
			for (Tower t : TowersLayout.TOWERS) {
				int level = TowersFlowRules.combatLevel(t, dx, dy, dz);
				if (level >= 0 && !TowersFlowRules.waveDone(waves, t.role(), level)) spawnWave(world, t, level);
				if (TowersFlowRules.inBossRoom(t, dx, dy, dz)) {
					switch (t.role()) {
						case VASSAL_WEST -> westIn = true;
						case VASSAL_EAST -> eastIn = true;
						case LORD -> lordIn = true;
					}
				}
			}
		}
		if (TowersFlowRules.shouldWake(west, westIn)) wakeGuardian(world, Role.VASSAL_WEST);
		if (TowersFlowRules.shouldWake(east, eastIn)) wakeGuardian(world, Role.VASSAL_EAST);
		if (TowersFlowRules.shouldWakeColossus(doorOpen, lord, lordIn)) wakeColossus(world);
		// Un jefe despierto que desaparece sin morir (descarga o un fallo) vuelve si hay alguien en su sala.
		if (west == Boss.AWAKE && westIn) westId = revive(world, westId, Role.VASSAL_WEST, 0);
		if (east == Boss.AWAKE && eastIn) eastId = revive(world, eastId, Role.VASSAL_EAST, 1);
		if (lord == Boss.AWAKE && lordIn) lordId = revive(world, lordId, Role.LORD, 2);
	}

	// ------------------------------------------------------------------------------------------ Despertar

	private void wakeGuardian(ServerWorld world, Role role) {
		UUID id = spawnBoss(world, BossEntities.DISMANTLED_GUARDIAN, role == Role.VASSAL_WEST ? TowersLayout.build0().guardianWest : TowersLayout.build0().guardianEast, role);
		if (id == null) return;
		if (role == Role.VASSAL_WEST) {
			west = TowersFlowRules.advance(west, Boss.AWAKE);
			westId = id;
		} else {
			east = TowersFlowRules.advance(east, Boss.AWAKE);
			eastId = id;
		}
		tell(nearby(world, RANGE), "towers.aptitudes.guardian_woke", Formatting.DARK_RED);
		markDirty();
	}

	private void wakeColossus(ServerWorld world) {
		UUID id = spawnBoss(world, BossEntities.FACELESS_COLOSSUS, TowersLayout.build0().colossus, Role.LORD);
		if (id == null) return;
		lord = TowersFlowRules.advance(lord, Boss.AWAKE);
		lordId = id;
		world.playSound(null, pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 1.0f, 0.6f);
		tell(nearby(world, RANGE), "towers.aptitudes.colossus_woke", Formatting.DARK_RED);
		markDirty();
	}

	/** Vuelve a hacer aparecer a un jefe despierto que ya no está, tras {@link TowersFlowRules#MISSING_CHECKS} comprobaciones. */
	private UUID revive(ServerWorld world, UUID id, Role role, int slot) {
		if (id != null && world.getEntity(id) != null) {
			setMissing(slot, 0);
			return id;
		}
		if (getMissing(slot) + 1 < TowersFlowRules.MISSING_CHECKS) {
			setMissing(slot, getMissing(slot) + 1);
			return id;
		}
		setMissing(slot, 0);
		var layout = TowersLayout.build0();
		UUID fresh = switch (role) {
			case VASSAL_WEST -> spawnBoss(world, BossEntities.DISMANTLED_GUARDIAN, layout.guardianWest, role);
			case VASSAL_EAST -> spawnBoss(world, BossEntities.DISMANTLED_GUARDIAN, layout.guardianEast, role);
			case LORD -> spawnBoss(world, BossEntities.FACELESS_COLOSSUS, layout.colossus, role);
		};
		if (fresh != null) markDirty();
		return fresh != null ? fresh : id;
	}

	private int getMissing(int slot) {
		return slot == 0 ? missingWest : slot == 1 ? missingEast : missingLord;
	}

	private void setMissing(int slot, int value) {
		if (slot == 0) missingWest = value; else if (slot == 1) missingEast = value; else missingLord = value;
	}

	private UUID spawnBoss(ServerWorld world, EntityType<? extends MobEntity> type, int[] rel, Role role) {
		MobEntity mob = type.create(world);
		if (mob == null) return null;
		BlockPos at = pos.add(rel[0], rel[1], rel[2]);
		mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
		EntityAttributeInstance health = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (health != null) {
			AttributeApplier.set(health, BOSS_HEALTH, TowersFlowRules.bossHealthBonus(ring), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			mob.setHealth(mob.getMaxHealth());
		}
		mob.addCommandTag(TowersBlocks.CONTROLLER_TAG + pos.asLong());
		mob.addCommandTag(TowersBlocks.ROLE_TAG + role.name());
		mob.setPersistent();
		world.spawnEntityAndPassengers(mob);
		return mob.getUuid();
	}

	// ------------------------------------------------------------------------------------------ Oleadas

	private void spawnWave(ServerWorld world, Tower t, int level) {
		waves = TowersFlowRules.withWave(waves, t.role(), level);
		int perSpawn = TowersFlowRules.mobsPerSpawn(level, ring);
		int index = 0;
		for (var s : TowersLayout.build0().spawns) {
			if (s.role() != t.role() || s.level() != level) continue;
			for (int i = 0; i < perSpawn; i++) spawnMinion(world, TowersFlowRules.mobKind(level, index + i), pos.add(s.x(), s.y(), s.z()));
			index++;
		}
		markDirty();
	}

	private void spawnMinion(ServerWorld world, TowersFlowRules.MobKind kind, BlockPos at) {
		EntityType<? extends MobEntity> type = switch (kind) {
			case ZOMBIE -> EntityType.ZOMBIE;
			case SKELETON -> EntityType.SKELETON;
			case HUSK -> EntityType.HUSK;
			case STRAY -> EntityType.STRAY;
		};
		MobEntity mob = type.create(world);
		if (mob == null) return;
		mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.STRUCTURE, null);
		Elites.makeElite(mob, false, world.random);
		equip(mob);
		EntityAttributeInstance health = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (health != null) {
			AttributeApplier.set(health, MINION_HEALTH, TowersFlowRules.minionHealthBonus(ring), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			mob.setHealth(mob.getMaxHealth());
		}
		mob.setPersistent();
		world.spawnEntityAndPassengers(mob);
	}

	/** Armadura completa y visible por anillo (hierro, diamante, netherita); no se suelta al morir. */
	private void equip(MobEntity mob) {
		ItemStack[] pieces = ring >= 5
				? new ItemStack[] {new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE), new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS)}
				: ring == 4
				? new ItemStack[] {new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS)}
				: new ItemStack[] {new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE), new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		for (int i = 0; i < slots.length; i++) {
			mob.equipStack(slots[i], pieces[i]);
			mob.setEquipmentDropChance(slots[i], 0f);
		}
	}

	// ------------------------------------------------------------------------------------------ Derrotas

	/** Cae un jefe de la estructura: se anota, se abre la puerta del Señor si ya han caído los dos Guardianes y se premia al vencer al Coloso. */
	void bossDefeated(ServerWorld world, Role role) {
		switch (role) {
			case VASSAL_WEST -> {
				west = TowersFlowRules.advance(west, Boss.DEFEATED);
				westId = null;
			}
			case VASSAL_EAST -> {
				east = TowersFlowRules.advance(east, Boss.DEFEATED);
				eastId = null;
			}
			case LORD -> {
				lord = TowersFlowRules.advance(lord, Boss.DEFEATED);
				lordId = null;
			}
		}
		if (role == Role.LORD) {
			finish(world);
		} else if (TowersFlowRules.lordDoorOpen(west, east) && !doorOpen) {
			openLordDoor(world);
		} else {
			int left = (west == Boss.DEFEATED ? 0 : 1) + (east == Boss.DEFEATED ? 0 : 1);
			for (ServerPlayerEntity p : nearby(world, RANGE)) {
				p.sendMessage(Text.translatable("towers.aptitudes.guardian_down", left).formatted(Formatting.GOLD), true);
			}
		}
		markDirty();
	}

	private void openLordDoor(ServerWorld world) {
		doorOpen = true;
		for (int[] c : TowersLayout.build0().sealed) {
			BlockPos p = pos.add(c[0], c[1], c[2]);
			if (world.getBlockState(p).isOf(Temples.GATE)) world.breakBlock(p, false);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1.4f, 0.6f);
		tell(nearby(world, RANGE), "towers.aptitudes.lord_open", Formatting.GOLD);
	}

	/** Cae el Coloso: un cofre de recompensa por jugador cercano (hasta cinco) y el logro. */
	private void finish(ServerWorld world) {
		List<ServerPlayerEntity> players = nearby(world, 64.0);
		var layout = TowersLayout.build0();
		int chests = Math.max(1, Math.min(TowersFlowRules.MAX_REWARD_CHESTS, players.size()));
		int[] offsets = {0, 1, -1, 2, -2};
		for (int i = 0; i < chests; i++) {
			BlockPos at = pos.add(layout.rewardChest[0] + offsets[i], layout.rewardChest[1], layout.rewardChest[2]);
			world.setBlockState(at, Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH));
			if (world.getBlockEntity(at) instanceof LootableContainerBlockEntity container) container.setLootTable(REWARD, world.random.nextLong());
		}
		for (ServerPlayerEntity p : players) Achievements.grant(p, Achievement.THREE_TOWERS);
		world.playSound(null, pos, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.PLAYERS, 1.0f, 1.0f);
		tell(players, "towers.aptitudes.colossus_down", Formatting.GOLD);
	}

	private static void tell(List<ServerPlayerEntity> players, String key, Formatting color) {
		for (ServerPlayerEntity p : players) p.sendMessage(Text.translatable(key).formatted(color, Formatting.BOLD), true);
	}

	// ------------------------------------------------------------------------------------------ Guardado

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putInt("ring", ring);
		nbt.putInt("west", west.ordinal());
		nbt.putInt("east", east.ordinal());
		nbt.putInt("lord", lord.ordinal());
		nbt.putBoolean("door_open", doorOpen);
		nbt.putInt("waves", waves);
		if (westId != null) nbt.putUuid("west_id", westId);
		if (eastId != null) nbt.putUuid("east_id", eastId);
		if (lordId != null) nbt.putUuid("lord_id", lordId);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		ring = nbt.contains("ring") ? Math.max(TowersFlowRules.MIN_RING, Math.min(TowersFlowRules.MAX_RING, nbt.getInt("ring"))) : TowersFlowRules.MIN_RING;
		west = boss(nbt.getInt("west"));
		east = boss(nbt.getInt("east"));
		lord = boss(nbt.getInt("lord"));
		doorOpen = nbt.getBoolean("door_open");
		waves = nbt.getInt("waves");
		westId = nbt.containsUuid("west_id") ? nbt.getUuid("west_id") : null;
		eastId = nbt.containsUuid("east_id") ? nbt.getUuid("east_id") : null;
		lordId = nbt.containsUuid("lord_id") ? nbt.getUuid("lord_id") : null;
	}

	/** Estado guardado → enumeración; un valor corrupto o futuro se trata como dormido. */
	private static Boss boss(int ordinal) {
		Boss[] all = Boss.values();
		return ordinal >= 0 && ordinal < all.length ? all[ordinal] : Boss.ASLEEP;
	}
}
