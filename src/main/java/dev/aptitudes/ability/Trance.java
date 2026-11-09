package dev.aptitudes.ability;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Fase F2 cableada: trance de combate. La sintonía (en memoria del servidor) se llena con golpes críticos a criaturas y
 * con habilidades; al llegar al máximo, el jugador entra en trance durante {@link TranceRules#DURATION_TICKS} ticks y
 * hace más daño. Las reglas puras están en {@link TranceRules}.
 *
 * <p>Sin HUD propio todavía: la sintonía se muestra en la barra de acción. El {@link #DAMAGE_MULTIPLIER} es una decisión
 * provisional de diseño (el plan no fijaba el bonus del trance) y debe validarse en partida.</p>
 */
public final class Trance {
	/** Multiplicador de daño saliente durante el trance (provisional). */
	public static final float DAMAGE_MULTIPLIER = 1.2f;

	private static final Map<UUID, Integer> GAUGE = new HashMap<>();
	/** Inicio del trance activo por jugador (tick del servidor). */
	private static final Map<UUID, Long> START = new HashMap<>();

	private Trance() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || damageTaken <= 0f) return;
			if (!(source.getAttacker() instanceof ServerPlayerEntity player) || source.getSource() != player) return;
			// Solo criaturas: ni jugadores, ni uno mismo.
			if (entity == player || entity instanceof PlayerEntity) return;
			// Golpe crítico de Minecraft: cayendo, sin tocar el suelo y sin correr.
			if (player.fallDistance > 0f && !player.isOnGround() && !player.isSprinting()) onCritical(player);
		});
		ServerTickEvents.END_SERVER_TICK.register(Trance::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> forget(handler.getPlayer()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			GAUGE.clear();
			START.clear();
		});
	}

	public static void onCritical(ServerPlayerEntity player) {
		gain(player, TranceRules.GAIN_CRITICAL);
	}

	public static void onSkill(ServerPlayerEntity player) {
		gain(player, TranceRules.GAIN_SKILL);
	}

	/** Multiplicador de daño saliente del jugador en este momento (1 fuera del trance). */
	public static float damageMultiplier(ServerPlayerEntity player) {
		return active(player) ? DAMAGE_MULTIPLIER : 1f;
	}

	public static boolean active(ServerPlayerEntity player) {
		Long start = START.get(player.getUuid());
		return start != null && TranceRules.active(start, now(player));
	}

	private static void gain(ServerPlayerEntity player, int amount) {
		// Durante el trance la sintonía no se llena: evita encadenar trances.
		if (active(player)) return;
		int gauge = TranceRules.gain(GAUGE.getOrDefault(player.getUuid(), 0), amount);
		if (TranceRules.full(gauge)) {
			start(player);
			return;
		}
		GAUGE.put(player.getUuid(), gauge);
		player.sendMessage(Text.literal("Sintonía " + gauge + "/" + TranceRules.MAX_GAUGE).formatted(Formatting.GOLD), true);
	}

	private static void start(ServerPlayerEntity player) {
		GAUGE.put(player.getUuid(), 0);
		START.put(player.getUuid(), now(player));
		player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
				SoundCategory.PLAYERS, 0.8f, 1.5f);
		player.sendMessage(Text.literal("¡Trance!").formatted(Formatting.GOLD, Formatting.BOLD), true);
	}

	/** Termina los trances cumplidos y avisa al jugador. */
	private static void tick(MinecraftServer server) {
		for (Iterator<Map.Entry<UUID, Long>> it = START.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<UUID, Long> entry = it.next();
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
			if (player == null) {
				it.remove();
				continue;
			}
			if (!TranceRules.active(entry.getValue(), now(player))) {
				it.remove();
				player.sendMessage(Text.literal("El trance termina").formatted(Formatting.GRAY), true);
			}
		}
	}

	public static void forget(ServerPlayerEntity player) {
		GAUGE.remove(player.getUuid());
		START.remove(player.getUuid());
	}

	private static long now(ServerPlayerEntity player) {
		return player.getServer() != null ? player.getServer().getTicks() : 0L;
	}
}
