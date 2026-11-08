package dev.aptitudes.role.datapack;

import dev.aptitudes.role.datapack.TalentPackRules.Anchor;
import dev.aptitudes.role.datapack.TalentPackRules.Node;
import dev.aptitudes.role.datapack.TalentPackRules.Template;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TalentPackRulesTest {
	private static final String VALID = """
			{"role":"swordsman","prerequisite":"parry","level":12,"ranks":1,"template":"speed"}
			""";
	private static final Map<String, Anchor> BASE = Map.of(
			"parry", new Anchor("swordsman", 8), "arcanum", new Anchor("mage", 1));

	@Test
	void loadsAValidBranchInEitherFileOrder() throws IOException {
		Node first = TalentPackRules.parse("pilot:light_step", VALID);
		Node next = node("pilot:step/firm", "swordsman", first.id(), 16, 2);
		assertEquals(Template.SPEED, first.template());
		assertDoesNotThrow(() -> validate(List.of(next, first)));
	}

	@Test
	void rejectsUnknownOrRepeatedFieldsAndWrongTypes() {
		assertThrows(IllegalArgumentException.class, () -> TalentPackRules.parse("p:a", VALID.replace(
				"\"level\":12", "\"level\":12,\"level\":13")));
		assertThrows(IllegalArgumentException.class, () -> TalentPackRules.parse("p:a", VALID.replace(
				"\"ranks\":1", "\"amount\":1")));
		assertThrows(IllegalArgumentException.class, () -> TalentPackRules.parse("p:a", VALID.replace(
				"\"level\":12", "\"level\":\"12\"")));
		assertThrows(IllegalArgumentException.class, () -> TalentPackRules.parse("p:a", VALID.replace(
				",\"template\":\"speed\"", "")));
	}

	@Test
	void rejectsInvalidJsonAndNumericalFormats() {
		assertThrows(IOException.class, () -> TalentPackRules.parse("p:a", VALID + "{}"));
		assertThrows(IOException.class, () -> TalentPackRules.parse("p:a", "/* comentario */ " + VALID));
		for (String invalid : List.of("1.5", "1e2", "2147483648", "999999999999999999999999")) {
			assertThrows(IllegalArgumentException.class,
					() -> TalentPackRules.parse("p:a", VALID.replace("12", invalid)));
		}
		assertThrows(IllegalArgumentException.class,
				() -> TalentPackRules.parse("p:a", VALID.replace("speed", "command")));
	}

	@Test
	void rejectsMissingCrossRoleAndLaterPrerequisites() {
		rejects("desconocido", node("p:a", "swordsman", "p:missing", 12, 1));
		rejects("otro rol", node("p:a", "swordsman", "arcanum", 12, 1));
		rejects("nivel mayor", node("p:a", "swordsman", "parry", 7, 1));
		rejects("otro rol", node("p:a", "swordsman", "p:b", 12, 1), node("p:b", "mage", "arcanum", 12, 1));
		rejects("rol desconocido", node("p:a", "missing", "parry", 12, 1));
	}

	@Test
	void rejectsCyclesAndRepeatedIds() {
		rejects("ciclo", node("p:a", "swordsman", "p:a", 12, 1));
		rejects("ciclo", node("p:a", "swordsman", "p:b", 12, 1), node("p:b", "swordsman", "p:a", 12, 1));
		Node original = node("p:a", "swordsman", "parry", 12, 1);
		rejects("repetido", original, original);
	}

	@Test
	void boundsIdsLevelsAndRanks() {
		rejects("identificador", node("plain", "swordsman", "parry", 12, 1));
		rejects("identificador", node("p:../a", "swordsman", "parry", 12, 1));
		rejects("identificador", node("p:" + "a".repeat(63), "swordsman", "parry", 12, 1));
		rejects("fuera de rango", node("p:a", "swordsman", "parry", 12, 0));
		rejects("fuera de rango", node("p:a", "swordsman", "parry", 12, 4));
		rejects("fuera de rango", node("p:a", "swordsman", "parry", -1, 1));
		rejects("fuera de rango", node("p:a", "swordsman", "parry", Integer.MAX_VALUE, 3));
		rejects("fuera de rango", node("p:a", "swordsman", "parry", 41, 3));
		assertDoesNotThrow(() -> validate(List.of(node("p:a", "swordsman", "parry", 40, 3))));
	}

	@Test
	void boundsCatalogAndPerRoleSize() {
		Node original = node("p:a", "swordsman", "parry", 12, 1);
		var over = Collections.nCopies(TalentPackRules.MAX_NODES + 1, original);
		assertThrows(IllegalArgumentException.class, () -> validate(over));
		List<Node> sameRole = new ArrayList<>();
		for (int i = 0; i <= TalentPackRules.MAX_PER_ROLE; i++) {
			sameRole.add(node("p:a" + i, "swordsman", "parry", 12, 1));
		}
		var error = assertThrows(IllegalArgumentException.class, () -> validate(sameRole));
		assertTrue(error.getMessage().contains("talentos por rol"));
	}

	private static Node node(String id, String role, String parent, int level, int ranks) {
		return new Node(id, role, parent, level, ranks, Template.SPEED);
	}

	private static void rejects(String reason, Node... nodes) {
		var error = assertThrows(IllegalArgumentException.class, () -> validate(List.of(nodes)));
		assertTrue(error.getMessage().contains(reason), error.getMessage());
	}

	private static void validate(List<Node> nodes) {
		TalentPackRules.validate(nodes, role -> role.equals("swordsman") || role.equals("mage"), BASE::get);
	}
}
