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
