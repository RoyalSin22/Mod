package dev.aptitudes.boss;

import dev.aptitudes.boss.AttackTags.Tag;
import dev.aptitudes.boss.PostureRules.Posture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostureRulesTest {
	private static final long NOW = 1000;

	@Test void normalHitsReduceThePostureWithoutBreaking() {
		var r = PostureRules.drain(Posture.fresh(100), 30, AttackTags.of(Tag.NORMAL), NOW);
		assertFalse(r.broke());
		assertEquals(70, r.state().current(), 1e-9);
	}

	@ParameterizedTest
	@CsvSource({
			"NORMAL,  50,  false, 50",
			"HEAVY,   50,  true,  0",   // 50 × 2 = 100 ≥ postura: rompe
			"HEAVY,   20,  false, 60",  // 20 × 2 = 40
	})
	void heavyHitsBreakFasterThanNormalOnes(Tag tag, double amount, boolean broke, double remaining) {
		var r = PostureRules.drain(Posture.fresh(100), amount, AttackTags.of(tag), NOW);
		assertEquals(broke, r.broke(), "rotura");
		if (!broke) assertEquals(remaining, r.state().current(), 1e-9);
	}

	@Test void breakingStunsForTheConfiguredTicksAndResetsThePosture() {
		var r = PostureRules.drain(Posture.fresh(100), 200, AttackTags.of(Tag.HEAVY), NOW);
		assertTrue(r.broke());
		Posture p = r.state();
		assertEquals(100, p.current(), 1e-9, "la postura vuelve al máximo");
		assertTrue(p.stunned(NOW));
		assertTrue(p.stunned(NOW + PostureRules.STUN_TICKS - 1));
		assertFalse(p.stunned(NOW + PostureRules.STUN_TICKS));
		assertEquals(PostureRules.VULNERABLE_MULTIPLIER, p.damageMultiplier(NOW), 1e-9);
		assertEquals(1.0, p.damageMultiplier(NOW + PostureRules.STUN_TICKS), 1e-9);
	}

	@Test void noStunLockWhileImmune() {
		Posture after = PostureRules.drain(Posture.fresh(100), 200, AttackTags.of(Tag.HEAVY), NOW).state();
		long wakeUp = NOW + PostureRules.STUN_TICKS;
		// Justo al acabar el aturdimiento, un golpe fuerte no vuelve a romper: está en inmunidad.
		var during = PostureRules.drain(after, 200, AttackTags.of(Tag.HEAVY), wakeUp);
		assertFalse(during.broke());
		assertEquals(100, during.state().current(), 1e-9);
		// Pasada la inmunidad, sí puede romperse otra vez.
		long afterImmunity = wakeUp + PostureRules.IMMUNITY_TICKS;
		assertTrue(PostureRules.drain(after, 200, AttackTags.of(Tag.HEAVY), afterImmunity).broke());
	}

	@Test void hitsWhileStunnedAreIgnoredAndDoNotRecover() {
		Posture stunned = PostureRules.drain(Posture.fresh(100), 200, AttackTags.of(Tag.HEAVY), NOW).state();
		var r = PostureRules.drain(stunned, 1000, AttackTags.of(Tag.HEAVY), NOW + 1);
		assertFalse(r.broke());
		assertEquals(stunned, r.state());
		assertEquals(stunned, PostureRules.recover(stunned, 5, NOW + 1));
	}

	@Test void recoveryNeverExceedsTheMaximum() {
		Posture p = PostureRules.drain(Posture.fresh(100), 40, AttackTags.of(Tag.NORMAL), NOW).state();
		Posture full = PostureRules.recover(p, 1000, NOW);
		assertEquals(100, full.current(), 1e-9);
		assertEquals(p, PostureRules.recover(p, 0, NOW), "sin recuperación no cambia");
	}

	@Test void zeroOrNegativeDamageDoesNothing() {
		Posture p = Posture.fresh(100);
		assertEquals(p, PostureRules.drain(p, 0, AttackTags.of(Tag.HEAVY), NOW).state());
		assertEquals(p, PostureRules.drain(p, -5, AttackTags.of(Tag.HEAVY), NOW).state());
	}

	@Test void invalidValuesAreClampedOnConstruction() {
		Posture p = new Posture(500, 0, -1, -1);
		assertEquals(1, p.max(), 1e-9);
		assertEquals(1, p.current(), 1e-9);
		assertEquals(0, p.stunnedUntil());
		assertEquals(0, p.immuneUntil());
	}
}
