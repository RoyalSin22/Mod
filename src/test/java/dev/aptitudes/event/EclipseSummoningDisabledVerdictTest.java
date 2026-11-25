package dev.aptitudes.event;

import org.junit.jupiter.api.Test;

import static dev.aptitudes.event.EclipseSummoningRules.Verdict;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Fusión de la espera del Behelit con la del Astrolabio: {@code DISABLED} (Eclipse apagado en la configuración) se añadió al final del
 * enumerado y lo decide {@code EclipseManager.summonVerdict}, no las reglas puras.
 */
class EclipseSummoningDisabledVerdictTest {
	@Test void disabledIsAppendedAtTheEndAndKeepsTheExistingOrdinals() {
		Verdict[] all = Verdict.values();
		assertEquals(Verdict.DISABLED, all[all.length - 1]);
		assertEquals(0, Verdict.ALLOWED.ordinal());
		assertEquals(4, Verdict.COOLDOWN.ordinal());
		assertEquals(5, Verdict.DISABLED.ordinal());
	}

	@Test void thePureRulesNeverReturnDisabled() {
		for (boolean op : new boolean[] {true, false}) for (boolean leader : new boolean[] {true, false})
			for (boolean overworld : new boolean[] {true, false}) for (boolean busy : new boolean[] {true, false}) {
				assertNotEquals(Verdict.DISABLED, EclipseSummoningRules.astrolabe(op, leader, overworld, busy, 0, 1, 3));
				assertNotEquals(Verdict.DISABLED, EclipseSummoningRules.behelit(overworld, busy, 0, 1, 3));
			}
	}
}
