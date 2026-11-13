package dev.aptitudes.logic;

import dev.aptitudes.logic.TreeScreenLayout.Rect;
import dev.aptitudes.logic.UnifiedTreeLayout.Block;
import dev.aptitudes.logic.UnifiedTreeLayout.Section;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnifiedTreeLayoutTest {
	private static final Rect CANVAS = new Rect(0, 0, UnifiedTreeLayout.WIDTH, 100_000);

	@Test void sectionsAreStackedInOrderWithoutOverlapping() {
		List<Block> blocks = UnifiedTreeLayout.blocks(3, 3);
		assertEquals(Section.values().length, blocks.size());
		for (int i = 1; i < blocks.size(); i++) {
			assertTrue(blocks.get(i).section().ordinal() > blocks.get(i - 1).section().ordinal(), "orden de secciones");
			assertTrue(blocks.get(i).y() >= blocks.get(i - 1).bottom() + UnifiedTreeLayout.GAP, "sin solaparse");
		}
		assertEquals(blocks.getLast().bottom(), UnifiedTreeLayout.contentHeight(blocks));
	}

	@Test void emptyBridgeSectionsTakeNoSpace() {
		List<Block> blocks = UnifiedTreeLayout.blocks(0, 0);
		assertNull(UnifiedTreeLayout.block(blocks, Section.VETERAN));
		assertNull(UnifiedTreeLayout.block(blocks, Section.ASCENT));
		assertNotNull(UnifiedTreeLayout.block(blocks, Section.MASTER));
		assertTrue(UnifiedTreeLayout.contentHeight(blocks) < UnifiedTreeLayout.contentHeight(UnifiedTreeLayout.blocks(1, 1)));
	}

	@Test void pathNodesFitTheCanvasAndNeverOverlap() {
		Block paths = UnifiedTreeLayout.block(UnifiedTreeLayout.blocks(0, 0), Section.PATHS);
		List<Rect> all = new ArrayList<>();
		for (int p = 0; p < 3; p++) {
			assertTrue(UnifiedTreeLayout.pathHeader(paths, p).inside(CANVAS));
			for (int row = 0; row < UnifiedTreeLayout.PATH_ROWS; row++) {
				for (int slot = 0; slot < (row == UnifiedTreeLayout.BIFURCATION_ROW ? 2 : 1); slot++) {
					Rect r = UnifiedTreeLayout.pathNode(paths, p, row, slot);
					assertTrue(r.inside(CANVAS), r.toString());
					assertTrue(r.y() >= paths.bodyY() + UnifiedTreeLayout.PATH_HEAD_H && r.bottom() <= paths.bottom());
					all.add(r);
				}
			}
		}
		for (int i = 0; i < all.size(); i++) {
			for (int j = i + 1; j < all.size(); j++) assertFalse(all.get(i).overlaps(all.get(j)), all.get(i) + " / " + all.get(j));
		}
	}

	@Test void kitConnectorsLineUpWithNodeCentersAndStayInTheirColumn() {
		Block paths = UnifiedTreeLayout.block(UnifiedTreeLayout.blocks(0, 0), Section.PATHS);
		int column = UnifiedTreeLayout.WIDTH / 3;
		for (int p = 0; p < 3; p++) {
			for (int row : new int[]{0, 1, 2, 5}) {
				Rect link = UnifiedTreeLayout.link(paths, p, row);
				Rect upper = UnifiedTreeLayout.pathNode(paths, p, row, 0);
				Rect lower = UnifiedTreeLayout.pathNode(paths, p, row + 1, 0);
				assertEquals(upper.x() + 11, link.x() + 6);
				assertTrue(link.y() + 10 <= upper.bottom() && link.bottom() - 10 >= lower.y(), "engarces tras los nodos");
			}
			Rect fork = UnifiedTreeLayout.fork(paths, p);
			Rect s4 = UnifiedTreeLayout.pathNode(paths, p, 3, 0);
			assertEquals(s4.x() + 11, fork.x() + 32, "tallo sobre S4");
			assertEquals(UnifiedTreeLayout.pathNode(paths, p, 4, 0).x() + 11, fork.x() + 10, "pata sobre S5x");
			assertEquals(UnifiedTreeLayout.pathNode(paths, p, 4, 1).x() + 11, fork.x() + 54, "pata sobre S5y");
			Rect join = UnifiedTreeLayout.join(paths, p);
			assertEquals(fork.x(), join.x());
			assertTrue(fork.x() >= p * column && fork.right() <= (p + 1) * column, "bifurcación en su columna");
		}
	}

	@Test void centeredRowsAreSymmetric() {
		for (int count = 1; count <= 7; count++) {
			int[] xs = UnifiedTreeLayout.rowXs(count, UnifiedTreeLayout.EVO_PITCH);
			int left = xs[0];
			int right = UnifiedTreeLayout.WIDTH - (xs[count - 1] + UnifiedTreeLayout.NODE);
			assertTrue(Math.abs(left - right) <= 1, "centrada con " + count);
			assertTrue(left >= 0 && right >= 0);
		}
		assertEquals(0, UnifiedTreeLayout.rowXs(0, 44).length);
	}

	@Test void theMasteryBarFitsItsBlock() {
		Block mastery = UnifiedTreeLayout.block(UnifiedTreeLayout.blocks(0, 0), Section.MASTERY);
		Rect bar = UnifiedTreeLayout.masteryBar(mastery);
		assertTrue(bar.inside(CANVAS));
		assertTrue(bar.y() >= mastery.bodyY() && bar.bottom() <= mastery.bottom());
	}
}
