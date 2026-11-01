package dev.aptitudes.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Transformaciones visibles en el cliente. Para cada jugador transformado se
 * mantiene una entidad "falsa" (no añadida al mundo) que se dibuja en su lugar,
 * copiando posición, rotación y animación del jugador real.
 */
public final class ClientMorphs {
	private static final Map<UUID, EntityType<?>> TYPES = new HashMap<>();
	private static final Map<UUID, LivingEntity> FAKES = new HashMap<>();

	private ClientMorphs() {
	}

	static void set(UUID player, String typeId) {
		FAKES.remove(player);
		Identifier id = typeId.isEmpty() ? null : Identifier.tryParse(typeId);
		EntityType<?> type = id == null ? null : Registries.ENTITY_TYPE.getOrEmpty(id).orElse(null);
		if (type == null || type == EntityType.PLAYER) TYPES.remove(player);
		else TYPES.put(player, type);
	}

	static void clear() {
		TYPES.clear();
		FAKES.clear();
	}

	/** Avanza la animación de caminar de cada entidad falsa al ritmo del jugador. */
	static void tick(MinecraftClient client) {
		if (client.world == null) return;
		for (Map.Entry<UUID, LivingEntity> e : FAKES.entrySet()) {
			Entity player = client.world.getPlayerByUuid(e.getKey());
			if (player instanceof LivingEntity living) {
				e.getValue().limbAnimator.updateLimbs(living.limbAnimator.getSpeed(), 1.0f);
				e.getValue().age = living.age;
			}
		}
	}

	/** Entidad a dibujar en lugar del jugador, o {@code null} si no está transformado. */
	@Nullable
	public static LivingEntity fakeFor(AbstractClientPlayerEntity player) {
		EntityType<?> type = TYPES.get(player.getUuid());
		if (type == null) return null;
		LivingEntity fake = FAKES.get(player.getUuid());
		if (fake == null || fake.getType() != type || fake.getWorld() != player.getWorld()) {
			Entity created = type.create(player.getWorld());
			if (!(created instanceof LivingEntity living)) return null;
			fake = living;
			FAKES.put(player.getUuid(), fake);
		}
		copyPose(player, fake);
		return fake;
	}

	private static void copyPose(AbstractClientPlayerEntity p, LivingEntity f) {
		f.setPos(p.getX(), p.getY(), p.getZ());
		f.prevX = p.prevX;
		f.prevY = p.prevY;
		f.prevZ = p.prevZ;
		f.lastRenderX = p.lastRenderX;
		f.lastRenderY = p.lastRenderY;
		f.lastRenderZ = p.lastRenderZ;
		f.setYaw(p.getYaw());
		f.prevYaw = p.prevYaw;
		f.setPitch(p.getPitch());
		f.prevPitch = p.prevPitch;
		f.bodyYaw = p.bodyYaw;
		f.prevBodyYaw = p.prevBodyYaw;
		f.headYaw = p.headYaw;
		f.prevHeadYaw = p.prevHeadYaw;
		f.hurtTime = p.hurtTime;
		f.handSwingProgress = p.handSwingProgress;
		f.lastHandSwingProgress = p.lastHandSwingProgress;
		f.setOnGround(p.isOnGround());
		f.setInvisible(p.isInvisible()); // la invisibilidad (Asesino, pociones) también oculta el disfraz
	}
}
