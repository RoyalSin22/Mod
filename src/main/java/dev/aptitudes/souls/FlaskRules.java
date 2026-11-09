package dev.aptitudes.souls;

/** Límites y balance de los frascos. El tope de compra no rebaja mejoras ya pagadas. */
public final class FlaskRules {
	public static final int MAX_RANK = 3;
	public static final int DEFAULT_RANK_LIMIT = 2;
	public static final int BASE_CHARGES = 5;

	public record Upgrade(int rank, int shards) {}

	private FlaskRules() {}

	public static int rank(int stored) {
		return Math.clamp(stored, 0, MAX_RANK);
	}

	public static int capacity(int rank) {
		return BASE_CHARGES + rank(rank);
	}

	public static int remaining(int rank, int spent) {
		int capacity = capacity(rank);
		return capacity - Math.clamp(spent, 0, capacity);
	}

	public static int estusHealing(int rank) {
		return 8 + 2 * rank(rank);
	}

	public static int etherPercent(int rank) {
		return 40 + 10 * rank(rank);
	}

	public static Upgrade next(int current, int limit) {
		int rank = rank(current);
		return rank < rank(limit) ? new Upgrade(rank + 1, 8 * (rank + 1)) : null;
	}
}
