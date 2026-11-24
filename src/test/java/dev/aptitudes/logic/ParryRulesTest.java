package dev.aptitudes.logic;

import dev.aptitudes.boss.GuardRules;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ParryRulesTest {
	private final UUID player = UUID.randomUUID();
	private final UUID mob = UUID.randomUUID();

	@Test void windowMatchesThePerfectGuardOfTheBosses() {
		assertTrue(ParryRules.inWindow(0));
		assertTrue(ParryRules.inWindow(GuardRules.PERFECT_WINDOW_TICKS));
		assertFalse(ParryRules.inWindow(GuardRules.PERFECT_WINDOW_TICKS + 1));
		assertFalse(ParryRules.inWindow(GuardRules.NOT_BLOCKING));
	}

	@Test void onlyDirectMeleeFromNonBossCreaturesCanBeParried() {
		assertTrue(ParryRules.canParry(false, true, false, false));
		assertFalse(ParryRules.canParry(true, true, false, false), "sin PvP");
		assertFalse(ParryRules.canParry(false, false, false, false), "proyectil o magia");
		assertFalse(ParryRules.canParry(false, true, true, false), "jefe");
		assertFalse(ParryRules.canParry(false, true, false, true), "campeón");
	}

	@Test void finisherMultiplierIsClamped() {
		assertEquals(15f, ParryRules.finisherDamage(10f, 1.5), 1e-4);
		assertEquals(10f, ParryRules.finisherDamage(10f, 0.2), 1e-4, "nunca resta daño");
		assertEquals(40f, ParryRules.finisherDamage(10f, 99), 1e-4, "tope ×4");
		assertEquals(10f, ParryRules.finisherDamage(10f, Double.NaN), 1e-4, "NaN no puede dar daño NaN");
	}

	@Test void expiryIsComputedInLong() {
		assertEquals(100 + 60, ParryRules.expiresAt(100, 60));
		assertEquals(100 + ParryRules.MIN_FINISHER_TICKS, ParryRules.expiresAt(100, 0));
		assertEquals(100 + ParryRules.MAX_FINISHER_TICKS, ParryRules.expiresAt(100, Integer.MAX_VALUE));
		assertTrue(ParryRules.expiresAt(Integer.MAX_VALUE, Integer.MAX_VALUE) > Integer.MAX_VALUE);
	}

	@Test void aFinisherIsSingleUseAndBelongsToOnePlayer() {
		ParryRules.Book book = new ParryRules.Book();
		book.open(mob, player, 200);
		assertFalse(book.consume(mob, UUID.randomUUID(), 100), "otro jugador no puede gastarlo");
		assertTrue(book.consume(mob, player, 100));
		assertFalse(book.consume(mob, player, 100), "un solo golpe de gracia por parada");
	}

	@Test void aFinisherExpires() {
		ParryRules.Book book = new ParryRules.Book();
		book.open(mob, player, 200);
		assertTrue(book.isOpen(mob, player, 199));
		assertFalse(book.isOpen(mob, player, 200));
		assertFalse(book.consume(mob, player, 500));
	}

	@Test void pruneAndForgetReleaseMemory() {
		ParryRules.Book book = new ParryRules.Book();
		UUID other = UUID.randomUUID();
		book.open(mob, player, 100);
		book.open(other, player, 1000);
		book.prune(150);
		assertEquals(1, book.size());
		book.forget(other);
		assertEquals(0, book.size());
		book.open(mob, player, 100);
		book.clear();
		assertEquals(0, book.size());
	}
}
