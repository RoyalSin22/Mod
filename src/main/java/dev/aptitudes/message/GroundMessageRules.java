package dev.aptitudes.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * C7 · S5 Mensajes en el suelo: reglas puras (sin Minecraft, con JUnit en {@code GroundMessageRulesTest}).
 *
 * <p>Un jugador deja una nota con una <b>frase predefinida</b> (nunca texto libre: no hay nada que moderar) y los demás
 * pueden valorarla una vez. Las notas caducan y cada jugador tiene un máximo y una espera entre notas. Los tiempos van en
 * ticks del reloj del mundo y los cálculos con valores de la configuración, en {@code long}.</p>
 */
public final class GroundMessageRules {
	/** Frases disponibles. El texto sale de la clave {@code message.aptitudes.phrase.<id>} en cada idioma. */
	public static final List<String> PHRASES = List.of("danger", "treasure", "rest", "hidden", "boss", "ambush", "trap",
			"look_up", "look_down", "dead_end", "need_light", "well_done");

	public static final long TICKS_PER_DAY = 24_000L;

	private GroundMessageRules() {}

	public static boolean validPhrase(String id) {
		return id != null && PHRASES.contains(id);
	}

	/** ¿Puede dejar otra nota quien ya tiene {@code placed}? */
	public static boolean underLimit(int placed, int max) {
		return placed < Math.max(0, max);
	}

	/** Espera entre notas del mismo jugador. Sin nota previa ({@code last < 0}) siempre está lista. */
	public static boolean cooldownReady(long last, long now, int cooldownSeconds) {
		if (last < 0) return true;
		long elapsed = now - last;
		return elapsed < 0 || elapsed >= Math.max(0L, (long) cooldownSeconds) * 20L;
	}

	public static boolean expired(long createdTick, long now, int lifetimeDays) {
		return now - createdTick >= Math.max(1L, (long) lifetimeDays) * TICKS_PER_DAY;
	}

	/** Resultado de valorar una nota. */
	public enum VoteResult { OK, UNKNOWN, OWN, ALREADY }

	/** Una nota colocada. */
	public static final class Entry {
		public final UUID author;
		public final String phrase;
		public final long created;
		private final Set<UUID> voters = new HashSet<>();

		public Entry(UUID author, String phrase, long created) {
			this.author = author;
			this.phrase = phrase;
			this.created = created;
		}

		public int votes() {
			return voters.size();
		}

		public Set<UUID> voters() {
			return java.util.Collections.unmodifiableSet(voters);
		}

		/** Para restaurar desde disco, con tope por si el archivo está dañado. */
		public void restoreVoter(UUID id) {
			if (voters.size() < MAX_VOTERS) voters.add(id);
		}
	}

	/** Tope de valoraciones guardadas por nota: protege el archivo y la memoria. */
	public static final int MAX_VOTERS = 500;

	/** Notas vivas del mundo, por id. */
	public static final class Book {
		private final Map<UUID, Entry> entries = new LinkedHashMap<>();
		private final Map<UUID, Long> lastPlaced = new HashMap<>();

		public Entry place(UUID id, UUID author, String phrase, long now) {
			Entry e = new Entry(author, phrase, now);
			entries.put(id, e);
			lastPlaced.put(author, now);
			return e;
		}

		/** Restaura una nota guardada (no toca la espera del autor). */
		public void restore(UUID id, Entry entry) {
			entries.put(id, entry);
		}

		public Entry get(UUID id) {
			return entries.get(id);
		}

		public boolean contains(UUID id) {
			return entries.containsKey(id);
		}

		public Map<UUID, Entry> entries() {
			return java.util.Collections.unmodifiableMap(entries);
		}

		public int countBy(UUID author) {
			int n = 0;
			for (Entry e : entries.values()) if (e.author.equals(author)) n++;
			return n;
		}

		public long lastPlacedBy(UUID author) {
			return lastPlaced.getOrDefault(author, -1L);
		}

		public VoteResult vote(UUID id, UUID voter) {
			Entry e = entries.get(id);
			if (e == null) return VoteResult.UNKNOWN;
			if (e.author.equals(voter)) return VoteResult.OWN;
			if (e.voters.contains(voter)) return VoteResult.ALREADY;
			if (e.voters.size() >= MAX_VOTERS) return VoteResult.OK; // tope: se acepta sin guardar más
			e.voters.add(voter);
			return VoteResult.OK;
		}

		public boolean remove(UUID id) {
			return entries.remove(id) != null;
		}

		/** Ids caducados; no los borra (quien llama quita primero las entidades). */
		public List<UUID> expiredIds(long now, int lifetimeDays) {
			List<UUID> out = new ArrayList<>();
			for (Map.Entry<UUID, Entry> e : entries.entrySet()) {
				if (expired(e.getValue().created, now, lifetimeDays)) out.add(e.getKey());
			}
			return out;
		}

		public void forgetPlayer(UUID player) {
			lastPlaced.remove(player);
		}

		public void clear() {
			entries.clear();
			lastPlaced.clear();
		}
	}
}
