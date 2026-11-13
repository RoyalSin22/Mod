package dev.aptitudes.role;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * v67 (F2 en adelante): las 10 habilidades nuevas de cada rol (N1–N10), repartidas por el árbol unificado.
 *
 * <ul>
 *   <li><b>Talentos</b> (N1–N4): ramas nuevas de los carriles de pasivas y activas, con posición en la cuadrícula.</li>
 *   <li><b>Veterano</b> (N5–N7): puente entre las Sendas y la evolución (niveles 34–38).</li>
 *   <li><b>Ascenso</b> (N8–N10): puente entre Maestro y Definitivo; exigen la evolución Maestro (niveles 42–48).</li>
 * </ul>
 *
 * <p>Solo datos, sin Minecraft. {@link RoleTree} añade estos nodos <b>al final</b> de la lista del rol y sus activas
 * al final de las activas (índices 8 en adelante), así que no cambia ningún índice guardado. Diseño en
 * {@code docs/V67_ARBOL_UNIFICADO.md}.</p>
 */
public final class RoleExtras {
	/** Sección del árbol unificado en la que se dibuja el nodo. */
	public enum Section { TALENTS, VETERAN, ASCENT }

	/**
	 * Un nodo nuevo.
	 *
	 * @param col columna de la cuadrícula de Talentos (−1 en Veterano y Ascenso)
	 * @param row fila de la cuadrícula de Talentos (−1 en Veterano y Ascenso)
	 */
	public record Extra(String id, RoleNode.Kind kind, Section section, int level, int ranks,
						@Nullable String prerequisite, int energyCost, int cooldownSecs, int col, int row) {
		public boolean active() {
			return kind == RoleNode.Kind.ACTIVE;
		}

		/** Evolución mínima del rol: Ascenso exige Maestro. */
		public int requiredTier() {
			return section == Section.ASCENT ? Evolution.MASTER : Evolution.BASE;
		}

		/** Número del nodo (1–10), sacado del sufijo {@code _nX}. */
		public int number() {
			return Integer.parseInt(id.substring(id.lastIndexOf("_n") + 2));
		}

		RoleNode node(Role role) {
			return new RoleNode(id, role, kind, level, prerequisite, energyCost, cooldownSecs, requiredTier(), ranks);
		}
	}

	private RoleExtras() {
	}

	/** Nodos nuevos del rol, en orden N1–N10 (vacío si el rol aún no los tiene). */
	public static List<Extra> of(Role role) {
		return of(role.key());
	}

	/** Igual que {@link #of(Role)}, por la clave del rol ({@code berserker}…): sin Minecraft, para las pruebas. */
	public static List<Extra> of(String roleKey) {
		return switch (roleKey) {
			case "berserker" -> berserker();
			default -> List.of();
		};
	}

	private static List<Extra> berserker() {
		String k = "berserker_";
		return List.of(
				// Talentos — pasivas: Sed de sangre → Cicatrices → Sangre hirviente (columna 2, filas 3 y 4).
				talent(k + "n1", RoleNode.Kind.PASSIVE, 8, 3, "bloodlust", 0, 0, 2, 3),
				talent(k + "n2", RoleNode.Kind.PASSIVE, 16, 3, k + "n1", 0, 0, 2, 4),
				// Talentos — activas: Presteza → Hacha arrojadiza → Cadena de sangre (columna 6, filas 3 y 5).
				talent(k + "n3", RoleNode.Kind.ACTIVE, 9, 3, k + "haste", 25, 14, 6, 3),
				talent(k + "n4", RoleNode.Kind.ACTIVE, 20, 3, k + "n3", 30, 18, 6, 5),
				// Veterano.
				bridge(k + "n5", RoleNode.Kind.PASSIVE, Section.VETERAN, 34, 2, 0, 0),
				bridge(k + "n6", RoleNode.Kind.ACTIVE, Section.VETERAN, 36, 2, 45, 30),
				bridge(k + "n7", RoleNode.Kind.PASSIVE, Section.VETERAN, 38, 1, 0, 0),
				// Ascenso (exige Maestro).
				bridge(k + "n8", RoleNode.Kind.ACTIVE, Section.ASCENT, 42, 2, 60, 45),
				bridge(k + "n9", RoleNode.Kind.PASSIVE, Section.ASCENT, 45, 2, 0, 0),
				bridge(k + "n10", RoleNode.Kind.ACTIVE, Section.ASCENT, 48, 1, 80, 90));
	}

	private static Extra talent(String id, RoleNode.Kind kind, int level, int ranks, String prerequisite, int energy,
								int cooldown, int col, int row) {
		return new Extra(id, kind, Section.TALENTS, level, ranks, prerequisite, energy, cooldown, col, row);
	}

	private static Extra bridge(String id, RoleNode.Kind kind, Section section, int level, int ranks, int energy,
								int cooldown) {
		return new Extra(id, kind, section, level, ranks, null, energy, cooldown, -1, -1);
	}
}
