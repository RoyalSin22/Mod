package dev.aptitudes.block;

import dev.aptitudes.Aptitudes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

public final class ModBlocks {
	public static final Block BONFIRE = Registry.register(Registries.BLOCK, Aptitudes.id("bonfire"),
			new BonfireBlock(AbstractBlock.Settings.create()
					.mapColor(MapColor.STONE_GRAY)
					.strength(2.0f, 6.0f)
					.sounds(BlockSoundGroup.STONE)
					.luminance(state -> 15)
					.nonOpaque()));

	public static final Item BONFIRE_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("bonfire"),
			new BlockItem(BONFIRE, new Item.Settings().rarity(Rarity.UNCOMMON)));

	private ModBlocks() {
	}

	public static void init() {
	}
}
