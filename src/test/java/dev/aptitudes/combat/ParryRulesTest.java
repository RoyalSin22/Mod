package dev.aptitudes.combat;

import dev.aptitudes.boss.GuardRules;
import dev.aptitudes.combat.ParryRules.Attacker;
import dev.aptitudes.combat.ParryRules.Hit;
import dev.aptitudes.combat.ParryRules.Outcome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** C7 (S3): parada y golpe de gracia contra criaturas. */
class ParryRulesTest {
	private static Hit melee(Attacker attacker, int ticks) {
		return new Hit(attacker, true, false, true, ticks);
	}

	@Test
	void windowMatchesTheBossGuard() {
		assertTrue(ParryRules.inWindow(0));
		assertTrue(ParryRules.inWindow(GuardRules.PERFECT_WINDOW_TICKS));
		assertFalse(ParryRules.inWindow(GuardRules.PERFECT_WINDOW_TICKS + 1));
		assertFalse(ParryRules.inWindow(GuardRules.NOT_BLOCKING));
	}

	@Test
	void normalCreaturesAreStunnedAndElitesLosePosture() {
		assertEquals(Outcome.STUN, ParryRules.resolve(true, melee(Attacker.NORMAL, 2)));
		assertEquals(Outcome.POSTURE, ParryRules.resolve(true, melee(Attacker.ELITE, 2)));
	}

	@Test
	void bossesKeepTheirOwnParry() {
		assertEquals(Outcome.NONE, ParryRules.resolve(true, melee(Attacker.BOSS, 0)));
	}

	@Test
	void onlyFrontalMeleeInsideTheWindowIsParried() {
		assertEquals(Outcome.NONE, ParryRules.resolve(true, new Hit(Attacker.NORMAL, false, false, true, 1)), "proyectil");
		assertEquals(Outcome.NONE, ParryRules.resolve(true, new Hit(Attacker.NORMAL, true, true, true, 1)), "ignora escudos");
		assertEquals(Outcome.NONE, ParryRules.resolve(true, new Hit(Attacker.NORMAL, true, false, false, 1)), "por la espalda");
		assertEquals(Outcome.NONE, ParryRules.resolve(true, melee(Attacker.NORMAL, 5)), "escudo levantado de antes");
		assertEquals(Outcome.NONE, ParryRules.resolve(true, melee(Attacker.NORMAL, GuardRules.NOT_BLOCKING)), "sin escudo");
	}

	@Test
	void disabledMeansNoParry() {
		assertEquals(Outcome.NONE, ParryRules.resolve(false, melee(Attacker.NORMAL, 0)));
		assertEquals(Outcome.NONE, ParryRules.resolve(false, melee(Attacker.ELITE, 0)));
	}

	@Test
	void frontFollowsTheVanillaShieldRule() {
		// Yaw 0 mira hacia +Z. Atacante en +Z: delante. En −Z: detrás.
		assertTrue(ParryRules.fromFront(0, 2, 0, 0, 0f));
		assertFalse(ParryRules.fromFront(0, -2, 0, 0, 0f));
		// Yaw 90 mira hacia −X.
		assertTrue(ParryRules.fromFront(-2, 0, 0, 0, 90f));
		assertFalse(ParryRules.fromFront(2, 0, 0, 0, 90f));
		// Justo al lado (90°): el producto escalar es 0, no cuenta como de frente.
		assertFalse(ParryRules.fromFront(2, 0, 0, 0, 0f));
		// Mismo punto: no es de frente.
		assertFalse(ParryRules.fromFront(0, 0, 0, 0, 0f));
	}

	@Test
	void stunHasACooldownSoItCannotBeChained() {
		long now = 1000;
		int stun = 30;
		long immune = ParryRules.immuneUntil(now, stun);
		assertEquals(now + stun + ParryRules.STUN_IMMUNITY_TICKS, immune);
		assertTrue(ParryRules.stunned(now + stun - 1, now + stun));
		assertFalse(ParryRules.stunned(now + stun, now + stun));
		assertFalse(ParryRules.canStun(now + stun + 10, immune));
		assertTrue(ParryRules.canStun(immune, immune));
		assertEquals(now + ParryRules.STUN_IMMUNITY_TICKS, ParryRules.immuneUntil(now, -5), "duración negativa = 0");
	}

	@Test
	void graceOnlyForTheParryingPlayerAndBeforeItExpires() {
		assertEquals(1.5, ParryRules.graceMultiplier(true, 100, 100, 1.5), 1e-9);
		assertEquals(1.0, ParryRules.graceMultiplier(true, 101, 100, 1.5), 1e-9);
		assertEquals(1.0, ParryRules.graceMultiplier(false, 50, 100, 1.5), 1e-9);
		assertEquals(1.0, ParryRules.graceMultiplier(true, 50, 100, 0.5), 1e-9, "nunca reduce el daño");
	}
}
