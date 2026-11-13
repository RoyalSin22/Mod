package dev.aptitudes.ability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F3): reducir una espera en curso (Eslabón, Cadena de montaje). */
class CooldownBookReduceTest {
	@Test
	void reducesWhatIsLeft() {
		CooldownBook book = new CooldownBook();
		book.start("fireball", 100, 200);
		book.reduce("fireball", 150, 60);
		assertEquals(90, book.remaining("fireball", 150));
	}

	@Test
	void reducingMoreThanLeftMakesItReady() {
		CooldownBook book = new CooldownBook();
		book.start("fireball", 0, 100);
		book.reduce("fireball", 10, 500);
		assertTrue(book.ready("fireball", 10));
		assertEquals(0, book.size());
	}

	@Test
	void unknownKeysAndNonPositiveAmountsDoNothing() {
		CooldownBook book = new CooldownBook();
		book.reduce("nothing", 0, 100);
		book.start("a", 0, 100);
		book.reduce("a", 0, 0);
		book.reduce("a", 0, -5);
		assertEquals(100, book.remaining("a", 0));
	}
}
