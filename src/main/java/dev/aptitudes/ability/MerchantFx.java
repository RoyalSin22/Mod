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
 * V88: efectos de batalla del Lanzamiento de moneda y la Guardia dorada del Mercader táctico. Dibujados por
 * {@code client/fx/MerchantClientFx}.
 */
public final class MerchantFx {
	private static final Set<String> HANDLED = Set.of("coin_toss", "golden_guard");

	private MerchantFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Moneda: sale en el tick 3 hasta {@code target}. */
	public static void coin(ServerPlayerEntity p, Vec3d target) {
		FxEvents.on(p, FxIds.MERCHANT_COIN, color(p), 1f, target.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.MERCHANT_COIN, () -> play(p, ModSounds.MERCHANT_COIN, p.getPos(), 0.9f, jitter(p, 1.0f, 0.1f)));
	}

	/** Guardia dorada: el escudo se forma en el tick 4. */
	public static void guard(ServerPlayerEntity p) {
		FxEvents.on(p, FxIds.MERCHANT_GUARD, color(p), 0f, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.MERCHANT_GUARD, () -> play(p, ModSounds.MERCHANT_GUARD, p.getPos(), 1.0f, jitter(p, 1.0f, 0.04f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.MERCHANT);
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
