package dev.aptitudes.world;

import dev.aptitudes.Aptitudes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;

/**
 * Utilidades para construir estructuras bloque a bloque desde un {@code Feature}:
 * coordenadas relativas a un origen, materiales con variación (musgo, grietas),
 * cofres con tabla de botín, generadores de monstruos y comprobación del terreno.
 */
final class StructureBuilder {
	final StructureWorldAccess world;
	final BlockPos origin;
	final Random random;

	StructureBuilder(StructureWorldAccess world, BlockPos origin, Random random) {
		this.world = world;
		this.origin = origin;
		this.random = random;
	}

	static RegistryKey<LootTable> loot(String path) {
		return RegistryKey.of(RegistryKeys.LOOT_TABLE, Aptitudes.id("chests/" + path));
	}

	BlockPos at(int x, int y, int z) {
		return origin.add(x, y, z);
	}

	void set(int x, int y, int z, BlockState state) {
		world.setBlockState(at(x, y, z), state, Block.NOTIFY_LISTENERS);
	}

	void air(int x, int y, int z) {
		set(x, y, z, Blocks.AIR.getDefaultState());
	}

	/** Ladrillo de piedra con variación: 70 % normal, 20 % musgoso, 10 % agrietado. */
	BlockState bricks() {
		float r = random.nextFloat();
		return r < 0.7f ? Blocks.STONE_BRICKS.getDefaultState()
				: r < 0.9f ? Blocks.MOSSY_STONE_BRICKS.getDefaultState() : Blocks.CRACKED_STONE_BRICKS.getDefaultState();
	}

	/** Rellena hacia abajo (hasta 6 bloques) bajo una columna para que no flote. */
	void foundation(int x, int z, BlockState state) {
		for (int y = -1; y >= -6; y--) {
			BlockPos p = at(x, y, z);
			BlockState current = world.getBlockState(p);
			if (!current.isReplaceable() && current.getFluidState().isEmpty()) break;
			world.setBlockState(p, state, Block.NOTIFY_LISTENERS);
		}
	}

	void chest(int x, int y, int z, String lootTable) {
		set(x, y, z, Blocks.CHEST.getDefaultState());
		BlockEntity be = world.getBlockEntity(at(x, y, z));
		if (be instanceof LootableContainerBlockEntity container) container.setLootTable(loot(lootTable), random.nextLong());
	}

	void barrel(int x, int y, int z, String lootTable) {
		set(x, y, z, Blocks.BARREL.getDefaultState());
		BlockEntity be = world.getBlockEntity(at(x, y, z));
		if (be instanceof LootableContainerBlockEntity container) container.setLootTable(loot(lootTable), random.nextLong());
	}

	void spawner(int x, int y, int z, EntityType<?> type) {
		set(x, y, z, Blocks.SPAWNER.getDefaultState());
		BlockEntity be = world.getBlockEntity(at(x, y, z));
		if (be instanceof MobSpawnerBlockEntity spawner) spawner.setEntityType(type, random);
	}

	/** Campeón (élite con barra de jefe) en la posición relativa. */
	void champion(EntityType<? extends MobEntity> type, int x, int y, int z) {
		MobEntity mob = type.create(world.toServerWorld());
		if (mob == null) return;
		BlockPos p = at(x, y, z);
		mob.refreshPositionAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(p), SpawnReason.STRUCTURE, null);
		dev.aptitudes.elite.Elites.makeElite(mob, true, random);
		world.spawnEntityAndPassengers(mob);
	}

	/** Jefe propio del mod generado dentro de una estructura existente. */
	void boss(EntityType<? extends MobEntity> type, int x, int y, int z, dev.aptitudes.boss.BossKind kind) {
		dev.aptitudes.boss.BossManager.spawn(world, type, at(x, y, z), kind, random);
	}

	/** Aparece un monstruo persistente (no desaparece) en la posición relativa. */
	void mob(EntityType<? extends MobEntity> type, int x, int y, int z) {
		MobEntity mob = type.create(world.toServerWorld());
		if (mob == null) return;
		BlockPos p = at(x, y, z);
		mob.refreshPositionAndAngles(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, random.nextFloat() * 360f, 0);
		mob.initialize(world, world.getLocalDifficulty(p), SpawnReason.STRUCTURE, null);
		mob.setPersistent();
		world.spawnEntityAndPassengers(mob);
	}

	/**
	 * ¿El terreno es apto? Seco, y la diferencia de altura entre las esquinas de un
	 * cuadrado de radio {@code radius} no supera {@code maxSlope}.
	 */
	static boolean suitable(StructureWorldAccess world, BlockPos origin, int radius, int maxSlope) {
		if (!world.getBlockState(origin.down()).getFluidState().isEmpty()) return false;
		if (!world.getBlockState(origin).getFluidState().isEmpty()) return false;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int dx = -radius; dx <= radius; dx += radius) {
			for (int dz = -radius; dz <= radius; dz += radius) {
				int h = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX() + dx, origin.getZ() + dz);
				min = Math.min(min, h);
				max = Math.max(max, h);
			}
		}
		return max - min <= maxSlope;
	}
}
