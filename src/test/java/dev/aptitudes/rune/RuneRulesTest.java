package dev.aptitudes.rune;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RuneRulesTest {
	@Test void onlyTwoDifferentKnownRunesCanBeSocketed() {
		assertTrue(RuneRules.canSocket(List.of(), "flame"));
		assertFalse(RuneRules.canSocket(List.of("flame"), "flame"));
		assertTrue(RuneRules.canSocket(List.of("flame"), "leech"));
		assertFalse(RuneRules.canSocket(List.of("flame", "leech"), "swift"));
		assertFalse(RuneRules.canSocket(List.of(), "unknown"));
	}
	@Test void effectsHaveBoundedFixedValues() {
		assertEquals(1.0f, RuneRules.damageBonus(1));
		assertEquals(0.10, RuneRules.attackSpeedBonus(1));
		assertEquals(1.0f, RuneRules.leechAmount());
		assertEquals(80, RuneRules.leechCooldownTicks());
		assertEquals(3, RuneRules.flameSeconds());
	}
}
