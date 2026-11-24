package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Role;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

/**
 * V85: efectos de batalla de las tres activas base del Herbolista que tienen efecto visible (Bálsamo, Lluvia sanadora y Nube de esporas).
 * Misma estructura que {@link MageFx}: el servidor manda un evento por efecto y el cliente lo dibuja
 * ({@code client/fx/HerbalistClientFx}), con los sonidos en el fotograma de impacto de la animación.
 */
public final class HerbalistFx {
	private static final Set<String> HANDLED = Set.of("balm", "healing_rain", "spore_cloud");

	private HerbalistFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.HERBALIST);
	}

	private static int seed(ServerPlayerEntity p) {
		return p.getRandom().nextInt();
	}

	private static void sound(ServerPlayerEntity p, SoundEvent sound, float volume, float pitch) {
		p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(), sound, SoundCategory.PLAYERS, volume, pitch);
	}

	private static float jitter(ServerPlayerEntity p, float base, float spread) {
		return base + (p.getRandom().nextFloat() - 0.5f) * spread;
	}

	private static void atImpact(ServerPlayerEntity p, String animation, Runnable task) {
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(animation), () -> {
			if (p.isAlive()) task.run();
		});
	}

	/** Bálsamo: la onda sale del herbolista y cada aliado curado recibe su propio remolino de hojas. */
	public static void balm(ServerPlayerEntity p, double radius, List<PlayerEntity> allies) {
		FxEvents.on(p, FxIds.HERBALIST_BALM, color(p), (float) radius, Vec3d.ZERO, seed(p));
		for (PlayerEntity ally : allies) {
			FxEvents.on(ally, FxIds.HERBALIST_BALM_ALLY, color(p), 0f, Vec3d.ZERO, seed(p));
		}
		atImpact(p, PlayerAnimIds.HERBALIST_BALM, () -> sound(p, ModSounds.HERBALIST_BALM, 1.0f, jitter(p, 1.0f, 0.06f)));
	}

	/** Lluvia sanadora: la nube se forma sobre {@code center} y llueve {@code ticks} ticks. */
	public static void rain(ServerPlayerEntity p, Vec3d center, double radius, int ticks) {
		FxEvents.onAt(p, center, FxIds.HERBALIST_RAIN, color(p), (float) radius, new Vec3d(ticks, 0, 0), seed(p));
		atImpact(p, PlayerAnimIds.HERBALIST_RAIN, () -> p.getWorld().playSound(null, center.x, center.y, center.z,
				ModSounds.HERBALIST_RAIN, SoundCategory.PLAYERS, 1.1f, jitter(p, 1.0f, 0.04f)));
	}

	/** Nube de esporas: la vaina sale de la mano en el tick 3 y estalla en {@code center}. */
	public static void spore(ServerPlayerEntity p, Vec3d center, double radius) {
		FxEvents.onAt(p, center, FxIds.HERBALIST_SPORE, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.HERBALIST_SPORE, () -> p.getWorld().playSound(null, center.x, center.y, center.z,
				ModSounds.HERBALIST_SPORE, SoundCategory.PLAYERS, 1.0f, jitter(p, 1.0f, 0.08f)));
	}
}
