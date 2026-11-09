package dev.aptitudes.world;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossArenaLayoutTest {
	@Test
	void containsTheThreeGeneratedSpawnPositions() {
		assertTrue(BossArenaLayout.TOWER.contains(1.5, 0, 1.5));
		assertTrue(BossArenaLayout.SHRINE.contains(0.5, 1, 2.5));
		assertTrue(BossArenaLayout.CAMP.contains(1.5, 0, 2.5));
	}

	@Test
	void boundsTheFloorAndNeverIncludesTheOutsideOrTheFloorBelow() {
		for (BossArenaLayout layout : BossArenaLayout.values()) {
			assertFalse(layout.contains(0.5, -1, 0.5));
			assertFalse(layout.contains(0.5, 3, 0.5));
			assertFalse(layout.contains(100, 0, 0.5));
			assertFalse(layout.contains(Double.NaN, 0, 0.5));
			assertEquals(layout.wall().size(), new HashSet<>(layout.wall()).size());
			assertTrue(layout.wall().size() < 160);
		}
	}

	@Test
	void campIncludesPlayersAndBossesStandingOnDirtPaths() {
		assertTrue(BossArenaLayout.CAMP.contains(1.5, -1.0 / 16, 2.5));
		assertFalse(BossArenaLayout.CAMP.contains(1.5, -0.1, 2.5));
		assertFalse(BossArenaLayout.TOWER.contains(1.5, -1.0 / 16, 1.5));
	}

	@Test
	void everyHorizontalExitHasThreeLayersOfWall() {
		int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (BossArenaLayout layout : BossArenaLayout.values()) {
			var wall = new HashSet<>(layout.wall());
			for (int x = -5; x <= 5; x++) {
				for (int z = -5; z <= 5; z++) {
					if (!layout.contains(x + 0.5, 0, z + 0.5)) continue;
					for (int[] direction : directions) {
						int nx = x + direction[0];
						int nz = z + direction[1];
						if (layout.contains(nx + 0.5, 0, nz + 0.5)) continue;
						for (int y = 0; y < 3; y++) assertTrue(wall.contains(new BossArenaLayout.Cell(nx, y, nz)));
					}
				}
			}
		}
	}

	@Test
	void towerClosesTheLadderExitWithoutTouchingTheFloorBelow() {
		var roof = BossArenaLayout.TOWER.wall();
		assertFalse(roof.contains(new BossArenaLayout.Cell(2, -1, 2)));
		for (int y = 0; y < 3; y++) assertTrue(roof.contains(new BossArenaLayout.Cell(2, y, 2)));
	}
}
