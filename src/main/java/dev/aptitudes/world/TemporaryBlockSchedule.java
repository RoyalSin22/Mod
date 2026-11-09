package dev.aptitudes.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agenda de bloques temporales de un mundo: posición ({@code BlockPos#asLong}) → bloque e instante de
 * caducidad en tiempo de juego. Clase pura (sin Minecraft) para poder probarla; la persistencia y la
 * retirada real están en {@link TemporaryBlocks}.
 */
public final class TemporaryBlockSchedule {
	/** Tope de bloques temporales por mundo (un muro son 15): protege de datos corruptos o abusos. */
	static final int MAX_ENTRIES = 4096;

	public record Entry(long pos, String block, long expireAt) {}

	private final Map<Long, Entry> entries = new HashMap<>();

	/** Registra un bloque temporal. Devuelve false si se alcanzó el tope (no se debe colocar). */
	public boolean add(long pos, String block, long expireAt) {
		if (!entries.containsKey(pos) && entries.size() >= MAX_ENTRIES) return false;
		entries.put(pos, new Entry(pos, block, expireAt));
		return true;
	}

	public boolean contains(long pos) {
		return entries.containsKey(pos);
	}

	Entry get(long pos) {
		return entries.get(pos);
	}

	public Entry remove(long pos) {
		return entries.remove(pos);
	}

	/** Entradas caducadas en {@code now} (no las elimina: quien las retira decide). */
	public List<Entry> expired(long now) {
		List<Entry> out = new ArrayList<>();
		for (Entry e : entries.values()) if (e.expireAt() <= now) out.add(e);
		return out;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public int size() {
		return entries.size();
	}

	public Iterable<Entry> all() {
		return entries.values();
	}
}
