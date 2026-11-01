package dev.aptitudes.role;

import dev.aptitudes.ability.RolePassives;
import dev.aptitudes.morph.MorphManager;
import dev.aptitudes.network.ResourcePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Valores de los recursos de rol. Son transitorios (se reinician al reconectar):
 * representan el "momento" del combate o del trabajo, no un progreso.
 */
public final class RoleResources {
	private static final int DECAY_DELAY_TICKS = 5 * 20;
	private static final float DECAY_PER_SECOND = 4f;

	private static final Map<UUID, EnumMap<Role, Float>> VALUES = new HashMap<>();
	private static final Map<UUID, Integer> LAST_GAIN = new HashMap<>();
	private static final Map<UUID, Integer> LAST_HURT = new HashMap<>();
	private static final Map<UUID, Long> LAST_SENT = new HashMap<>();

	private RoleResources() {
	}

	public static float get(ServerPlayerEntity player, Role role) {
		EnumMap<Role, Float> map = VALUES.get(player.getUuid());
		return map == null ? 0f : map.getOrDefault(role, 0f);
	}

	/** Carga el recurso del rol si es el principal o el secundario. */
	public static void gain(ServerPlayerEntity player, Role role, float amount) {
		if (amount <= 0 || !RoleManager.get(player).isActive(role)) return;
		amount *= 1.0f + (float) dev.aptitudes.ability.PathEffects.value(player, PathEffect.RESOURCE_GAIN); // Sendas
		EnumMap<Role, Float> map = VALUES.computeIfAbsent(player.getUuid(), k -> new EnumMap<>(Role.class));
		map.put(role, Math.min(RoleResource.MAX, map.getOrDefault(role, 0f) + amount));
		LAST_GAIN.put(player.getUuid(), player.getServer().getTicks());
		send(player, false);
	}

	/** El jugador recibió daño: Furia y Devoción se cargan; Concentración se rompe. */
	public static void onHurt(ServerPlayerEntity player, float amount) {
		LAST_HURT.put(player.getUuid(), player.getServer().getTicks());
		gain(player, Role.BERSERKER, amount * 3);
		gain(player, Role.PALADIN, amount * 1.5f);
	}

	public static boolean canEmpower(ServerPlayerEntity player, Role role) {
		return get(player, role) >= RoleResource.EMPOWER_COST;
	}

	public static void consumeEmpower(ServerPlayerEntity player, Role role) {
		EnumMap<Role, Float> map = VALUES.get(player.getUuid());
		if (map == null) return;
		map.put(role, Math.max(0f, map.getOrDefault(role, 0f) - RoleResource.EMPOWER_COST));
		RoleResource resource = RoleResource.of(role);
		player.sendMessage(Text.translatable("aptitudes.resource.empowered", Text.translatable(resource.translationKey()))
				.withColor(resource.color()), true);
		player.getServerWorld().spawnParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getBodyY(0.5), player.getZ(),
				20, 0.5, 0.6, 0.5, 0.1);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.8f, 1.4f);
		send(player, true);
	}

	/** Cada segundo: cargas pasivas y disipación de los recursos de combate. */
	public static void tick(MinecraftServer server) {
		int now = server.getTicks();
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			RoleData data = RoleManager.get(p);
			if (data.isActive(Role.MAGE) && now - LAST_HURT.getOrDefault(p.getUuid(), -10_000) > 100) gain(p, Role.MAGE, 2);
			if (data.isActive(Role.SHAPESHIFTER) && MorphManager.get(p).isMorphed()) gain(p, Role.SHAPESHIFTER, 2);
			if (data.isActive(Role.DRUID) && RolePassives.inWoodland(p)) gain(p, Role.DRUID, 1);
			if (data.isActive(Role.LANCER) && p.isSprinting()) gain(p, Role.LANCER, 3);
			if (data.isActive(Role.MONK) && p.isSneaking() && p.getVelocity().horizontalLengthSquared() < 1.0e-3) gain(p, Role.MONK, 4);

			EnumMap<Role, Float> map = VALUES.get(p.getUuid());
			if (map != null && now - LAST_GAIN.getOrDefault(p.getUuid(), 0) > DECAY_DELAY_TICKS) {
				map.replaceAll((role, value) -> RoleResource.of(role).decays() ? Math.max(0f, value - DECAY_PER_SECOND) : value);
			}
			send(p, false);
		}
	}

	public static void send(ServerPlayerEntity player, boolean force) {
		if (!ServerPlayNetworking.canSend(player, ResourcePayload.ID)) return;
		RoleData data = RoleManager.get(player);
		float primary = data.primary() == null ? 0 : get(player, data.primary());
		float secondary = data.secondary() == null ? 0 : get(player, data.secondary());
		long key = ((long) (int) primary << 32) | (int) secondary;
		Long last = LAST_SENT.put(player.getUuid(), key);
		if (force || last == null || last != key) {
			ServerPlayNetworking.send(player, new ResourcePayload(primary, secondary));
		}
	}

	public static void forget(ServerPlayerEntity player) {
		VALUES.remove(player.getUuid());
		LAST_GAIN.remove(player.getUuid());
		LAST_HURT.remove(player.getUuid());
		LAST_SENT.remove(player.getUuid());
	}
}
