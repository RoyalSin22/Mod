package dev.aptitudes.event;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Estado guardado del Eclipse (v34), en el almacén persistente del Mundo Normal. */
public final class EclipseState extends PersistentState {
	private static final String NAME = "aptitudes_eclipse";
	private static final Type<EclipseState> TYPE = new Type<>(EclipseState::new, EclipseState::fromNbt, null);

	/** ¿Hay un eclipse en curso? */
	boolean active;
	/** Iniciado por comando: el tiempo del evento es el transcurrido desde {@link #startTick}. */
	boolean forced;
	/** Tiempo de juego del mundo ({@code getTime}) al empezar. */
	long startTick;
	/** Día del último eclipse empezado (evita repetirlo el mismo día tras detenerlo). */
	long lastDay = -1;
	/** Día cuyo aviso ya se dio. */
	long announcedDay = -1;
	boolean heraldSpawned;
	/** Jugadores que murieron durante el eclipse en curso (no reciben la recompensa de superviviente). */
	final Set<UUID> fallen = new HashSet<>();

	public static EclipseState get(ServerWorld overworld) {
		return overworld.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		nbt.putBoolean("active", active);
		nbt.putBoolean("forced", forced);
		nbt.putLong("start", startTick);
		nbt.putLong("last_day", lastDay);
		nbt.putLong("announced_day", announcedDay);
		nbt.putBoolean("herald", heraldSpawned);
		NbtList list = new NbtList();
		for (UUID id : fallen) list.add(NbtString.of(id.toString()));
		nbt.put("fallen", list);
		return nbt;
	}

	private static EclipseState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		EclipseState s = new EclipseState();
		s.active = nbt.getBoolean("active");
		s.forced = nbt.getBoolean("forced");
		s.startTick = nbt.getLong("start");
		s.lastDay = nbt.contains("last_day") ? nbt.getLong("last_day") : -1;
		s.announcedDay = nbt.contains("announced_day") ? nbt.getLong("announced_day") : -1;
		s.heraldSpawned = nbt.getBoolean("herald");
		NbtList list = nbt.getList("fallen", NbtElement.STRING_TYPE);
		for (int i = 0; i < list.size() && i < 10_000; i++) {
			try {
				s.fallen.add(UUID.fromString(list.getString(i)));
			} catch (IllegalArgumentException ignored) {
				// entrada corrupta: se ignora
			}
		}
		return s;
	}
}
