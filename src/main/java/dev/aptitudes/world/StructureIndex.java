package dev.aptitudes.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.StructureWorldAccess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * V77: índice por mundo de las estructuras del mod ya generadas, para {@code /aptitudes locate}. Las estructuras son
 * {@code Feature}s, así que el {@code /locate} de vanilla no las conoce.
 *
 * <p>La generación corre en hilos de trabajo: ahí solo se encola ({@link #record}) y el hilo del servidor vuelca la cola en el
 * estado persistente cada segundo ({@link #drain}). Solo se indexan las estructuras generadas con esta versión o posterior.</p>
 */
public final class StructureIndex extends PersistentState {
	private static final String NAME = "aptitudes_structures";
	private static final Type<StructureIndex> TYPE = new Type<>(StructureIndex::new, StructureIndex::fromNbt, null);
	private static final ConcurrentLinkedQueue<Pending> QUEUE = new ConcurrentLinkedQueue<>();

	private record Pending(String world, String kind, long pos) {
	}

	private final Map<String, List<Long>> byKind = new LinkedHashMap<>();

	public static StructureIndex get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
	}

	/** Se llama desde la generación (cualquier hilo) al terminar de construir una estructura. */
	public static void record(StructureWorldAccess world, String kind, BlockPos origin) {
		if (!StructureLocateRules.isKind(kind)) return;
		QUEUE.add(new Pending(world.toServerWorld().getRegistryKey().getValue().toString(), kind, origin.asLong()));
	}

	/** Vuelca la cola en el estado persistente de cada mundo. Solo desde el hilo del servidor. */
	public static void drain(MinecraftServer server) {
		if (QUEUE.isEmpty()) return;
		Pending p;
		while ((p = QUEUE.poll()) != null) {
			for (ServerWorld world : server.getWorlds()) {
				if (world.getRegistryKey().getValue().toString().equals(p.world())) {
					get(world).add(p.kind(), p.pos());
					break;
				}
			}
		}
	}

	void add(String kind, long pos) {
		List<Long> list = byKind.computeIfAbsent(kind, k -> new ArrayList<>());
		if (!list.contains(pos)) {
			list.add(pos);
			markDirty();
		}
	}

	public long[] positions(String kind) {
		List<Long> list = byKind.getOrDefault(kind, List.of());
		long[] out = new long[list.size()];
		for (int i = 0; i < out.length; i++) out[i] = list.get(i);
		return out;
	}

	public int count(String kind) {
		return byKind.getOrDefault(kind, List.of()).size();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		for (Map.Entry<String, List<Long>> e : byKind.entrySet()) nbt.putLongArray(e.getKey(), positions(e.getKey()));
		return nbt;
	}

	private static StructureIndex fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		StructureIndex index = new StructureIndex();
		for (String kind : StructureLocateRules.KINDS) {
			if (!nbt.contains(kind)) continue;
			List<Long> list = new ArrayList<>();
			for (long p : nbt.getLongArray(kind)) list.add(p);
			index.byKind.put(kind, list);
		}
		return index;
	}
}
