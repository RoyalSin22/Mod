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
		assertEquals(0, TreeScreenLayout.maxScroll(308));
		assertEquals(38, TreeScreenLayout.maxScroll(270));
		assertEquals((540 - TreeScreenLayout.HEIGHT) / 2, TreeScreenLayout.place(640, 540, 0).window().y());
		assertEquals(4, TreeScreenLayout.place(640, 270, 0).window().y());
		assertEquals(4 - 38, TreeScreenLayout.place(640, 270, 38).window().y());
		assertEquals(4 - 38, TreeScreenLayout.place(640, 270, 999).window().y(), "el desplazamiento se limita");
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
}
