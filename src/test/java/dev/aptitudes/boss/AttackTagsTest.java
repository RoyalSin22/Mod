package dev.aptitudes.boss;

import dev.aptitudes.boss.AttackTags.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttackTagsTest {
	@Test void withoutTagsTheAttackIsNormal() {
		assertEquals(java.util.Set.of(Tag.NORMAL), AttackTags.of());
		assertEquals(java.util.Set.of(Tag.NORMAL), AttackTags.of((Tag) null));
	}

	@Test void resultIsImmutable() {
		assertThrows(UnsupportedOperationException.class, () -> AttackTags.of(Tag.HEAVY).add(Tag.GROUND));
	}

	@ParameterizedTest
	@CsvSource({
			"NORMAL,        true,  false, 1.0",
			"HEAVY,         true,  false, 2.0",
			"PARRYABLE,     true,  true,  1.0",
			"PROJECTILE,    true,  false, 1.0",
			"GROUND,        true,  false, 1.0",
			"UNBLOCKABLE,   false, false, 1.0",
	})
	void singleTagDefinesBlockParryAndPosture(Tag tag, boolean blockable, boolean parryable, double postureMultiplier) {
		var tags = AttackTags.of(tag);
		assertEquals(blockable, AttackTags.blockable(tags), "bloqueable");
		assertEquals(parryable, AttackTags.parryable(tags), "parry");
		assertEquals(postureMultiplier, AttackTags.postureMultiplier(tags), 1e-9, "postura");
	}

	@Test void unblockableOverridesParryable() {
		var tags = AttackTags.of(Tag.PARRYABLE, Tag.UNBLOCKABLE);
		assertFalse(AttackTags.blockable(tags));
		assertFalse(AttackTags.parryable(tags));
	}

	@ParameterizedTest
	@EnumSource(value = Tag.class, names = {"HEAVY"}, mode = EnumSource.Mode.EXCLUDE)
	void onlyHeavyMultipliesPosture(Tag tag) {
		assertEquals(1.0, AttackTags.postureMultiplier(AttackTags.of(tag)), 1e-9);
	}

	@Test void heavyAndParryableCombineAsDeclared() {
		var tags = AttackTags.of(Tag.HEAVY, Tag.PARRYABLE);
		assertTrue(AttackTags.parryable(tags));
		assertEquals(AttackTags.HEAVY_POSTURE_MULTIPLIER, AttackTags.postureMultiplier(tags), 1e-9);
	}

	@ParameterizedTest
	@CsvSource({
			"1.0,  true,  HEAVY",
			"0.9,  true,  HEAVY",
			"0.89, true,  NORMAL",
			"1.0,  false, NORMAL",
			"0.0,  true,  NORMAL",
	})
	void playerHitIsHeavyOnlyWithAFullMeleeCharge(float charge, boolean melee, Tag expected) {
		assertEquals(java.util.Set.of(expected), AttackTags.playerHit(charge, melee));
	}
}
