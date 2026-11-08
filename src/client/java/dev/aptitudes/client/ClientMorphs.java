package dev.aptitudes.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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
		// v26: el cliente calcula la misma caja de colisión que el servidor.
		dev.aptitudes.morph.MorphSize.setClient(player, type);
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world != null && client.world.getPlayerByUuid(player) instanceof net.minecraft.entity.player.PlayerEntity p) {
			p.calculateDimensions();
		}
	}

	static void clear() {
		TYPES.clear();
		FAKES.clear();
		dev.aptitudes.morph.MorphSize.clearClient();
	}

	/** Avanza la animación de caminar de cada entidad falsa al ritmo del jugador. */
	static void tick(MinecraftClient client) {
		if (client.world == null) return;
		// v26: jugadores transformados que aparecieron después del aviso: ajusta su caja si no coincide.
		for (Map.Entry<UUID, EntityType<?>> e : TYPES.entrySet()) {
			Entity player = client.world.getPlayerByUuid(e.getKey());
			if (player != null && Math.abs(player.getWidth() - player.getDimensions(player.getPose()).width()) > 0.01f) {
				player.calculateDimensions();
			}
		}
		FAKES.entrySet().removeIf(e -> {
			var player = client.world.getPlayerByUuid(e.getKey());
			LivingEntity fake = e.getValue();
			if (player == null || fake.getWorld() != client.world) return true;
			animate(player, fake);
			return false;
		});
	}

	/** Avanza solo el estado visual. Las entidades de render nunca ejecutan IA ni física. */
	private static void animate(net.minecraft.entity.player.PlayerEntity player, LivingEntity fake) {
		fake.age = player.age;
		fake.limbAnimator.updateLimbs(player.limbAnimator.getSpeed(), 1.0f);
		if (fake instanceof BatEntity bat) {
			bat.setRoosting(false);
			bat.roostingAnimationState.stop();
			bat.flyingAnimationState.startIfNotRunning(fake.age);
		} else if (fake instanceof ChickenEntity chicken) {
			chicken.prevFlapProgress = chicken.flapProgress;
			chicken.prevMaxWingDeviation = chicken.maxWingDeviation;
			chicken.maxWingDeviation = MathHelper.clamp(chicken.maxWingDeviation + (player.isOnGround() ? -0.3f : 1.2f), 0f, 1f);
			if (!player.isOnGround()) chicken.flapSpeed = Math.max(chicken.flapSpeed, 1f);
			chicken.flapSpeed *= 0.9f;
			chicken.flapProgress += chicken.flapSpeed * 2f;
		} else if (fake instanceof SlimeEntity slime) {
			slime.lastStretch = slime.stretch;
			double motion = player.getY() - player.prevY;
			float target = player.isOnGround() ? 0f : (float) Math.clamp(motion * 0.5, -0.2, 0.35);
			slime.stretch += (target - slime.stretch) * 0.5f;
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
			animate(player, fake);
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
