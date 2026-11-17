package dev.aptitudes.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * V77: búsqueda activa de una estructura del mod en terreno sin explorar. Va generando chunks en anillos alrededor del punto de
 * partida, <b>repartido entre ticks</b> (unos 25 ms por tick) para no congelar el servidor: las estructuras se colocan al generarse
 * el chunk y quedan en {@link StructureIndex}. Termina al encontrar una o al agotar el radio.
 */
public final class StructureSearch {
	/** Radio máximo (chunks) y tiempo de generación por tick. */
	public static final int MAX_CHUNKS = 90;
	/** Radio máximo (regiones) para las estructuras colocadas por cuadrícula: cada región cuesta generar un solo chunk. */
	public static final int MAX_REGIONS = 14;
	private static final long TICK_BUDGET_NANOS = 25_000_000L;
	private static final int REPORT_EVERY_TICKS = 100;

	private static final Map<String, Job> JOBS = new ConcurrentHashMap<>();

	private static final class Job {
		final ServerWorld world;
		final String kind;
		final int cx, cz, px, pz, regionSize;
		final Consumer<StructureLocateRules.Hit> onFound;
		final IntConsumer onFail, onProgress;
		int ring;
		int[][] cells = StructureLocateRules.ring(0);
		int next;
		int ticks;

		Job(ServerWorld world, String kind, int px, int pz, Consumer<StructureLocateRules.Hit> onFound, IntConsumer onFail, IntConsumer onProgress) {
			this.world = world;
			this.kind = kind;
			this.px = px;
			this.pz = pz;
			this.regionSize = StructureLocateRules.regionSize(kind);
			int chunkX = px >> 4, chunkZ = pz >> 4;
			this.cx = regionSize > 0 ? StructureLocateRules.regionOf(chunkX, regionSize) : chunkX; // en modo región: la región del jugador
			this.cz = regionSize > 0 ? StructureLocateRules.regionOf(chunkZ, regionSize) : chunkZ;
			this.onFound = onFound;
			this.onFail = onFail;
			this.onProgress = onProgress;
		}
	}

	private StructureSearch() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(StructureSearch::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> JOBS.clear());
	}

	/** Empieza (o sustituye) la búsqueda de {@code owner}. */
	public static void start(String owner, ServerWorld world, String kind, int px, int pz, Consumer<StructureLocateRules.Hit> onFound,
							 IntConsumer onFail, IntConsumer onProgress) {
		JOBS.put(owner, new Job(world, kind, px, pz, onFound, onFail, onProgress));
	}

	public static boolean running(String owner) {
		return JOBS.containsKey(owner);
	}

	private static void tick(MinecraftServer server) {
		if (JOBS.isEmpty()) return;
		for (Map.Entry<String, Job> entry : JOBS.entrySet()) {
			Job job = entry.getValue();
			long deadline = System.nanoTime() + TICK_BUDGET_NANOS;
			boolean done = false;
			while (!done && System.nanoTime() < deadline) {
				if (job.next < job.cells.length) {
					int[] d = job.cells[job.next++];
					if (job.regionSize > 0) {
						// Un solo chunk por región: el designado, donde se intenta colocar la estructura.
						int[] chunk = StructureLocateRules.designatedChunk(job.world.getSeed(), job.kind, job.cx + d[0], job.cz + d[1], job.regionSize);
						job.world.getChunk(chunk[0], chunk[1]);
					} else {
						job.world.getChunk(job.cx + d[0], job.cz + d[1]); // genera el chunk si no existía
					}
					continue;
				}
				// Anillo completo: se vuelca el índice y se comprueba.
				StructureIndex.drain(server);
				Optional<StructureLocateRules.Hit> hit = StructureLocateRules.nearest(StructureIndex.get(job.world).positions(job.kind), job.px, job.pz);
				if (hit.isPresent()) {
					JOBS.remove(entry.getKey());
					job.onFound.accept(hit.get());
					done = true;
				} else if (job.ring >= (job.regionSize > 0 ? MAX_REGIONS : MAX_CHUNKS)) {
					JOBS.remove(entry.getKey());
					job.onFail.accept(job.ring * 16 * Math.max(1, job.regionSize));
					done = true;
				} else {
					job.cells = StructureLocateRules.ring(++job.ring);
					job.next = 0;
				}
			}
			if (!done && ++job.ticks % REPORT_EVERY_TICKS == 0) job.onProgress.accept(job.ring * 16 * Math.max(1, job.regionSize));
		}
	}
}
