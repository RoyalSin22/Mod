package dev.aptitudes.social;

import dev.aptitudes.social.GroundMessageRules.PlaceResult;
import dev.aptitudes.social.GroundMessageRules.Spot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** C7 (S5): reglas de las notas en el suelo. */
class GroundMessageRulesTest {
	@Test
	void placementRespectsSwitchSpacingAndChunkCap() {
		List<Spot> none = List.of();
		assertEquals(PlaceResult.OK, GroundMessageRules.canPlace(true, 0, 64, 0, none, 2, 0, 4));
		assertEquals(PlaceResult.DISABLED, GroundMessageRules.canPlace(false, 0, 64, 0, none, 2, 0, 4));
		assertEquals(PlaceResult.TOO_CLOSE, GroundMessageRules.canPlace(true, 0, 64, 0, List.of(new Spot(1, 64, 0)), 2, 1, 4));
		assertEquals(PlaceResult.OK, GroundMessageRules.canPlace(true, 0, 64, 0, List.of(new Spot(2, 64, 0)), 2, 1, 4), "a 2 bloques justos");
		assertEquals(PlaceResult.TOO_CLOSE, GroundMessageRules.canPlace(true, 0, 64, 0, List.of(new Spot(0, 64, 0)), 0, 1, 4), "mismo bloque, aunque no haya espaciado");
		assertEquals(PlaceResult.CHUNK_FULL, GroundMessageRules.canPlace(true, 0, 64, 0, none, 2, 4, 4));
	}

	@Test
	void oldestNotesAreEvictedAtTheCap() {
		assertEquals(0, GroundMessageRules.evictBeforeAdding(9, 10));
		assertEquals(1, GroundMessageRules.evictBeforeAdding(10, 10));
		assertEquals(3, GroundMessageRules.evictBeforeAdding(12, 10), "si se baja el tope, se recorta al añadir");
		assertEquals(1, GroundMessageRules.evictBeforeAdding(1, 0), "tope mínimo 1");
	}

	@Test
	void notesExpireAfterTheirLifetime() {
		long day = GroundMessageRules.DAY_TICKS;
		assertFalse(GroundMessageRules.expired(0, 30 * day - 1, 30));
		assertTrue(GroundMessageRules.expired(0, 30 * day, 30));
		assertFalse(GroundMessageRules.expired(0, 1000 * day, 0), "0 = no caduca");
		assertFalse(GroundMessageRules.expired(5000, 100, 30), "reloj hacia atrás");
		assertEquals(-1, GroundMessageRules.day(-1));
		assertEquals(2, GroundMessageRules.day(2 * day + 5));
	}

	@Test
	void ratingAndDeletingNeedTheRightPersonAndDistance() {
		assertTrue(GroundMessageRules.canRate(false, false, 25));
		assertFalse(GroundMessageRules.canRate(true, false, 1), "la propia");
		assertFalse(GroundMessageRules.canRate(false, true, 1), "dos veces");
		assertFalse(GroundMessageRules.canRate(false, false, 37), "lejos (más de 6 bloques)");
		assertTrue(GroundMessageRules.canDelete(true, false, 1));
		assertTrue(GroundMessageRules.canDelete(false, true, 1));
		assertFalse(GroundMessageRules.canDelete(false, false, 1));
		assertFalse(GroundMessageRules.canDelete(true, false, 100));
	}

	@Test
	void rewardsHaveAntiFarmBrakes() {
		long ok = GroundMessageRules.MIN_REWARD_AGE_TICKS;
		assertTrue(GroundMessageRules.rewards(0, false, false, ok, 5));
		assertTrue(GroundMessageRules.rewards(4, false, false, ok, 5));
		assertFalse(GroundMessageRules.rewards(5, false, false, ok, 5), "tope diario");
		assertFalse(GroundMessageRules.rewards(0, true, false, ok, 5), "misma persona el mismo día");
		assertFalse(GroundMessageRules.rewards(0, false, true, ok, 5), "mismo grupo");
		assertFalse(GroundMessageRules.rewards(0, false, false, ok - 1, 5), "nota de menos de un minuto");
		assertFalse(GroundMessageRules.rewards(0, false, false, ok, 0), "recompensas desactivadas");
	}

	@Test
	void glowGrowsAndSaturates() {
		assertEquals(0.35f, GroundMessageRules.glow(0), 1e-6);
		assertEquals(1.0f, GroundMessageRules.glow(10), 1e-6);
		assertEquals(1.0f, GroundMessageRules.glow(500), 1e-6);
		assertEquals(0.35f, GroundMessageRules.glow(-3), 1e-6);
		assertTrue(GroundMessageRules.glow(1) < GroundMessageRules.glow(4));
	}
}
