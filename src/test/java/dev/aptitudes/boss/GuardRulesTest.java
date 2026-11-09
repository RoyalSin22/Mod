package dev.aptitudes.boss;

import dev.aptitudes.boss.AttackTags.Tag;
import dev.aptitudes.boss.GuardRules.Outcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuardRulesTest {
	@ParameterizedTest
	@CsvSource({
			// ticks desde que se levantó el escudo, resultado
			"0,  PARRY",   // justo al levantar el escudo
			"4,  PARRY",   // último tick de la ventana perfecta
			"5,  HIT",     // fuera de la ventana: el escudo solo reduce daño
			"100, HIT",
			"-1, HIT",     // sin escudo levantado
	})
	void onlyTheFirstTicksOfBlockingCountAsPerfectGuard(int ticks, Outcome expected) {
		assertEquals(expected, GuardRules.resolve(AttackTags.of(Tag.PARRYABLE), ticks));
	}

	@ParameterizedTest
	@CsvSource({
			"NORMAL,      HIT",
			"HEAVY,       HIT",
			"GROUND,      HIT",
			"PROJECTILE,  HIT",
			"UNBLOCKABLE, HIT",
	})
	void onlyParryableAttacksCanBeDeflected(Tag tag, Outcome expected) {
		assertEquals(expected, GuardRules.resolve(AttackTags.of(tag), 0));
	}

	@Test void unblockableEvenWithPerfectTimingIsNotParried() {
		assertEquals(Outcome.HIT, GuardRules.resolve(AttackTags.of(Tag.PARRYABLE, Tag.UNBLOCKABLE), 0));
	}

	@Test void perfectWindowIsPositiveAndSmall() {
		assertTrue(GuardRules.PERFECT_WINDOW_TICKS > 0 && GuardRules.PERFECT_WINDOW_TICKS <= 10);
		assertFalse(GuardRules.resolve(AttackTags.of(Tag.PARRYABLE), GuardRules.PERFECT_WINDOW_TICKS + 1) == Outcome.PARRY);
	}

	@Test void fourParriesInARowBreakThePostureAndThreeDoNot() {
		PostureRules.Posture p = PostureRules.Posture.fresh(100);
		for (int i = 0; i < 3; i++) {
			PostureRules.Result r = PostureRules.drain(p, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of(), 0);
			assertFalse(r.broke(), "parry " + (i + 1));
			p = r.state();
		}
		assertTrue(PostureRules.drain(p, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of(), 0).broke());
	}
}
