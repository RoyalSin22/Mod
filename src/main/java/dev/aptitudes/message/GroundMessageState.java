package dev.aptitudes.message;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.Map;
import java.util.UUID;

/** C7 · S5: notas colocadas y sus valoraciones, en el almacén persistente del Mundo Normal. */
public final class GroundMessageState extends PersistentState {
	private static final String NAME = "aptitudes_ground_messages";
	private static final Type<GroundMessageState> TYPE = new Type<>(GroundMessageState::new, GroundMessageState::fromNbt, null);
	/** Tope de notas guardadas: protege el archivo si algo las multiplica. */
	private static final int MAX_ENTRIES = 5_000;

	final GroundMessageRules.Book book = new GroundMessageRules.Book();

	public static GroundMessageState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public GroundMessageRules.Book book() {
		return book;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Map.Entry<UUID, GroundMessageRules.Entry> e : book.entries().entrySet()) {
			NbtCompound tag = new NbtCompound();
			tag.putString("id", e.getKey().toString());
			tag.putString("author", e.getValue().author.toString());
			tag.putString("phrase", e.getValue().phrase);
			tag.putLong("created", e.getValue().created);
			NbtList voters = new NbtList();
			for (UUID v : e.getValue().voters()) voters.add(NbtString.of(v.toString()));
			tag.put("voters", voters);
			list.add(tag);
		}
		nbt.put("messages", list);
		return nbt;
	}

	private static GroundMessageState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		GroundMessageState state = new GroundMessageState();
		NbtList list = nbt.getList("messages", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size() && i < MAX_ENTRIES; i++) {
			NbtCompound tag = list.getCompound(i);
			try {
				String phrase = tag.getString("phrase");
				if (!GroundMessageRules.validPhrase(phrase)) continue; // frase retirada o archivo dañado
				GroundMessageRules.Entry entry = new GroundMessageRules.Entry(UUID.fromString(tag.getString("author")), phrase,
						tag.getLong("created"));
				NbtList voters = tag.getList("voters", NbtElement.STRING_TYPE);
				for (int v = 0; v < voters.size() && v < GroundMessageRules.MAX_VOTERS; v++) {
					try {
						entry.restoreVoter(UUID.fromString(voters.getString(v)));
					} catch (IllegalArgumentException ignored) {
						// votante corrupto: se omite
					}
				}
				state.book.restore(UUID.fromString(tag.getString("id")), entry);
			} catch (IllegalArgumentException ignored) {
				// nota con UUID corrupto: se descarta
			}
		}
		return state;
	}
}
