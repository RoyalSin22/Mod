package dev.aptitudes.role.datapack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static dev.aptitudes.role.RoleNode.LEVELS_PER_RANK;
import static dev.aptitudes.role.RoleNode.MAX_RANK;

/** Conserva los rangos que siguen respaldados por el catálogo y el progreso actual del rol. */
public final class TalentProgressRules {
	public static final int MAX_SPEED_RANKS = 3;

	record Requirement(int roleLevel, int firstLevel, int maxRanks, String prerequisite) {}

	private TalentProgressRules() {}

	public static String key(String role, String id) {
		return role + "/" + id;
	}

	public static Map<String, Integer> readRanks(Map<String, Integer> saved) {
		Map<String, Integer> ranks = new HashMap<>();
		saved.entrySet().stream().filter(entry -> {
			String key = entry.getKey();
			int slash = key.indexOf('/');
			return slash > 0 && slash <= 32 && entry.getValue() > 0
					&& TalentPackRules.isCustomId(key.substring(slash + 1));
		}).sorted(Map.Entry.comparingByKey()).limit(TalentPackRules.MAX_NODES)
				.forEach(entry -> ranks.put(entry.getKey(), Math.min(MAX_RANK, entry.getValue())));
		return ranks;
	}

	public static int levelForRank(int firstLevel, int rank) {
		return firstLevel + (rank - 1) * LEVELS_PER_RANK;
	}

	static Map<String, Integer> reconcile(Map<String, Integer> saved, Map<String, Requirement> catalog,
															 ToIntFunction<String> builtinRank) {
		Map<String, Integer> kept = new HashMap<>();
		saved.forEach((id, rank) -> {
			Requirement rule = catalog.get(id);
			if (rule == null || rule.roleLevel() < rule.firstLevel()) return;
			int unlocked = 1 + (rule.roleLevel() - rule.firstLevel()) / LEVELS_PER_RANK;
			int allowed = Math.min(rank, Math.min(unlocked, rule.maxRanks()));
			if (allowed > 0) kept.put(id, allowed);
		});
		// Una rama solo conserva rangos si su prerrequisito sigue aprendido, incluso tras varios reembolsos.
		while (kept.keySet().removeIf(id -> {
			String pre = catalog.get(id).prerequisite();
			return pre.indexOf(':') >= 0 ? !kept.containsKey(pre) : builtinRank.applyAsInt(pre) <= 0;
		})) {
			// El catálogo es acíclico y está acotado a 32 nodos.
		}
		return kept;
	}

	static int speedRanks(int ranks) {
		return Math.clamp(ranks, 0, MAX_SPEED_RANKS);
	}

	static boolean matches(long revision, long requestRevision, int rank, int expectedRank) {
		return revision == requestRevision && rank == expectedRank;
	}
}
