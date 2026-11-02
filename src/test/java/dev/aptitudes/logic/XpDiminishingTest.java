package dev.aptitudes.logic;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class XpDiminishingTest {
	private static final UUID A = new UUID(0, 1);
	private static final UUID B = new UUID(0, 2);

	@Test
	public void multiplierIsFullUpToThresholdThenDecaysToFloor() {
		assertEquals(1.0, XpDiminishing.multiplier(1, 20, 0.1), 1e-9);
		assertEquals(1.0, XpDiminishing.multiplier(20, 20, 0.1), 1e-9);
		assertEquals(0.5, XpDiminishing.multiplier(40, 20, 0.1), 1e-9);
		assertEquals(0.1, XpDiminishing.multiplier(1000, 20, 0.1), 1e-9);
		assertEquals(1.0, XpDiminishing.multiplier(1000, 0, 0.1), 1e-9); // umbral 0 = desactivado
		assertEquals(0.2, XpDiminishing.multiplier(100, 20, -5), 1e-9);  // suelo inválido se acota a [0,1]
	}

	@Test
	public void applyRoundsAndNeverGoesNegative() {
		assertEquals(5, XpDiminishing.apply(10, 0.5));
		assertEquals(1, XpDiminishing.apply(10, 0.1));
		assertEquals(0, XpDiminishing.apply(-3, 1.0));
		assertEquals(10, XpDiminishing.apply(10, 7.0)); // multiplicador > 1 no da XP extra
	}

	@Test
	public void windowSlidesAndSourcesArePerPlayerAndPerSource() {
		XpDiminishing d = new XpDiminishing();
		for (int i = 0; i < 5; i++) d.record(A, "zombie", i, 100);
		assertEquals(6, d.record(A, "zombie", 5, 100));
		assertEquals(1, d.record(A, "skeleton", 5, 100)); // otra fuente
		assertEquals(1, d.record(B, "zombie", 5, 100));   // otro jugador
		assertEquals(2, d.record(A, "zombie", 104, 100)); // salen los eventos con tick <= 4
	}

	@Test
	public void farmLosesMostXpButNormalPlayKeepsAll() {
		XpDiminishing d = new XpDiminishing();
		int normal = 0;
		for (int i = 0; i < 10; i++) normal += d.scale(A, "zombie", 10, i * 120L, 1200, 20, 0.1);
		assertEquals(100, normal); // 10 muertes por minuto: sin penalización

		int farm = 0;
		for (int i = 0; i < 200; i++) farm += d.scale(B, "zombie", 10, i * 6L, 1200, 20, 0.1);
		// 200 muertes en un minuto: muy por debajo de 2000 pero nunca 0
		assertEquals(true, farm < 1000 && farm > 200); // < 50 % de lo que daría sin reducción
	}

	@Test
	public void memoryIsBoundedAndForgetClears() {
		XpDiminishing d = new XpDiminishing();
		int count = 0;
		for (int i = 0; i < 5000; i++) count = d.record(A, "afk", i, 1_000_000);
		assertEquals(XpDiminishing.MAX_EVENTS_PER_SOURCE, count);
		d.forget(A);
		assertEquals(0, d.trackedPlayers());
	}
}
