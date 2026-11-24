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
 * V90: efectos de batalla de la Maldición de agonía, el Rayo abismal, los Diablillos y la Perdición del Brujo abisal. Dibujados por
 * {@code client/fx/WarlockClientFx}.
 */
public final class WarlockFx {
	private static final Set<String> HANDLED = Set.of("curse_of_agony", "abyssal_bolt", "summon_imps", "doom");

	private WarlockFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Maldición: el hilo carmesí llega a la víctima en el tick 3. */
	public static void curse(ServerPlayerEntity p, LivingEntity target) {
		Vec3d at = bodyOf(target);
		FxEvents.onAt(p, at, FxIds.WARLOCK_CURSE, color(p), 0f, at.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.WARLOCK_CURSE, () -> play(p, ModSounds.WARLOCK_CURSE, at, 0.9f, jitter(p, 1.0f, 0.06f)));
	}

	/** Un latido de la maldición. */
	public static void pulse(ServerPlayerEntity p, LivingEntity target) {
		FxEvents.onAt(p, bodyOf(target), FxIds.WARLOCK_PULSE, color(p), 0f, Vec3d.ZERO, seed(p));
	}

	/** Rayo abismal hacia {@code end}. */
	public static void bolt(ServerPlayerEntity p, Vec3d end) {
		FxEvents.on(p, FxIds.WARLOCK_BOLT, color(p), 1f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.WARLOCK_BOLT, () -> play(p, ModSounds.WARLOCK_BOLT, p.getPos(), 1.0f, jitter(p, 1.0f, 0.08f)));
	}

	/** Diablillos: surgen del suelo en el tick 5. */
	public static void imps(ServerPlayerEntity p) {
		FxEvents.on(p, FxIds.WARLOCK_IMPS, color(p), 0f, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.WARLOCK_IMPS, () -> play(p, ModSounds.WARLOCK_IMPS, p.getPos(), 1.0f, jitter(p, 1.0f, 0.06f)));
	}

	/** Perdición: la marca se fija ({@code sentence} falso) o cae la sentencia ({@code sentence} verdadero). */
	public static void doom(ServerPlayerEntity p, LivingEntity target, boolean sentence) {
		Vec3d at = bodyOf(target);
		FxEvents.onAt(p, at, FxIds.WARLOCK_DOOM, color(p), sentence ? 1f : 0f, Vec3d.ZERO, seed(p));
		if (sentence) play(p, ModSounds.WARLOCK_DOOM, at, 1.2f, jitter(p, 1.0f, 0.04f));
		else atImpact(p, PlayerAnimIds.WARLOCK_DOOM, () -> play(p, ModSounds.WARLOCK_CURSE, at, 0.8f, 0.7f));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.WARLOCK);
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

	private static Vec3d bodyOf(LivingEntity e) {
		return new Vec3d(e.getX(), e.getBodyY(0.6), e.getZ());
	}
}
