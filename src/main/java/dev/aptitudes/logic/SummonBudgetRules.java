package dev.aptitudes.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * V94: reglas puras del presupuesto de invocaciones por jugador. Cada invocación activa cuesta puntos según su tipo (un lobo pesa más
 * que un diablillo) y la suma de todas las de un jugador, sea cual sea el rol que las lanzó, no puede pasar del presupuesto. Si una
 * invocación nueva no cabe, se retiran las más antiguas hasta que quepa; la nueva nunca se rechaza.
 */
public final class SummonBudgetRules {
	/** Presupuesto por defecto: más de lo que un solo rol suele tener y menos que apilar varios roles. 0 = sin límite. */
	public static final int DEFAULT_BUDGET = 16;
	public static final int MAX_BUDGET = 64;

	private SummonBudgetRules() {
	}

	/** Una invocación viva de un jugador. */
	public record Entry(UUID id, int spawnTick, int cost) {
	}

	/** Coste en puntos de una invocación según el id de su tipo de entidad. */
	public static int costOfType(String typeId) {
		if (typeId == null) return 2;
		return switch (typeId) {
			case "minecraft:vex" -> 1;
			case "minecraft:zombie", "minecraft:skeleton" -> 2;
			case "minecraft:wolf" -> 3;
			case "minecraft:iron_golem", "minecraft:snow_golem" -> 4;
			default -> 2;
		};
	}

	public static int total(List<Entry> entries) {
		long sum = 0;
		for (Entry e : entries) sum += Math.max(0, e.cost());
		return (int) Math.min(Integer.MAX_VALUE, sum);
	}

	/**
	 * Invocaciones que hay que retirar (de la más antigua a la más reciente) para que {@code newCost} quepa en {@code budget}. Con
	 * presupuesto 0 o negativo no hay límite. Si el coste nuevo solo ya supera el presupuesto, se retira todo lo demás y la nueva se
	 * acepta igualmente.
	 */
	public static List<UUID> evictions(List<Entry> current, int newCost, int budget) {
		List<UUID> out = new ArrayList<>();
		if (budget <= 0 || current.isEmpty()) return out;
		List<Entry> sorted = new ArrayList<>(current);
		sorted.sort(Comparator.comparingInt(Entry::spawnTick).thenComparing(Entry::id));
		long used = total(sorted);
		long need = Math.max(0, newCost);
		for (Entry e : sorted) {
			if (used + need <= budget) break;
			used -= Math.max(0, e.cost());
			out.add(e.id());
		}
		return out;
	}

	/** Valida el presupuesto de la configuración: 0 (sin límite) a {@link #MAX_BUDGET}. */
	public static int clamp(int budget) {
		return Math.max(0, Math.min(MAX_BUDGET, budget));
	}
}
