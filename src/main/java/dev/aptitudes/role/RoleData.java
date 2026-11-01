package dev.aptitudes.role;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Estado persistente de roles de un jugador. Igual que {@code PlayerProgress},
 * los puntos de árbol disponibles se derivan (nivel − rangos comprados) en lugar
 * de almacenarse.
 */
public final class RoleData {
	private static final Codec<Map<String, Integer>> INT_MAP = Codec.unboundedMap(Codec.STRING, Codec.INT);

	public static final Codec<RoleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("primary", "").forGetter(d -> d.primary == null ? "" : d.primary.key()),
			Codec.STRING.optionalFieldOf("secondary", "").forGetter(d -> d.secondary == null ? "" : d.secondary.key()),
			INT_MAP.optionalFieldOf("levels", Map.of()).forGetter(d -> toNames(d.levels)),
			INT_MAP.optionalFieldOf("xp", Map.of()).forGetter(d -> toNames(d.xp)),
			INT_MAP.optionalFieldOf("ranks", Map.of()).forGetter(d -> new HashMap<>(d.ranks)),
			Codec.DOUBLE.optionalFieldOf("energy", 0.0).forGetter(d -> d.energy),
			Codec.LONG.optionalFieldOf("last_change", Long.MIN_VALUE).forGetter(d -> d.lastRoleChange),
			Codec.STRING.listOf().optionalFieldOf("elements", List.of())
					.forGetter(d -> d.elements.stream().map(Element::key).toList()),
			Codec.INT.optionalFieldOf("active_element", 0).forGetter(d -> d.activeElement),
			INT_MAP.optionalFieldOf("tiers", Map.of()).forGetter(d -> toNames(d.tiers)),
			INT_MAP.optionalFieldOf("trials", Map.of()).forGetter(d -> toNames(d.trials)),
			Codec.STRING.listOf().optionalFieldOf("boss_slain", List.of())
					.forGetter(d -> d.bossSlain.stream().map(Role::key).toList())
	).apply(instance, RoleData::new));

	@Nullable private Role primary;
	@Nullable private Role secondary;
	private final EnumMap<Role, Integer> levels;
	private final EnumMap<Role, Integer> xp;
	private final Map<String, Integer> ranks;
	private double energy;
	/** Tiempo del mundo (ticks) del último cambio de rol. */
	private long lastRoleChange;
	/** Elementos dominados por el Elementalista, en orden de elección (máx. 3). */
	private final List<Element> elements;
	/** Índice dentro de {@link #elements} del elemento activo. */
	private int activeElement;
	/** Nivel de evolución por rol (ver {@link Evolution}). */
	private final EnumMap<Role, Integer> tiers;
	/** Progreso de la prueba de Maestro por rol. */
	private final EnumMap<Role, Integer> trials;
	/** Roles con los que se ha derrotado a un jefe (requisito del Definitivo). */
	private final EnumSet<Role> bossSlain;

	public RoleData() {
		this("", "", Map.of(), Map.of(), Map.of(), 0.0, Long.MIN_VALUE, List.of(), 0, Map.of(), Map.of(), List.of());
	}

	private RoleData(String primary, String secondary, Map<String, Integer> levels, Map<String, Integer> xp,
					 Map<String, Integer> ranks, double energy, long lastRoleChange,
					 List<String> elements, int activeElement, Map<String, Integer> tiers,
					 Map<String, Integer> trials, List<String> bossSlain) {
		this.primary = Role.byKey(primary).orElse(null);
		this.secondary = Role.byKey(secondary).orElse(null);
		if (this.secondary == this.primary) this.secondary = null;
		this.levels = fromNames(levels);
		this.xp = fromNames(xp);
		this.ranks = new HashMap<>();
		ranks.forEach((id, r) -> {
			if (RoleTree.get(id) != null && r > 0) this.ranks.put(id, Math.min(r, RoleNode.MAX_RANK));
		});
		this.energy = Math.max(0.0, energy);
		this.lastRoleChange = lastRoleChange;
		this.elements = new ArrayList<>();
		for (String key : elements) {
			Element.byKey(key).filter(e -> !this.elements.contains(e)).ifPresent(this.elements::add);
			if (this.elements.size() >= Element.MAX_MASTERED) break;
		}
		this.activeElement = this.elements.isEmpty() ? 0 : Math.floorMod(activeElement, this.elements.size());
		this.tiers = fromNames(tiers);
		this.tiers.replaceAll((r, t) -> Math.min(t, Evolution.ULTIMATE));
		this.trials = fromNames(trials);
		this.bossSlain = EnumSet.noneOf(Role.class);
		for (String key : bossSlain) Role.byKey(key).ifPresent(this.bossSlain::add);
	}

	public int tier(Role role) { return tiers.getOrDefault(role, Evolution.BASE); }
	public void setTier(Role role, int tier) { tiers.put(role, Math.max(0, Math.min(Evolution.ULTIMATE, tier))); }
	public int trialProgress(Role role) { return trials.getOrDefault(role, 0); }
	public void addTrialProgress(Role role, int amount) {
		trials.merge(role, amount, (a, b) -> (int) Math.min(Integer.MAX_VALUE, (long) a + b));
	}
	public boolean hasSlainBoss(Role role) { return bossSlain.contains(role); }
	public void markBossSlain(Role role) { bossSlain.add(role); }

	@Nullable public Role primary() { return primary; }
	@Nullable public Role secondary() { return secondary; }
	public void setPrimary(@Nullable Role role) { this.primary = role; }
	public void setSecondary(@Nullable Role role) { this.secondary = role; }

	public boolean isActive(Role role) {
		return role == primary || role == secondary;
	}

	public int level(Role role) { return levels.getOrDefault(role, 0); }
	public void setLevel(Role role, int value) { levels.put(role, Math.max(0, value)); }
	public int xp(Role role) { return xp.getOrDefault(role, 0); }
	public void setXp(Role role, int value) { xp.put(role, Math.max(0, value)); }

	public int rank(String nodeId) { return ranks.getOrDefault(nodeId, 0); }
	public void setRank(String nodeId, int rank) {
		if (rank <= 0) ranks.remove(nodeId); else ranks.put(nodeId, Math.min(rank, RoleNode.MAX_RANK));
	}
	public Map<String, Integer> ranks() { return ranks; }

	public int spentPoints(Role role) {
		int sum = 0;
		for (RoleNode node : RoleTree.nodes(role)) sum += rank(node.id());
		return sum;
	}

	/** Reinicia el árbol de un rol. En el Elementalista también libera los elementos dominados. */
	public void clearRanks(Role role) {
		for (RoleNode node : RoleTree.nodes(role)) ranks.remove(node.id());
		if (role == Role.ELEMENTALIST) clearElements();
	}

	public List<Element> elements() { return Collections.unmodifiableList(elements); }

	public boolean masters(Element element) { return elements.contains(element); }

	public boolean addElement(Element element) {
		if (elements.contains(element) || elements.size() >= Element.MAX_MASTERED) return false;
		elements.add(element);
		if (elements.size() == 1) activeElement = 0;
		return true;
	}

	public void clearElements() {
		elements.clear();
		activeElement = 0;
	}

	@Nullable
	public Element activeElement() {
		return elements.isEmpty() ? null : elements.get(activeElement);
	}

	public int activeElementIndex() { return activeElement; }

	public void cycleElement() {
		if (!elements.isEmpty()) activeElement = (activeElement + 1) % elements.size();
	}

	public double energy() { return energy; }
	public void setEnergy(double value) { this.energy = Math.max(0.0, value); }

	public long lastRoleChange() { return lastRoleChange; }
	public void setLastRoleChange(long time) { this.lastRoleChange = time; }

	private static Map<String, Integer> toNames(EnumMap<Role, Integer> map) {
		Map<String, Integer> out = new HashMap<>();
		map.forEach((k, v) -> {
			if (v != 0) out.put(k.key(), v);
		});
		return out;
	}

	private static EnumMap<Role, Integer> fromNames(Map<String, Integer> map) {
		EnumMap<Role, Integer> out = new EnumMap<>(Role.class);
		map.forEach((k, v) -> Role.byKey(k.toLowerCase(Locale.ROOT)).ifPresent(r -> out.put(r, Math.max(0, v))));
		return out;
	}
}
