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
			case "mage" -> standard("mage", "clear_mind", new int[]{22, 25, 45, 60, 80}, new int[]{12, 25, 30, 45, 90});
			case "assassin" -> standard("assassin", "backstab", new int[]{20, 30, 45, 60, 80}, new int[]{14, 18, 30, 45, 90});
			case "herbalist" -> standard("herbalist", "herbal_immunity", new int[]{20, 40, 45, 60, 80},
					new int[]{12, 40, 30, 45, 90});
			case "engineer" -> standard("engineer", "battery", new int[]{20, 40, 45, 60, 80}, new int[]{10, 35, 30, 45, 90});
			case "lancer" -> standard("lancer", "charge", new int[]{20, 30, 45, 60, 80}, new int[]{10, 18, 30, 45, 90});
			case "swordsman" -> standard("swordsman", "parry", new int[]{20, 30, 45, 60, 80}, new int[]{8, 16, 30, 45, 90});
			case "paladin" -> standard("paladin", "protective_aura", new int[]{25, 30, 45, 60, 80},
					new int[]{14, 25, 30, 45, 90});
			case "necromancer" -> standard("necromancer", "life_drain", new int[]{22, 40, 45, 60, 80},
					new int[]{12, 30, 30, 45, 90});
			case "druid" -> standard("druid", "growth", new int[]{20, 30, 45, 60, 80}, new int[]{12, 25, 30, 45, 90});
			case "merchant" -> standard("merchant", "deep_pockets", new int[]{15, 30, 45, 60, 80}, new int[]{8, 20, 30, 45, 90});
			case "monk" -> standard("monk", "evasion", new int[]{25, 30, 45, 60, 80}, new int[]{10, 14, 30, 45, 90});
			case "elementalist" -> standard("elementalist", "elemental_dominion", new int[]{20, 30, 45, 60, 80},
					new int[]{8, 18, 30, 45, 90});
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

	/**
	 * Estructura común de F3 (mismo reparto que el Bersérker): N1 y N2 pasivas de Talentos (tras la pasiva B del rol),
	 * N3 y N4 activas de Talentos (tras Presteza), Veterano P/A/P y Ascenso A/P/A.
	 *
	 * @param cost coste de maná de las activas N3, N4, N6, N8 y N10 (en ese orden)
	 * @param cooldown espera en segundos de esas mismas activas
	 */
	private static List<Extra> standard(String role, String passiveB, int[] cost, int[] cooldown) {
		String k = role + "_";
		return List.of(
				talent(k + "n1", RoleNode.Kind.PASSIVE, 8, 3, passiveB, 0, 0, 2, 3),
				talent(k + "n2", RoleNode.Kind.PASSIVE, 16, 3, k + "n1", 0, 0, 2, 4),
				talent(k + "n3", RoleNode.Kind.ACTIVE, 9, 3, k + "haste", cost[0], cooldown[0], 6, 3),
				talent(k + "n4", RoleNode.Kind.ACTIVE, 20, 3, k + "n3", cost[1], cooldown[1], 6, 5),
				bridge(k + "n5", RoleNode.Kind.PASSIVE, Section.VETERAN, 34, 2, 0, 0),
				bridge(k + "n6", RoleNode.Kind.ACTIVE, Section.VETERAN, 36, 2, cost[2], cooldown[2]),
				bridge(k + "n7", RoleNode.Kind.PASSIVE, Section.VETERAN, 38, 1, 0, 0),
				bridge(k + "n8", RoleNode.Kind.ACTIVE, Section.ASCENT, 42, 2, cost[3], cooldown[3]),
				bridge(k + "n9", RoleNode.Kind.PASSIVE, Section.ASCENT, 45, 2, 0, 0),
				bridge(k + "n10", RoleNode.Kind.ACTIVE, Section.ASCENT, 48, 1, cost[4], cooldown[4]));
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
