package dev.aptitudes.social;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * C7 (S5): notas en el suelo de una dimensión (fuente de verdad; los clientes solo reciben copias). Solo se toca desde
 * el hilo del servidor.
 *
 * <p>El estado del Mundo Normal guarda además el registro global de valoraciones: totales por autor (para el logro) y
 * las recompensas del día (freno contra granjas, {@link GroundMessageRules#rewards}).</p>
 */
public final class GroundMessageState extends PersistentState {
	private static final String NAME = "aptitudes_ground_messages";
	private static final Type<GroundMessageState> TYPE = new Type<>(GroundMessageState::new, GroundMessageState::fromNbt, null);

	/** Una nota. {@code raters} guarda como mucho {@link GroundMessageRules#MAX_RATERS_STORED} jugadores. */
	public static final class Note {
		public final UUID id;
		public final BlockPos pos;
		public final UUID author;
		public final String authorName;
		public final String template;
		public final String word;
		public final long created;
		int ratings;
		final Set<UUID> raters = new LinkedHashSet<>();

		Note(UUID id, BlockPos pos, UUID author, String authorName, String template, String word, long created) {
			this.id = id;
			this.pos = pos;
			this.author = author;
			this.authorName = authorName;
			this.template = template;
			this.word = word;
			this.created = created;
		}

		public int ratings() { return ratings; }

		public boolean ratedBy(UUID player) { return raters.contains(player); }
	}

	private final Map<UUID, Note> notes = new LinkedHashMap<>();
	// Registro global (solo se usa el del Mundo Normal).
	private final Map<UUID, Integer> totalRatings = new HashMap<>();
	private long rewardDay = Long.MIN_VALUE;
	private final Map<UUID, Integer> rewardsToday = new HashMap<>();
	private final Set<String> rewardPairsToday = new LinkedHashSet<>();

	public static GroundMessageState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public Iterable<Note> notes() {
		return notes.values();
	}

	public Note note(UUID id) {
		return notes.get(id);
	}

	public int size() {
		return notes.size();
	}

	Note add(BlockPos pos, UUID author, String authorName, String template, String word, long created) {
		Note n = new Note(UUID.randomUUID(), pos.toImmutable(), author, authorName, template, word, created);
		notes.put(n.id, n);
		markDirty();
		return n;
	}

	boolean remove(UUID id) {
		boolean removed = notes.remove(id) != null;
		if (removed) markDirty();
		return removed;
	}

	/** Borra las notas caducadas; devuelve cuántas. */
	int removeExpired(long now, int lifetimeDays) {
		int before = notes.size();
		notes.values().removeIf(n -> GroundMessageRules.expired(n.created, now, lifetimeDays));
		int removed = before - notes.size();
		if (removed > 0) markDirty();
		return removed;
	}

	/** Total de valoraciones recibidas por un autor (registro global). */
	public int total(UUID author) {
		return totalRatings.getOrDefault(author, 0);
	}

	/** Suma una valoración (sin comprobar reglas: lo hace {@link GroundMessages}). */
	void rate(Note note, UUID rater) {
		note.ratings = (int) Math.min(Integer.MAX_VALUE, (long) note.ratings + 1);
		if (note.raters.size() < GroundMessageRules.MAX_RATERS_STORED) note.raters.add(rater);
		markDirty();
	}

	// ------------------------------------------------------------------ Registro global

	/** Suma una valoración al total del autor y devuelve el nuevo total. */
	int addTotal(UUID author) {
		int total = (int) Math.min(Integer.MAX_VALUE, (long) totalRatings.getOrDefault(author, 0) + 1);
		totalRatings.put(author, total);
		markDirty();
		return total;
	}

	private void rollDay(long day) {
		if (day == rewardDay) return;
		rewardDay = day;
		rewardsToday.clear();
		rewardPairsToday.clear();
		markDirty();
	}

	int rewardsToday(UUID author, long day) {
		rollDay(day);
		return rewardsToday.getOrDefault(author, 0);
	}

	boolean raterRewardedToday(UUID author, UUID rater, long day) {
		rollDay(day);
		return rewardPairsToday.contains(author + "|" + rater);
	}

	void recordReward(UUID author, UUID rater, long day) {
		rollDay(day);
		rewardsToday.merge(author, 1, Integer::sum);
		rewardPairsToday.add(author + "|" + rater);
		markDirty();
	}

	// ------------------------------------------------------------------ Guardado

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Note n : notes.values()) {
			NbtCompound c = new NbtCompound();
			c.putUuid("id", n.id);
			c.putLong("pos", n.pos.asLong());
			c.putUuid("author", n.author);
			c.putString("author_name", n.authorName);
			c.putString("template", n.template);
			c.putString("word", n.word);
			c.putLong("created", n.created);
			c.putInt("ratings", n.ratings);
			NbtList raters = new NbtList();
			for (UUID r : n.raters) {
				NbtCompound rc = new NbtCompound();
				rc.putUuid("id", r);
				raters.add(rc);
			}
			c.put("raters", raters);
			list.add(c);
		}
		nbt.put("notes", list);
		NbtList totals = new NbtList();
		for (Map.Entry<UUID, Integer> e : totalRatings.entrySet()) {
			NbtCompound c = new NbtCompound();
			c.putUuid("id", e.getKey());
			c.putInt("total", e.getValue());
			totals.add(c);
		}
		nbt.put("totals", totals);
		nbt.putLong("reward_day", rewardDay);
		NbtList today = new NbtList();
		for (Map.Entry<UUID, Integer> e : rewardsToday.entrySet()) {
			NbtCompound c = new NbtCompound();
			c.putUuid("id", e.getKey());
			c.putInt("count", e.getValue());
			today.add(c);
		}
		nbt.put("rewards_today", today);
		NbtList pairs = new NbtList();
		for (String p : rewardPairsToday) {
			NbtCompound c = new NbtCompound();
			c.putString("pair", p);
			pairs.add(c);
		}
		nbt.put("reward_pairs", pairs);
		return nbt;
	}

	private static GroundMessageState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		GroundMessageState state = new GroundMessageState();
		NbtList list = nbt.getList("notes", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound c = list.getCompound(i);
			String template = c.getString("template");
			String word = c.getString("word");
			// Una pieza que ya no existe en el catálogo deja la nota ilegible: se descarta.
			if (!c.containsUuid("id") || !c.containsUuid("author") || !MessageCatalog.isValid(template, word)) continue;
			Note n = new Note(c.getUuid("id"), BlockPos.fromLong(c.getLong("pos")), c.getUuid("author"),
					c.getString("author_name"), template, word, c.getLong("created"));
			n.ratings = Math.max(0, c.getInt("ratings"));
			NbtList raters = c.getList("raters", NbtElement.COMPOUND_TYPE);
			for (int j = 0; j < raters.size() && n.raters.size() < GroundMessageRules.MAX_RATERS_STORED; j++) {
				NbtCompound rc = raters.getCompound(j);
				if (rc.containsUuid("id")) n.raters.add(rc.getUuid("id"));
			}
			state.notes.put(n.id, n);
		}
		NbtList totals = nbt.getList("totals", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < totals.size(); i++) {
			NbtCompound c = totals.getCompound(i);
			if (c.containsUuid("id")) state.totalRatings.put(c.getUuid("id"), Math.max(0, c.getInt("total")));
		}
		state.rewardDay = nbt.contains("reward_day") ? nbt.getLong("reward_day") : Long.MIN_VALUE;
		NbtList today = nbt.getList("rewards_today", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < today.size(); i++) {
			NbtCompound c = today.getCompound(i);
			if (c.containsUuid("id")) state.rewardsToday.put(c.getUuid("id"), Math.max(0, c.getInt("count")));
		}
		NbtList pairs = nbt.getList("reward_pairs", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < pairs.size(); i++) state.rewardPairsToday.add(pairs.getCompound(i).getString("pair"));
		return state;
	}
}
