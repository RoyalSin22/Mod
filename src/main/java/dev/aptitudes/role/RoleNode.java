package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import org.jetbrains.annotations.Nullable;

/**
 * Nodo del árbol de un rol. Cada nodo tiene hasta {@link #MAX_RANK} rangos;
 * cada rango cuesta 1 punto del rol y exige {@link #levelForRank(int)}.
 *
 * @param kind         tipo de nodo (ver {@link Kind})
 * @param energyCost   coste de maná por uso (solo activas)
 * @param cooldownSecs enfriamiento base en segundos (solo activas)
 */
public record RoleNode(String id, Role role, Kind kind, int requiredLevel,
					   @Nullable String prerequisite, int energyCost, int cooldownSecs) {

	public enum Kind {
		/** Pasiva con mecánica propia. */
		PASSIVE,
		/** Habilidad activa (tecla). */
		ACTIVE,
		/** Entrenamiento: bonificación de estadística por rango (ver {@code RoleTree.STATS}). */
		STAT,
		/** Presteza: −10 % de enfriamiento por rango en las activas del rol. */
		HASTE,
		/** Eficiencia: −10 % de maná por rango en las activas del rol. */
		EFFICIENCY,
		/** Maestría: +1 rango efectivo por rango en las activas del rol. */
		MASTERY,
		/** Nodo de Senda: aplica un {@link PathEffect}. Solo se siguen 2 de las 3 sendas de un rol. */
		PATH,
		/** Nodo legendario (antigua habilidad única): 1 rango; se activa con la tecla H. */
		LEGENDARY
	}

	/** {@code true} si es una habilidad activa (compatibilidad con el código anterior). */
	public boolean active() {
		return kind == Kind.ACTIVE;
	}

	public static final int MAX_RANK = 3;

	/** Rangos máximos de este nodo (el legendario tiene uno). */
	public int maxRank() {
		return kind == Kind.LEGENDARY ? 1 : MAX_RANK;
	}
	/** Niveles de rol adicionales que exige cada rango después del primero. */
	public static final int LEVELS_PER_RANK = 5;

	public int levelForRank(int rank) {
		return requiredLevel + LEVELS_PER_RANK * (rank - 1);
	}

	public String translationKey() {
		return "node." + Aptitudes.MOD_ID + "." + id;
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	static RoleNode passiveNode(String id, Role role, int level, @Nullable String prerequisite) {
		return new RoleNode(id, role, Kind.PASSIVE, level, prerequisite, 0, 0);
	}

	static RoleNode extraNode(String id, Role role, Kind kind, int level, @Nullable String prerequisite) {
		return new RoleNode(id, role, kind, level, prerequisite, 0, 0);
	}

	static RoleNode activeNode(String id, Role role, int level, @Nullable String prerequisite, int energy, int cooldown) {
		return new RoleNode(id, role, Kind.ACTIVE, level, prerequisite, energy, cooldown);
	}
}
