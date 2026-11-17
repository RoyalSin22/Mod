package dev.aptitudes.ability;

import dev.aptitudes.ability.Y1Rules.Category;
import dev.aptitudes.ability.Y1Rules.PathProfile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Y1RulesTest {
	private static final PathProfile SPEED = new PathProfile(true, false, false, false, false, false, false);
	private static final PathProfile CRIT = new PathProfile(false, true, false, false, false, false, false);
	private static final PathProfile MINION = new PathProfile(false, false, false, true, false, false, false);

	@Test
	void classifiesBaseExtraAndEvolvedActives() {
		assertTrue(Y1Rules.has("leap_slam", null, Category.MOVEMENT));
		assertTrue(Y1Rules.has("leap_slam", null, Category.DAMAGE));
		assertTrue(Y1Rules.has("whirlwind", null, Category.DAMAGE));
		assertFalse(Y1Rules.has("whirlwind", null, Category.MOVEMENT));
		assertTrue(Y1Rules.has("summon_wolves", null, Category.SUMMON));
		assertTrue(Y1Rules.has("mage_n10", null, Category.DAMAGE));
		assertTrue(Y1Rules.has("tamer_n4", null, Category.SUMMON));
		assertFalse(Y1Rules.has("tamer_n8", null, Category.DAMAGE));
		assertFalse(Y1Rules.has("rage", null, Category.DAMAGE));
		assertTrue(Y1Rules.has("berserker_m4", "NOVA", Category.DAMAGE));
		assertTrue(Y1Rules.has("berserker_m5", "DASH", Category.MOVEMENT));
		assertTrue(Y1Rules.has("mage_u4", "SUMMON", Category.SUMMON));
		assertFalse(Y1Rules.has("mage_u5", "HEAL", Category.DAMAGE));
		assertFalse(Y1Rules.has(null, null, Category.DAMAGE));
		assertFalse(Y1Rules.has("whirlwind", null, Category.NONE));
	}

	@Test
	void noEffectsMeansNoBonus() {
		assertEquals(1.0f, Y1Rules.radius(PathProfile.EMPTY, "leap_slam", null, 1.2));
		assertEquals(1.0f, Y1Rules.damage(PathProfile.EMPTY, "whirlwind", null, 1.15));
		assertEquals(1.0f, Y1Rules.energy(PathProfile.EMPTY, 0.9));
	}

	@Test
	void radiusNeedsSpeedAndMovementAbility() {
		assertEquals(1.2f, Y1Rules.radius(SPEED, "leap_slam", null, 1.2), 1e-6);
		assertEquals(1.2f, Y1Rules.radius(SPEED, "berserker_m5", "DASH", 1.2), 1e-6);
		assertEquals(1.0f, Y1Rules.radius(SPEED, "whirlwind", null, 1.2));
		assertEquals(1.0f, Y1Rules.radius(CRIT, "leap_slam", null, 1.2));
	}

	@Test
	void damageFollowsEffectAndCategory() {
		assertEquals(1.15f, Y1Rules.damage(CRIT, "whirlwind", null, 1.15), 1e-6);
		assertEquals(1.15f, Y1Rules.damage(CRIT, "mage_u4", "BEAM", 1.15), 1e-6);
		assertEquals(1.0f, Y1Rules.damage(CRIT, "turret", null, 1.15));
		assertEquals(1.15f, Y1Rules.damage(MINION, "turret", null, 1.15), 1e-6);
		assertEquals(1.0f, Y1Rules.damage(MINION, "whirlwind", null, 1.15));
	}

	@Test
	void energyNeedsBothEffects() {
		assertEquals(0.9f, Y1Rules.energy(new PathProfile(false, false, false, false, true, false, true), 0.9), 1e-6);
		assertEquals(1.0f, Y1Rules.energy(new PathProfile(false, false, false, false, true, false, false), 0.9));
		assertEquals(1.0f, Y1Rules.energy(new PathProfile(false, false, false, false, false, false, true), 0.9));
	}
}
