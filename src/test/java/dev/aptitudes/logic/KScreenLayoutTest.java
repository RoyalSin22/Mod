package dev.aptitudes.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.aptitudes.logic.TreeScreenLayout.Rect;
import java.util.List;
import org.junit.jupiter.api.Test;

class KScreenLayoutTest {
	private static final int[] SCREEN_HEIGHTS = {240, 270, 300, 360, 540};

	private static void assertNoOverlap(List<Rect> rects) {
		for (int i = 0; i < rects.size(); i++) {
			for (int j = i + 1; j < rects.size(); j++) {
				assertFalse(rects.get(i).overlaps(rects.get(j)), "solape entre " + rects.get(i) + " y " + rects.get(j));
			}
		}
	}

	@Test
	void everythingFitsInsideTheWindow() {
		for (int h : SCREEN_HEIGHTS) {
			KScreenLayout l = KScreenLayout.place(854, h, 0);
			Rect window = l.window();
			for (Rect r : l.attributeElements(4)) assertTrue(r.inside(window), "fuera de la ventana: " + r);
			for (Rect r : l.skillElements()) assertTrue(r.inside(window), "fuera de la ventana: " + r);
		}
	}

	@Test
	void noElementOverlapsAnother() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		assertNoOverlap(l.attributeElements(3));
		assertNoOverlap(l.attributeElements(4));
		assertNoOverlap(l.skillElements());
	}

	@Test
	void contentStaysAboveTheBottomBorder() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		int limit = l.window().bottom() - KScreenLayout.BORDER;
		for (int i = 0; i < KScreenLayout.SKILLS; i++) assertTrue(l.card(i).bottom() <= limit, "tarjeta " + i);
		assertTrue(l.hint().bottom() <= limit);
		assertTrue(l.button(0, 3).bottom() <= l.hint().y());
	}

	@Test
	void tenCardsInFiveRowsOfTwo() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		assertEquals(10, KScreenLayout.SKILLS);
		for (int i = 0; i < KScreenLayout.SKILLS; i += 2) {
			assertEquals(l.card(i).y(), l.card(i + 1).y());
			assertTrue(l.card(i).right() < l.card(i + 1).x());
		}
		assertEquals(l.card(0).x(), l.card(2).x());
		assertEquals(l.card(0).bottom(), l.card(2).y());
	}

	@Test
	void rowsLiveInsideTheirColumnPanels() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		for (int i = 0; i < KScreenLayout.ATTRIBUTES; i++) assertTrue(l.attributeRow(i).inside(l.columnA()), "atributo " + i);
		for (int i = 0; i < KScreenLayout.STATS; i++) assertTrue(l.statRow(i).inside(l.columnB()), "estadística " + i);
		assertTrue(l.detailInner().inside(l.columnC()));
	}

	@Test
	void tabsFitNextToTheEmblem() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		Rect window = l.window();
		for (int i = 0; i < KScreenLayout.TABS; i++) {
			assertTrue(l.tab(i).inside(window));
			assertFalse(l.tab(i).overlaps(l.emblem()));
			if (i > 0) assertFalse(l.tab(i).overlaps(l.tab(i - 1)));
		}
	}

	@Test
	void buttonRowIsCentered() {
		KScreenLayout l = KScreenLayout.place(854, 480, 0);
		for (int count : new int[] {1, 3, 4}) {
			Rect first = l.button(0, count);
			Rect last = l.button(count - 1, count);
			assertEquals(l.window().x() + l.window().w() - last.right(), first.x() - l.window().x());
		}
	}

	@Test
	void shortScreensScrollAndTallOnesCenter() {
		assertEquals(0, KScreenLayout.maxScroll(360));
		assertTrue(KScreenLayout.maxScroll(240) > 0);
		KScreenLayout centered = KScreenLayout.place(854, 480, 0);
		assertEquals((480 - KScreenLayout.HEIGHT) / 2, centered.y());
		int max = KScreenLayout.maxScroll(240);
		assertEquals(8 - max, KScreenLayout.place(854, 240, max).y());
		assertEquals(8, KScreenLayout.place(854, 240, -50).y());
		assertEquals(8 - max, KScreenLayout.place(854, 240, 9999).y());
	}

	@Test
	void narrowScreensStillCenterTheWindow() {
		KScreenLayout l = KScreenLayout.place(426, 300, 0);
		assertEquals((426 - KScreenLayout.WIDTH) / 2, l.x());
	}
}
