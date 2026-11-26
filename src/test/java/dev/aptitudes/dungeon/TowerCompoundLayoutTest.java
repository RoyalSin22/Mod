package dev.aptitudes.dungeon;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TowerCompoundLayoutTest {
	private static String key(int x, int y, int z) { return x + "," + y + "," + z; }
	private static boolean free(TowerCompoundLayout.Cell c) {
		return c == null || c == TowerCompoundLayout.Cell.AIR || c == TowerCompoundLayout.Cell.DOOR
				|| c == TowerCompoundLayout.Cell.LADDER
				|| c == TowerCompoundLayout.Cell.BOSS;
	}
	private static boolean stand(TowerCompoundLayout l, int x, int y, int z) {
		return TowerCompoundLayout.inside(x, z) && y > TowerCompoundLayout.BOTTOM && y < TowerCompoundLayout.TOP
				&& free(l.cell(x, y, z)) && free(l.cell(x, y + 1, z)) && !free(l.cell(x, y - 1, z));
	}
	private static Set<String> walk(TowerCompoundLayout l) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(new int[] {0, 0, 9});
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			if (!seen.add(key(p[0], p[1], p[2]))) continue;
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[2] + d[1];
				if (l.cell(x, p[1], z) == TowerCompoundLayout.Cell.LADDER)
					queue.add(new int[] {x, p[1], z});
				if (l.cell(x, p[1] - 1, z) == TowerCompoundLayout.Cell.LADDER)
					queue.add(new int[] {x, p[1] - 1, z});
				for (int dy = -1; dy <= 1; dy++) {
					int y = p[1] + dy;
					if (stand(l, x, y, z) && (dy <= 0 || free(l.cell(p[0], p[1] + 2, p[2]))))
						queue.add(new int[] {x, y, z});
				}
			}
			if (l.cell(p[0], p[1], p[2]) == TowerCompoundLayout.Cell.LADDER
					|| l.cell(p[0], p[1] - 1, p[2]) == TowerCompoundLayout.Cell.LADDER) {
				for (int dy : new int[] {-1, 1})
					if (p[1] + dy > TowerCompoundLayout.BOTTOM && p[1] + dy < TowerCompoundLayout.TOP)
						queue.add(new int[] {p[0], p[1] + dy, p[2]});
			}
		}
		return seen;
	}
	@Test void everyShapeAndDamageFitsAndKeepsRewardRoute() {
		for (var shape : TowerCompoundLayout.Shape.values()) for (int damage = 0; damage <= 2; damage++) {
			var l = new TowerCompoundLayout(shape, damage, 4921);
			var reach = walk(l);
			assertTrue(reach.contains(key(TowerCompoundLayout.CX + 1, 26, TowerCompoundLayout.CZ)), shape + " / " + damage + " boss");
			assertTrue(reach.contains(key(TowerCompoundLayout.CX - 2, 26, TowerCompoundLayout.CZ - 2))
					|| reach.contains(key(TowerCompoundLayout.CX - 2, 26, TowerCompoundLayout.CZ)), "azotea");
			assertTrue(reach.contains(key(TowerCompoundLayout.CX + 1, -4, TowerCompoundLayout.CZ)), "sótano");
			for (int floor = 0; floor < 5; floor++) {
				int y = floor * 5 + 1;
				assertTrue(reach.contains(key(TowerCompoundLayout.CX - 1, y, TowerCompoundLayout.CZ - 2)),
						shape + " / " + damage + " cofre piso " + floor);
			}
			assertTrue(reach.contains(key(TowerCompoundLayout.CX - 1, 26, TowerCompoundLayout.CZ - 1)),
					shape + " / " + damage + " cofre superior");
			assertTrue(reach.contains(key(TowerCompoundLayout.CX + 3, 18, TowerCompoundLayout.CZ + 2)),
					shape + " / " + damage + " galería");
			assertTrue(reach.contains(key(TowerCompoundLayout.CX - 1, -4, TowerCompoundLayout.CZ + 2)),
					shape + " / " + damage + " pista del cofre oculto");
			assertEquals(TowerCompoundLayout.Cell.CHEST, l.cell(6, 0, -7));
			assertTrue(reach.contains(key(6, 0, -6)), shape + " / " + damage + " caseta");
			assertEquals(TowerCompoundLayout.Cell.HIDDEN_CHEST,
					l.cell(TowerCompoundLayout.CX - 2, -6, TowerCompoundLayout.CZ + 2));
			assertEquals(TowerCompoundLayout.Cell.CLUE,
					l.cell(TowerCompoundLayout.CX - 2, -5, TowerCompoundLayout.CZ + 2));
			assertNull(l.cell(11, 0, 0));
			assertNull(l.cell(0, 28, 0));
			if (shape == TowerCompoundLayout.Shape.SQUARE)
				assertEquals(TowerCompoundLayout.Cell.STONE,
						l.cell(TowerCompoundLayout.CX + 5, 4, TowerCompoundLayout.CZ + 3));
		}
	}
	@Test void sameInputsGiveSameCells() {
		for (var shape : TowerCompoundLayout.Shape.values()) for (int damage = 0; damage <= 2; damage++) {
			var a = new TowerCompoundLayout(shape, damage, 4);
			var b = new TowerCompoundLayout(shape, damage, 4);
			for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++)
				for (int y = -6; y <= 27; y++) assertEquals(a.cell(x, y, z), b.cell(x, y, z));
		}
	}
}
