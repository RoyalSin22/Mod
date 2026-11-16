package dev.aptitudes.temple;

import dev.aptitudes.temple.CryptRules.HatchUse;
import dev.aptitudes.temple.CryptRules.Stage;
import dev.aptitudes.temple.CryptRules.Variant;
import org.junit.jupiter.api.Test;

import static dev.aptitudes.temple.CryptRules.*;
import static org.junit.jupiter.api.Assertions.*;

/** V69: reglas puras de la Cripta de la Niebla (etapa interna del Templo del Custodio). */
class CryptRulesTest {
	@Test void stageOrderIsStableBecauseItIsSavedAsAnOrdinal() {
		assertArrayEquals(new Stage[] {Stage.LOCKED, Stage.READY, Stage.OPEN, Stage.WAVE_1, Stage.WAVE_2, Stage.WAVE_3,
				Stage.CLIMAX, Stage.DONE}, Stage.values());
	}

	@Test void unknownOrdinalsMeanLocked() {
		assertEquals(Stage.OPEN, stageFromOrdinal(2));
		assertEquals(Stage.LOCKED, stageFromOrdinal(-1));
		assertEquals(Stage.LOCKED, stageFromOrdinal(99));
		assertEquals(Stage.LOCKED, stageFromOrdinal(Integer.MIN_VALUE));
	}

	// ------------------------------------------------------------------------------------------------- Flujo

	@Test void cryptOnlyUnlocksWhenTheCustodianFalls() {
		assertEquals(Stage.READY, afterCustodianCleared(Stage.LOCKED));
		assertEquals(Stage.OPEN, afterCustodianCleared(Stage.OPEN), "no retrocede ni se repite");
		assertEquals(Stage.DONE, afterCustodianCleared(Stage.DONE));
	}

	@Test void fullPathGoesThroughEveryStageInOrder() {
		Stage s = afterCustodianCleared(Stage.LOCKED);
		s = afterKeyUsed(s);
		assertEquals(Stage.OPEN, s);
		s = afterPlayerEntered(s);
		assertEquals(Stage.WAVE_1, s);
		s = afterWaveCleared(s);
		assertEquals(Stage.WAVE_2, s);
		s = afterWaveCleared(s);
		assertEquals(Stage.WAVE_3, s);
		s = afterWaveCleared(s);
		assertEquals(Stage.CLIMAX, s);
		s = afterClimaxDown(s);
		assertEquals(Stage.DONE, s);
	}

	@Test void transitionsOutOfOrderChangeNothing() {
		assertEquals(Stage.LOCKED, afterKeyUsed(Stage.LOCKED));
		assertEquals(Stage.READY, afterPlayerEntered(Stage.READY));
		assertEquals(Stage.OPEN, afterWaveCleared(Stage.OPEN));
		assertEquals(Stage.WAVE_2, afterClimaxDown(Stage.WAVE_2));
		assertEquals(Stage.DONE, afterWaveCleared(Stage.DONE));
	}

	@Test void fightingStagesAreTheWavesAndTheClimax() {
		for (Stage s : Stage.values()) {
			boolean expected = s == Stage.WAVE_1 || s == Stage.WAVE_2 || s == Stage.WAVE_3 || s == Stage.CLIMAX;
			assertEquals(expected, isFighting(s), s.name());
		}
	}

	@Test void waveIndexIsOneToThreeAndZeroOtherwise() {
		assertEquals(0, waveIndex(Stage.READY));
		assertEquals(1, waveIndex(Stage.WAVE_1));
		assertEquals(2, waveIndex(Stage.WAVE_2));
		assertEquals(3, waveIndex(Stage.WAVE_3));
		assertEquals(0, waveIndex(Stage.CLIMAX));
	}

	// ------------------------------------------------------------------------------------------- Trampilla

	@Test void hatchOnlyOpensWithTheKeyOnceTheTempleIsCleared() {
		assertEquals(HatchUse.OPEN, useHatch(Stage.READY, true, true));
		assertEquals(HatchUse.NEEDS_KEY, useHatch(Stage.READY, false, true));
		assertEquals(HatchUse.NOT_READY, useHatch(Stage.LOCKED, true, true));
		assertEquals(HatchUse.ALREADY_OPEN, useHatch(Stage.OPEN, true, true));
		assertEquals(HatchUse.ALREADY_OPEN, useHatch(Stage.WAVE_2, true, true));
		assertEquals(HatchUse.ALREADY_OPEN, useHatch(Stage.DONE, true, true));
	}

	@Test void hatchOfATempleWithoutCryptDoesNothing() {
		assertEquals(HatchUse.NO_CRYPT, useHatch(Stage.READY, true, false));
		assertEquals(HatchUse.NO_CRYPT, useHatch(Stage.LOCKED, false, false));
	}

	// ----------------------------------------------------------------------------------------- Tamaño de oleada

	@Test void waveSizeFollowsTheBaseAndGrowsWithPlayersAndRing() {
		assertEquals(5, waveSize(1, 1, 0, 100));
		assertEquals(7, waveSize(2, 1, 0, 100));
		assertEquals(9, waveSize(3, 1, 0, 100));
		assertEquals(7, waveSize(1, 2, 0, 100), "+2 por jugador adicional");
		assertEquals(11, waveSize(1, 4, 0, 100));
		assertEquals(6, waveSize(1, 1, 1, 100), "x(1 + 0,25 x anillo)");
		assertEquals(11, waveSize(1, 1, 5, 100));
	}

	@Test void waveSizeScalesWithThePercentAndIsAlwaysSafe() {
		assertEquals(3, waveSize(1, 1, 0, 50));
		assertEquals(10, waveSize(1, 1, 0, 200));
		assertEquals(1, waveSize(1, 1, 0, 25), "nunca baja de 1");
		assertEquals(1, waveSize(1, 1, 0, -300), "porcentaje acotado");
		assertEquals(15, waveSize(1, 1, 0, 9999), "porcentaje acotado a 300");
	}

	@Test void waveSizeIgnoresNonsenseInputs() {
		assertEquals(5, waveSize(0, 1, 0, 100), "oleada fuera de rango se toma como la primera");
		assertEquals(9, waveSize(9, 1, 0, 100), "oleada fuera de rango se toma como la última");
		assertEquals(5, waveSize(1, 0, 0, 100));
		assertEquals(5, waveSize(1, -3, 0, 100));
		assertEquals(waveSize(1, 6, 5, 300), waveSize(1, 600, 5, 300), "los jugadores se acotan");
		assertTrue(waveSize(3, 600, 99, 99999) <= MAX_WAVE_SIZE);
	}

	@Test void trialWavesAreSoftAndAlwaysBelowTheCryptOnes() {
		assertEquals(4, trialWaveSize(1, 1, 0, 100));
		assertEquals(6, trialWaveSize(2, 1, 0, 100));
		assertEquals(6, trialWaveSize(2, 1, 0, 100));
		assertEquals(8, trialWaveSize(1, 3, 0, 100), "+2 por jugador adicional");
		assertEquals(5, trialWaveSize(1, 1, 1, 100));
		assertEquals(6, trialWaveSize(9, 1, 0, 100), "oleada fuera de rango = la última de las dos");
		assertEquals(4, trialWaveSize(-1, 0, 0, 100));
		assertTrue(trialWaveSize(3, 600, 99, 99999) <= MAX_WAVE_SIZE);
		for (int ring = 0; ring <= 5; ring++) for (int players = 1; players <= 6; players++) {
			assertTrue(trialWaveSize(2, players, ring, 100) <= waveSize(2, players, ring, 100), "ring " + ring + " jugadores " + players);
			assertTrue(trialWaveSize(1, players, ring, 100) < waveSize(3, players, ring, 100));
		}
	}

	@Test void climaxHasTheBossLikeScalingAndTwoEscorts() {
		assertEquals(0.0, climaxHealthBonus(0));
		assertEquals(0.25, climaxHealthBonus(1), 1e-9);
		assertEquals(1.25, climaxHealthBonus(5), 1e-9);
		assertEquals(1.25, climaxHealthBonus(50), 1e-9);
		assertEquals(0.0, climaxHealthBonus(-4));
		assertEquals(2, ESCORTS);
	}

	// ------------------------------------------------------------------------------------- Oleada en curso

	@Test void neverMoreThanTheCapAreAliveAtOnce() {
		assertEquals(12, toSpawn(20, 0));
		assertEquals(4, toSpawn(20, 8));
		assertEquals(0, toSpawn(20, 12));
		assertEquals(0, toSpawn(20, 30));
		assertEquals(3, toSpawn(3, 0));
		assertEquals(0, toSpawn(0, 0));
		assertEquals(0, toSpawn(-5, 0));
		assertEquals(12, toSpawn(20, -3), "vivas negativas se toman como cero");
	}

	@Test void waveEndsOnlyWhenNothingIsLeftToSpawnAndNothingIsAlive() {
		assertTrue(waveCleared(0, 0));
		assertFalse(waveCleared(1, 0));
		assertFalse(waveCleared(0, 1));
		assertFalse(waveCleared(5, 5));
		assertTrue(waveCleared(-1, -1));
	}

	@Test void variantsFollowTheDesignedMix() {
		int errante = 0, osario = 0, vanilla = 0;
		for (int roll = 0; roll < 100; roll++) {
			switch (choose(roll)) {
				case ERRANTE -> errante++;
				case OSARIO -> osario++;
				case VANILLA -> vanilla++;
			}
		}
		assertEquals(30, errante);
		assertEquals(25, osario);
		assertEquals(45, vanilla);
		assertEquals(Variant.ERRANTE, choose(0));
		assertEquals(Variant.VANILLA, choose(99));
		assertEquals(Variant.ERRANTE, choose(-7));
		assertEquals(Variant.VANILLA, choose(1234));
	}

	// ------------------------------------------------------------------------------- Inactividad y reinicio

	@Test void waveResetsAfterTheIdleTime() {
		assertFalse(shouldResetWave(599, 30));
		assertTrue(shouldResetWave(600, 30));
		assertTrue(shouldResetWave(100_000, 30));
		assertFalse(shouldResetWave(0, 30));
	}

	@Test void idleTimeIsClampedToTheDesignedRange() {
		assertFalse(shouldResetWave(99, 5), "5 s = 100 ticks");
		assertTrue(shouldResetWave(100, 5));
		assertTrue(shouldResetWave(100, -10), "mínimo 5 s");
		assertFalse(shouldResetWave(5999, 9999), "máximo 300 s = 6000 ticks");
		assertTrue(shouldResetWave(6000, 9999));
	}

	@Test void idleTicksNeverGoNegative() {
		assertFalse(shouldResetWave(-1, 30));
		assertFalse(shouldResetWave(Long.MIN_VALUE, 30));
	}

	@Test void onlyAFightingTempleCountsIdleTicks() {
		assertTrue(countsIdle(Stage.WAVE_2));
		assertTrue(countsIdle(Stage.CLIMAX));
		assertFalse(countsIdle(Stage.OPEN));
		assertFalse(countsIdle(Stage.DONE));
	}
}
