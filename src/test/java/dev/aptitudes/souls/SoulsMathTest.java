package dev.aptitudes.souls;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SoulsMathTest {
	@Test
	public void levelsForPointsRespectsPointsPerLevel() {
		assertEquals(0, SoulsMath.levelsForPoints(0, 0, 3));
		assertEquals(3, SoulsMath.levelsForPoints(0, 1, 3));   // de 0 a 3
		assertEquals(1, SoulsMath.levelsForPoints(2, 1, 3));   // de 2 a 3: ya llevaba 2 niveles hacia el punto
		assertEquals(6, SoulsMath.levelsForPoints(0, 2, 3));
		assertEquals(1, SoulsMath.levelsForPoints(0, 1, 0));   // per <= 0 se trata como 1 (sin división por cero)
	}

	@Test
	public void levelCostGrowsAndIsCapped() {
		assertEquals(1, SoulsMath.levelCost(0, 1, 6, 10));
		assertEquals(2, SoulsMath.levelCost(6, 1, 6, 10));
		assertEquals(10, SoulsMath.levelCost(1000, 1, 6, 10));
		assertEquals(6, SoulsMath.levelCost(5, 1, 0, 10));     // divisor 0 se trata como 1 (sin división por cero)
		int previous = 0;
		for (int level = 0; level < 500; level++) {
			int cost = SoulsMath.levelCost(level, 1, 6, 10);
			assertTrue(cost >= previous);
			previous = cost;
		}
	}

	@Test
	public void validRequestAcceptsNormalRequests() {
		assertEquals(5, SoulsMath.validRequestTotal(new int[]{2, 3, 0}, new int[]{10, 0, 99}, 99));
	}

	@Test
	public void validRequestRejectsInvalidInput() {
		assertEquals(-1, SoulsMath.validRequestTotal(new int[]{-1, 2}, new int[]{0, 0}, 99));   // negativo
		assertEquals(-1, SoulsMath.validRequestTotal(new int[]{1}, new int[]{99}, 99));        // supera el máximo
		assertEquals(-1, SoulsMath.validRequestTotal(new int[]{0, 0}, new int[]{0, 0}, 99));   // nada que hacer
		assertEquals(-1, SoulsMath.validRequestTotal(new int[]{1, 1}, new int[]{0}, 99));      // longitudes distintas
		assertEquals(-1, SoulsMath.validRequestTotal(null, new int[]{0}, 99));
	}

	/**
	 * Regresión del fallo de seguridad: con int, "asignado + n" desbordaba a negativo y pasaba el control.
	 * Con {MAX, MAX, 50} el total int era −2 + 50 = 48 (aceptado) y los dos atributos desbordados acababan
	 * a 0 (setAllocated limita negativos), devolviendo sus puntos: reasignación gratuita.
	 */
	@Test
	public void validRequestRejectsOverflowAttack() {
		int[] requested = {Integer.MAX_VALUE, Integer.MAX_VALUE, 50};
		int[] allocated = {10, 10, 0};
		// La comprobación anterior (int) lo aceptaba:
		int oldTotal = 0;
		boolean oldAccepted = true;
		for (int i = 0; i < requested.length; i++) {
			if (requested[i] < 0 || allocated[i] + requested[i] > 99) oldAccepted = false;
			oldTotal += requested[i];
		}
		assertTrue("la validación anterior aceptaba el ataque", oldAccepted && oldTotal == 48);
		// La nueva lo rechaza:
		assertEquals(-1, SoulsMath.validRequestTotal(requested, allocated, 99));
		assertEquals(-1, SoulsMath.validRequestTotal(new int[]{Integer.MAX_VALUE}, new int[]{10}, 99));
	}
}
