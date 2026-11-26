package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import dev.aptitudes.dungeon.TowerCompoundLayout;
import dev.aptitudes.dungeon.TowerCompoundLayout.Cell;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Torre menor V90: patio roto, sótano, cinco plantas y Centinela en la azotea. */
public class TowerFeature extends Feature<DefaultFeatureConfig> {
	public TowerFeature(Codec<DefaultFeatureConfig> codec) { super(codec); }

	@Override public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
		BlockPos origin = context.getOrigin();
		var world = context.getWorld();
		if (!StructureBuilder.suitable(world, origin, TowerCompoundLayout.R, 3)
				|| origin.getY() + TowerCompoundLayout.TOP >= world.getTopY() - 1
				|| origin.getY() + TowerCompoundLayout.BOTTOM <= world.getBottomY() + 2) return false;
		long seed = context.getRandom().nextLong();
		TowerCompoundLayout layout = new TowerCompoundLayout(context.getRandom().nextBoolean()
				? TowerCompoundLayout.Shape.SQUARE : TowerCompoundLayout.Shape.ROUND,
				context.getRandom().nextInt(3), seed);
		for (int x = -TowerCompoundLayout.R; x <= TowerCompoundLayout.R; x++) {
			for (int z = -TowerCompoundLayout.R; z <= TowerCompoundLayout.R; z++) {
				if (layout.cell(x, -1, z) == null) continue;
				int surface = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, origin.getX() + x, origin.getZ() + z);
				if (Math.abs(surface - origin.getY()) > 3) return false;
				for (int y = TowerCompoundLayout.BOTTOM; y <= 0; y++)
					if (layout.cell(x, y, z) != null && !world.getFluidState(origin.add(x, y, z)).isEmpty()) return false;
			}
		}
		StructureBuilder b = new StructureBuilder(world, origin, context.getRandom());
		int ring = Math.min(5, CustodianTempleFeature.ring(context, origin));
		MinorStructurePalette p = MinorStructurePalette.of(world, origin, ring);
		for (int x = -TowerCompoundLayout.R; x <= TowerCompoundLayout.R; x++) {
			for (int z = -TowerCompoundLayout.R; z <= TowerCompoundLayout.R; z++) {
				if (layout.cell(x, -1, z) != null) b.foundation(x, z, p.stone());
				for (int y = TowerCompoundLayout.BOTTOM; y <= TowerCompoundLayout.TOP; y++) {
					Cell c = layout.cell(x, y, z);
					if (c == null) continue;
					switch (c) {
						case AIR, DOOR -> b.air(x, y, z);
						case WINDOW, BARS -> b.set(x, y, z, Blocks.IRON_BARS.getDefaultState());
						case STONE -> b.set(x, y, z, p.stone());
						case FLOOR -> b.set(x, y, z, y >= 0 && x >= TowerCompoundLayout.CX - 4 && x <= TowerCompoundLayout.CX + 4
								&& z >= TowerCompoundLayout.CZ - 4 && z <= TowerCompoundLayout.CZ + 4 ? p.wood() : p.ground());
						case WOOD -> b.set(x, y, z, p.wood());
						case RAMPART -> b.set(x, y, z, p.accent());
						case RUBBLE -> b.set(x, y, z, y > 2 ? Blocks.AIR.getDefaultState() : p.cracked());
						case STAIR_EAST -> b.set(x, y, z, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.EAST));
						case STAIR_WEST -> b.set(x, y, z, Blocks.STONE_BRICK_STAIRS.getDefaultState().with(StairsBlock.FACING, Direction.WEST));
						case LADDER -> b.set(x, y, z, Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction.WEST));
						case LANTERN -> b.set(x, y, z, p.light());
						case BED -> b.set(x, y, z, Blocks.RED_WOOL.getDefaultState());
						case BOOKSHELF -> b.set(x, y, z, Blocks.BOOKSHELF.getDefaultState());
						case ARMORY -> b.set(x, y, z, Blocks.SMITHING_TABLE.getDefaultState());
						case WORKBENCH -> b.set(x, y, z, Blocks.CRAFTING_TABLE.getDefaultState());
						case CLUE -> b.set(x, y, z, Blocks.BLUE_WOOL.getDefaultState());
						case HIDDEN_CHEST, CHEST -> b.chest(x, y, z, "tower_floor");
						case UPPER_CHEST -> b.chest(x, y, z, "tower_upper");
						case TOP_CHEST -> b.chest(x, y, z, "tower_top");
						case SPAWNER -> b.spawner(x, y, z, y == 11
								? (context.getRandom().nextBoolean() ? EntityType.ZOMBIE : EntityType.SKELETON)
								: (context.getRandom().nextBoolean() ? EntityType.SPIDER : EntityType.SKELETON));
						case BOSS -> { /* Se crea al terminar los bloques. */ }
					}
				}
			}
		}
		int perFloor = ring >= 4 ? 2 : 1;
		for (int floor = 0; floor < 5; floor++) {
			for (int n = 0; n < perFloor; n++) {
				MobEntity elite = b.elite(n == 0 ? EntityType.SKELETON : EntityType.ZOMBIE,
						TowerCompoundLayout.CX + (n == 0 ? -1 : 1), floor * 5 + 1,
						TowerCompoundLayout.CZ + (n == 0 ? 1 : 2));
				armor(elite, ring);
			}
		}
		if (ring >= 2) armor(b.champion(EntityType.ZOMBIE, 3, 0, -4), ring);
		for (int i = 0; i < (ring >= 4 ? 4 : ring >= 2 ? 3 : 2); i++)
			b.mob(EntityType.SKELETON, 4 + i % 2, 0, 2 + i / 2);
		b.boss(EntityType.WITHER_SKELETON, TowerCompoundLayout.CX + 1, 26, TowerCompoundLayout.CZ,
				dev.aptitudes.boss.BossKind.TOWER_SENTINEL);
		StructureIndex.record(world, "tower", origin);
		return true;
	}

	static void armor(MobEntity mob, int ring) {
		if (mob == null) return;
		var helmet = ring >= 4 ? Items.DIAMOND_HELMET : ring >= 2 ? Items.IRON_HELMET : Items.CHAINMAIL_HELMET;
		var chest = ring >= 4 ? Items.DIAMOND_CHESTPLATE : ring >= 2 ? Items.IRON_CHESTPLATE : Items.CHAINMAIL_CHESTPLATE;
		mob.equipStack(EquipmentSlot.HEAD, new ItemStack(helmet));
		mob.equipStack(EquipmentSlot.CHEST, new ItemStack(chest));
		mob.setEquipmentDropChance(EquipmentSlot.HEAD, 0);
		mob.setEquipmentDropChance(EquipmentSlot.CHEST, 0);
	}
}
