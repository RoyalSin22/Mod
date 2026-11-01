package dev.aptitudes.unique;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Registro global (por mundo) de quién posee cada habilidad única. Se guarda en
 * {@code data/aptitudes_unique.dat} del Overworld, así sobrevive a reinicios y es
 * el mismo para todos los jugadores.
 */
public final class UniqueState extends PersistentState {
	public record Holder(UUID uuid, String name, long lastSeenMillis) {}

	private static final Type<UniqueState> TYPE = new Type<>(UniqueState::new, UniqueState::fromNbt, null);
	private static final String NAME = "aptitudes_unique";

	private final Map<UniqueSkill, Holder> holders = new EnumMap<>(UniqueSkill.class);

	public static UniqueState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	@Nullable
	public Holder holder(UniqueSkill skill) {
		return holders.get(skill);
	}

	@Nullable
	public UniqueSkill skillOf(UUID player) {
		for (Map.Entry<UniqueSkill, Holder> e : holders.entrySet()) {
			if (e.getValue().uuid().equals(player)) return e.getKey();
		}
		return null;
	}

	public void assign(UniqueSkill skill, UUID uuid, String name) {
		holders.put(skill, new Holder(uuid, name, System.currentTimeMillis()));
		markDirty();
	}

	public void release(UniqueSkill skill) {
		if (holders.remove(skill) != null) markDirty();
	}

	public void touch(UniqueSkill skill) {
		Holder h = holders.get(skill);
		if (h != null) {
			holders.put(skill, new Holder(h.uuid(), h.name(), System.currentTimeMillis()));
			markDirty();
		}
	}

	public Map<UniqueSkill, Holder> all() {
		return holders;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtCompound list = new NbtCompound();
		holders.forEach((skill, h) -> {
			NbtCompound entry = new NbtCompound();
			entry.putUuid("uuid", h.uuid());
			entry.putString("name", h.name());
			entry.putLong("last_seen", h.lastSeenMillis());
			list.put(skill.key(), entry);
		});
		nbt.put("holders", list);
		return nbt;
	}

	private static UniqueState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		UniqueState state = new UniqueState();
		NbtCompound list = nbt.getCompound("holders");
		for (String key : list.getKeys()) {
			UniqueSkill.byKey(key).ifPresent(skill -> {
				NbtCompound entry = list.getCompound(key);
				if (entry.containsUuid("uuid")) {
					state.holders.put(skill, new Holder(entry.getUuid("uuid"), entry.getString("name"),
							entry.getLong("last_seen")));
				}
			});
		}
		return state;
	}
}
