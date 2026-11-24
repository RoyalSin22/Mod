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
 * V88: efectos de batalla de la Manada y la Tormenta de espinas del Druida. Dibujados por {@code client/fx/DruidClientFx}.
 */
public final class DruidFx {
	private static final Set<String> HANDLED = Set.of("summon_wolves", "thorn_storm");

	private DruidFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Un lobo aparece en {@code pos}. */
	public static void wolf(ServerPlayerEntity p, Vec3d pos) {
		FxEvents.onAt(p, pos, FxIds.DRUID_WOLF_SPAWN, color(p), 0f, Vec3d.ZERO, seed(p));
	}

	/** Manada: el aullido y la ola de hojas salen en el tick 5. */
	public static void howl(ServerPlayerEntity p) {
		FxEvents.on(p, FxIds.DRUID_WOLVES, color(p), 0f, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.DRUID_WOLVES, () -> play(p, ModSounds.DRUID_HOWL, p.getPos(), 0.9f, jitter(p, 1.0f, 0.06f)));
	}

	/** Tormenta de espinas: brotan en el tick 4. */
	public static void thorns(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.DRUID_THORNS, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.DRUID_THORNS, () -> play(p, ModSounds.DRUID_THORNS, p.getPos(), 1.0f, jitter(p, 1.0f, 0.08f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.DRUID);
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
