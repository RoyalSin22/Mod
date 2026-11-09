package dev.aptitudes.event;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Expediciones activas (F5), guardadas por mundo. Una por grupo: el dueño la inicia y los miembros la comparten. */
public final class ExpeditionState extends PersistentState {
	private static final String NAME = "aptitudes_expeditions";
	private static final Type<ExpeditionState> TYPE = new Type<>(ExpeditionState::new, ExpeditionState::fromNbt, null);
	private static final int MAX_LOADED = 10_000;
	private static final int MAX_MEMBERS = 5;

	/** Una expedición en curso. Campos mutables: los cambia {@link Expeditions}. */
	public static final class Expedition {
		/** Jugador que la inició; la clave del mapa. */
		public final UUID owner;
		public final double x;
		public final double y;
		public final double z;
		public final List<ExpeditionRules.Module> modules;
		/** Todos los que comparten la expedición (incluye al dueño). */
		public final Set<UUID> members = new LinkedHashSet<>();
		/** Modificadores activos (ids de {@link ExpeditionRules#MODIFIERS}). */
		public final List<String> modifiers = new ArrayList<>();
		/** Módulo actual (índice en {@link #modules}). */
		public int index;
		/** Acciones hechas en el módulo actual. */
		public int progress;
		public final long startTick;
		/** Fragmentos de alma en el inventario del dueño la última vez que se miró (módulo de recolección). */
		public int shardsSeen;

		public Expedition(UUID owner, double x, double y, double z, List<ExpeditionRules.Module> modules, long startTick) {
			this.owner = owner;
			this.x = x;
			this.y = y;
			this.z = z;
			this.modules = modules;
			this.startTick = startTick;
			this.members.add(owner);
		}

		public ExpeditionRules.Module current() {
			return index < modules.size() ? modules.get(index) : null;
		}
	}

	private final Map<UUID, Expedition> active = new LinkedHashMap<>();

	public static ExpeditionState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	/** La expedición en la que participa este jugador (como dueño o como miembro), o {@code null}. */
	public Expedition get(UUID player) {
		for (Expedition e : active.values()) {
			if (e.members.contains(player)) return e;
		}
		return null;
	}

	public Iterable<Expedition> all() {
		return active.values();
	}

	/** Guarda la expedición por su dueño. */
	public void put(Expedition expedition) {
		active.put(expedition.owner, expedition);
		markDirty();
	}

	/** Borra la expedición del dueño (y con ella la de todos sus miembros). */
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
			for (ExpeditionRules.Module m : e.modules) modules.add(NbtString.of(m.name()));
			entry.put("modules", modules);
			NbtList members = new NbtList();
			for (UUID m : e.members) members.add(NbtString.of(m.toString()));
			entry.put("members", members);
			NbtList mods = new NbtList();
			for (String m : e.modifiers) mods.add(NbtString.of(m));
			entry.put("modifiers", mods);
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
			boolean valid = true;
			for (int j = 0; j < names.size(); j++) {
				try {
					modules.add(ExpeditionRules.Module.valueOf(names.getString(j)));
				} catch (IllegalArgumentException ignored) {
					valid = false;
				}
			}
			if (!valid || modules.isEmpty()) continue;
			Expedition e = new Expedition(entry.getUuid("owner"), entry.getDouble("x"), entry.getDouble("y"),
					entry.getDouble("z"), modules, entry.getLong("start"));
			e.index = Math.max(0, Math.min(modules.size(), entry.getInt("index")));
			e.progress = Math.max(0, entry.getInt("progress"));
			e.shardsSeen = Math.max(0, entry.getInt("shards"));
			NbtList members = entry.getList("members", NbtElement.STRING_TYPE);
			for (int j = 0; j < Math.min(members.size(), MAX_MEMBERS); j++) {
				try {
					e.members.add(UUID.fromString(members.getString(j)));
				} catch (IllegalArgumentException ignored) {
					// Miembro corrupto: se omite.
				}
			}
			NbtList mods = entry.getList("modifiers", NbtElement.STRING_TYPE);
			for (int j = 0; j < Math.min(mods.size(), 3); j++) {
				String m = mods.getString(j);
				if (ExpeditionRules.MODIFIERS.contains(m)) e.modifiers.add(m);
			}
			state.active.put(e.owner, e);
		}
		return state;
	}
}
