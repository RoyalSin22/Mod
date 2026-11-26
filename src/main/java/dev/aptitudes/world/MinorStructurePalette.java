package dev.aptitudes.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.BiomeKeys;

/** Materiales compartidos de torre y campamento, por anillo de Amenaza y bioma. */
record MinorStructurePalette(BlockState stone, BlockState cracked, BlockState accent, BlockState wood,
		BlockState ground, BlockState light) {
	static MinorStructurePalette of(StructureWorldAccess world, BlockPos origin, int ring) {
		var biome = world.getBiome(origin);
		boolean desert = biome.matchesKey(BiomeKeys.DESERT);
		boolean badlands = biome.matchesKey(BiomeKeys.BADLANDS) || biome.matchesKey(BiomeKeys.ERODED_BADLANDS)
				|| biome.matchesKey(BiomeKeys.WOODED_BADLANDS);
		BlockState stone = ring >= 4 ? Blocks.POLISHED_BLACKSTONE_BRICKS.getDefaultState()
				: ring >= 2 ? Blocks.DEEPSLATE_BRICKS.getDefaultState()
				: badlands ? Blocks.RED_SANDSTONE.getDefaultState()
				: desert ? Blocks.SANDSTONE.getDefaultState() : Blocks.STONE_BRICKS.getDefaultState();
		BlockState cracked = ring >= 4 ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.getDefaultState()
				: ring >= 2 ? Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
				: badlands ? Blocks.RED_SANDSTONE.getDefaultState()
				: desert ? Blocks.CUT_SANDSTONE.getDefaultState() : Blocks.MOSSY_STONE_BRICKS.getDefaultState();
		BlockState accent = ring >= 4 ? Blocks.GILDED_BLACKSTONE.getDefaultState()
				: ring >= 2 ? Blocks.DEEPSLATE_TILES.getDefaultState()
				: badlands ? Blocks.TERRACOTTA.getDefaultState()
				: desert ? Blocks.CHISELED_SANDSTONE.getDefaultState() : Blocks.CHISELED_STONE_BRICKS.getDefaultState();
		BlockState wood = badlands || desert ? Blocks.ACACIA_PLANKS.getDefaultState()
				: biome.isIn(BiomeTags.IS_JUNGLE) ? Blocks.JUNGLE_PLANKS.getDefaultState()
				: biome.isIn(BiomeTags.IS_TAIGA) ? Blocks.SPRUCE_PLANKS.getDefaultState()
				: biome.matchesKey(BiomeKeys.SWAMP) || biome.matchesKey(BiomeKeys.MANGROVE_SWAMP)
				? Blocks.DARK_OAK_PLANKS.getDefaultState() : Blocks.OAK_PLANKS.getDefaultState();
		BlockState ground = badlands ? Blocks.RED_SANDSTONE.getDefaultState()
				: desert ? Blocks.SANDSTONE.getDefaultState()
				: ring >= 2 ? Blocks.GRAVEL.getDefaultState() : Blocks.COARSE_DIRT.getDefaultState();
		BlockState light = ring >= 4 ? Blocks.SOUL_LANTERN.getDefaultState() : Blocks.LANTERN.getDefaultState();
		return new MinorStructurePalette(stone, cracked, accent, wood, ground, light);
	}
}
