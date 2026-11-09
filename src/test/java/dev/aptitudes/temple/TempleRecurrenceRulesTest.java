package dev.aptitudes.temple;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempleRecurrenceRulesTest {
	private static final long RESOLVED = 500L;

	@Test void neverResolvedHasNoChance() {
		assertEquals(0, TempleRecurrenceRules.chancePermille(TempleRecurrenceRules.NEVER, 10_000_000L));
	}

	@Test void noRegenerationBeforeTheMinimumGap() {
		long justBefore = RESOLVED + TempleRecurrenceRules.MIN_GAP_TICKS - 1;
		assertEquals(0, TempleRecurrenceRules.chancePermille(RESOLVED, justBefore));
		assertFalse(TempleRecurrenceRules.shouldRegenerate(RESOLVED, justBefore, 0));
	}

	@ParameterizedTest
	@CsvSource({
			"0,      0",     // al cumplirse la espera, probabilidad inicial nula
			"36000,  125",   // mitad de la rampa
			"72000,  250",   // máximo alcanzado
			"999999, 250",   // mucho después, sigue en el máximo
	})
	void chanceRampsLinearlyUpToTheMaximum(long extra, int expected) {
		long gap = extra == 999999L ? extra : TempleRecurrenceRules.MIN_GAP_TICKS + extra;
		assertEquals(expected, TempleRecurrenceRules.chancePermille(RESOLVED, RESOLVED + gap));
	}

	@Test void rollDecidesRegenerationOnceTheChanceIsOpen() {
		long now = RESOLVED + TempleRecurrenceRules.FULL_CHANCE_TICKS;
		assertTrue(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, 0));
		assertTrue(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, 249));
		assertFalse(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, 250));
		assertFalse(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, 999));
	}

	@Test void outOfRangeRollsAreClamped() {
		long now = RESOLVED + TempleRecurrenceRules.FULL_CHANCE_TICKS;
		assertTrue(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, -50));
		assertFalse(TempleRecurrenceRules.shouldRegenerate(RESOLVED, now, 5000));
	}

	@Test void eachPlayerClaimsOnlyOnce() {
		UUID ana = UUID.randomUUID();
		Set<UUID> none = Set.of();
		assertTrue(TempleRecurrenceRules.canClaim(none, ana));
		Set<UUID> afterClaim = TempleRecurrenceRules.claim(none, ana);
		assertFalse(TempleRecurrenceRules.canClaim(afterClaim, ana));
	}

	@Test void claimsDoNotBlockOtherPlayersAndAreImmutable() {
		UUID ana = UUID.randomUUID();
		UUID luis = UUID.randomUUID();
		Set<UUID> afterAna = TempleRecurrenceRules.claim(Set.of(), ana);
		assertTrue(TempleRecurrenceRules.canClaim(afterAna, luis));
		assertThrows(UnsupportedOperationException.class, () -> afterAna.add(luis));
		assertFalse(TempleRecurrenceRules.canClaim(afterAna, null));
	}
}
