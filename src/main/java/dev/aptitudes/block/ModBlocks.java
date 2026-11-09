package dev.aptitudes.block;

import dev.aptitudes.Aptitudes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.TransparentBlock;
import net.minecraft.block.piston.PistonBehavior;
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
	public static final Block CONTRACT_BOARD = Registry.register(Registries.BLOCK, Aptitudes.id("contract_board"),
			new dev.aptitudes.contract.ContractBoardBlock(AbstractBlock.Settings.create().mapColor(MapColor.BROWN)
					.strength(2.0f).sounds(BlockSoundGroup.WOOD)));
	public static final Item CONTRACT_BOARD_ITEM = Registry.register(Registries.ITEM, Aptitudes.id("contract_board"),
			new BlockItem(CONTRACT_BOARD, new Item.Settings()));

	public static final Block ARENA_FOG = Registry.register(Registries.BLOCK, Aptitudes.id("arena_fog"),
			new TransparentBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_PURPLE)
					.strength(-1f, 3_600_000f).dropsNothing().pistonBehavior(PistonBehavior.BLOCK)
					.nonOpaque().suffocates((state, world, pos) -> false)));

	private ModBlocks() {
	}

	public static void init() {
	}
}
