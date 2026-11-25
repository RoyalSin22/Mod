package dev.aptitudes.social;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.achievement.Achievement;
import dev.aptitudes.achievement.Achievements;
import dev.aptitudes.config.AptitudesConfig;
import dev.aptitudes.data.Skill;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.logic.ProgressManager;
import dev.aptitudes.logic.XpEvents;
import dev.aptitudes.network.GroundMessageRatePayload;
import dev.aptitudes.network.GroundMessageSyncPayload;
import dev.aptitudes.network.GroundMessageWritePayload;
import dev.aptitudes.network.RequestLimiter;
import dev.aptitudes.party.PartyManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * C7 (S5): notas en el suelo en el servidor. Escribir, valorar, borrar, caducar y sincronizar con los clientes; todo
 * se valida aquí (diseño en {@code docs/C7_S5_MENSAJES_DISENO.md}). Solo hilo del servidor.
 */
public final class GroundMessages {
	private static final int SYNC_INTERVAL_TICKS = 40;
	private static final int EXPIRE_INTERVAL_TICKS = 1200;
	/** Logro «Cronista de la ceniza». */
	public static final int CHRONICLER_RATINGS = 10;
	private static final RequestLimiter LIMITER = new RequestLimiter();
	/** Última lista enviada a cada jugador: solo se reenvía si cambia. */
	private static final Map<UUID, List<GroundMessageSyncPayload.Entry>> LAST_SENT = new HashMap<>();

	private GroundMessages() {}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(GroundMessageWritePayload.ID, (payload, context) -> write(context.player(), payload));
		ServerPlayNetworking.registerGlobalReceiver(GroundMessageRatePayload.ID, (payload, context) -> rate(context.player(), payload.id()));
		ServerTickEvents.END_SERVER_TICK.register(GroundMessages::tick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			LAST_SENT.remove(handler.getPlayer().getUuid());
			checkChronicler(handler.getPlayer());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			LAST_SENT.remove(handler.getPlayer().getUuid());
			LIMITER.forget(handler.getPlayer().getUuid());
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			LAST_SENT.remove(player.getUuid());
			sync(player);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_SENT.clear());
	}

	private static void tick(MinecraftServer server) {
		int t = server.getTicks();
		AptitudesConfig cfg = Aptitudes.config();
		if (t % EXPIRE_INTERVAL_TICKS == 0) {
			for (ServerWorld world : server.getWorlds()) {
				GroundMessageState.get(world).removeExpired(world.getTime(), cfg.groundMessageLifetimeDays);
			}
		}
		if (t % SYNC_INTERVAL_TICKS == 0) {
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) sync(p);
		}
	}

	// ------------------------------------------------------------------ Sincronización

	/** Envía al jugador sus notas cercanas si han cambiado desde el último envío. */
	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, GroundMessageSyncPayload.ID)) return;
		List<GroundMessageSyncPayload.Entry> entries = nearby(player);
		if (entries.equals(LAST_SENT.get(player.getUuid()))) return;
		LAST_SENT.put(player.getUuid(), entries);
		ServerPlayNetworking.send(player, new GroundMessageSyncPayload(entries));
	}

	private static List<GroundMessageSyncPayload.Entry> nearby(ServerPlayerEntity player) {
		AptitudesConfig cfg = Aptitudes.config();
		if (!cfg.groundMessagesEnabled) return List.of();
		ServerWorld world = player.getServerWorld();
		long now = world.getTime();
		double r2 = GroundMessageRules.SYNC_RADIUS * GroundMessageRules.SYNC_RADIUS;
		List<GroundMessageSyncPayload.Entry> out = new ArrayList<>();
		for (GroundMessageState.Note n : GroundMessageState.get(world).notes()) {
			if (GroundMessageRules.expired(n.created, now, cfg.groundMessageLifetimeDays)) continue;
			if (player.squaredDistanceTo(Vec3d.ofCenter(n.pos)) > r2) continue;
			if (out.size() >= GroundMessageSyncPayload.MAX_NOTES) break;
			boolean mine = n.author.equals(player.getUuid());
			out.add(new GroundMessageSyncPayload.Entry(n.id, n.pos, n.template, n.word,
					cfg.groundMessageShowAuthor ? n.authorName : "", n.ratings(), mine, n.ratedBy(player.getUuid())));
		}
		return List.copyOf(out);
	}

	/** Tras un cambio: reenvía a los jugadores cercanos de esa dimensión. */
	private static void resyncAround(ServerWorld world, BlockPos pos) {
		double r = GroundMessageRules.SYNC_RADIUS + 16;
		for (ServerPlayerEntity p : world.getPlayers()) {
			if (p.squaredDistanceTo(Vec3d.ofCenter(pos)) <= r * r) sync(p);
		}
	}

	// ------------------------------------------------------------------ Escribir

	/** ¿Se puede escribir en {@code pos}? Suelo sólido debajo, hueco sin colisión y sin fluido. */
	public static boolean validSpot(ServerWorld world, BlockPos pos) {
		if (!world.isInBuildLimit(pos)) return false;
		BlockPos ground = pos.down();
		BlockState below = world.getBlockState(ground);
		BlockState at = world.getBlockState(pos);
		return below.isSideSolidFullSquare(world, ground, Direction.UP)
				&& at.getCollisionShape(world, pos).isEmpty() && world.getFluidState(pos).isEmpty();
	}

	private static Hand chalkHand(ServerPlayerEntity player) {
		if (player.getMainHandStack().isOf(ModItems.ASH_CHALK)) return Hand.MAIN_HAND;
		if (player.getOffHandStack().isOf(ModItems.ASH_CHALK)) return Hand.OFF_HAND;
		return null;
	}

	private static void write(ServerPlayerEntity player, GroundMessageWritePayload payload) {
		AptitudesConfig cfg = Aptitudes.config();
		MinecraftServer server = player.getServer();
		if (server == null || !LIMITER.tryAcquire(player.getUuid(), "ground_write", server.getTicks(), 20)) return;
		if (!cfg.groundMessagesEnabled) {
			player.sendMessage(Text.translatable("message.aptitudes.ground.disabled"), true);
			return;
		}
		Hand hand = chalkHand(player);
		if (hand == null || !MessageCatalog.isValid(payload.template(), payload.word()) || player.isSpectator()) return;
		ServerWorld world = player.getServerWorld();
		BlockPos pos = payload.pos();
		double reach = GroundMessageRules.WRITE_REACH + 1.0;
		if (player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > reach * reach || !validSpot(world, pos)
				|| !world.canPlayerModifyAt(player, pos.down())) {
			player.sendMessage(Text.translatable("message.aptitudes.ground.invalid"), true);
			return;
		}
		GroundMessageState state = GroundMessageState.get(world);
		ChunkPos chunk = new ChunkPos(pos);
		int inChunk = 0;
		List<GroundMessageRules.Spot> close = new ArrayList<>();
		List<GroundMessageState.Note> own = new ArrayList<>();
		for (GroundMessageState.Note n : state.notes()) {
			if (new ChunkPos(n.pos).equals(chunk)) inChunk++;
			if (n.pos.getSquaredDistance(pos) <= 100) close.add(new GroundMessageRules.Spot(n.pos.getX(), n.pos.getY(), n.pos.getZ()));
			if (n.author.equals(player.getUuid())) own.add(n);
		}
		GroundMessageRules.PlaceResult result = GroundMessageRules.canPlace(true, pos.getX(), pos.getY(), pos.getZ(), close,
				cfg.groundMessageMinSpacing, inChunk, cfg.groundMessagesPerChunk);
		if (result != GroundMessageRules.PlaceResult.OK) {
			String key = result == GroundMessageRules.PlaceResult.TOO_CLOSE ? "too_close" : "chunk_full";
			player.sendMessage(Text.translatable("message.aptitudes.ground." + key), true);
			return;
		}
		int evict = GroundMessageRules.evictBeforeAdding(own.size(), cfg.groundMessagesPerPlayer);
		if (evict > 0) {
			own.sort(Comparator.comparingLong(n -> n.created));
			for (int i = 0; i < evict && i < own.size(); i++) state.remove(own.get(i).id);
			player.sendMessage(Text.translatable("message.aptitudes.ground.oldest_removed"), false);
		}
		state.add(pos, player.getUuid(), player.getGameProfile().getName(), payload.template(), payload.word(), world.getTime());
		if (!player.isCreative()) {
			ItemStack stack = player.getStackInHand(hand);
			stack.decrement(1);
		}
		world.playSound(null, pos, SoundEvents.BLOCK_SAND_HIT, SoundCategory.PLAYERS, 0.8f, 0.8f);
		world.spawnParticles(ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 12, 0.25, 0.02, 0.25, 0.01);
		player.sendMessage(Text.translatable("message.aptitudes.ground.placed"), true);
		resyncAround(world, pos);
	}

	// ------------------------------------------------------------------ Borrar

	/** Mayús + clic con la tiza sobre el suelo: borra la nota propia (o cualquiera, si es operador) de ese hueco. */
	public static void eraseAt(ServerPlayerEntity player, BlockPos pos) {
		ServerWorld world = player.getServerWorld();
		GroundMessageState state = GroundMessageState.get(world);
		boolean op = player.hasPermissionLevel(2);
		GroundMessageState.Note target = null;
		for (GroundMessageState.Note n : state.notes()) {
			boolean author = n.author.equals(player.getUuid());
			if (n.pos.getSquaredDistance(pos) <= 2
					&& GroundMessageRules.canDelete(author, op, player.squaredDistanceTo(Vec3d.ofCenter(n.pos)))) {
				target = n;
				break;
			}
		}
		if (target == null) {
			player.sendMessage(Text.translatable("message.aptitudes.ground.none_here"), true);
			return;
		}
		state.remove(target.id);
		world.spawnParticles(ParticleTypes.ASH, target.pos.getX() + 0.5, target.pos.getY() + 0.05, target.pos.getZ() + 0.5,
				8, 0.25, 0.02, 0.25, 0.01);
		player.sendMessage(Text.translatable("message.aptitudes.ground.removed"), true);
		resyncAround(world, target.pos);
	}

	// ------------------------------------------------------------------ Valorar

	private static void rate(ServerPlayerEntity player, UUID id) {
		AptitudesConfig cfg = Aptitudes.config();
		MinecraftServer server = player.getServer();
		if (server == null || !cfg.groundMessagesEnabled) return;
		if (!LIMITER.tryAcquire(player.getUuid(), "ground_rate", server.getTicks(), 10)) return;
		ServerWorld world = player.getServerWorld();
		GroundMessageState state = GroundMessageState.get(world);
		GroundMessageState.Note note = state.note(id);
		if (note == null || player.isSpectator()) return;
		boolean isAuthor = note.author.equals(player.getUuid());
		if (!GroundMessageRules.canRate(isAuthor, note.ratedBy(player.getUuid()), player.squaredDistanceTo(Vec3d.ofCenter(note.pos)))) {
			return;
		}
		state.rate(note, player.getUuid());
		player.sendMessage(Text.translatable("message.aptitudes.ground.rated"), true);
		world.spawnParticles(ParticleTypes.WAX_ON, note.pos.getX() + 0.5, note.pos.getY() + 0.1, note.pos.getZ() + 0.5,
				6, 0.25, 0.05, 0.25, 0.02);

		GroundMessageState global = GroundMessageState.get(server.getOverworld());
		global.addTotal(note.author);
		ServerPlayerEntity author = server.getPlayerManager().getPlayer(note.author);
		if (author != null) {
			long day = GroundMessageRules.day(server.getOverworld().getTime());
			boolean reward = GroundMessageRules.rewards(global.rewardsToday(note.author, day),
					global.raterRewardedToday(note.author, player.getUuid(), day),
					PartyManager.inSameParty(author, player), world.getTime() - note.created, cfg.groundMessageRewardsPerDay);
			if (reward && author.isAlive()) {
				global.recordReward(note.author, player.getUuid(), day);
				if (cfg.groundMessageRewardHeal > 0) author.heal((float) cfg.groundMessageRewardHeal);
				int xp = XpEvents.diminished(author, "ground_message", cfg.groundMessageRewardXp);
				if (xp > 0) ProgressManager.addXp(author, Skill.COMBAT, xp);
				author.sendMessage(Text.translatable("message.aptitudes.ground.author_rewarded"), false);
			} else {
				author.sendMessage(Text.translatable("message.aptitudes.ground.author_rated"), false);
			}
			checkChronicler(author);
		}
		resyncAround(world, note.pos);
	}

	private static void checkChronicler(ServerPlayerEntity player) {
		MinecraftServer server = player.getServer();
		if (server == null) return;
		if (GroundMessageState.get(server.getOverworld()).total(player.getUuid()) >= CHRONICLER_RATINGS) {
			Achievements.grant(player, Achievement.ASH_CHRONICLER);
		}
	}

	// ------------------------------------------------------------------ Comando (operadores)

	/** Notas de la dimensión del jugador a {@code radius} bloques o menos, de la más cercana a la más lejana. */
	public static List<GroundMessageState.Note> near(ServerWorld world, Vec3d center, int radius) {
		List<GroundMessageState.Note> out = new ArrayList<>();
		for (GroundMessageState.Note n : GroundMessageState.get(world).notes()) {
			if (Vec3d.ofCenter(n.pos).squaredDistanceTo(center) <= (double) radius * radius) out.add(n);
		}
		out.sort(Comparator.comparingDouble(n -> Vec3d.ofCenter(n.pos).squaredDistanceTo(center)));
		return out;
	}

	/** Borra notas (operador): las de {@code radius} bloques alrededor o, si {@code author} no es null, las de ese autor. */
	public static int clear(ServerWorld world, Vec3d center, int radius, UUID author) {
		GroundMessageState state = GroundMessageState.get(world);
		List<UUID> ids = new ArrayList<>();
		for (GroundMessageState.Note n : state.notes()) {
			boolean match = author != null ? n.author.equals(author)
					: Vec3d.ofCenter(n.pos).squaredDistanceTo(center) <= (double) radius * radius;
			if (match) ids.add(n.id);
		}
		ids.forEach(state::remove);
		LAST_SENT.clear();
		return ids.size();
	}
}
