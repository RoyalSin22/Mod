package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Role;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * V88: efectos de batalla de la Lanza de hueso y el Sifón de almas del Nigromante. Un evento por efecto (y uno por hilo de alma),
 * dibujado por {@code client/fx/NecromancerClientFx}, con los sonidos en el fotograma de impacto de la animación.
 */
public final class NecromancerFx {
	private static final Set<String> HANDLED = Set.of("bone_spear", "soul_siphon");

	private NecromancerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Lanza de hueso: sale en el tick 4 y atraviesa hasta {@code end}. */
	public static void spear(ServerPlayerEntity p, Vec3d end) {
		FxEvents.on(p, FxIds.NECRO_SPEAR, color(p), 1f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.NECRO_SPEAR, () -> play(p, ModSounds.NECROMANCER_SPEAR, p.getPos(), 1.0f, jitter(p, 1.0f, 0.08f)));
	}

	/** Sifón: el pulso sale del nigromante en el tick 4. */
	public static void siphon(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.NECRO_SIPHON, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.NECRO_SIPHON, () -> play(p, ModSounds.NECROMANCER_SIPHON, p.getPos(), 1.0f, jitter(p, 1.0f, 0.06f)));
	}

	/** Un hilo de alma que va de {@code victim} al nigromante. */
	public static void link(ServerPlayerEntity p, LivingEntity victim) {
		Vec3d from = new Vec3d(victim.getX(), victim.getBodyY(0.6), victim.getZ());
		FxEvents.onAt(p, from, FxIds.NECRO_SIPHON_LINK, color(p), 0f, p.getEyePos().subtract(from), seed(p));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.NECROMANCER);
	}

	private static int seed(ServerPlayerEntity p) {
		return p.getRandom().nextInt();
	}

	private static float jitter(ServerPlayerEntity p, float base, float spread) {
		return base + (p.getRandom().nextFloat() - 0.5f) * spread;
	}

	private static void play(ServerPlayerEntity p, SoundEvent sound, Vec3d at, float volume, float pitch) {
		p.getWorld().playSound(null, at.x, at.y, at.z, sound, SoundCategory.PLAYERS, volume, pitch);
	}

	private static void atImpact(ServerPlayerEntity p, String animation, Runnable task) {
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(animation), () -> {
			if (p.isAlive()) task.run();
		});
	}
}
