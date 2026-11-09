package dev.aptitudes.world;

import dev.aptitudes.block.ModBlocks;
import dev.aptitudes.boss.BossKind;
import dev.aptitudes.sound.ModSounds;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Cierra las salas ancladas a una estructura y renueva su niebla solo mientras se lucha dentro. */
public final class BossArenas {
	private static final String ANCHOR = "aptitudes.arena.";
	private static final int INTERVAL = 10;
	private static final long LEASE = 40;
	/** Las columnas de las ruinas duran 20 minutos (24000 ticks). */
	private static final long RUINS_LEASE = 24_000;
	private static final Map<UUID, Arena> ARENAS = new HashMap<>();
	private static final Map<ServerPlayerEntity, UUID> LISTENERS = new HashMap<>();

	private record Seal(List<BlockPos> fog, List<BlockPos> solid) {}

	private static final class Arena {
		final MobEntity boss;
		final ServerWorld world;
		final BlockPos center;
		final BossArenaLayout layout;
		final List<BlockPos> wall;
		Seal seal;
		/** Ya se avisó a los participantes de que las ruinas no se pueden cerrar. */
		boolean warnedBlocked;

		Arena(MobEntity boss, ServerWorld world, BlockPos center, BossArenaLayout layout) {
			this.boss = boss;
			this.world = world;
			this.center = center;
			this.layout = layout;
			wall = layout.wall().stream().map(cell -> center.add(cell.x(), cell.y(), cell.z())).toList();
		}

		boolean contains(Entity entity) {
			return layout.contains(entity.getX() - center.getX(), entity.getY() - center.getY(), entity.getZ() - center.getZ());
		}

		boolean loaded(BlockPos pos) {
			return world.isInBuildLimit(pos) && world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
		}

		void open() {
			if (seal == null) return;
			removeFog(seal.fog());
			seal = null;
		}

		private void removeFog(List<BlockPos> fog) {
			for (BlockPos pos : fog) TemporaryBlocks.remove(world, pos, ModBlocks.ARENA_FOG);
		}

		Seal close() {
			List<BlockPos> gaps = new ArrayList<>();
			List<BlockPos> solid = new ArrayList<>();
			for (BlockPos pos : wall) {
				if (!loaded(pos) || !world.getWorldBorder().contains(pos)) return null;
				var state = world.getBlockState(pos);
				if (state.isAir()) gaps.add(pos);
				else if (!state.isOf(ModBlocks.ARENA_FOG) && state.isFullCube(world, pos)) solid.add(pos);
				else return null;
			}
			// Quien esté cruzando la entrada debe poder terminar de hacerlo antes del cierre.
			var players = world.getPlayers(p -> p.isAlive() && !p.isSpectator()
					&& p.squaredDistanceTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5) <= 12 * 12);
			for (BlockPos pos : gaps) {
				Box cell = new Box(pos);
				if (boss.getBoundingBox().intersects(cell)) return null;
				for (ServerPlayerEntity player : players) {
					if (player.getBoundingBox().intersects(cell)) return null;
				}
			}
			List<BlockPos> placed = new ArrayList<>();
			for (BlockPos pos : gaps) {
				if (!TemporaryBlocks.placeInAir(world, pos, ModBlocks.ARENA_FOG.getDefaultState(), LEASE)) {
					removeFog(placed);
					return null;
				}
				placed.add(pos);
			}
			return new Seal(placed, solid);
		}

		boolean renew() {
			for (BlockPos pos : seal.solid()) {
				if (!loaded(pos) || world.getBlockState(pos).isOf(ModBlocks.ARENA_FOG)
						|| !world.getBlockState(pos).isFullCube(world, pos)) return false;
			}
			for (BlockPos pos : seal.fog()) {
				if (!TemporaryBlocks.renew(world, pos, ModBlocks.ARENA_FOG, LEASE)) return false;
			}
			return true;
		}
	}

	private BossArenas() {}

	/** Ancla la sala al punto {@code center} (lo llama el sello del Coloso antes de invocarlo). */
	public static void mark(MobEntity boss, BlockPos center) {
		boss.addCommandTag(ANCHOR + center.asLong());
	}

	public static void register() {
		ServerEntityEvents.ENTITY_LOAD.register(BossArenas::load);
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> forget(entity));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> forget(entity));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % INTERVAL == 0) tick();
		});
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ARENAS.values().forEach(Arena::open);
			ARENAS.clear();
			updateListeners(Map.of());
		});
	}

	private static void load(Entity entity, ServerWorld world) {
		if (world.getRegistryKey() != World.OVERWORLD || !(entity instanceof MobEntity boss)) return;
		BlockPos center = null;
		BossArenaLayout layout = null;
		for (String tag : boss.getCommandTags()) {
			if (tag.startsWith(ANCHOR)) {
				try {
					center = BlockPos.fromLong(Long.parseLong(tag.substring(ANCHOR.length())));
				} catch (NumberFormatException ignored) {
					return;
				}
			} else {
				BossKind kind = BossKind.fromTag(tag);
				if (kind != null) layout = switch (kind) {
					case TOWER_SENTINEL -> BossArenaLayout.TOWER;
					case SHRINE_ORACLE -> BossArenaLayout.SHRINE;
					case CAMP_CHAMPION -> BossArenaLayout.CAMP;
					case ECLIPSE_HERALD -> null;
				};
			}
		}
		// El Coloso no tiene etiqueta de jefe: su sala es la de las ruinas.
		if (layout == null && boss instanceof dev.aptitudes.boss.FacelessColossusEntity) layout = BossArenaLayout.RUINS;
		if (center != null && layout != null) ARENAS.put(boss.getUuid(), new Arena(boss, world, center, layout));
	}

	/** Columnas rotas como cobertura dentro de las ruinas, temporales (no dejan restos). */
	public static void raiseRuins(ServerWorld world, BlockPos center) {
		for (int i = 0; i < 8; i++) {
			double angle = i * Math.PI / 4;
			int x = (int) Math.round(Math.cos(angle) * 6);
			int z = (int) Math.round(Math.sin(angle) * 6);
			int height = 2 + world.getRandom().nextInt(3);
			for (int y = 0; y < height; y++) {
				BlockPos pos = center.add(x, y, z);
				if (!world.isInBuildLimit(pos)) continue;
				boolean top = y == height - 1;
				var state = top && world.getRandom().nextBoolean()
						? net.minecraft.block.Blocks.CRACKED_STONE_BRICKS.getDefaultState()
						: net.minecraft.block.Blocks.STONE_BRICKS.getDefaultState();
				TemporaryBlocks.placeInAir(world, pos, state, RUINS_LEASE);
			}
		}
	}

	private static void forget(Entity entity) {
		Arena arena = ARENAS.remove(entity.getUuid());
		if (arena != null) arena.open();
	}

	private static void tick() {
		if (ARENAS.isEmpty() && LISTENERS.isEmpty()) return;
		Map<ServerPlayerEntity, UUID> listeners = new HashMap<>();
		for (Arena arena : ARENAS.values()) {
			if (!arena.boss.isAlive() || arena.boss.getWorld() != arena.world || !arena.contains(arena.boss)) {
				arena.open();
				continue;
			}
			var participants = arena.world.getPlayers(p -> p.isAlive() && !p.isCreative()
					&& !p.isSpectator() && arena.contains(p));
			if (participants.isEmpty()) {
				arena.open();
				continue;
			}
			if (arena.seal == null) {
				arena.seal = arena.close();
				if (arena.seal == null && arena.layout == BossArenaLayout.RUINS && !arena.warnedBlocked) {
					arena.warnedBlocked = true;
					for (ServerPlayerEntity player : participants) {
						player.sendMessage(net.minecraft.text.Text.translatable("boss.aptitudes.ruins_blocked")
								.formatted(net.minecraft.util.Formatting.GRAY), false);
					}
				}
			}
			else if (!arena.renew()) arena.open();
			if (arena.seal != null) {
				for (ServerPlayerEntity player : participants) listeners.putIfAbsent(player, arena.boss.getUuid());
			}
		}
		updateListeners(listeners);
	}

	private static void updateListeners(Map<ServerPlayerEntity, UUID> next) {
		for (var listener : LISTENERS.entrySet()) {
			ServerPlayerEntity player = listener.getKey();
			if (!listener.getValue().equals(next.get(player)) && !player.isDisconnected()) {
				player.networkHandler.sendPacket(new StopSoundS2CPacket(ModSounds.ARENA_THEME.getId(), SoundCategory.MUSIC));
			}
		}
		for (var listener : next.entrySet()) {
			if (!listener.getValue().equals(LISTENERS.get(listener.getKey()))) {
				listener.getKey().playSoundToPlayer(ModSounds.ARENA_THEME, SoundCategory.MUSIC, 0.85f, 1f);
			}
		}
		LISTENERS.clear();
		LISTENERS.putAll(next);
	}
}
