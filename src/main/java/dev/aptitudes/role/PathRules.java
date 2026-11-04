package dev.aptitudes.role;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Reglas puras de las Sendas (v46). Sin Minecraft, con pruebas ({@code role/PathRulesTest}).
 *
 * <ul>
 *   <li>Cada rol tiene 3 sendas (A, B, C) y desde la v46 son <b>exclusivas</b>: el primer rango en una la elige y
 *       bloquea las otras dos ({@link #MAX_PATHS} = 1).</li>
 *   <li>Cada senda tiene escalones ({@link Step}): S1–S3 (los de antes), S4, una <b>bifurcación</b> S5x/S5y
 *       (se elige una) y S6. La piedra angular S7 llega en la v46.1.</li>
 *   <li>Migración: quien tenga rangos en más de una senda conserva la que tiene más rangos (empate: A, luego B) y
 *       recupera los puntos de las demás ({@link #refunds}).</li>
 * </ul>
 *
 * <p>Ids estables (se guardan en el mundo): {@code <rol>_<a|b|c><1|2|3|4|5x|5y|6>}.</p>
 */
public final class PathRules {
	/** Sendas por rol. */
	public static final int PATHS = 3;
	/** Sendas que se pueden seguir a la vez (v46: exclusivas; antes 2). */
	public static final int MAX_PATHS = 1;

	/** Escalones de una senda. Solo se añade al final (S7 en la v46.1). */
	public enum Step {
		S1("1", 10, 1.0), S2("2", 14, 1.0), S3("3", 20, 1.0), S4("4", 26, 1.0),
		S5X("5x", 32, 1.0), S5Y("5y", 32, 1.0), S6("6", 40, 1.5);

		private final String suffix;
		private final int level;
		private final double multiplier;

		Step(String suffix, int level, double multiplier) {
			this.suffix = suffix;
			this.level = level;
			this.multiplier = multiplier;
		}

		public String suffix() { return suffix; }
		/** Nivel de rol del primer rango. */
		public int level() { return level; }
		/** Multiplicador sobre el valor por rango del efecto (S6 = ×1,5 del efecto de S4). */
		public double multiplier() { return multiplier; }
		/** ¿Es una de las dos ramas de la bifurcación? */
		public boolean branch() { return this == S5X || this == S5Y; }
		/** ¿Escalón nuevo de la v46 (no existía antes)? */
		public boolean added() { return ordinal() >= S4.ordinal(); }

		/** La otra rama de la bifurcación (o null si no es una rama). */
		public Step otherBranch() {
			return this == S5X ? S5Y : this == S5Y ? S5X : null;
		}
	}

	private static final String[] LETTERS = {"a", "b", "c"};

	private PathRules() {}

	/** Id del nodo de un escalón: {@code berserker_a5x}. {@code path} = 1 (A), 2 (B) o 3 (C). */
	public static String nodeId(String roleKey, int path, Step step) {
		if (path < 1 || path > PATHS) throw new IllegalArgumentException("senda " + path);
		return roleKey + "_" + LETTERS[path - 1] + step.suffix();
	}

	/** ¿Tiene algún rango en algún escalón de la senda? */
	public static boolean pathStarted(ToIntFunction<String> rankOf, String roleKey, int path) {
		for (Step s : Step.values()) if (rankOf.applyAsInt(nodeId(roleKey, path, s)) > 0) return true;
		return false;
	}

	public static int pathsStarted(ToIntFunction<String> rankOf, String roleKey) {
		int n = 0;
		for (int path = 1; path <= PATHS; path++) if (pathStarted(rankOf, roleKey, path)) n++;
		return n;
	}

	/** ¿La senda está bloqueada porque ya se sigue otra? */
	public static boolean pathLocked(ToIntFunction<String> rankOf, String roleKey, int path) {
		return !pathStarted(rankOf, roleKey, path) && pathsStarted(rankOf, roleKey) >= MAX_PATHS;
	}

	/** Senda elegida (1–3) o 0 si aún no hay ninguna. Con varias (mundos antiguos), la que se conservaría. */
	public static int chosenPath(ToIntFunction<String> rankOf, String roleKey) {
		int[] ranks = ranksPerPath(rankOf, roleKey);
		int best = keepPath(ranks);
		return ranks[best - 1] > 0 ? best : 0;
	}

	/** ¿La rama está bloqueada porque ya se aprendió la otra? */
	public static boolean branchLocked(ToIntFunction<String> rankOf, String roleKey, int path, Step step) {
		Step other = step.otherBranch();
		return other != null && rankOf.applyAsInt(nodeId(roleKey, path, other)) > 0;
	}

	/** Rama elegida en la bifurcación (S5X o S5Y) o null si no hay ninguna. */
	public static Step chosenBranch(ToIntFunction<String> rankOf, String roleKey, int path) {
		if (rankOf.applyAsInt(nodeId(roleKey, path, Step.S5X)) > 0) return Step.S5X;
		if (rankOf.applyAsInt(nodeId(roleKey, path, Step.S5Y)) > 0) return Step.S5Y;
		return null;
	}

	/**
	 * Requisito de un escalón nuevo: S4 exige S3; S5x y S5y exigen S4; S6 exige <b>cualquiera</b> de las dos ramas.
	 * (S1–S3 conservan su requisito de siempre, en {@link RoleNode#prerequisite()}.)
	 */
	public static boolean prerequisiteMet(ToIntFunction<String> rankOf, String roleKey, int path, Step step) {
		return switch (step) {
			case S1, S2, S3 -> true;
			case S4 -> rankOf.applyAsInt(nodeId(roleKey, path, Step.S3)) > 0;
			case S5X, S5Y -> rankOf.applyAsInt(nodeId(roleKey, path, Step.S4)) > 0;
			case S6 -> rankOf.applyAsInt(nodeId(roleKey, path, Step.S5X)) > 0
					|| rankOf.applyAsInt(nodeId(roleKey, path, Step.S5Y)) > 0;
		};
	}

	/** Rangos totales en cada senda (índice 0 = A). Sin desbordamiento. */
	public static int[] ranksPerPath(ToIntFunction<String> rankOf, String roleKey) {
		int[] out = new int[PATHS];
		for (int path = 1; path <= PATHS; path++) {
			long sum = 0;
			for (Step s : Step.values()) sum += Math.max(0, rankOf.applyAsInt(nodeId(roleKey, path, s)));
			out[path - 1] = (int) Math.min(Integer.MAX_VALUE, sum);
		}
		return out;
	}

	/** Senda que se conserva en la migración: la de más rangos; en empate, la primera (A, luego B). */
	public static int keepPath(int[] ranksPerPath) {
		int best = 1;
		for (int path = 2; path <= Math.min(PATHS, ranksPerPath.length); path++) {
			if (ranksPerPath[path - 1] > ranksPerPath[best - 1]) best = path;
		}
		return best;
	}

	/**
	 * Nodos cuyos rangos se devuelven al pasar a sendas exclusivas: todos los de las sendas que no se conservan.
	 * Vacío si solo hay una senda empezada (o ninguna).
	 */
	public static List<String> refunds(ToIntFunction<String> rankOf, String roleKey) {
		List<String> out = new ArrayList<>();
		if (pathsStarted(rankOf, roleKey) <= MAX_PATHS) return out;
		int keep = keepPath(ranksPerPath(rankOf, roleKey));
		for (int path = 1; path <= PATHS; path++) {
			if (path == keep) continue;
			for (Step s : Step.values()) {
				String id = nodeId(roleKey, path, s);
				if (rankOf.applyAsInt(id) > 0) out.add(id);
			}
		}
		return out;
	}
}
