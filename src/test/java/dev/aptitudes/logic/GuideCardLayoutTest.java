package dev.aptitudes.logic;

import dev.aptitudes.logic.GuideCardLayout.Card;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** v67 (G2): maqueta de la carta del bestiario. */
public class GuideCardLayoutTest {
	private static final int LINE = 10;

	@Test public void sectionsStackWithoutOverlapping() {
		for (int pageW = 200; pageW <= 380; pageW += 20) {
			for (int pageH = 170; pageH <= 280; pageH += 10) {
				Card c = GuideCardLayout.layout(pageW, pageH, LINE);
				String at = pageW + "x" + pageH;
				assertTrue(at, c.ribbonY() < c.kindY());
				assertTrue(at, c.kindY() < c.statsY());
				assertTrue(at, c.statsY() + GuideCardLayout.STATS_LINES * LINE <= c.tipsY());
				assertTrue(at, c.tipsY() + LINE + c.tipsLines() * LINE <= c.lootY());
				assertTrue(at, c.lootY() + LINE + GuideCardLayout.SLOT <= c.loreY());
			}
		}
	}

	@Test public void cardFitsTheFreeHeightOfTheBook() {
		// Páginas del libro en pantallas habituales (alto de página entre 170 y 270).
		for (int pageH = 170; pageH <= 270; pageH += 10) {
			Card c = GuideCardLayout.layout(300, pageH, LINE);
			assertTrue("alto " + pageH, GuideCardLayout.height(c, LINE) <= pageH + LINE);
			assertTrue(c.tipsLines() >= 1 && c.loreLines() >= 1);
		}
	}

	@Test public void portraitAndTextColumnsDoNotOverlap() {
		for (int pageW = 200; pageW <= 380; pageW += 20) {
			Card c = GuideCardLayout.layout(pageW, 230, LINE);
			assertTrue(c.portrait().right() < c.textX());
			assertTrue(c.textX() + c.textW() <= pageW);
			assertTrue(c.portrait().w() >= GuideCardLayout.MIN_PORTRAIT_WIDTH);
			assertTrue(c.portrait().w() <= GuideCardLayout.MAX_PORTRAIT_WIDTH);
			assertTrue(c.seal().y() >= c.portrait().bottom());
		}
	}

	@Test public void moreHeightMeansMoreTextLines() {
		Card small = GuideCardLayout.layout(300, 180, LINE);
		Card big = GuideCardLayout.layout(300, 280, LINE);
		assertTrue(big.tipsLines() + big.loreLines() > small.tipsLines() + small.loreLines());
		assertEquals(0, small.portrait().x());
		assertEquals(0, small.portrait().y());
	}
}
