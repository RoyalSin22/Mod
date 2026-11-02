package dev.aptitudes.morph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MorphSizeRulesTest {

	@Test
	public void smallMobKeepsItsSize() {
		float[] bat = MorphSizeRules.clamp(0.5f, 0.9f, 0.45f);
		assertEquals(0.5f, bat[0], 1e-6);
		assertEquals(0.9f, bat[1], 1e-6);
		assertEquals(0.45f, bat[2], 1e-6);
	}

	@Test
	public void hugeMobIsCappedSoThePlayerDoesNotGetStuck() {
		float[] ravager = MorphSizeRules.clamp(1.95f, 2.2f, 2.1f);
		assertEquals(MorphSizeRules.MAX_WIDTH, ravager[0], 1e-6);
		float[] ghast = MorphSizeRules.clamp(4f, 4f, 2.6f);
		assertEquals(MorphSizeRules.MAX_HEIGHT, ghast[1], 1e-6);
		assertTrue(ghast[2] <= ghast[1] - 0.05f + 1e-6);
	}

	@Test
	public void tinyAndInvalidValuesUseTheMinimums() {
		float[] endermite = MorphSizeRules.clamp(0.1f, 0.1f, 0.05f);
		assertEquals(MorphSizeRules.MIN_WIDTH, endermite[0], 1e-6);
		assertEquals(MorphSizeRules.MIN_HEIGHT, endermite[1], 1e-6);
		assertEquals(MorphSizeRules.MIN_EYE, endermite[2], 1e-6);
		float[] broken = MorphSizeRules.clamp(Float.NaN, Float.POSITIVE_INFINITY, -1f);
		assertEquals(MorphSizeRules.MIN_WIDTH, broken[0], 1e-6);
		assertEquals(MorphSizeRules.MIN_HEIGHT, broken[1], 1e-6);
		assertEquals(MorphSizeRules.MIN_EYE, broken[2], 1e-6);
	}
}
