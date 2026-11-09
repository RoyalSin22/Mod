package dev.aptitudes.boss;

import dev.aptitudes.boss.AttackTags.Tag;
import dev.aptitudes.boss.GuardRules.Outcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GuardRulesTest {
	@ParameterizedTest
	@CsvSource({
			// etiqueta,      bloqueando, resultado
			"PARRYABLE,      true,       PARRY",
			"PARRYABLE,      false,      HIT",
			"NORMAL,         true,       HIT",
			"HEAVY,          true,       HIT",
			"GROUND,         true,       HIT",
			"PROJECTILE,     true,       HIT",
	})
	void onlyParryableAttacksAreDeflectedAndOnlyWhenBlocking(Tag tag, boolean blocking, Outcome expected) {
		assertEquals(expected, GuardRules.resolve(AttackTags.of(tag), blocking));
	}

	@Test void unblockableAttacksAreNeverParried() {
		assertEquals(Outcome.HIT, GuardRules.resolve(AttackTags.of(Tag.PARRYABLE, Tag.UNBLOCKABLE), true));
	}

	@Test void fourParriesInARowBreakThePostureAndThreeDoNot() {
		PostureRules.Posture p = PostureRules.Posture.fresh(100);
		for (int i = 0; i < 3; i++) {
			PostureRules.Result r = PostureRules.drain(p, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of(), 0);
			assertEquals(false, r.broke(), "parry " + (i + 1));
			p = r.state();
		}
		assertEquals(true, PostureRules.drain(p, GuardRules.PARRY_POSTURE_DAMAGE, AttackTags.of(), 0).broke());
	}
}
