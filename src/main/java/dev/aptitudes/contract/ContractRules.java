package dev.aptitudes.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/** Generación determinista y balance puro de contratos del gremio. */
public final class ContractRules {
	public enum Kind { HUNT, DELIVERY, FISHING, ENCHANTING, PROFESSION, FORGE }
	public enum Difficulty { EASY, NORMAL, HARD, ELITE, LEGENDARY }
	public record Offer(Kind kind, Difficulty difficulty, int target, int goal, int emeralds, int fragments,
					int roleXp, int reputation, boolean voucher, boolean emblem) {}

	private ContractRules() {}
	public static int activeLimit(int reputation) { return reputation >= 300 ? 3 : 2; }
	public static String rank(int reputation) {
		return reputation >= 700 ? "MAESTRO" : reputation >= 300 ? "VETERANO" : reputation >= 100 ? "OFICIAL" : "APRENDIZ";
	}
	public static int reputationTier(int reputation) { return reputation >= 700 ? 3 : reputation >= 300 ? 2 : reputation >= 100 ? 1 : 0; }
	public static List<Offer> dailyOffers(UUID player, long day, int reputation, int evolution) {
		Random random = new Random(player.getMostSignificantBits() ^ Long.rotateLeft(player.getLeastSignificantBits(), 17) ^ day);
		List<Offer> offers = new ArrayList<>(3);
		for (int i = 0; i < 3; i++) {
			int tier = Math.max(reputationTier(reputation), evolution >= 2 ? 4 : evolution >= 1 ? 3 : 0);
			Difficulty difficulty = Difficulty.values()[random.nextInt(tier + 1)];
			Kind kind = Kind.values()[random.nextInt(Kind.values().length)];
			int scale = difficulty.ordinal() + 1;
			int target = kind == Kind.HUNT ? random.nextInt(5) : kind == Kind.DELIVERY ? random.nextInt(6) : kind == Kind.PROFESSION ? random.nextInt(10) : -1;
			offers.add(new Offer(kind, difficulty, target, 4 + scale * 3 + random.nextInt(3), 2 + scale * 2,
					difficulty.ordinal() >= 3 ? 1 + scale / 2 : 0, 15 + scale * 10,
					10 + scale * 10, difficulty == Difficulty.ELITE && random.nextInt(3) == 0,
					difficulty == Difficulty.LEGENDARY && random.nextInt(4) == 0));
		}
		return List.copyOf(offers);
	}
	public static int progress(int current, int amount, int goal) {
		if (amount <= 0) return Math.max(0, Math.min(goal, current));
		return (int) Math.min(Math.max(0L, goal), (long) Math.max(0, current) + amount);
	}
}
