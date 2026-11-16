package dev.aptitudes.guide;

import static org.junit.jupiter.api.Assertions.*;

import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LoreStateTest {
	@Test
	void emptyStateInitializes() {
		LoreState state = new LoreState();
		assertFalse(state.isUnlocked("any_id"));
	}

	@Test
	void unlock() {
		LoreState state = new LoreState().unlock("test_lore");
		assertTrue(state.isUnlocked("test_lore"));
		assertFalse(state.isUnlocked("other_lore"));
	}

	@Test
	void unlockIdempotent() {
		LoreState state = new LoreState();
		LoreState once = state.unlock("test");
		LoreState twice = once.unlock("test");
		assertEquals(once, twice);
	}

	@Test
	void unlockAll() {
		LoreState state = new LoreState().unlockAll(Set.of("a", "b", "c"));
		assertTrue(state.isUnlocked("a"));
		assertTrue(state.isUnlocked("b"));
		assertTrue(state.isUnlocked("c"));
		assertFalse(state.isUnlocked("d"));
	}

	@Test
	void codecRoundtrip() {
		LoreState original = new LoreState().unlock("lore1").unlock("lore2");
		var encoded = LoreState.CODEC.encodeStart(JsonOps.INSTANCE, original).result().orElseThrow();
		var decoded = LoreState.CODEC.parse(JsonOps.INSTANCE, encoded).result().orElseThrow();

		assertEquals(original, decoded);
		assertTrue(decoded.isUnlocked("lore1"));
		assertTrue(decoded.isUnlocked("lore2"));
		assertFalse(decoded.isUnlocked("lore3"));
	}

	@Test
	void immutable() {
		LoreState state1 = new LoreState().unlock("test");
		LoreState state2 = state1.unlock("other");
		assertFalse(state1.isUnlocked("other"));
		assertTrue(state2.isUnlocked("other"));
	}
}
