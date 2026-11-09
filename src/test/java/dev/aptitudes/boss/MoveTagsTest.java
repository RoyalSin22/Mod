package dev.aptitudes.boss;

import dev.aptitudes.boss.AttackTags.Tag;
import dev.aptitudes.boss.BossMoveRules.Move;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoveTagsTest {
	@Test void movesWithoutTagsAreNormal() {
		Move m = new Move("x", 1, 0, 3, 10, 10, 1);
		assertEquals(Set.of(Tag.NORMAL), m.tags());
	}

	@Test void nullTagsFallBackToNormal() {
		assertEquals(Set.of(Tag.NORMAL), new Move("x", 1, 0, 3, 10, 10, 1, null).tags());
	}

	@Test void tagsAreImmutable() {
		Move m = new Move("x", 1, 0, 3, 10, 10, 1, AttackTags.of(Tag.HEAVY));
		assertThrows(UnsupportedOperationException.class, () -> m.tags().add(Tag.GROUND));
	}

	@Test void declaredTagsAreKeptAsGivenAndNotMergedWithNormal() {
		Move slam = new Move("slam", 1, 0, 5.5, 28, 70, 2, EnumSet.of(Tag.HEAVY, Tag.GROUND));
		assertEquals(EnumSet.of(Tag.HEAVY, Tag.GROUND), slam.tags());
		assertEquals(false, slam.tags().contains(Tag.NORMAL));
	}
}
// Nota: las etiquetas concretas de cada jefe (Coloso, Guardián y Autómata) se fijan en sus clases de entidad.
// No se prueban aquí porque cargar esas clases exige Minecraft; se verifican con el build y en partida.
