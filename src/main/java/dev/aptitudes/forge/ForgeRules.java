package dev.aptitudes.forge;

import org.jetbrains.annotations.Nullable;

/** Costes y efectos de la Reforja y el Templado. */
public final class ForgeRules {
	public enum Profile {
		RESERVE("reserve", 2), IMPETUS("impetus", 5);

		private final String key;
		private final int percentPerRank;

		Profile(String key, int percentPerRank) {
			this.key = key;
			this.percentPerRank = percentPerRank;
		}

		public String key() {
			return key;
		}
	}

	public record Affix(Profile profile, int rank) {
		public Affix {
			if (profile == null || rank < 0 || rank > MAX_RANK) throw new IllegalArgumentException("Invalid affix");
		}

		public int percent() {
			return profile.percentPerRank * (rank + 1);
		}
	}

	public record Upgrade(Affix result, int trophies, int shards) {}

	public static final int MAX_RANK = 2;

	private ForgeRules() {
	}

	@Nullable
	public static Affix read(String profile, int rank) {
		if (rank < 0 || rank > MAX_RANK) return null;
		for (Profile option : Profile.values()) {
			if (option.key().equals(profile)) return new Affix(option, rank);
		}
		return null;
	}

	@Nullable
	public static Upgrade reforge(@Nullable Affix current, Profile selected) {
		if (current != null && current.profile() == selected) return null;
		return new Upgrade(new Affix(selected, current == null ? 0 : current.rank()), 1, 8);
	}

	@Nullable
	public static Upgrade temper(@Nullable Affix current) {
		if (current == null || current.rank() == MAX_RANK) return null;
		int rank = current.rank() + 1;
		return new Upgrade(new Affix(current.profile(), rank), rank, 8 + 4 * rank);
	}

	public static double energyCost(double cost, @Nullable Affix affix) {
		if (affix == null || affix.profile() != Profile.RESERVE || cost < 1) return cost;
		return Math.max(1, cost * (1 - affix.percent() / 100.0));
	}

	public static float impactGain(float amount, @Nullable Affix affix) {
		if (affix == null || affix.profile() != Profile.IMPETUS) return amount;
		return amount * (1 + affix.percent() / 100f);
	}
}
