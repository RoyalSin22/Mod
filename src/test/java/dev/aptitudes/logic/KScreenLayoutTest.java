package dev.aptitudes.logic;

import dev.aptitudes.logic.KScreenLayout.Tile;
import dev.aptitudes.logic.TreeScreenLayout.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KScreenLayoutTest {
	private static final Rect WINDOW = new Rect(0, 0, KScreenLayout.W, KScreenLayout.H);
	/** Línea de ayuda al pie de la ventana: las tarjetas deben terminar antes. */
	private static final int HINT_Y = KScreenLayout.H - 16;

	@Test void theTenCardsFitInTheWindowWithoutOverlapping() {
		for (int i = 0; i < 10; i++) {
			Rect a = KScreenLayout.card(0, 0, i);
			assertTrue(a.inside(WINDOW), "tarjeta " + i);
			assertTrue(a.bottom() <= HINT_Y, "la tarjeta " + i + " no tapa la línea de ayuda");
			for (int j = i + 1; j < 10; j++) assertFalse(a.overlaps(KScreenLayout.card(0, 0, j)), i + " y " + j);
		}
	}

	@Test void theCardsAreSymmetricInTwoColumnsOfFive() {
		Rect first = KScreenLayout.card(0, 0, 0), second = KScreenLayout.card(0, 0, 1), last = KScreenLayout.card(0, 0, 9);
		assertEquals(first.y(), second.y());
		assertEquals(first.x(), KScreenLayout.W - second.right(), "márgenes izquierdo y derecho iguales");
		assertEquals(first.y() + 4 * KScreenLayout.CARD_STEP_Y, last.y());
	}

	@Test void theBarAndIconSitInsideTheCard() {
		Rect card = new Rect(0, 0, KScreenLayout.CARD_W, KScreenLayout.CARD_H);
		Rect bar = new Rect(KScreenLayout.BAR_X, KScreenLayout.BAR_Y, KScreenLayout.BAR_W, KScreenLayout.BAR_H);
		Rect icon = new Rect(KScreenLayout.ICON_X, KScreenLayout.ICON_Y, 16, 16);
		assertTrue(bar.inside(card));
		assertTrue(icon.inside(card));
		assertFalse(bar.overlaps(icon));
	}

	@Test void theAttributeColumnsDoNotOverlap() {
		Rect attributes = KScreenLayout.attributeColumn(0, 0, 8);
		Rect stats = KScreenLayout.statsColumn(0, 0, 11, 14);
		Rect detail = KScreenLayout.detailPanel(0, 0);
		for (Rect r : List.of(attributes, stats, detail)) assertTrue(r.inside(WINDOW), r.toString());
		assertFalse(attributes.overlaps(stats));
		assertFalse(stats.overlaps(detail));
		for (int i = 0; i < 8; i++) assertTrue(KScreenLayout.attributeRow(0, 0, i).inside(attributes), "fila " + i);
		assertTrue(attributes.bottom() <= KScreenLayout.H - 44, "por encima de los botones");
	}

	@Test void theTabsFitToTheRightAndDoNotOverlap() {
		List<Rect> tabs = KScreenLayout.tabs(0, 0, new int[]{70, 28, 58, 48});
		assertEquals(4, tabs.size());
		for (int i = 0; i < tabs.size(); i++) {
			assertTrue(tabs.get(i).inside(WINDOW), "pestaña " + i);
			for (int j = i + 1; j < tabs.size(); j++) assertFalse(tabs.get(i).overlaps(tabs.get(j)));
		}
		assertEquals(KScreenLayout.W - 8, tabs.get(0).right());
		assertEquals(2 * KScreenLayout.TAB_CAP + 2, KScreenLayout.tabs(0, 0, new int[]{0}).get(0).w(), "nunca menos que dos esquinas");
	}

	@Test void tilesCoverTheLengthExactlyWithoutGaps() {
		for (int length : new int[]{1, 5, 16, 18, 80, 112, 140, 333}) {
			List<Tile> tiles = KScreenLayout.tiles(length, 12, 6, 80);
			int next = 0;
			for (Tile t : tiles) {
				assertEquals(next, t.dest(), "sin huecos ni solapes con " + length);
				assertTrue(t.len() > 0);
				assertTrue(t.src() >= 0 && t.src() + t.len() <= 80, "dentro de la textura");
				next += t.len();
			}
			assertEquals(length, next, "cubre exactamente " + length);
		}
	}

	@Test void tilesKeepBothCapsAndRepeatTheMiddle() {
		List<Tile> tiles = KScreenLayout.tiles(140, 12, 6, 80);
		assertEquals(new Tile(0, 0, 12), tiles.get(0));
		assertEquals(new Tile(134, 74, 6), tiles.get(tiles.size() - 1));
		for (Tile t : tiles.subList(1, tiles.size() - 1)) assertEquals(12, t.src(), "el centro se repite desde la columna 12");
		assertEquals(List.of(new Tile(0, 0, 12), new Tile(12, 12, 62), new Tile(74, 74, 6)),
				KScreenLayout.tiles(80, 12, 6, 80), "a su tamaño nativo, la pieza se copia tal cual");
	}

	@Test void hostileTileInputsReturnNothingOrStayInBounds() {
		assertTrue(KScreenLayout.tiles(0, 12, 6, 80).isEmpty());
		assertTrue(KScreenLayout.tiles(-5, 12, 6, 80).isEmpty());
		assertTrue(KScreenLayout.tiles(50, 12, 6, 0).isEmpty());
		assertEquals(List.of(new Tile(0, 0, 10)), KScreenLayout.tiles(10, 50, 50, 80));
	}

	@Test void barFillIsClampedAndRounded() {
		assertEquals(0, KScreenLayout.filled(160, 0));
		assertEquals(0, KScreenLayout.filled(160, -1));
		assertEquals(0, KScreenLayout.filled(160, Double.NaN));
		assertEquals(80, KScreenLayout.filled(160, 0.5));
		assertEquals(160, KScreenLayout.filled(160, 7.0));
		assertEquals(0, KScreenLayout.filled(0, 0.5));
	}
}
