package dev.aptitudes.role;

import dev.aptitudes.role.RoleExtras.Extra;
import dev.aptitudes.role.RoleExtras.Section;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** v67 (F2): datos de las habilidades nuevas N1–N10 (piloto: Bersérker). */
class RoleExtrasTest {
	/** Casillas de la cuadrícula de Talentos que ya ocupan los nodos clásicos (ver {@code RoleTree.define}). */
	private static final Set<String> OCCUPIED = Set.of("1,1", "4,1", "7,1", "0,2", "2,2", "6,2", "8,2", "1,3", "7,3",
			"0,4", "8,4", "1,5", "7,5", "4,6", "8,6");

	private final List<Extra> berserker = RoleExtras.of("berserker");

	@Test
	void berserkerHasTenNodesInOrder() {
		assertEquals(10, berserker.size());
		for (int i = 0; i < berserker.size(); i++) {
			assertEquals("berserker_n" + (i + 1), berserker.get(i).id());
			assertEquals(i + 1, berserker.get(i).number());
		}
		assertTrue(RoleExtras.of("mage").isEmpty(), "los demás roles llegan en F3–F7");
	}

	@Test
	void sectionsFollowTheDesign() {
		for (Extra e : berserker) {
			Section expected = e.number() <= 4 ? Section.TALENTS : e.number() <= 7 ? Section.VETERAN : Section.ASCENT;
			assertEquals(expected, e.section(), e.id());
			assertEquals(expected == Section.ASCENT ? Evolution.MASTER : Evolution.BASE, e.requiredTier(), e.id());
		}
	}

	@Test
	void everyRankFitsBelowTheMaximumLevel() {
		for (Extra e : berserker) {
			assertTrue(e.ranks() >= 1 && e.ranks() <= RoleNode.MAX_RANK, e.id());
			int lastRankLevel = e.level() + RoleNode.LEVELS_PER_RANK * (e.ranks() - 1);
			assertTrue(lastRankLevel <= 50, e.id() + " pide nivel " + lastRankLevel);
		}
	}

	@Test
	void activesHaveCostAndCooldownAndPassivesDoNot() {
		int actives = 0;
		for (Extra e : berserker) {
			if (e.active()) {
				actives++;
				assertTrue(e.energyCost() > 0 && e.cooldownSecs() > 0, e.id());
			} else {
				assertEquals(0, e.energyCost(), e.id());
				assertEquals(0, e.cooldownSecs(), e.id());
			}
		}
		// 8 activas anteriores + estas: deben caber en el códec de equipamiento.
		assertTrue(8 + actives <= LoadoutCodec.MAX_INDEX + 1);
	}

	@Test
	void talentNodesUseFreeCellsBelowTheirParent() {
		Set<String> used = new HashSet<>();
		for (Extra e : berserker) {
			if (e.section() != Section.TALENTS) {
				assertEquals(-1, e.col(), e.id());
				assertNull(e.prerequisite(), e.id());
				continue;
			}
			String cell = e.col() + "," + e.row();
			assertTrue(e.col() >= 0 && e.col() <= 8 && e.row() >= 1 && e.row() <= 6, e.id());
			assertFalse(OCCUPIED.contains(cell), e.id() + " pisa un nodo clásico");
			assertTrue(used.add(cell), e.id() + " repite casilla");
			assertNotNull(e.prerequisite(), e.id());
		}
	}

	@Test
	void prerequisitesInsideTheListComeEarlierAndAskLess() {
		for (Extra e : berserker) {
			if (e.prerequisite() == null || !e.prerequisite().startsWith("berserker_n")) continue;
			Extra parent = berserker.stream().filter(p -> p.id().equals(e.prerequisite())).findFirst().orElseThrow();
			assertTrue(parent.number() < e.number(), e.id());
			assertTrue(parent.level() <= e.level(), e.id());
		}
	}
}
