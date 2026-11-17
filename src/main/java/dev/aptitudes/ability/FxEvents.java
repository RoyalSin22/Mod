package dev.aptitudes.ability;

import dev.aptitudes.network.FxEventPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** V77: envío de los eventos de efectos a los jugadores que pueden verlos. */
public final class FxEvents {
	/** Distancia máxima a la que se manda un efecto (la misma de las animaciones de lanzamiento). */
	public static final double RANGE = 48.0;

	private FxEvents() {
	}

	/** Efecto en un punto: lo reciben los jugadores de ese mundo a menos de {@link #RANGE} bloques. */
	public static void at(ServerWorld world, Vec3d pos, int id, int color, float power, Vec3d dir, int seed) {
		send(world, pos, new FxEventPayload(id, -1, pos.x, pos.y, pos.z, (float) dir.x, (float) dir.y, (float) dir.z, color, power, seed));
	}

	/** Efecto ligado a una entidad (el cliente la sigue si se mueve). */
	public static void on(Entity entity, int id, int color, float power, Vec3d dir, int seed) {
		if (!(entity.getWorld() instanceof ServerWorld world)) return;
		Vec3d pos = entity.getPos();
		send(world, pos, new FxEventPayload(id, entity.getId(), pos.x, pos.y, pos.z, (float) dir.x, (float) dir.y, (float) dir.z, color, power, seed));
	}

	/** Efecto ligado a una entidad pero situado en otro punto (el aterrizaje de un salto cae en el suelo). */
	public static void onAt(Entity entity, Vec3d pos, int id, int color, float power, Vec3d dir, int seed) {
		if (!(entity.getWorld() instanceof ServerWorld world)) return;
		send(world, pos, new FxEventPayload(id, entity.getId(), pos.x, pos.y, pos.z, (float) dir.x, (float) dir.y, (float) dir.z, color, power, seed));
	}

	private static void send(ServerWorld world, Vec3d pos, FxEventPayload payload) {
		double max = RANGE * RANGE;
		for (ServerPlayerEntity viewer : PlayerLookup.around(world, pos, RANGE)) {
			if (viewer.squaredDistanceTo(pos) <= max && ServerPlayNetworking.canSend(viewer, FxEventPayload.ID)) {
				ServerPlayNetworking.send(viewer, payload);
			}
		}
	}
}
