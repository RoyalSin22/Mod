package dev.aptitudes.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** V77: localizar las estructuras del mod (Features) con /aptitudes locate. */
class StructureLocateRulesTest {
	@Test void kindsAreTheFiveStructuresOfTheMod() {
		assertEquals(5, StructureLocateRules.KINDS.size());
		for (String k : new String[] {"custodian_temple", "temple", "tower", "bonfire_shrine", "camp"}) assertTrue(StructureLocateRules.isKind(k), k);
		assertFalse(StructureLocateRules.isKind("village"));
		assertFalse(StructureLocateRules.isKind(""));
		assertFalse(StructureLocateRules.isKind(null));
	}

	@Test void ringsCoverEveryChunkExactlyOnceAndSpiralOutwards() {
		assertArrayEquals(new int[][] {{0, 0}}, StructureLocateRules.ring(0));
		java.util.Set<String> seen = new java.util.HashSet<>();
		for (int r = 0; r <= 6; r++) {
			int[][] ring = StructureLocateRules.ring(r);
			assertEquals(r == 0 ? 1 : 8 * r, ring.length, "chunks del anillo " + r);
			for (int[] c : ring) {
				assertEquals(r, Math.max(Math.abs(c[0]), Math.abs(c[1])), "distancia de Chebyshev");
				assertTrue(seen.add(c[0] + "," + c[1]), "sin repetir " + c[0] + "," + c[1]);
			}
		}
		assertEquals(13 * 13, seen.size(), "los anillos 0..6 cubren el cuadrado de 13×13 sin huecos");
	}

	@Test void searchesOnlyWhenNothingIsKnownOrTheKnownOneIsFar() {
		var near = StructureLocateRules.nearest(new long[] {StructureLocateRules.pack(100, 64, 0)}, 0, 0);
		var far = StructureLocateRules.nearest(new long[] {StructureLocateRules.pack(5000, 64, 0)}, 0, 0);
		assertFalse(StructureLocateRules.shouldSearch(near, 600));
		assertTrue(StructureLocateRules.shouldSearch(far, 600));
		assertTrue(StructureLocateRules.shouldSearch(java.util.Optional.empty(), 600));
	}

	@Test void theOldTempleIsNotSearchedBecauseItNoLongerGenerates() {
		assertFalse(StructureLocateRules.searchable("temple"));
		assertTrue(StructureLocateRules.searchable("custodian_temple"));
		assertFalse(StructureLocateRules.searchable("village"));
		assertFalse(StructureLocateRules.searchable(null));
	}

	@Test void designatedChunkStaysInsideItsRegionAndIsDeterministic() {
		for (int rx = -5; rx <= 5; rx++) for (int rz = -5; rz <= 5; rz++) {
			int[] c = StructureLocateRules.designatedChunk(123456789L, "custodian_temple", rx, rz, 32);
			assertEquals(rx, StructureLocateRules.regionOf(c[0], 32), "región x " + rx);
			assertEquals(rz, StructureLocateRules.regionOf(c[1], 32), "región z " + rz);
			assertArrayEquals(c, StructureLocateRules.designatedChunk(123456789L, "custodian_temple", rx, rz, 32));
			assertTrue(StructureLocateRules.isDesignated(123456789L, "custodian_temple", c[0], c[1], 32));
		}
	}

	@Test void exactlyOneChunkPerRegionIsDesignated() {
		long seed = -987654321L;
		int count = 0;
		for (int cx = 32; cx < 64; cx++) for (int cz = -32; cz < 0; cz++) if (StructureLocateRules.isDesignated(seed, "custodian_temple", cx, cz, 32)) count++;
		assertEquals(1, count, "una región de 32×32 chunks, un solo chunk designado");
	}

	@Test void designationVariesWithSeedRegionAndKind() {
		int[] base = StructureLocateRules.designatedChunk(1L, "custodian_temple", 0, 0, 32);
		int different = 0;
		for (long seed = 2; seed < 40; seed++) if (!java.util.Arrays.equals(base, StructureLocateRules.designatedChunk(seed, "custodian_temple", 0, 0, 32))) different++;
		assertTrue(different > 30, "la semilla cambia la posición");
		java.util.Set<String> seen = new java.util.HashSet<>();
		for (int rx = 0; rx < 20; rx++) seen.add(StructureLocateRules.designatedChunk(1L, "custodian_temple", rx, 0, 32)[1] + "");
		assertTrue(seen.size() > 8, "las regiones no repiten patrón");
		assertFalse(java.util.Arrays.equals(base, StructureLocateRules.designatedChunk(1L, "tower", 0, 0, 32)), "otro tipo, otra posición");
	}

	@Test void onlyTheCustodianTempleUsesTheRegionGrid() {
		assertEquals(32, StructureLocateRules.regionSize("custodian_temple"));
		for (String k : new String[] {"temple", "tower", "bonfire_shrine", "camp", "village"}) assertEquals(0, StructureLocateRules.regionSize(k), k);
		assertEquals(-1, StructureLocateRules.regionOf(-1, 32));
		assertEquals(0, StructureLocateRules.regionOf(31, 32));
		assertEquals(1, StructureLocateRules.regionOf(32, 32));
		assertEquals(-2, StructureLocateRules.regionOf(-33, 32));
	}

	@Test void packMatchesTheBlockPosLayout() {
		// BlockPos.asLong: x en los 26 bits altos, z en los 26 siguientes, y en los 12 bajos.
		assertEquals(((long) 10 << 38) | ((long) 20 << 12) | 64, StructureLocateRules.pack(10, 64, 20));
		var hit = StructureLocateRules.nearest(new long[] {StructureLocateRules.pack(-300, 70, 450)}, 0, 0).orElseThrow();
		assertEquals(-300, hit.x());
		assertEquals(450, hit.z());
	}

	@Test void nearestPicksTheClosestOnTheHorizontalPlane() {
		long[] positions = {StructureLocateRules.pack(500, 64, 500), StructureLocateRules.pack(-100, 90, 30), StructureLocateRules.pack(60, 10, -700)};
		var hit = StructureLocateRules.nearest(positions, 0, 0).orElseThrow();
		assertEquals(StructureLocateRules.pack(-100, 90, 30), hit.pos());
		assertEquals(104, hit.distance());
		assertEquals(StructureLocateRules.pack(60, 10, -700), StructureLocateRules.nearest(positions, 40, -650).orElseThrow().pos());
	}

	@Test void nearestOfNothingIsEmptyAndNegativeCoordinatesWork() {
		assertTrue(StructureLocateRules.nearest(new long[0], 0, 0).isEmpty());
		var hit = StructureLocateRules.nearest(new long[] {StructureLocateRules.pack(-5000, 64, -7000)}, -5000, -7000).orElseThrow();
		assertEquals(0, hit.distance());
		assertEquals(-5000, hit.x());
		assertEquals(-7000, hit.z());
	}

	@Test void placementOffsetsSpiralOutwardsAndStayInTheRing() {
		int[][] offsets = StructureLocateRules.placementOffsets(60, 24, 96, 42L);
		assertEquals(60, offsets.length);
		double previous = 0;
		for (int[] o : offsets) {
			double r = Math.hypot(o[0], o[1]);
			assertTrue(r >= 20 && r <= 98, "radio " + r);
			assertTrue(r + 3 >= previous, "de cerca a lejos");
			previous = r;
		}
		assertArrayEquals(offsets[7], StructureLocateRules.placementOffsets(60, 24, 96, 42L)[7], "determinista con la misma semilla");
	}

	@Test void placementOffsetsAreClampedToSaneValues() {
		assertEquals(1, StructureLocateRules.placementOffsets(0, 10, 20, 1L).length);
		assertEquals(200, StructureLocateRules.placementOffsets(99999, 10, 20, 1L).length);
		assertEquals(1, StructureLocateRules.placementOffsets(-5, 50, 10, 1L).length);
	}
}
