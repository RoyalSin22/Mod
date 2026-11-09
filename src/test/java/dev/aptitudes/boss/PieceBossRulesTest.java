package dev.aptitudes.boss;

import dev.aptitudes.boss.PieceBossRules.Guardian;
import dev.aptitudes.boss.PieceBossRules.Piece;
import dev.aptitudes.boss.PieceBossRules.Stun;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PieceBossRulesTest {
	@Test public void piecesBreakInOrderAndOnlyTheActiveOneTakesDamage() {
		Guardian g = Guardian.fresh();
		assertEquals(Piece.HELM, g.active());
		assertEquals(0, PieceBossRules.coreDamage(g, 10), 1e-9);
		// Un golpe a la pieza que no está activa no hace nada.
		assertFalse(g.hit(Piece.SHIELD, 50, 40).broke());
		PieceBossRules.Result first = g.hit(Piece.HELM, 50, 40);
		assertTrue(first.broke());
		assertEquals(Piece.SHIELD, first.state().active());
		assertEquals(List.of(Piece.SHIELD, Piece.GAUNTLETS), PieceBossRules.remaining(first.state()));
	}

	@Test public void weakHitsDoNotBreakAPiece() {
		Guardian g = Guardian.fresh();
		PieceBossRules.Result r = g.hit(Piece.HELM, 10, 40);
		assertFalse(r.broke());
		assertEquals(Piece.HELM, r.state().active());
	}

	@Test public void coreIsExposedOnlyAfterAllPiecesAndTakesMoreDamage() {
		Guardian g = new Guardian(0);
		assertTrue(g.coreExposed());
		assertNull(g.active());
		assertEquals(15, PieceBossRules.coreDamage(g, 10), 1e-9);
		assertEquals(0, PieceBossRules.coreDamage(Guardian.fresh(), 10), 1e-9);
		assertEquals(0, PieceBossRules.coreDamage(g, -3), 1e-9);
	}

	@Test public void guardianStateIsClamped() {
		assertEquals(3, new Guardian(9).intact());
		assertEquals(0, new Guardian(-2).intact());
	}

	@Test public void stunLastsAndDoublesDamage() {
		assertEquals(20, PieceBossRules.automatonDamage(new Stun(0), 20), 1e-9);
		Stun s = Stun.hit();
		assertTrue(s.stunned());
		assertEquals(40, PieceBossRules.automatonDamage(s, 20), 1e-9);
		Stun done = new Stun(1).tick();
		assertFalse(done.stunned());
		assertEquals(0, new Stun(0).tick().ticksLeft());
		assertEquals(0, PieceBossRules.automatonDamage(s, -5), 1e-9);
	}

	@Test public void deflectedOrbsAreFasterButCapped() {
		assertEquals(0.525, PieceBossRules.deflectedSpeed(0.35, 1.0), 1e-9);
		assertEquals(1.0, PieceBossRules.deflectedSpeed(0.9, 1.0), 1e-9);
		assertEquals(0.0, PieceBossRules.deflectedSpeed(-0.2, 0.0), 1e-9);
	}
}
