package dev.aptitudes.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComboCounterTest {
	private static final long WINDOW = 50; // 2,5 s

	@Test
	public void hitsInsideTheWindowChainAndExpireAfterIt() {
		ComboCounter c = new ComboCounter();
		assertEquals(1, c.hit(0, WINDOW));
		assertEquals(2, c.hit(40, WINDOW));
		assertEquals(3, c.hit(90, WINDOW));
		assertEquals(3, c.count(140, WINDOW));
		assertEquals(0, c.count(141, WINDOW)); // pasó la ventana
		assertEquals(1, c.hit(200, WINDOW));
	}

	@Test
	public void finisherNeedsThresholdAndRespectsItsCooldown() {
		ComboCounter c = new ComboCounter();
		for (int i = 0; i < 4; i++) c.hit(i * 10, WINDOW);
		assertEquals(0, c.tryFinisher(35, WINDOW, 5, 80)); // solo 4 golpes
		c.hit(40, WINDOW);
		assertEquals(5, c.tryFinisher(45, WINDOW, 5, 80)); // remate con 5
		assertEquals(0, c.count(45, WINDOW));              // el combo se consume
		for (int i = 0; i < 5; i++) c.hit(80 + i * 5, WINDOW); // golpes en 80..100
		assertEquals(0, c.tryFinisher(100, WINDOW, 5, 80)); // espera de remate (80 desde 45) aún no cumplida
		assertEquals(5, c.tryFinisher(126, WINDOW, 5, 80)); // combo vigente (26 < 50) y espera cumplida
	}

	@Test
	public void bonusGrowsPerHitAndIsCapped() {
		assertEquals(0.0, ComboCounter.bonus(0, 0.03, 0.15), 1e-9);
		assertEquals(0.09, ComboCounter.bonus(3, 0.03, 0.15), 1e-9);
		assertEquals(0.15, ComboCounter.bonus(40, 0.03, 0.15), 1e-9);
		assertEquals(0.0, ComboCounter.bonus(5, Double.NaN, 0.15), 1e-9);
	}

	@Test
	public void countIsBoundedAndResetClears() {
		ComboCounter c = new ComboCounter();
		for (int i = 0; i < 500; i++) c.hit(i, WINDOW);
		assertEquals(ComboCounter.MAX_COUNT, c.count(499, WINDOW));
		c.reset();
		assertEquals(0, c.count(499, WINDOW));
	}
}
