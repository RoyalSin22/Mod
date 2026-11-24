package dev.aptitudes.role;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * C2 (R3): {@code RoleTree.keystone} indexa los nodos S7 por id. {@code RoleTree} carga registros de Minecraft y no se
 * puede probar con JUnit simple, así que aquí se comprueba lo que el índice usa: que las 60 piedras angulares existen y
 * que sus ids de nodo S7 son únicos (si dos chocaran, el mapa perdería una).
 */
class KeystoneIndexTest {
	@Test void everyRoleAndPathHasAKeystoneWithAUniqueNodeId() {
		Set<String> ids = new HashSet<>();
		for (Role role : Role.values()) {
			for (int path = 1; path <= 3; path++) {
				assertNotNull(KeystoneRules.template(role.key(), path), role.key() + " senda " + path);
				assertTrue(ids.add(PathRules.nodeId(role.key(), path, PathRules.Step.S7)), "id repetido");
			}
		}
		assertEquals(60, ids.size(), "20 roles × 3 sendas");
	}
}
