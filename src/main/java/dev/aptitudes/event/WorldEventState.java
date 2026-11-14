package dev.aptitudes.event;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.UUID;

/**
 * v41: estado guardado de la rotación de eventos (Mundo Normal). El Eclipse conserva su propio estado
 * ({@code aptitudes_eclipse}) por compatibilidad con mundos guardados; aquí van la elección de cada fecha y los
 * eventos nuevos en curso.
 */
public final class WorldEventState extends PersistentState {
	private static final String NAME = "aptitudes_world_events";
	private static final Type<WorldEventState> TYPE = new Type<>(WorldEventState::new, WorldEventState::fromNbt, null);

	/** Último evento elegido (número de evento y tipo), para no repetirlo. */
	long lastIndex = -1;
	String lastKind = "";
	/** Elección ya hecha para el siguiente evento (se fija al anunciarlo). */
	long nextIndex = -1;
	String nextKind = "";
	long announcedIndex = -1;
	long startedIndex = -1;
	/** Evento nuevo en curso (Niebla de almas o Lluvia de brasas); vacío si no hay. */
	String active = "";
	boolean forced;
	long startTick;
	long activeDay = -1;
	UUID fogSession;
	UUID lastClosedFogSession;
	long lastFogCloseTick;
	/** T1: reloj del mundo en que empezó la última Niebla / Lluvia provocada por un jugador (-1 = nunca). */
	long lastFogStart = -1;
	long lastEmberStart = -1;

	public static WorldEventState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		nbt.putLong("last_index", lastIndex);
		nbt.putString("last_kind", lastKind);
		nbt.putLong("next_index", nextIndex);
		nbt.putString("next_kind", nextKind);
		nbt.putLong("announced_index", announcedIndex);
		nbt.putLong("started_index", startedIndex);
		nbt.putString("active", active);
		nbt.putBoolean("forced", forced);
		nbt.putLong("start", startTick);
		nbt.putLong("active_day", activeDay);
		if (fogSession != null) nbt.putUuid("fog_session", fogSession);
		if (lastClosedFogSession != null) {
			nbt.putUuid("closed_fog_session", lastClosedFogSession);
			nbt.putLong("fog_close_tick", lastFogCloseTick);
		}
		nbt.putLong("last_fog_start", lastFogStart);
		nbt.putLong("last_ember_start", lastEmberStart);
		return nbt;
	}

	static WorldEventState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		WorldEventState s = new WorldEventState();
		s.lastIndex = nbt.contains("last_index") ? nbt.getLong("last_index") : -1;
		s.lastKind = nbt.getString("last_kind");
		s.nextIndex = nbt.contains("next_index") ? nbt.getLong("next_index") : -1;
		s.nextKind = nbt.getString("next_kind");
		s.announcedIndex = nbt.contains("announced_index") ? nbt.getLong("announced_index") : -1;
		s.startedIndex = nbt.contains("started_index") ? nbt.getLong("started_index") : -1;
		s.active = nbt.getString("active");
		s.forced = nbt.getBoolean("forced");
		s.startTick = nbt.getLong("start");
		s.activeDay = nbt.contains("active_day") ? nbt.getLong("active_day") : -1;
		if (nbt.containsUuid("fog_session")) s.fogSession = nbt.getUuid("fog_session");
		if (nbt.containsUuid("closed_fog_session")) {
			s.lastClosedFogSession = nbt.getUuid("closed_fog_session");
			s.lastFogCloseTick = nbt.getLong("fog_close_tick");
		}
		s.lastFogStart = nbt.contains("last_fog_start") ? nbt.getLong("last_fog_start") : -1;
		s.lastEmberStart = nbt.contains("last_ember_start") ? nbt.getLong("last_ember_start") : -1;
		return s;
	}
}
