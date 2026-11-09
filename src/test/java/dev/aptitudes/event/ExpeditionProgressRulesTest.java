package dev.aptitudes.event;

import dev.aptitudes.event.ExpeditionRules.Module;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpeditionProgressRulesTest {
	@ParameterizedTest
	@EnumSource(Module.class)
	void everyModuleHasAPositiveGoal(Module module) {
		assertTrue(ExpeditionProgressRules.goal(module) > 0);
	}

	@Test void singleTargetModulesNeedOneAction() {
		assertEquals(1, ExpeditionProgressRules.goal(Module.ELITE));
		assertEquals(1, ExpeditionProgressRules.goal(Module.EVENT));
		assertEquals(1, ExpeditionProgressRules.goal(Module.BOSS));
	}

	@Test void zoneIncludesTheRadiusAndExcludesWhatIsBeyond() {
		int r = ExpeditionProgressRules.ZONE_RADIUS;
		assertTrue(ExpeditionProgressRules.inZone((double) r * r));
		assertFalse(ExpeditionProgressRules.inZone((double) (r + 1) * (r + 1)));
	}

	@Test void expiresAfterTheTimeLimit() {
		long limit = ExpeditionProgressRules.TIME_LIMIT_TICKS;
		assertFalse(ExpeditionProgressRules.expired(100, 100 + limit - 1));
		assertTrue(ExpeditionProgressRules.expired(100, 100 + limit));
	}
}
