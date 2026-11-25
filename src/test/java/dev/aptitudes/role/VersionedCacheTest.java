package dev.aptitudes.role;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class VersionedCacheTest {
	private final UUID id = UUID.randomUUID();

	@Test void computesOnceWhileOwnerAndVersionAreTheSame() {
		VersionedCache<String> cache = new VersionedCache<>();
		Object owner = new Object();
		AtomicInteger calls = new AtomicInteger();
		for (int i = 0; i < 5; i++) assertEquals("v", cache.get(id, owner, 1, () -> { calls.incrementAndGet(); return "v"; }));
		assertEquals(1, calls.get());
	}

	@Test void recomputesWhenTheVersionChanges() {
		VersionedCache<Integer> cache = new VersionedCache<>();
		Object owner = new Object();
		assertEquals(1, cache.get(id, owner, 1, () -> 1));
		assertEquals(2, cache.get(id, owner, 2, () -> 2));
		assertEquals(2, cache.get(id, owner, 2, () -> 99));
	}

	@Test void recomputesWhenTheOwnerObjectIsReplaced() {
		VersionedCache<Integer> cache = new VersionedCache<>();
		assertEquals(1, cache.get(id, new Object(), 0, () -> 1));
		// Mismo número de versión pero otro objeto (reinicio o copia al morir): no puede devolver el valor viejo.
		assertEquals(2, cache.get(id, new Object(), 0, () -> 2));
	}

	@Test void playersDoNotShareEntriesAndForgetReleasesMemory() {
		VersionedCache<Integer> cache = new VersionedCache<>();
		Object owner = new Object();
		UUID other = UUID.randomUUID();
		assertEquals(1, cache.get(id, owner, 0, () -> 1));
		assertEquals(2, cache.get(other, owner, 0, () -> 2));
		assertEquals(2, cache.size());
		cache.forget(id);
		assertEquals(1, cache.size());
		assertEquals(3, cache.get(id, owner, 0, () -> 3));
	}
}
