package dev.aptitudes.world;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

/**
 * Bloques temporales de habilidades (Muro elemental), persistentes por mundo.
 *
 * <ul>
 *   <li>Cada bloque se guarda con su instante de caducidad en tiempo de juego del mundo. Si el servidor
 *       se cierra con un muro levantado, al volver se retira cuando le toca (antes se quedaba para siempre).</li>
 *   <li>Romperlo no suelta nada: el muro no es una fuente gratuita de cobre, amatista u obsidiana llorosa.</li>
 *   <li>Solo se retira si en la posición sigue estando el mismo bloque (no borra lo que otro haya puesto).</li>
 * </ul>
 */
public final class TemporaryBlocks extends PersistentState {
	private static final String NAME = "aptitudes_temporary_blocks";
	private static final Type<TemporaryBlocks> TYPE = new Type<>(TemporaryBlocks::new, TemporaryBlocks::fromNbt, null);

	private final TemporaryBlockSchedule schedule = new TemporaryBlockSchedule();

	public static TemporaryBlocks get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 5) tick(server);
		});
		// Romper un bloque temporal: se quita sin soltar objetos y se cancela la rotura normal.
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (!(world instanceof ServerWorld sw)) return true;
			TemporaryBlocks temp = get(sw);
			if (state.isOf(dev.aptitudes.block.ModBlocks.ARENA_FOG)) return false;
			var entry = temp.schedule.remove(pos.asLong());
			if (entry == null) return true;
			temp.markDirty();
			if (!Registries.BLOCK.getId(state.getBlock()).toString().equals(entry.block())) return true;
			sw.setBlockState(pos, Blocks.AIR.getDefaultState());
			return false;
		});
	}

	/** Coloca {@code state} en {@code pos} durante {@code ticks}. Devuelve false si no se pudo registrar. */
	public static boolean place(ServerWorld world, BlockPos pos, BlockState state, long ticks) {
		TemporaryBlocks temp = get(world);
		String id = Registries.BLOCK.getId(state.getBlock()).toString();
		if (!temp.schedule.add(pos.asLong(), id, world.getTime() + Math.max(1, ticks))) return false;
		temp.markDirty();
		world.setBlockState(pos, state);
		return true;
	}

	/** Las arenas solo ocupan aire y nunca se adueñan de otra colocación temporal. */
	static boolean placeInAir(ServerWorld world, BlockPos pos, BlockState state, long ticks) {
		if (!loaded(world, pos) || !world.getBlockState(pos).isAir() || get(world).schedule.contains(pos.asLong())) return false;
		if (!place(world, pos, state, ticks)) return false;
		if (world.getBlockState(pos).isOf(state.getBlock())) return true;
		remove(world, pos, state.getBlock());
		return false;
	}

	static boolean renew(ServerWorld world, BlockPos pos, Block block, long ticks) {
		if (!loaded(world, pos)) return false;
		TemporaryBlocks temp = get(world);
		var entry = temp.schedule.get(pos.asLong());
		if (entry == null || !entry.block().equals(Registries.BLOCK.getId(block).toString())
				|| !world.getBlockState(pos).isOf(block)) return false;
		long now = world.getTime();
		if (entry.expireAt() <= now + ticks / 2) {
			temp.schedule.add(entry.pos(), entry.block(), now + ticks);
			temp.markDirty();
		}
		return true;
	}

	static void remove(ServerWorld world, BlockPos pos, Block block) {
		// Si el chunk se descargó, la agenda conserva la entrada hasta que vuelva a cargar.
		if (!loaded(world, pos)) return;
		TemporaryBlocks temp = get(world);
		var entry = temp.schedule.get(pos.asLong());
		if (entry == null || !entry.block().equals(Registries.BLOCK.getId(block).toString())) return;
		if (world.getBlockState(pos).isOf(block)) world.setBlockState(pos, Blocks.AIR.getDefaultState());
		temp.schedule.remove(entry.pos());
		temp.markDirty();
	}

	private static boolean loaded(ServerWorld world, BlockPos pos) {
		return world.isInBuildLimit(pos) && world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
	}

	private static void tick(MinecraftServer server) {
		for (ServerWorld world : server.getWorlds()) {
			TemporaryBlocks temp = get(world);
			if (temp.schedule.isEmpty()) continue;
			long now = world.getTime();
			for (TemporaryBlockSchedule.Entry e : temp.schedule.expired(now)) {
				BlockPos pos = BlockPos.fromLong(e.pos());
				// Chunk sin cargar: se espera a que se cargue (la entrada sigue guardada).
				if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) continue;
				Block block = Registries.BLOCK.get(Identifier.of(e.block()));
				if (world.getBlockState(pos).isOf(block)) world.setBlockState(pos, Blocks.AIR.getDefaultState());
				temp.schedule.remove(e.pos());
				temp.markDirty();
			}
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		NbtList list = new NbtList();
		for (TemporaryBlockSchedule.Entry e : schedule.all()) {
			NbtCompound c = new NbtCompound();
			c.putLong("pos", e.pos());
			c.putString("block", e.block());
			c.putLong("expire", e.expireAt());
			list.add(c);
		}
		nbt.put("blocks", list);
		return nbt;
	}

	private static TemporaryBlocks fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		TemporaryBlocks temp = new TemporaryBlocks();
		NbtList list = nbt.getList("blocks", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound c = list.getCompound(i);
			if (!temp.schedule.add(c.getLong("pos"), c.getString("block"), c.getLong("expire"))) {
				Aptitudes.LOGGER.warn("[Aptitudes] Demasiados bloques temporales guardados; se ignoran los sobrantes");
				break;
			}
		}
		return temp;
	}
}
