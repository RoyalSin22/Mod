package dev.aptitudes.logic;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

/**
 * v36: guarda con cada mundo las posiciones de bloques colocados por jugadores ({@link PlacementTracker#LOG}).
 *
 * <p>Al cargar el mundo, las posiciones guardadas pasan al registro en memoria; al guardar, se escribe lo que
 * haya en ese momento en el registro para este mundo. Formato: un {@code long[]} en orden de inserción.</p>
 */
public final class PlacedBlocksState extends PersistentState {
	private static final String NAME = "aptitudes_placed_blocks";
	private static final Type<PlacedBlocksState> TYPE = new Type<>(PlacedBlocksState::new, PlacedBlocksState::fromNbt, null);

	/** Clave del mundo en el registro; se asigna al cargar el mundo. */
	private String worldKey;
	/** Posiciones leídas del disco hasta que se pasan al registro. */
	private long[] pending = new long[0];

	public static PlacedBlocksState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	/** Enlaza el estado con su mundo y vuelca en el registro lo leído del disco. */
	void attach(String key, PlacedBlockLog log) {
		this.worldKey = key;
		log.load(key, pending);
		pending = new long[0];
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		long[] positions = worldKey != null ? PlacementTracker.LOG.snapshot(worldKey) : pending;
		nbt.putLongArray("positions", positions);
		return nbt;
	}

	private static PlacedBlocksState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		PlacedBlocksState state = new PlacedBlocksState();
		state.pending = nbt.getLongArray("positions");
		return state;
	}
}
