package dev.aptitudes.logic;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Detecta colocaciones REALES de bloques por jugadores, sin mixins.
 *
 * <p>{@code UseBlockCallback} se dispara antes de colocar, sin saber si la colocación tendrá éxito.
 * Se anota la posición prevista y, al final del tick, se comprueba que allí está el bloque del objeto
 * usado y que antes no estaba. Solo entonces se registra en {@link #LOG} (para no dar XP por romperlo)
 * y se avisa a los oyentes (XP de Ingeniero/Druida por colocar).</p>
 */
public final class PlacementTracker {
	/** Bloques colocados por jugadores: romperlos no da XP de habilidad. 100 000 posiciones por mundo. */
	public static final PlacedBlockLog LOG = new PlacedBlockLog(100_000);

	/** Oyente de colocaciones confirmadas. */
	public interface Listener {
		void onPlaced(ServerPlayerEntity player, Item item, BlockPos pos);
	}

	private record Pending(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Item item, Block block,
						   BlockState before) {}

	private static final List<Pending> PENDING = new ArrayList<>();
	private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

	private PlacementTracker() {
	}

	public static void addListener(Listener listener) {
		LISTENERS.add(listener);
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
			if (world.isClient() || !(player instanceof ServerPlayerEntity sp) || !(world instanceof ServerWorld sw)) {
				return ActionResult.PASS;
			}
			Item item = player.getStackInHand(hand).getItem();
			if (!(item instanceof BlockItem blockItem)) return ActionResult.PASS;
			BlockPos clicked = hit.getBlockPos();
			BlockPos target = sw.getBlockState(clicked).isReplaceable() ? clicked : clicked.offset(hit.getSide());
			if (PENDING.size() < 4096) { // tope defensivo por tick
				PENDING.add(new Pending(sp, sw, target.toImmutable(), item, blockItem.getBlock(), sw.getBlockState(target)));
			}
			return ActionResult.PASS; // nunca interfiere con la colocación
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> confirm());
	}

	private static void confirm() {
		if (PENDING.isEmpty()) return;
		List<Pending> batch = new ArrayList<>(PENDING);
		PENDING.clear();
		for (Pending p : batch) {
			BlockState now = p.world().getBlockState(p.pos());
			if (!now.isOf(p.block()) || p.before().isOf(p.block())) continue; // no se colocó (o ya estaba)
			if (p.player().isRemoved()) continue;
			// Plantas (brotes, cultivos, hongos) no se registran: crecen y se transforman (un brote acaba
			// siendo el tronco de un árbol natural), y plantar/cosechar es el juego normal de Agricultura.
			if (!(p.block() instanceof net.minecraft.block.PlantBlock)) LOG.mark(key(p.world()), p.pos().asLong());
			for (Listener l : LISTENERS) {
				try {
					l.onPlaced(p.player(), p.item(), p.pos());
				} catch (RuntimeException e) {
					Aptitudes.LOGGER.error("[Aptitudes] Error en un oyente de colocación", e);
				}
			}
		}
	}

	/** ¿El bloque en {@code pos} lo colocó un jugador? Lo olvida (se está rompiendo). */
	public static boolean consumePlaced(World world, BlockPos pos) {
		return LOG.consume(key(world), pos.asLong());
	}

	private static String key(World world) {
		return world.getRegistryKey().getValue().toString();
	}
}
