package dev.aptitudes.echo;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.config.AptitudesConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * C7 · S6 Ecos de muerte. Cada jugador va anotando su recorrido (los últimos 5 s, ver {@link DeathEchoRules}); al morir,
 * aparece en el sitio un «eco»: la cabeza del jugador, brillando, que repite ese recorrido en bucle mientras haya otro
 * jugador a menos de {@link DeathEchoRules#WAKE_RADIUS} bloques (sin gente cerca no cuesta nada). Desaparece al cabo de
 * {@code deathEchoLifetimeSeconds}. Sin cliente ni mixins: es un {@code item_display} de vanilla movido desde el servidor,
 * así que el movimiento llega a unos 10 pasos por segundo. Los ecos no sobreviven a un reinicio: al cargarse una
 * entidad etiquetada que no está activa, se retira. <b>Desactivado por defecto</b> ({@code deathEchoesEnabled}).
 */
public final class DeathEchoes {
	public static final String TAG = Aptitudes.MOD_ID + ".death_echo";

	private static final Map<UUID, DeathEchoRules.Track> TRACKS = new HashMap<>();
	private static final List<Echo> ACTIVE = new ArrayList<>();

	private static final class Echo {
		final Entity entity;
		final DeathEchoRules.Frames frames;
		final long created;
		long elapsed;

		Echo(Entity entity, DeathEchoRules.Frames frames, long created) {
			this.entity = entity;
			this.frames = frames;
			this.created = created;
		}
	}

	private DeathEchoes() {}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(DeathEchoes::tick);
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity player) onDeath(player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> TRACKS.remove(handler.getPlayer().getUuid()));
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			// Un eco que no está activo (quedó guardado en el chunk antes de un reinicio) se retira.
			if (entity.getCommandTags().contains(TAG) && ACTIVE.stream().noneMatch(e -> e.entity == entity)) entity.discard();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			TRACKS.clear();
			ACTIVE.clear();
		});
	}

	private static void tick(MinecraftServer server) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.deathEchoesEnabled) {
			if (!ACTIVE.isEmpty()) clearAll(); // se desactivó con ecos vivos
			return;
		}
		int ticks = server.getTicks();
		if (!DeathEchoRules.shouldSample(ticks)) return;
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			DeathEchoRules.Track track = TRACKS.computeIfAbsent(p.getUuid(), id -> new DeathEchoRules.Track());
			if (!p.isAlive() || p.isSpectator()) {
				track.clear();
				continue;
			}
			track.add(p.getX(), p.getY(), p.getZ(), p.getYaw());
		}
		if (ACTIVE.isEmpty()) return;
		Iterator<Echo> it = ACTIVE.iterator();
		while (it.hasNext()) {
			Echo echo = it.next();
			if (echo.entity.isRemoved() || DeathEchoRules.expired(echo.created, ticks, cfg.deathEchoLifetimeSeconds)) {
				echo.entity.discard();
				it.remove();
				continue;
			}
			if (!hasViewerNearby(server, echo.entity)) continue; // sin público no se reproduce
			echo.elapsed += DeathEchoRules.SAMPLE_INTERVAL;
			int frame = DeathEchoRules.frameAt(echo.elapsed, echo.frames.size());
			if (frame < 0) continue; // pausa entre repeticiones
			// 1,6: la cabeza va a la altura de los ojos de quien murió.
			echo.entity.refreshPositionAndAngles(echo.frames.x()[frame], echo.frames.y()[frame] + 1.6, echo.frames.z()[frame],
					echo.frames.yaw()[frame], 0f);
		}
	}

	private static boolean hasViewerNearby(MinecraftServer server, Entity echo) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (p.getWorld() == echo.getWorld() && DeathEchoRules.withinWakeRadius(p.squaredDistanceTo(echo))) return true;
		}
		return false;
	}

	private static void onDeath(ServerPlayerEntity player) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.deathEchoesEnabled || !(player.getWorld() instanceof ServerWorld world) || world.getServer() == null) return;
		DeathEchoRules.Track track = TRACKS.remove(player.getUuid());
		if (track == null || track.size() < DeathEchoRules.MIN_FRAMES) return;
		// Tope por servidor: se retira el más antiguo en lugar de negarse a crear el nuevo (el reciente importa más).
		while (!DeathEchoRules.canSpawn(ACTIVE.size(), cfg.deathEchoMaxActive) && !ACTIVE.isEmpty()) ACTIVE.remove(0).entity.discard();

		DisplayEntity.ItemDisplayEntity display = EntityType.ITEM_DISPLAY.create(world);
		if (display == null) return;
		DeathEchoRules.Frames frames = track.snapshot();
		ItemStack head = new ItemStack(Items.PLAYER_HEAD);
		head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
		display.setPosition(player.getX(), player.getY() + 1.6, player.getZ());
		NbtCompound tag = new NbtCompound();
		display.writeNbt(tag);
		tag.put("item", head.encode(world.getRegistryManager()));
		tag.putString("item_display", "head");
		tag.putBoolean("Glowing", true);
		tag.putBoolean("NoGravity", true);
		tag.putFloat("view_range", 0.5f);
		display.readNbt(tag);
		display.addCommandTag(TAG);
		Echo echo = new Echo(display, frames, world.getServer().getTicks());
		ACTIVE.add(echo); // antes de aparecer: ENTITY_LOAD retira las etiquetadas que no están activas
		world.spawnEntity(display);
	}

	private static void clearAll() {
		for (Echo e : ACTIVE) e.entity.discard();
		ACTIVE.clear();
		TRACKS.clear();
	}
}
