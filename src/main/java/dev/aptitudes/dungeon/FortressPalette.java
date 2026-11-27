package dev.aptitudes.dungeon;

import dev.aptitudes.dungeon.FortressLayout.Cell;

/**
 * Paleta pura de la fortaleza. El identificador incluye las propiedades de estado que deben
 * compartir el Feature y las vistas previas. Controlador y rejas son las dos excepciones
 * funcionales a los bloques vanilla del diseño V88.
 */
public final class FortressPalette {
	private FortressPalette() {}

	public static String state(Cell cell, int x, int y, int z, int ring, String biome) {
		if (cell == null) return "minecraft:air";
		int tier = Math.max(0, Math.min(5, ring));
		boolean dry = biome != null && (biome.contains("desert") || biome.contains("badlands") || biome.contains("savanna"));
		boolean cold = biome != null && (biome.contains("snow") || biome.contains("frozen") || biome.contains("taiga"));
		int weathering = Math.floorMod(x * 73 + y * 19 + z * 37, 16);
		return switch (cell) {
			case AIR -> "minecraft:air";
			case STONE -> masonry(tier, dry, weathering);
			case FLOOR -> tier >= 4 ? "minecraft:polished_blackstone_bricks"
					: tier >= 2 ? "minecraft:deepslate_bricks" : "minecraft:stone_bricks";
			case WOOD -> dry ? "minecraft:spruce_planks" : "minecraft:dark_oak_planks";
			case ROOF -> tier >= 4 ? "minecraft:polished_blackstone_bricks" : "minecraft:deepslate_tiles";
			case RUBBLE -> tier >= 4 ? "minecraft:blackstone" : tier >= 2 ? "minecraft:cobbled_deepslate" : "minecraft:cobblestone";
			case MERLON -> tier >= 4 ? "minecraft:polished_blackstone_bricks" : "minecraft:deepslate_bricks";
			case WINDOW -> cold ? "minecraft:light_blue_stained_glass_pane" : "minecraft:blue_stained_glass_pane";
			case SOUL_LIGHT -> "minecraft:soul_lantern";
			case STAIR_EAST -> stairs(tier) + "[facing=east]";
			case STAIR_WEST -> stairs(tier) + "[facing=west]";
			case GATE, REWARD_GATE -> "aptitudes:vigil_gate";
			case CONTROLLER -> "aptitudes:three_towers_controller";
			case BOOKSHELF -> "minecraft:bookshelf";
			case BED_FOOT -> "minecraft:red_bed[facing=south,part=foot]";
			case BED_HEAD -> "minecraft:red_bed[facing=south,part=head]";
			case BREWING -> "minecraft:brewing_stand";
			case ENCHANTING -> "minecraft:enchanting_table";
			case ALTAR -> tier >= 4 ? "minecraft:chiseled_polished_blackstone" : "minecraft:chiseled_deepslate";
			case HIDDEN_CHEST, SUPPLY_CHEST -> "minecraft:chest";
		};
	}

	private static String masonry(int tier, boolean dry, int weathering) {
		if (tier >= 4) return weathering < 3 ? "minecraft:cracked_polished_blackstone_bricks" : "minecraft:polished_blackstone_bricks";
		if (tier >= 2) return weathering < 3 ? "minecraft:cracked_deepslate_bricks" : "minecraft:deepslate_bricks";
		if (weathering < 3) return "minecraft:cracked_stone_bricks";
		return !dry && weathering < 7 ? "minecraft:mossy_stone_bricks" : "minecraft:stone_bricks";
	}

	private static String stairs(int tier) {
		return tier >= 4 ? "minecraft:polished_blackstone_brick_stairs"
				: tier >= 2 ? "minecraft:deepslate_brick_stairs" : "minecraft:stone_brick_stairs";
	}
}
