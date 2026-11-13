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

/** v67 (F2 y F3): datos de las habilidades nuevas N1–N10 de los roles que ya las tienen. */
class RoleExtrasTest {
	/** Casillas de la cuadrícula de Talentos que ya ocupan los nodos clásicos (ver {@code RoleTree.define}). */
	private static final Set<String> OCCUPIED = Set.of("1,1", "4,1", "7,1", "0,2", "2,2", "6,2", "8,2", "1,3", "7,3",
			"0,4", "8,4", "1,5", "7,5", "4,6", "8,6");

	/** F2: Bersérker. F3: Mago, Asesino, Herbolista e Ingeniero. F4: Lancero, Espadachín, Paladín y Nigromante. F5: Druida, Mercader, Monje y Chamán. F6: Cambiaformas, Pistolero, Invocador de armas y Cazador. */
	private static final List<String> ROLES = List.of("berserker", "mage", "assassin", "herbalist", "engineer", "lancer",
			"swordsman", "paladin", "necromancer", "druid", "merchant", "monk", "elementalist",
				"shapeshifter", "gunslinger", "weapon_summoner", "hunter");
	/** Pasiva B de cada rol (de la que cuelga N1), según {@code RoleTree}. */
	private static final java.util.Map<String, String> PASSIVE_B = java.util.Map.ofEntries(
			java.util.Map.entry("berserker", "bloodlust"), java.util.Map.entry("mage", "clear_mind"),
			java.util.Map.entry("assassin", "backstab"), java.util.Map.entry("herbalist", "herbal_immunity"),
			java.util.Map.entry("engineer", "battery"), java.util.Map.entry("lancer", "charge"),
			java.util.Map.entry("swordsman", "parry"), java.util.Map.entry("paladin", "protective_aura"),
			java.util.Map.entry("necromancer", "life_drain"), java.util.Map.entry("druid", "growth"),
				java.util.Map.entry("merchant", "deep_pockets"), java.util.Map.entry("monk", "evasion"),
				java.util.Map.entry("elementalist", "elemental_dominion"), java.util.Map.entry("shapeshifter", "trickster"),
				java.util.Map.entry("gunslinger", "eagle_eye"), java.util.Map.entry("weapon_summoner", "blade_guard"),
				java.util.Map.entry("hunter", "tracker"));

	@Test
	void everyRoleHasTenNodesInOrder() {
		for (String role : ROLES) {
			List<Extra> extras = RoleExtras.of(role);
			assertEquals(10, extras.size(), role);
			for (int i = 0; i < extras.size(); i++) {
				assertEquals(role + "_n" + (i + 1), extras.get(i).id());
				assertEquals(i + 1, extras.get(i).number());
			}
		}
		assertTrue(RoleExtras.of("bard").isEmpty(), "los demás roles llegan en F7");
	}

	@Test
	void sectionsFollowTheDesign() {
		for (String role : ROLES) {
			for (Extra e : RoleExtras.of(role)) {
				Section expected = e.number() <= 4 ? Section.TALENTS : e.number() <= 7 ? Section.VETERAN : Section.ASCENT;
				assertEquals(expected, e.section(), e.id());
				assertEquals(expected == Section.ASCENT ? Evolution.MASTER : Evolution.BASE, e.requiredTier(), e.id());
			}
		}
	}

	@Test
	void everyRoleFollowsTheSameKindPatternAndRankCounts() {
		RoleNode.Kind p = RoleNode.Kind.PASSIVE, a = RoleNode.Kind.ACTIVE;
		RoleNode.Kind[] kinds = {p, p, a, a, p, a, p, a, p, a};
		int[] ranks = {3, 3, 3, 3, 2, 2, 1, 2, 2, 1};
		int[] levels = {8, 16, 9, 20, 34, 36, 38, 42, 45, 48};
		for (String role : ROLES) {
			List<Extra> extras = RoleExtras.of(role);
			for (int i = 0; i < 10; i++) {
				assertEquals(kinds[i], extras.get(i).kind(), extras.get(i).id());
				assertEquals(ranks[i], extras.get(i).ranks(), extras.get(i).id());
				assertEquals(levels[i], extras.get(i).level(), extras.get(i).id());
			}
		}
	}

	@Test
	void everyRankFitsBelowTheMaximumLevel() {
		for (String role : ROLES) {
			for (Extra e : RoleExtras.of(role)) {
				assertTrue(e.ranks() >= 1 && e.ranks() <= RoleNode.MAX_RANK, e.id());
				int lastRankLevel = e.level() + RoleNode.LEVELS_PER_RANK * (e.ranks() - 1);
				assertTrue(lastRankLevel <= 50, e.id() + " pide nivel " + lastRankLevel);
			}
		}
	}

	@Test
	void activesHaveCostAndCooldownAndPassivesDoNot() {
		for (String role : ROLES) {
			int actives = 0;
			for (Extra e : RoleExtras.of(role)) {
				if (e.active()) {
					actives++;
					assertTrue(e.energyCost() > 0 && e.cooldownSecs() > 0, e.id());
				} else {
					assertEquals(0, e.energyCost(), e.id());
					assertEquals(0, e.cooldownSecs(), e.id());
				}
			}
			assertEquals(5, actives, role);
			// 8 activas anteriores + estas: deben caber en el códec de equipamiento.
			assertTrue(8 + actives <= LoadoutCodec.MAX_INDEX + 1, role);
		}
	}

	@Test
	void ultimateOfAscentCostsMoreAndWaitsLongerThanTheRest() {
		for (String role : ROLES) {
			List<Extra> extras = RoleExtras.of(role);
			Extra last = extras.get(9);
			for (Extra e : extras) {
				if (!e.active() || e == last) continue;
				assertTrue(last.energyCost() >= e.energyCost(), role);
				assertTrue(last.cooldownSecs() >= e.cooldownSecs(), role);
			}
		}
	}

	@Test
	void talentNodesUseFreeCellsBelowTheirParent() {
		for (String role : ROLES) {
			Set<String> used = new HashSet<>();
			for (Extra e : RoleExtras.of(role)) {
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
	}

	@Test
	void prerequisitesPointToTheRightParents() {
		for (String role : ROLES) {
			List<Extra> extras = RoleExtras.of(role);
			assertEquals(PASSIVE_B.get(role), extras.get(0).prerequisite(), role + " N1 cuelga de la pasiva B");
			assertEquals(role + "_n1", extras.get(1).prerequisite());
			assertEquals(role + "_haste", extras.get(2).prerequisite(), role + " N3 cuelga de Presteza");
			assertEquals(role + "_n3", extras.get(3).prerequisite());
		}
	}

	@Test
	void prerequisitesInsideTheListComeEarlierAndAskLess() {
		for (String role : ROLES) {
			List<Extra> extras = RoleExtras.of(role);
			for (Extra e : extras) {
				if (e.prerequisite() == null || !e.prerequisite().startsWith(role + "_n")) continue;
				Extra parent = extras.stream().filter(p -> p.id().equals(e.prerequisite())).findFirst().orElseThrow();
				assertTrue(parent.number() < e.number(), e.id());
				assertTrue(parent.level() <= e.level(), e.id());
			}
		}
	}
}
