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
 * V87: efectos de batalla de la Consagración y el Martillo de la justicia del Paladín. Un evento por efecto, dibujado por
 * {@code client/fx/PaladinClientFx}, con los sonidos en el fotograma de impacto de cada animación.
 */
public final class PaladinFx {
	private static final Set<String> HANDLED = Set.of("consecration", "hammer_of_justice");

	private PaladinFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Consagración: el suelo se ilumina en el tick 5 y dura {@code ticks} ticks. */
	public static void consecrate(ServerPlayerEntity p, Vec3d center, double radius, int ticks) {
		FxEvents.onAt(p, center, FxIds.PALADIN_CONSECRATE, color(p), (float) radius, new Vec3d(ticks, 0, 0), seed(p));
		atImpact(p, PlayerAnimIds.PALADIN_CONSECRATE, () -> play(p, ModSounds.PALADIN_CONSECRATE, center, 1.2f, jitter(p, 1.0f, 0.04f)));
	}

	/** Martillo: el rayo cae en el tick 4 sobre {@code end}. */
	public static void hammer(ServerPlayerEntity p, Vec3d end) {
		FxEvents.on(p, FxIds.PALADIN_HAMMER, color(p), 1f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.PALADIN_HAMMER, () -> play(p, ModSounds.PALADIN_HAMMER, end, 1.2f, jitter(p, 1.0f, 0.06f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.PALADIN);
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
