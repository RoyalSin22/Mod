package dev.aptitudes.contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Generación determinista y balance puro de los contratos del gremio (v30). Sin Minecraft, con pruebas.
 *
 * <p>Cada jugador recibe 3 ofertas al día, fijas para ese jugador y ese día (no cambian al cerrar y abrir el
 * tablón). La primera es la «destacada»: siempre de la dificultad máxima que permiten su reputación y la
 * evolución de su rol principal. Los tipos disponibles dependen de la dificultad: los contratos de Jefe solo
 * son Legendarios y los de Evento empiezan en Difícil.</p>
 */
public final class ContractRules {
	/** Tipos de contrato. Se guardan por ordinal dentro del contrato aceptado: solo se añade al final. */
	public enum Kind { HUNT, DELIVERY, FISHING, ENCHANTING, PROFESSION, FORGE, EVENT, BOSS, NETHER, NEW_MOB, EXPEDITION }
	/** Dificultades. Élite con el rol en Maestro y Legendario en Definitivo. Solo se añade al final. */
	public enum Difficulty { EASY, NORMAL, HARD, ELITE, LEGENDARY }

	public record Offer(Kind kind, Difficulty difficulty, int target, int goal, int emeralds, int fragments,
					int roleXp, int reputation, boolean voucher, boolean emblem) {}

	/** Número de objetivos posibles por tipo (criaturas, objetos, habilidades, eventos, jefes). */
	public static final int HUNT_TARGETS = 5;
	public static final int DELIVERY_TARGETS = 6;
	public static final int PROFESSION_TARGETS = 10;
	public static final int EVENT_TARGETS = 3;
	public static final int BOSS_TARGETS = 4;
	public static final int OFFERS_PER_DAY = 3;
	public static final int MAX_ACTIVE = 3;

	private ContractRules() {}

	public static int activeLimit(int reputation) { return reputation >= 300 ? 3 : 2; }

	public static String rank(int reputation) {
		return reputation >= 700 ? "MAESTRO" : reputation >= 300 ? "VETERANO" : reputation >= 100 ? "OFICIAL" : "APRENDIZ";
	}

	/** Rango como índice (0 Aprendiz, 1 Oficial, 2 Veterano, 3 Maestro), para traducirlo en el cliente. */
	public static int rankIndex(int reputation) {
		return reputation >= 700 ? 3 : reputation >= 300 ? 2 : reputation >= 100 ? 1 : 0;
	}

	public static int reputationTier(int reputation) { return reputation >= 300 ? 2 : reputation >= 100 ? 1 : 0; }

	public static Difficulty maxDifficulty(int reputation, int evolution) {
		int tier = Math.max(reputationTier(reputation), evolution >= 2 ? 4 : evolution >= 1 ? 3 : 0);
		return Difficulty.values()[Math.max(0, Math.min(Difficulty.values().length - 1, tier))];
	}

	/** Tipos que pueden salir con cada dificultad. */
	public static List<Kind> kindsFor(Difficulty difficulty) {
		return switch (difficulty) {
			case EASY -> List.of(Kind.HUNT, Kind.DELIVERY, Kind.FISHING, Kind.PROFESSION);
			case NORMAL -> List.of(Kind.HUNT, Kind.DELIVERY, Kind.FISHING, Kind.PROFESSION, Kind.ENCHANTING, Kind.FORGE,
					Kind.NEW_MOB);
			case HARD -> List.of(Kind.HUNT, Kind.DELIVERY, Kind.ENCHANTING, Kind.FORGE, Kind.EVENT, Kind.NETHER, Kind.NEW_MOB);
			case ELITE -> List.of(Kind.HUNT, Kind.DELIVERY, Kind.ENCHANTING, Kind.FORGE, Kind.EVENT, Kind.NETHER, Kind.NEW_MOB,
					Kind.EXPEDITION);
			case LEGENDARY -> List.of(Kind.HUNT, Kind.EVENT, Kind.BOSS, Kind.NEW_MOB, Kind.EXPEDITION);
		};
	}

	/** Cantidad necesaria según tipo y dificultad ({@code s} = 1–5). Eventos y jefes: siempre 1. */
	public static int goal(Kind kind, Difficulty difficulty, int extra) {
		int s = difficulty.ordinal() + 1;
		int e = Math.max(0, extra);
		return switch (kind) {
			case HUNT -> 6 + 4 * s + e;
			case DELIVERY -> 16 * s;
			case FISHING -> 3 + 2 * s + e;
			case ENCHANTING -> 1 + s;
			case PROFESSION -> 150 * s;
			case FORGE -> s >= 4 ? 2 : 1;
			case EVENT, BOSS, EXPEDITION -> 1;
			case NETHER -> 4 + 4 * s + e;
			case NEW_MOB -> 1 + s;
		};
	}

	private static int targetFor(Kind kind, Random random) {
		return switch (kind) {
			case HUNT -> random.nextInt(HUNT_TARGETS);
			case DELIVERY -> random.nextInt(DELIVERY_TARGETS);
			case PROFESSION -> random.nextInt(PROFESSION_TARGETS);
			case EVENT -> random.nextInt(EVENT_TARGETS);
			case BOSS -> random.nextInt(BOSS_TARGETS);
			default -> -1;
		};
	}

	public static List<Offer> dailyOffers(UUID player, long day, int reputation, int evolution) {
		Random random = new Random(player.getMostSignificantBits() ^ Long.rotateLeft(player.getLeastSignificantBits(), 17) ^ day);
		int max = maxDifficulty(reputation, evolution).ordinal();
		List<Offer> offers = new ArrayList<>(OFFERS_PER_DAY);
		for (int i = 0; i < OFFERS_PER_DAY; i++) {
			// La primera es la destacada: siempre de la dificultad máxima permitida.
			Difficulty difficulty = Difficulty.values()[i == 0 ? max : random.nextInt(max + 1)];
			List<Kind> kinds = kindsFor(difficulty);
			Kind kind = kinds.get(random.nextInt(kinds.size()));
			int s = difficulty.ordinal() + 1;
			int tier = reputationTier(reputation);
			// Recompensas por nivel (v65): Oficial y Veterano pagan más esmeraldas, fragmentos y vales.
			int emeralds = 2 + s * 2 + (kind == Kind.BOSS ? 6 : 0) + tier * (2 + s);
			int fragments = (difficulty.ordinal() >= 3 ? 1 + s / 2 : 0) + (difficulty.ordinal() >= 2 ? tier : 0);
			boolean voucher = (difficulty == Difficulty.ELITE && random.nextInt(3) == 0)
					|| (difficulty == Difficulty.HARD && tier == 2 && random.nextInt(4) == 0);
			offers.add(new Offer(kind, difficulty, targetFor(kind, random), goal(kind, difficulty, random.nextInt(3)),
					emeralds, fragments, 15 + s * 10, 10 + s * 10, voucher,
					difficulty == Difficulty.LEGENDARY && random.nextInt(4) == 0));
		}
		return List.copyOf(offers);
	}

	public static int progress(int current, int amount, int goal) {
		if (amount <= 0) return Math.max(0, Math.min(goal, current));
		return (int) Math.min(Math.max(0L, goal), (long) Math.max(0, current) + amount);
	}

	/** Una entrega solo se acepta completa: si faltan objetos no se quita ninguno. */
	public static boolean canDeliver(int have, int need) {
		return need > 0 && have >= need;
	}
}
