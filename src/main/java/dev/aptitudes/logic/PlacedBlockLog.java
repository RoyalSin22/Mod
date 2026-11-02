package dev.aptitudes.logic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Registro de posiciones de bloques colocados por jugadores, por mundo (anti "colocar y romper").
 *
 * <p>Clase pura (sin Minecraft) para poder probarla. Las posiciones se guardan como el {@code long}
 * empaquetado de {@code BlockPos#asLong}. Cada mundo tiene un tope: al superarlo se olvidan las
 * posiciones más antiguas (orden de inserción). Vive en memoria: tras reiniciar el servidor los bloques
 * colocados antes vuelven a dar XP, lo que no compensa como granja.</p>
 */
public final class PlacedBlockLog {
	private final int maxPerWorld;
	private final Map<String, LinkedHashSet<Long>> placed = new HashMap<>();

	public PlacedBlockLog(int maxPerWorld) {
		this.maxPerWorld = Math.max(1, maxPerWorld);
	}

	/** Anota que un jugador colocó un bloque en {@code pos}. */
	public void mark(String world, long pos) {
		LinkedHashSet<Long> set = placed.computeIfAbsent(world, k -> new LinkedHashSet<>());
		set.remove(pos); // reinsertar: pasa a ser la entrada más reciente
		set.add(pos);
		if (set.size() > maxPerWorld) {
			Iterator<Long> oldest = set.iterator();
			oldest.next();
			oldest.remove();
		}
	}

	/** ¿Lo colocó un jugador? Si es así, lo olvida (el bloque ya se ha roto). */
	public boolean consume(String world, long pos) {
		LinkedHashSet<Long> set = placed.get(world);
		return set != null && set.remove(pos);
	}

	public boolean contains(String world, long pos) {
		LinkedHashSet<Long> set = placed.get(world);
		return set != null && set.contains(pos);
	}

	public int size(String world) {
		LinkedHashSet<Long> set = placed.get(world);
		return set == null ? 0 : set.size();
	}
}
