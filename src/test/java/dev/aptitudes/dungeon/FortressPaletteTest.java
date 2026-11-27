package dev.aptitudes.dungeon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FortressPaletteTest {
	@Test void ringAndBiomeChangeVanillaMaterialsDeterministically() {
		var cell = FortressLayout.Cell.STONE;
		String plains = FortressPalette.state(cell, 5, 2, 7, 1, "minecraft:plains");
		assertEquals(plains, FortressPalette.state(cell, 5, 2, 7, 1, "minecraft:plains"));
		assertTrue(plains.startsWith("minecraft:"));
		assertTrue(FortressPalette.state(cell, 5, 2, 7, 3, "minecraft:desert").contains("deepslate"));
		assertTrue(FortressPalette.state(cell, 5, 2, 7, 5, "minecraft:plains").contains("blackstone"));
		assertEquals("minecraft:spruce_planks", FortressPalette.state(FortressLayout.Cell.WOOD, 0, 0, 0, 1, "minecraft:desert"));
	}

	@Test void directionalBlocksAndFunctionalExceptionsAreExplicit() {
		assertEquals("minecraft:deepslate_brick_stairs[facing=east]",
				FortressPalette.state(FortressLayout.Cell.STAIR_EAST, 0, 1, 0, 3, "minecraft:plains"));
		assertEquals("minecraft:red_bed[facing=south,part=head]",
				FortressPalette.state(FortressLayout.Cell.BED_HEAD, 0, 1, 0, 3, "minecraft:plains"));
		assertEquals("aptitudes:three_towers_controller",
				FortressPalette.state(FortressLayout.Cell.CONTROLLER, 0, 0, 0, 3, "minecraft:plains"));
		for (var type : FortressLayout.Cell.values()) {
			String state = FortressPalette.state(type, 0, 0, 0, 3, "minecraft:plains");
			assertTrue(state.startsWith("minecraft:") || type == FortressLayout.Cell.CONTROLLER
					|| type == FortressLayout.Cell.GATE || type == FortressLayout.Cell.REWARD_GATE, type.toString());
		}
	}
}
