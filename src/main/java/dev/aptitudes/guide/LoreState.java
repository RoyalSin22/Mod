package dev.aptitudes.guide;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Estado de desbloqueo del lore del Grimorio por jugador (v67, T7a).
 *
 * <p>Almacena qué entradas de lore se han desbloqueado: cada entrada tiene un id y se marca como
 * desbloqueada. Los desbloqueos son permanentes (no se pierden al morir).
 *
 * @param unlocked conjunto de ids de lore desbloqueados
 */
public record LoreState(Map<String, Boolean> unlocked) {
	public static final Codec<LoreState> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Codec.STRING, Codec.BOOL)
					.optionalFieldOf("unlocked", Collections.emptyMap())
					.forGetter(LoreState::unlocked)
	).apply(i, LoreState::new));

	public LoreState {
		unlocked = Map.copyOf(unlocked);
	}

	public LoreState() {
		this(new TreeMap<>());
	}

	/** True si el lore de una entrada está desbloqueado. */
	public boolean isUnlocked(String loreId) {
		return unlocked.getOrDefault(loreId, false);
	}

	/** Desbloquea una entrada de lore. Retorna un nuevo state (inmutable). */
	public LoreState unlock(String loreId) {
		if (isUnlocked(loreId)) return this;
		Map<String, Boolean> updated = new TreeMap<>(unlocked);
		updated.put(loreId, true);
		return new LoreState(updated);
	}

	/** Desbloquea múltiples entradas. Retorna un nuevo state. */
	public LoreState unlockAll(Set<String> loreIds) {
		Map<String, Boolean> updated = new TreeMap<>(unlocked);
		boolean changed = false;
		for (String id : loreIds) {
			if (!updated.containsKey(id)) {
				updated.put(id, true);
				changed = true;
			}
		}
		return changed ? new LoreState(updated) : this;
	}
}
