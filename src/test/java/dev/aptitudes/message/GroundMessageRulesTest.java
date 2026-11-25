package dev.aptitudes.message;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static dev.aptitudes.message.GroundMessageRules.*;
import static org.junit.jupiter.api.Assertions.*;

class GroundMessageRulesTest {
	private final UUID author = UUID.randomUUID();
	private final UUID other = UUID.randomUUID();
	private final UUID id = UUID.randomUUID();

	@Test void onlyPredefinedPhrasesAreValid() {
		assertTrue(validPhrase("danger"));
		assertFalse(validPhrase("texto libre"));
		assertFalse(validPhrase(""));
		assertFalse(validPhrase(null));
		assertEquals(PHRASES.size(), new HashSet<>(PHRASES).size(), "sin ids repetidos");
		for (String p : PHRASES) assertTrue(p.matches("[a-z_]+"), "id seguro para clave de idioma: " + p);
	}

	@Test void perPlayerLimit() {
		assertTrue(underLimit(0, 3));
		assertTrue(underLimit(2, 3));
		assertFalse(underLimit(3, 3));
		assertFalse(underLimit(0, 0));
		assertFalse(underLimit(0, -4));
	}

	@Test void cooldownIsComputedInLong() {
		assertTrue(cooldownReady(-1, 0, 30));
		assertFalse(cooldownReady(100, 100 + 30 * 20 - 1, 30));
		assertTrue(cooldownReady(100, 100 + 30 * 20, 30));
		assertTrue(cooldownReady(500, 100, 30), "reloj anómalo no bloquea");
		assertFalse(cooldownReady(0, 1, Integer.MAX_VALUE));
	}

	@Test void messagesExpire() {
		assertFalse(expired(0, TICKS_PER_DAY * 7 - 1, 7));
		assertTrue(expired(0, TICKS_PER_DAY * 7, 7));
		assertFalse(expired(0, TICKS_PER_DAY - 1, 0), "mínimo un día");
	}

	@Test void aPlayerCannotVoteTwiceNorForTheirOwnNote() {
		Book book = new Book();
		book.place(id, author, "danger", 10);
		assertEquals(VoteResult.OWN, book.vote(id, author));
		assertEquals(VoteResult.OK, book.vote(id, other));
		assertEquals(VoteResult.ALREADY, book.vote(id, other));
		assertEquals(VoteResult.UNKNOWN, book.vote(UUID.randomUUID(), other));
		assertEquals(1, book.get(id).votes());
	}

	@Test void countsPerAuthorAndRemembersTheLastPlacement() {
		Book book = new Book();
		assertEquals(-1, book.lastPlacedBy(author));
		book.place(id, author, "rest", 40);
		book.place(UUID.randomUUID(), author, "hidden", 90);
		book.place(UUID.randomUUID(), other, "boss", 95);
		assertEquals(2, book.countBy(author));
		assertEquals(90, book.lastPlacedBy(author));
		book.remove(id);
		assertEquals(1, book.countBy(author));
		book.forgetPlayer(author);
		assertEquals(-1, book.lastPlacedBy(author));
	}

	@Test void expiredIdsAreListedButNotRemoved() {
		Book book = new Book();
		book.place(id, author, "trap", 0);
		UUID fresh = UUID.randomUUID();
		book.place(fresh, author, "trap", TICKS_PER_DAY * 6);
		assertEquals(java.util.List.of(id), book.expiredIds(TICKS_PER_DAY * 7, 7));
		assertTrue(book.contains(id));
	}

	@Test void restoredVotersAreCapped() {
		Entry e = new Entry(author, "danger", 0);
		for (int i = 0; i < MAX_VOTERS + 50; i++) e.restoreVoter(UUID.randomUUID());
		assertEquals(MAX_VOTERS, e.votes());
	}
}
