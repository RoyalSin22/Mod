package dev.aptitudes.morph;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.ModAttachments;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tamaño de colisión de los jugadores transformados (v26). Lo usa el mixin de
 * {@code PlayerEntity#getBaseDimensions} en los DOS lados: el servidor (física real) lee la forma guardada
 * del jugador; el cliente (su propio movimiento y el dibujo de la caja) usa la que recibe por red
 * ({@link #setClient}). Así ambos calculan lo mismo y no hay tirones de posición.
 */
public final class MorphSize {
	/** Formas conocidas por el cliente (jugador → tipo); en un mundo local convive con el servidor. */
	private static final Map<UUID, EntityType<?>> CLIENT = new ConcurrentHashMap<>();
	private static boolean warned;

	private MorphSize() {
	}

	public static void setClient(UUID player, @Nullable EntityType<?> type) {
		if (type == null || type == EntityType.PLAYER) CLIENT.remove(player);
		else CLIENT.put(player, type);
	}

	public static void clearClient() {
		CLIENT.clear();
	}

	@Nullable
	public static EntityType<?> typeOf(PlayerEntity player) {
		if (player.getWorld().isClient()) return CLIENT.get(player.getUuid());
		MorphData data = player.getAttached(ModAttachments.MORPH);
		if (data == null || !data.isMorphed()) return null;
		Identifier id = Identifier.tryParse(data.activeType());
		return id == null ? null : Registries.ENTITY_TYPE.getOrEmpty(id).orElse(null);
	}

	/** Dimensiones de la forma, o {@code null} si el jugador no está transformado (o duerme). */
	@Nullable
	public static EntityDimensions dimensions(PlayerEntity player, EntityPose pose) {
		if (pose == EntityPose.SLEEPING) return null;
		EntityType<?> type = typeOf(player);
		if (type == null || type == EntityType.PLAYER) return null;
		EntityDimensions base = type.getDimensions();
		float[] s = MorphSizeRules.clamp(base.width(), base.height(), base.eyeHeight());
		return EntityDimensions.changing(s[0], s[1]).withEyeHeight(s[2]);
	}

	/**
	 * Recalcula la caja del jugador. Si el mixin no se aplicó (nombre de método distinto en esta versión),
	 * lo avisa una vez en el registro en lugar de tumbar el juego (el mixin usa {@code require = 0}).
	 */
	public static void refresh(PlayerEntity player) {
		player.calculateDimensions();
		EntityDimensions expected = dimensions(player, player.getPose());
		if (expected != null && !warned && Math.abs(player.getWidth() - expected.width() * player.getScale()) > 0.01f) {
			warned = true;
			Aptitudes.LOGGER.warn("[Aptitudes] La colisión de las transformaciones no se aplicó "
					+ "(mixin de getBaseDimensions no encontrado); las formas mantienen el tamaño de jugador.");
		}
	}
}
