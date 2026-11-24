package dev.aptitudes.logic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** C2: invalidación de la caché de Sendas y piedras angulares por número de versión. */
class VersionedCacheTest {
	/** Imita a {@code RoleData}: un dato, un contador que sube en cada mutación y una caché derivada. */
	private static final class Owner {
		long version;
		int rank;
		final VersionedCache<Integer> derived = new VersionedCache<>();
		final AtomicInteger computations = new AtomicInteger();

		void setRank(int value) {
			rank = value;
			version++;
		}

		int derived() {
			return derived.get(version, this, o -> {
				o.computations.incrementAndGet();
				return o.rank * 10;
			});
		}
	}

	@Test
	void computesOnceWhileTheVersionDoesNotChange() {
		Owner o = new Owner();
		o.setRank(2);
		assertEquals(20, o.derived());
		assertEquals(20, o.derived());
		assertEquals(20, o.derived());
		assertEquals(1, o.computations.get());
	}

	@Test
	void everyMutationInvalidates() {
		Owner o = new Owner();
		assertEquals(0, o.derived());
		o.setRank(3);
		assertEquals(30, o.derived());
		o.setRank(0); // olvidar el nodo (reinicio)
		assertEquals(0, o.derived());
		assertEquals(3, o.computations.get());
	}

	@Test
	void aNewOwnerStartsEmpty() {
		// Muerte con copyOnDeath o reinicio completo: el jugador recibe otro objeto de datos, con su propia caché.
		Owner before = new Owner();
		before.setRank(4);
		assertEquals(40, before.derived());
		Owner after = new Owner();
		after.setRank(4);
		assertFalse(after.derived.isValidFor(after.version));
		assertEquals(40, after.derived());
		assertEquals(1, after.computations.get());
	}

	@Test
	void sameVersionReturnsTheSameInstance() {
		VersionedCache<Object> cache = new VersionedCache<>();
		Object first = cache.get(7, null, c -> new Object());
		assertSame(first, cache.get(7, null, c -> new Object()));
		assertTrue(cache.isValidFor(7));
		assertFalse(cache.isValidFor(8));
	}

	@Test
	void versionZeroIsNotMistakenForFilled() {
		VersionedCache<String> cache = new VersionedCache<>();
		assertFalse(cache.isValidFor(0));
		assertEquals("a", cache.get(0, "a", c -> c));
		assertTrue(cache.isValidFor(0));
	}

	@Test
	void cachesNullResultsWithoutRecomputing() {
		VersionedCache<String> cache = new VersionedCache<>();
		AtomicInteger calls = new AtomicInteger();
		assertNull(cache.get(1, calls, c -> { c.incrementAndGet(); return null; }));
		assertNull(cache.get(1, calls, c -> { c.incrementAndGet(); return null; }));
		assertEquals(1, calls.get());
	}

	@Test
	void invalidateForcesARecomputation() {
		VersionedCache<Integer> cache = new VersionedCache<>();
		AtomicInteger calls = new AtomicInteger();
		cache.get(5, calls, AtomicInteger::incrementAndGet);
		cache.invalidate();
		assertFalse(cache.isValidFor(5));
		assertEquals(2, cache.get(5, calls, AtomicInteger::incrementAndGet));
	}
}
