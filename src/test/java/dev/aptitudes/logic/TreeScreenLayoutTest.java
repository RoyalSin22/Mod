package dev.aptitudes.logic;

import dev.aptitudes.logic.TreeScreenLayout.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeScreenLayoutTest {
	private static final int[] WIDTHS = {426, 480, 520, 640, 1000};
	private static final int[] HEIGHTS = {240, 270, 360, 540};

	@Test void everythingFitsInsideTheWindow() {
		for (int w : WIDTHS) {
			for (int h : HEIGHTS) {
				TreeScreenLayout layout = TreeScreenLayout.place(w, h, 0);
				Rect window = layout.window();
				for (Rect r : layout.solidElements()) assertTrue(r.inside(window), r + " fuera de " + window + " con " + w + "x" + h);
			}
		}
	}

	@Test void noSolidElementsOverlap() {
		for (int w : WIDTHS) {
			List<Rect> all = TreeScreenLayout.place(w, 360, 0).solidElements();
			for (int i = 0; i < all.size(); i++) {
				for (int j = i + 1; j < all.size(); j++) {
					assertFalse(all.get(i).overlaps(all.get(j)), all.get(i) + " solapa con " + all.get(j) + " con ancho " + w);
				}
			}
		}
	}

	@Test void theWindowIsCenteredAndNeverWiderThanTheMaximum() {
		for (int w : WIDTHS) {
			Rect window = TreeScreenLayout.place(w, 360, 0).window();
			assertTrue(window.w() <= TreeScreenLayout.MAX_WIDTH);
			assertEquals((w - window.w()) / 2, window.x());
		}
		assertEquals(TreeScreenLayout.MAX_WIDTH, TreeScreenLayout.place(1000, 540, 0).window().w());
		assertEquals(426 - 8, TreeScreenLayout.place(426, 360, 0).window().w());
	}

	@Test void aTallScreenCentersVerticallyAndAShortOneScrolls() {
		assertEquals(0, TreeScreenLayout.maxScroll(540));
		assertEquals(0, TreeScreenLayout.maxScroll(TreeScreenLayout.HEIGHT + 8));
		assertEquals(44, TreeScreenLayout.maxScroll(308));
		assertEquals(82, TreeScreenLayout.maxScroll(270));
		assertEquals(0, TreeScreenLayout.maxScroll(360), "cabe sin desplazar a escala 3 en 1080p");
		assertEquals((540 - TreeScreenLayout.HEIGHT) / 2, TreeScreenLayout.place(640, 540, 0).window().y());
		assertEquals(4, TreeScreenLayout.place(640, 270, 0).window().y());
		assertEquals(4 - 38, TreeScreenLayout.place(640, 270, 38).window().y());
		assertEquals(4 - 82, TreeScreenLayout.place(640, 270, 999).window().y(), "el desplazamiento se limita");
		assertEquals(4, TreeScreenLayout.place(640, 270, -5).window().y(), "el desplazamiento no es negativo");
	}

	@Test void theTwoBifurcationOptionsAreSymmetricAroundTheColumnCenter() {
		TreeScreenLayout layout = TreeScreenLayout.place(640, 540, 0);
		for (int p = 0; p < TreeScreenLayout.PATHS; p++) {
			Rect left = layout.node(p, TreeScreenLayout.BIFURCATION_ROW, 0);
			Rect right = layout.node(p, TreeScreenLayout.BIFURCATION_ROW, 1);
			int center = layout.columnCenterX(p);
			assertEquals(center - (left.x() + TreeScreenLayout.NODE / 2), right.x() + TreeScreenLayout.NODE / 2 - center);
			assertFalse(left.overlaps(right));
			assertTrue(left.right() <= right.x());
			// los nodos únicos están centrados en la columna
			Rect single = layout.node(p, 0, 0);
			assertEquals(center, single.x() + TreeScreenLayout.NODE / 2);
		}
	}

	@Test void rowsAreStackedWithTheNominalPitch() {
		TreeScreenLayout layout = TreeScreenLayout.place(640, 540, 0);
		for (int row = 1; row < TreeScreenLayout.ROWS; row++) {
			assertEquals(TreeScreenLayout.PITCH,
					layout.node(0, row, 0).y() - layout.node(0, row - 1, 0).y());
		}
		assertEquals(1, TreeScreenLayout.slots(0));
		assertEquals(2, TreeScreenLayout.slots(TreeScreenLayout.BIFURCATION_ROW));
	}

	@Test void theDetailPanelKeepsEnoughRoomAtTheMinimumWidth() {
		assertTrue(TreeScreenLayout.place(426, 360, 0).detail().w() >= 84);
		assertTrue(TreeScreenLayout.place(640, 360, 0).detail().w() > TreeScreenLayout.place(426, 360, 0).detail().w());
	}

	@Test void aSmallScreenNeverProducesAWindowNarrowerThanTheMinimum() {
		assertEquals(TreeScreenLayout.MIN_WIDTH, TreeScreenLayout.place(300, 240, 0).window().w());
	}

	@Test void hitTestingUsesHalfOpenRectangles() {
		Rect r = new Rect(10, 10, 24, 24);
		assertTrue(r.contains(10, 10));
		assertTrue(r.contains(33.9, 33.9));
		assertFalse(r.contains(34, 20));
		assertFalse(r.contains(9.9, 20));
	}

	@Test void anchoredTilesCoverTheLengthExactly() {
		for (int tex : new int[]{240, 426, 128, 176}) {
			for (int length : new int[]{1, 84, 108, 186, 240, 300, 318, 418, 520, 1000}) {
				int next = 0;
				for (KScreenLayout.Tile t : TreeScreenLayout.anchoredTiles(length, tex, 12)) {
					assertEquals(next, t.dest(), "sin huecos con " + length + "/" + tex);
					assertTrue(t.len() > 0 && t.src() >= 0 && t.src() + t.len() <= tex, t + " dentro de la pieza " + tex);
					next += t.len();
				}
				assertEquals(length, next);
			}
		}
	}

	@Test void anchoredTilesKeepBothHalvesAndFillFromBeforeTheCenter() {
		List<KScreenLayout.Tile> tiles = TreeScreenLayout.anchoredTiles(TreeScreenLayout.HEIGHT, 240, 40, TreeScreenLayout.BRICK_PERIOD);
		assertEquals(new KScreenLayout.Tile(0, 0, 120), tiles.get(0), "mitad superior intacta");
		assertEquals(new KScreenLayout.Tile(TreeScreenLayout.HEIGHT - 120, 120, 120), tiles.get(tiles.size() - 1), "mitad inferior anclada");
		// 344 = 240 + 4 hiladas: el relleno (franja de 78 = 3 hiladas, antes del centro) conserva la fase en cada unión.
		assertEquals(List.of(new KScreenLayout.Tile(120, 94, 26), new KScreenLayout.Tile(146, 42, 78)), tiles.subList(1, tiles.size() - 1));
		for (KScreenLayout.Tile t : tiles) assertEquals(0, Math.floorMod(t.dest() - t.src(), 26), "en fase: " + t);
		// Panel de detalle (128 px, grietas hasta 38 px del borde) estirado a 186: el relleno sale de [40, 64).
		for (KScreenLayout.Tile t : TreeScreenLayout.anchoredTiles(186, 128, 40)) {
			if (t.dest() >= 64 && t.dest() < 186 - 64) assertTrue(t.src() >= 40 && t.src() + t.len() <= 64, t.toString());
		}
	}

	@Test void anchoredTilesShrinkFromTheCenter() {
		assertEquals(List.of(new KScreenLayout.Tile(0, 0, 54), new KScreenLayout.Tile(54, 122, 54)),
				TreeScreenLayout.anchoredTiles(108, 176, 12), "se conservan los dos extremos");
		assertTrue(TreeScreenLayout.anchoredTiles(0, 176, 12).isEmpty());
		assertTrue(TreeScreenLayout.anchoredTiles(10, 0, 12).isEmpty());
	}

	@Test void theMasteryBarSitsBelowTheButtonsAtEveryWidth() {
		for (int w : WIDTHS) {
			TreeScreenLayout layout = TreeScreenLayout.place(w, 540, 0);
			Rect m = layout.mastery();
			assertEquals(TreeScreenLayout.MASTERY_W, m.w());
			assertTrue(m.y() >= layout.closeButton().bottom(), "bajo los botones con ancho " + w);
			assertTrue(m.bottom() <= layout.window().bottom() - 12, "fuera del marco inferior");
		}
	}

	@Test void connectorsLineUpWithTheNodeCentersAndHideTheirSockets() {
		for (int w : WIDTHS) {
			TreeScreenLayout layout = TreeScreenLayout.place(w, 540, 0);
			for (int p = 0; p < TreeScreenLayout.PATHS; p++) {
				for (int row : new int[]{0, 1, 2, 5}) { // S1→S2→S3→S4 y S6→S7
					Rect link = layout.link(p, row), upper = layout.node(p, row, 0), lower = layout.node(p, row + 1, 0);
					assertEquals(upper.x() + 11, link.x() + 6, "línea central del conector sobre el nodo");
					assertTrue(link.y() + 10 <= upper.bottom() && link.bottom() - 10 >= lower.y(), "engarces tras los nodos");
					assertTrue(link.y() >= upper.y() && link.bottom() <= lower.bottom());
				}
				Rect fork = layout.fork(p), s4 = layout.node(p, 3, 0);
				Rect x = layout.node(p, TreeScreenLayout.BIFURCATION_ROW, 0), y = layout.node(p, TreeScreenLayout.BIFURCATION_ROW, 1);
				assertEquals(s4.x() + 11, fork.x() + 32, "tallo sobre S4");
				assertEquals(x.x() + 11, fork.x() + 10, "pata izquierda sobre S5x");
				assertEquals(y.x() + 11, fork.x() + 54, "pata derecha sobre S5y");
				assertTrue(fork.y() + 10 <= s4.bottom() && fork.bottom() - 10 >= x.y());
				Rect join = layout.join(p), s6 = layout.node(p, TreeScreenLayout.BIFURCATION_ROW + 1, 0);
				assertEquals(fork.x(), join.x());
				assertTrue(join.y() + 10 <= x.bottom() && join.bottom() - 10 >= s6.y());
				// dentro de la columna, sin invadir la vecina
				int column = layout.columnCenterX(p) - TreeScreenLayout.COLUMN_WIDTH / 2;
				assertTrue(fork.x() >= column && fork.right() <= column + TreeScreenLayout.COLUMN_WIDTH, "bifurcación en su columna");
			}
		}
	}

	@Test void theLastRowEndsAboveTheMasteryBar() {
		TreeScreenLayout layout = TreeScreenLayout.place(520, 540, 0);
		assertTrue(layout.node(0, TreeScreenLayout.ROWS - 1, 0).bottom() <= layout.mastery().y() - 4);
	}
}
