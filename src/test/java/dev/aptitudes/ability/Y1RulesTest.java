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
	void classifiesRealBaseActives() {
		assertEquals(Category.MOVEMENT, Y1Rules.categoryOf("leap_slam"));
		assertEquals(Category.DAMAGE, Y1Rules.categoryOf("whirlwind"));
		assertEquals(Category.SUMMON, Y1Rules.categoryOf("summon_wolves"));
		assertEquals(Category.NONE, Y1Rules.categoryOf("rage"));
		assertEquals(Category.NONE, Y1Rules.categoryOf("berserker_m4"));
		assertEquals(Category.NONE, Y1Rules.categoryOf(null));
	}

	@Test
	void noEffectsMeansNoBonus() {
		assertEquals(1.0f, Y1Rules.radius(PathProfile.EMPTY, "leap_slam", 1.2));
		assertEquals(1.0f, Y1Rules.damage(PathProfile.EMPTY, "whirlwind", 1.15));
		assertEquals(1.0f, Y1Rules.energy(PathProfile.EMPTY, 0.9));
	}

	@Test
	void radiusNeedsSpeedAndMovementAbility() {
		assertEquals(1.2f, Y1Rules.radius(SPEED, "leap_slam", 1.2), 1e-6);
		assertEquals(1.0f, Y1Rules.radius(SPEED, "whirlwind", 1.2));
		assertEquals(1.0f, Y1Rules.radius(CRIT, "leap_slam", 1.2));
	}

	@Test
	void damageFollowsEffectAndCategory() {
		assertEquals(1.15f, Y1Rules.damage(CRIT, "whirlwind", 1.15), 1e-6);
		assertEquals(1.0f, Y1Rules.damage(CRIT, "turret", 1.15));
		assertEquals(1.15f, Y1Rules.damage(MINION, "turret", 1.15), 1e-6);
		assertEquals(1.0f, Y1Rules.damage(MINION, "whirlwind", 1.15));
	}

	@Test
	void energyNeedsBothEffects() {
		assertEquals(0.9f, Y1Rules.energy(new PathProfile(false, false, false, false, true, false, true), 0.9), 1e-6);
		assertEquals(1.0f, Y1Rules.energy(new PathProfile(false, false, false, false, true, false, false), 0.9));
		assertEquals(1.0f, Y1Rules.energy(new PathProfile(false, false, false, false, false, false, true), 0.9));
	}
}
