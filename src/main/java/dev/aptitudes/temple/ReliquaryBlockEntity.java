package dev.aptitudes.temple;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Relicario de un templo (v64): se abre una vez con la llave de su templo y después entrega un Vaso de vida a cada
 * jugador una sola vez. Los jugadores que ya lo reclamaron se guardan aquí (viaja con el chunk).
 */
public class ReliquaryBlockEntity extends BlockEntity {
	/** Tope defensivo de jugadores guardados. */
	private static final int MAX_CLAIMS = 256;

	UUID templeId = new UUID(0, 0);
	boolean unlocked;
	final Set<UUID> claimed = new HashSet<>();

	public ReliquaryBlockEntity(BlockPos pos, BlockState state) {
		super(Temples.RELIQUARY_ENTITY, pos, state);
	}

	/** La llama la generación del templo. */
	public void setTemple(UUID temple) {
		this.templeId = temple;
		markDirty();
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.writeNbt(nbt, registries);
		nbt.putUuid("temple", templeId);
		nbt.putBoolean("unlocked", unlocked);
		NbtList list = new NbtList();
		for (UUID id : claimed) list.add(NbtHelper.fromUuid(id));
		nbt.put("claimed", list);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
		super.readNbt(nbt, registries);
		if (nbt.containsUuid("temple")) templeId = nbt.getUuid("temple");
		unlocked = nbt.getBoolean("unlocked");
		claimed.clear();
		NbtList list = nbt.getList("claimed", NbtElement.INT_ARRAY_TYPE);
		for (int i = 0; i < list.size() && claimed.size() < MAX_CLAIMS; i++) claimed.add(NbtHelper.toUuid(list.get(i)));
	}
}
