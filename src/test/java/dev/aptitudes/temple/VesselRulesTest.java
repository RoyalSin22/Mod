package dev.aptitudes.temple;

import dev.aptitudes.temple.VesselRules.Result;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VesselRulesTest {
	@Test public void vesselsStopAtTheLimit() {
		assertEquals(Result.OK, VesselRules.use(0, 5, false));
		assertEquals(Result.OK, VesselRules.use(4, 5, false));
		assertEquals(Result.AT_LIMIT, VesselRules.use(5, 5, false));
		assertEquals(Result.AT_LIMIT, VesselRules.use(7, 5, false));
	}

	@Test public void theSameVesselCannotBeUsedTwice() {
		assertEquals(Result.ALREADY_USED, VesselRules.use(0, 5, true));
	}

	@Test public void zeroDisablesAndTheLimitIsBounded() {
		assertEquals(Result.DISABLED, VesselRules.use(0, 0, false));
		assertEquals(0, VesselRules.limit(-4));
		assertEquals(VesselRules.MAX_LIMIT, VesselRules.limit(1000));
	}

	@Test public void bonusIsOneHeartPerVesselUpToTheLimit() {
		assertEquals(0.0, VesselRules.bonus(0, 5), 1e-9);
		assertEquals(6.0, VesselRules.bonus(3, 5), 1e-9);
		assertEquals(10.0, VesselRules.bonus(5, 5), 1e-9);
		// Tope rebajado: los vasos se conservan pero no cuentan.
		assertEquals(4.0, VesselRules.bonus(5, 2), 1e-9);
		assertEquals(0.0, VesselRules.bonus(-3, 5), 1e-9);
	}
}
