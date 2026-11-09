package dev.aptitudes.temple;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Ids de los Vasos de vida ya consumidos en este mundo (guardado en el Mundo Normal). Evita usar copias. */
public final class LifeVesselState extends PersistentState {
	private static final String NAME = "aptitudes_life_vessels";
	private static final Type<LifeVesselState> TYPE = new Type<>(LifeVesselState::new, LifeVesselState::fromNbt, null);

	private final Set<UUID> consumed = new HashSet<>();

	public static LifeVesselState get(MinecraftServer server) {
		return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public boolean contains(UUID id) {
		return consumed.contains(id);
	}

	public void add(UUID id) {
		if (consumed.add(id)) markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (UUID id : consumed) list.add(NbtHelper.fromUuid(id));
		nbt.put("consumed", list);
		return nbt;
	}

	private static LifeVesselState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		LifeVesselState state = new LifeVesselState();
		NbtList list = nbt.getList("consumed", NbtElement.INT_ARRAY_TYPE);
		for (int i = 0; i < list.size(); i++) state.consumed.add(NbtHelper.toUuid(list.get(i)));
		return state;
	}
}
