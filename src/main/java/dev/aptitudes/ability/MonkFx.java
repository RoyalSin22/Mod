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
 * V88: efectos de batalla de la Patada voladora y los Cien puños del Monje. Dibujados por {@code client/fx/MonkClientFx}.
 */
public final class MonkFx {
	private static final Set<String> HANDLED = Set.of("flying_kick", "hundred_fists");

	private MonkFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Patada: el impacto cae en el tick 3; {@code travel} es el recorrido del desplazamiento. */
	public static void kick(ServerPlayerEntity p, Vec3d travel, boolean hit) {
		FxEvents.on(p, FxIds.MONK_KICK, color(p), hit ? 1f : 0f, travel, seed(p));
		atImpact(p, PlayerAnimIds.MONK_KICK, () -> play(p, ModSounds.MONK_KICK, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	/** Un puñetazo sobre {@code target}; el primero ({@code index} 0) inicia la animación. */
	public static void fist(ServerPlayerEntity p, LivingEntity target, int index) {
		Vec3d at = new Vec3d(target.getX(), target.getBodyY(0.6), target.getZ());
		FxEvents.onAt(p, at, FxIds.MONK_FIST, color(p), index, Vec3d.ZERO, seed(p));
		play(p, ModSounds.MONK_FIST, at, 0.9f, 0.85f + 0.06f * index);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.MONK);
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
