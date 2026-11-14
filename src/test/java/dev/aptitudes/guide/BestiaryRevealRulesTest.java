package dev.aptitudes.guide;

import dev.aptitudes.guide.BestiaryRules.Kind;
import dev.aptitudes.guide.BestiaryRules.Section;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** v67 (G1): revelado progresivo de las cartas del bestiario por número de muertes. */
public class BestiaryRevealRulesTest {
	@Test public void creaturesRevealInFourSteps() {
		assertEquals(1, BestiaryRules.revealAt(Kind.CREATURE, Section.IDENTITY));
		assertEquals(3, BestiaryRules.revealAt(Kind.CREATURE, Section.STATS));
		assertEquals(5, BestiaryRules.revealAt(Kind.CREATURE, Section.TIPS));
		assertEquals(10, BestiaryRules.revealAt(Kind.CREATURE, Section.LORE));
		assertEquals(BestiaryRules.revealAt(Kind.CREATURE, Section.LORE), BestiaryRules.revealAt(Kind.ELITE, Section.LORE));
	}

	@Test public void bossesRevealAlmostEverythingAtOnce() {
		assertEquals(1, BestiaryRules.revealAt(Kind.BOSS, Section.IDENTITY));
		assertEquals(1, BestiaryRules.revealAt(Kind.BOSS, Section.STATS));
		assertEquals(1, BestiaryRules.revealAt(Kind.BOSS, Section.TIPS));
		assertEquals(2, BestiaryRules.revealAt(Kind.BOSS, Section.LORE));
	}

	@Test public void revealedAndMissing() {
		assertFalse(BestiaryRules.revealed(Kind.CREATURE, Section.IDENTITY, 0));
		assertTrue(BestiaryRules.revealed(Kind.CREATURE, Section.IDENTITY, 1));
		assertFalse(BestiaryRules.revealed(Kind.CREATURE, Section.STATS, 2));
		assertTrue(BestiaryRules.revealed(Kind.CREATURE, Section.STATS, 3));
		assertEquals(3, BestiaryRules.killsMissing(Kind.CREATURE, Section.STATS, 0));
		assertEquals(1, BestiaryRules.killsMissing(Kind.CREATURE, Section.STATS, 2));
		assertEquals(0, BestiaryRules.killsMissing(Kind.CREATURE, Section.STATS, 99));
		assertEquals(5, BestiaryRules.killsMissing(Kind.CREATURE, Section.LORE, 5));
		assertEquals("un recuento negativo cuenta como cero", 10, BestiaryRules.killsMissing(Kind.CREATURE, Section.LORE, -4));
	}

	@Test public void thresholdsNeverDecrease() {
		for (Kind kind : Kind.values()) {
			int previous = 0;
			for (Section s : Section.values()) {
				int at = BestiaryRules.revealAt(kind, s);
				assertTrue(kind + " " + s, at >= previous);
				previous = at;
			}
		}
	}
}
