package dev.aptitudes.event;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Expedición activa por jugador (F5), guardada por mundo. */
public final class ExpeditionState extends PersistentState {
	private static final String NAME = "aptitudes_expeditions";
	private static final Type<ExpeditionState> TYPE = new Type<>(ExpeditionState::new, ExpeditionState::fromNbt, null);
	private static final int MAX_LOADED = 10_000;

	/** Una expedición en curso. Campos mutables: los cambia {@link Expeditions}. */
	public static final class Expedition {
		public final UUID owner;
		public final double x;
		public final double y;
		public final double z;
		public final List<ExpeditionRules.Module> modules;
		/** Módulo actual (índice en {@link #modules}). */
		public int index;
		/** Acciones hechas en el módulo actual. */
		public int progress;
		public final long startTick;
		/** Fragmentos de alma en el inventario la última vez que se miró (para el módulo de recolección). */
		public int shardsSeen;

		public Expedition(UUID owner, double x, double y, double z, List<ExpeditionRules.Module> modules, long startTick) {
			this.owner = owner;
			this.x = x;
			this.y = y;
			this.z = z;
			this.modules = modules;
			this.startTick = startTick;
		}

		public ExpeditionRules.Module current() {
			return index < modules.size() ? modules.get(index) : null;
		}
	}

	private final Map<UUID, Expedition> active = new LinkedHashMap<>();

	public static ExpeditionState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public Expedition get(UUID owner) {
		return active.get(owner);
	}

	public Iterable<Expedition> all() {
		return active.values();
	}

	public void put(Expedition expedition) {
		active.put(expedition.owner, expedition);
		markDirty();
	}

	public void remove(UUID owner) {
		active.remove(owner);
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (Expedition e : active.values()) {
			NbtCompound entry = new NbtCompound();
			entry.putUuid("owner", e.owner);
			entry.putDouble("x", e.x);
			entry.putDouble("y", e.y);
			entry.putDouble("z", e.z);
			entry.putInt("index", e.index);
			entry.putInt("progress", e.progress);
			entry.putLong("start", e.startTick);
			entry.putInt("shards", e.shardsSeen);
			NbtList modules = new NbtList();
			for (ExpeditionRules.Module m : e.modules) modules.add(net.minecraft.nbt.NbtString.of(m.name()));
			entry.put("modules", modules);
			list.add(entry);
		}
		nbt.put("expeditions", list);
		return nbt;
	}

	private static ExpeditionState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		ExpeditionState state = new ExpeditionState();
		NbtList list = nbt.getList("expeditions", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < Math.min(list.size(), MAX_LOADED); i++) {
			NbtCompound entry = list.getCompound(i);
			if (!entry.containsUuid("owner")) continue;
			List<ExpeditionRules.Module> modules = new ArrayList<>();
			NbtList names = entry.getList("modules", NbtElement.STRING_TYPE);
			for (int j = 0; j < names.size(); j++) {
				try {
					modules.add(ExpeditionRules.Module.valueOf(names.getString(j)));
				} catch (IllegalArgumentException ignored) {
					// Módulo desconocido: se omite la expedición completa más abajo.
				}
			}
			if (modules.isEmpty() || modules.size() != names.size()) continue;
			Expedition e = new Expedition(entry.getUuid("owner"), entry.getDouble("x"), entry.getDouble("y"),
					entry.getDouble("z"), modules, entry.getLong("start"));
			e.index = Math.max(0, Math.min(modules.size(), entry.getInt("index")));
			e.progress = Math.max(0, entry.getInt("progress"));
			e.shardsSeen = Math.max(0, entry.getInt("shards"));
			state.active.put(e.owner, e);
		}
		return state;
	}
}
