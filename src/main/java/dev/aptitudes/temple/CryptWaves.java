package dev.aptitudes.temple;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.elite.Elites;
import dev.aptitudes.logic.AttributeApplier;
import dev.aptitudes.mob.ErranteEntity;
import dev.aptitudes.mob.NewMobs;
import dev.aptitudes.mob.OsarioEntities;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

/**
 * V69: criaturas de las oleadas de la Cripta de la Niebla y su cofre final. Todo lo que decide números vive en
 * {@link CryptRules}; aquí solo se crean las entidades y los bloques (en el hilo del servidor, nunca durante la
 * generación del mundo).
 */
final class CryptWaves {
	/** Etiqueta de las criaturas de oleada: el controlador las cuenta y las retira al reiniciar la oleada. */
	static final String TAG = Aptitudes.MOD_ID + ".crypt_mob";
	private static final Identifier CHAMPION_HEALTH = Aptitudes.id("crypt_champion_health");
	/** Tabla de botín del cofre final. */
	static final RegistryKey<LootTable> CHEST_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, Aptitudes.id("chests/custodian_crypt"));

	private CryptWaves() {
	}

	private static MobEntity create(ServerWorld world, CryptRules.Variant variant) {
		return switch (variant) {
			case ERRANTE -> NewMobs.ERRANTE.create(world);
			case OSARIO -> OsarioEntities.OSARIO.create(world);
			case VANILLA -> (world.random.nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON).create(world);
		};
	}

	/** Una criatura de oleada en {@code spot}, o {@code null} si no cabe. Sin botín ni XP del Velo. */
	@Nullable
	static MobEntity spawn(ServerWorld world, BlockPos spot, CryptRules.Variant variant, @Nullable ServerPlayerEntity target) {
		MobEntity mob = create(world, variant);
		if (mob == null) return null;
		if (!place(world, mob, spot)) return null;
		mob.initialize(world, world.getLocalDifficulty(spot), SpawnReason.EVENT, null);
		finish(world, mob, target);
		if (mob instanceof ErranteEntity errante) errante.dropNoExperience();
		else if (variant == CryptRules.Variant.VANILLA) noLoot(mob);
		return mob;
	}

	/** Campeón de la Cripta: un Osario Quebrado hecho campeón, con +25 % de vida por anillo. */
	@Nullable
	static MobEntity spawnChampion(ServerWorld world, BlockPos spot, int ring, @Nullable ServerPlayerEntity target) {
		MobEntity mob = OsarioEntities.OSARIO.create(world);
		if (mob == null || !place(world, mob, spot)) return null;
		mob.initialize(world, world.getLocalDifficulty(spot), SpawnReason.EVENT, null);
		Elites.makeElite(mob, true, world.random);
		for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0f);
		var health = mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (health != null) {
			AttributeApplier.set(health, CHAMPION_HEALTH, CryptRules.climaxHealthBonus(ring),
					EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
			mob.setHealth(mob.getMaxHealth());
		}
		mob.setCustomName(Text.translatable("temple.aptitudes.crypt.champion").formatted(Formatting.DARK_PURPLE));
		finish(world, mob, target);
		Elites.setChampionBarName(mob, mob.getDisplayName());
		return mob;
	}

	private static boolean place(ServerWorld world, MobEntity mob, BlockPos spot) {
		mob.refreshPositionAndAngles(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, world.random.nextFloat() * 360f, 0);
		return world.isSpaceEmpty(mob, mob.getBoundingBox());
	}

	private static void finish(ServerWorld world, MobEntity mob, @Nullable ServerPlayerEntity target) {
		mob.addCommandTag(TAG);
		mob.addCommandTag(Elites.CHECKED);
		mob.setPersistent();
		if (target != null) mob.setTarget(target);
		world.spawnEntityAndPassengers(mob);
		world.spawnParticles(ParticleTypes.SOUL, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 10, 0.3, 0.6, 0.3, 0.03);
	}

	/** Vacía la tabla de botín de una criatura vanilla (clave NBT {@code DeathLootTable}); no toca su equipo. */
	private static void noLoot(MobEntity mob) {
		NbtCompound nbt = new NbtCompound();
		mob.writeNbt(nbt);
		nbt.putString("DeathLootTable", "minecraft:empty");
		mob.readNbt(nbt);
		for (EquipmentSlot slot : EquipmentSlot.values()) mob.setEquipmentDropChance(slot, 0f);
	}

	/** Retira todas las criaturas de oleada de la sala (al reiniciar una oleada o al terminar la cripta). */
	static void discardAll(ServerWorld world, Box room) {
		for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, room, e -> e.getCommandTags().contains(TAG))) {
			world.spawnParticles(ParticleTypes.SOUL, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 8, 0.3, 0.5, 0.3, 0.03);
			mob.discard();
		}
	}

	/** Hace emerger el cofre final con su tabla de botín. */
	static void placeChest(ServerWorld world, BlockPos pos) {
		world.setBlockState(pos, Blocks.CHEST.getDefaultState());
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof LootableContainerBlockEntity container) container.setLootTable(CHEST_LOOT, world.random.nextLong());
	}
}
