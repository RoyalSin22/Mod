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
 * posiciones más antiguas (orden de inserción).</p>
 *
 * <p>v36: se guarda con el mundo ({@code PlacedBlocksState}) mediante {@link #snapshot} y {@link #load}, así
 * que reiniciar el servidor ya no vuelve a hacer que los bloques colocados den XP.</p>
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

	/** Posiciones de un mundo en orden de inserción (de la más antigua a la más reciente), para guardarlas. */
	public long[] snapshot(String world) {
		LinkedHashSet<Long> set = placed.get(world);
		if (set == null) return new long[0];
		long[] out = new long[set.size()];
		int i = 0;
		for (long pos : set) out[i++] = pos;
		return out;
	}

	/** Sustituye las posiciones de un mundo por las guardadas; si hay más que el tope, conserva las más recientes. */
	public void load(String world, long[] positions) {
		LinkedHashSet<Long> set = new LinkedHashSet<>();
		if (positions != null) {
			int start = Math.max(0, positions.length - maxPerWorld);
			for (int i = start; i < positions.length; i++) {
				set.remove(positions[i]);
				set.add(positions[i]);
			}
		}
		if (set.isEmpty()) placed.remove(world);
		else placed.put(world, set);
	}

	/** Olvida un mundo (al descargarlo, para no mezclar partidas distintas en el mismo cliente). */
	public void clear(String world) {
		placed.remove(world);
	}

	public int size(String world) {
		LinkedHashSet<Long> set = placed.get(world);
		return set == null ? 0 : set.size();
	}
}
