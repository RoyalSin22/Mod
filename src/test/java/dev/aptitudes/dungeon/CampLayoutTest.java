package dev.aptitudes.dungeon;

import org.junit.jupiter.api.Test;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class CampLayoutTest {
	private static String key(int x, int y, int z) { return x + "," + y + "," + z; }
	private static boolean free(CampLayout.Cell c) { return c == null || c == CampLayout.Cell.AIR || c == CampLayout.Cell.LADDER; }
	private static boolean stand(CampLayout l, int x, int y, int z) {
		return CampLayout.inside(x, z) && y > CampLayout.BOTTOM && y < CampLayout.TOP
				&& free(l.cell(x, y, z)) && free(l.cell(x, y + 1, z)) && !free(l.cell(x, y - 1, z));
	}
	private static Set<String> walk(CampLayout l) {
		Set<String> seen = new HashSet<>();
		ArrayDeque<int[]> queue = new ArrayDeque<>();
		queue.add(l.entrance());
		while (!queue.isEmpty()) {
			int[] p = queue.removeFirst();
			if (!seen.add(key(p[0], p[1], p[2]))) continue;
			for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
				int x = p[0] + d[0], z = p[2] + d[1];
				if (l.cell(x, p[1], z) == CampLayout.Cell.LADDER) queue.add(new int[] {x, p[1], z});
				for (int dy = -1; dy <= 1; dy++) {
					int y = p[1] + dy;
					if (stand(l, x, y, z) && (dy <= 0 || free(l.cell(p[0], p[1] + 2, p[2]))))
						queue.add(new int[] {x, y, z});
				}
			}
			if (l.cell(p[0], p[1], p[2]) == CampLayout.Cell.LADDER)
				for (int dy : new int[] {-1, 1})
					if (p[1] + dy > CampLayout.BOTTOM && p[1] + dy < CampLayout.TOP)
						queue.add(new int[] {p[0], p[1] + dy, p[2]});
		}
		return seen;
	}
	@Test void allTwentyEightCombinationsHaveReachableCaptainAndLoot() {
		for (var shape : CampLayout.Shape.values()) for (var kind : CampLayout.Kind.values()) {
			var l = new CampLayout(shape, kind, 75);
			var reach = walk(l);
			int[] boss = l.captain(), chest = l.visibleChest(), hidden = l.hiddenChest();
			assertTrue(reach.contains(key(boss[0], boss[1], boss[2])), shape + " " + kind + " captain");
			assertTrue(reach.stream().anyMatch(k -> k.equals(key(chest[0] + 1, chest[1], chest[2]))
					|| k.equals(key(chest[0] - 1, chest[1], chest[2]))), shape + " " + kind + " chest");
			assertEquals(CampLayout.Cell.HIDDEN_CHEST, l.cell(hidden[0], hidden[1], hidden[2]));
			assertEquals(CampLayout.Cell.CLUE, l.cell(hidden[0], hidden[1] + 1, hidden[2]));
			assertTrue(reach.stream().anyMatch(k -> k.equals(key(hidden[0] + 1, hidden[1] + 1, hidden[2]))
					|| k.equals(key(hidden[0] - 1, hidden[1] + 1, hidden[2]))), shape + " " + kind + " hidden");
			assertNull(l.cell(15, 0, 0));
			assertNull(l.cell(0, 10, 0));
			if (shape == CampLayout.Shape.PALISADE)
				assertTrue(reach.contains(key(11, 7, -11)), kind + " vigía");
		}
	}
	@Test void sameSeedMakesSamePlan() {
		for (var shape : CampLayout.Shape.values()) for (var kind : CampLayout.Kind.values()) {
			var a = new CampLayout(shape, kind, 82);
			var b = new CampLayout(shape, kind, 82);
			for (int x = -14; x <= 14; x++) for (int z = -14; z <= 14; z++)
				for (int y = -2; y <= 9; y++) assertEquals(a.cell(x, y, z), b.cell(x, y, z));
		}
	}
	@Test void sevenTypesCarryDistinctObjectsInEveryShape() {
		for (var shape : CampLayout.Shape.values()) {
			Set<CampLayout.Cell> markers = new HashSet<>();
			for (var kind : CampLayout.Kind.values())
				markers.add(new CampLayout(shape, kind, 1).cell(-4, 0, 9));
			assertEquals(7, markers.size(), shape.toString());
		}
	}
}
