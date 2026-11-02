package dev.aptitudes.world;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemporaryBlockScheduleTest {

	@Test
	public void expiresOnlyAfterItsTime() {
		TemporaryBlockSchedule s = new TemporaryBlockSchedule();
		s.add(1L, "minecraft:amethyst_block", 200);
		s.add(2L, "minecraft:amethyst_block", 400);
		assertEquals(0, s.expired(199).size());
		assertEquals(1, s.expired(200).size());
		assertEquals(2, s.expired(1000).size());
	}

	@Test
	public void survivesRestartBecauseTimeIsGameTime() {
		// Servidor cerrado con 100 ticks restantes; al volver, el tiempo de juego continúa desde donde estaba.
		TemporaryBlockSchedule s = new TemporaryBlockSchedule();
		s.add(5L, "minecraft:cobblestone", 10_100);
		assertEquals(0, s.expired(10_000).size()); // justo tras reiniciar: todavía no
		assertEquals(1, s.expired(10_100).size()); // 100 ticks después: se retira
	}

	@Test
	public void removeAndContains() {
		TemporaryBlockSchedule s = new TemporaryBlockSchedule();
		s.add(9L, "minecraft:glowstone", 50);
		assertTrue(s.contains(9L));
		assertEquals("minecraft:glowstone", s.remove(9L).block());
		assertFalse(s.contains(9L));
		assertTrue(s.isEmpty());
	}

	@Test
	public void capIsEnforcedButReplacingSamePosIsAllowed() {
		TemporaryBlockSchedule s = new TemporaryBlockSchedule();
		for (long i = 0; i < TemporaryBlockSchedule.MAX_ENTRIES; i++) assertTrue(s.add(i, "b", 10));
		assertFalse(s.add(-1L, "b", 10));
		assertTrue(s.add(0L, "c", 20)); // misma posición: reemplaza
		assertEquals(TemporaryBlockSchedule.MAX_ENTRIES, s.size());
	}
}
