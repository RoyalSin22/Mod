package dev.aptitudes.ability;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CooldownBookTest {

	@Test
	public void cooldownCountsDownAndExpires() {
		CooldownBook book = new CooldownBook();
		book.start("ultimate.mage", 1000, 3600);
		assertEquals(3600, book.remaining("ultimate.mage", 1000));
		assertEquals(100, book.remaining("ultimate.mage", 4500));
		assertTrue(book.ready("ultimate.mage", 4600));
		assertEquals(0, book.size()); // la entrada cumplida se elimina al leerla
	}

	@Test
	public void survivesSaveAndLoad() {
		CooldownBook book = new CooldownBook();
		book.start("fireball", 50_000, 400);
		// Simula guardar al desconectar y cargar tras reiniciar el servidor (el tiempo de juego continúa).
		CooldownBook loaded = new CooldownBook(book.raw());
		assertEquals(300, loaded.remaining("fireball", 50_100));
	}

	@Test
	public void tryStartOnlyWhenReady() {
		CooldownBook book = new CooldownBook();
		assertTrue(book.tryStart("crimson", 0, 6000));
		assertFalse(book.tryStart("crimson", 5999, 6000));
		assertTrue(book.tryStart("crimson", 6000, 6000));
	}

	@Test
	public void corruptOrImpossibleValuesNeverLockForever() {
		CooldownBook book = new CooldownBook(Map.of("x", Long.MAX_VALUE));
		assertEquals(0, book.remaining("x", 10)); // espera > 1 día: se descarta
		book.start("y", 0, Long.MAX_VALUE);
		assertEquals(CooldownBook.MAX_COOLDOWN_TICKS, book.remaining("y", 0));
		book.start("z", 0, -5);
		assertTrue(book.ready("z", 0));
	}

	@Test
	public void snapshotDropsExpiredAndSizeIsBounded() {
		CooldownBook book = new CooldownBook();
		book.start("old", 0, 10);
		book.start("new", 0, 1000);
		assertEquals(1, book.snapshot(500).size());
		for (int i = 0; i < 1000; i++) book.start("k" + i, 2000, 1); // todas caducan en 2001
		assertEquals(CooldownBook.MAX_ENTRIES, book.size());
	}
}
