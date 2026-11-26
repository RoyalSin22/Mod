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
 * V90: efectos de batalla del Domador: Azuzar, Vínculo vital, Estampida y Rugido del alfa. Dibujados por
 * {@code client/fx/TamerClientFx}.
 */
public final class TamerFx {
	private static final Set<String> HANDLED = Set.of("sic_em", "vital_bond", "stampede", "alpha_roar");
	public static final int SIC = 0;
	public static final int BOND = 1;
	public static final int ALPHA = 2;

	private TamerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Orden de {@code variant}; suena en el tick 4. */
	public static void command(ServerPlayerEntity p, int variant, double radius) {
		FxEvents.on(p, FxIds.TAMER_COMMAND, color(p), variant, new Vec3d(radius, 0, 0), seed(p));
		atImpact(p, PlayerAnimIds.TAMER_COMMAND, () -> play(p, ModSounds.TAMER_COMMAND, p.getPos(), 1.0f, variant == ALPHA ? 0.8f : 1.0f));
	}

	/** Efecto sobre una mascota. */
	public static void pet(ServerPlayerEntity p, LivingEntity pet, boolean bond) {
		FxEvents.onAt(p, bodyOf(pet), FxIds.TAMER_PET, color(p), bond ? 1f : 0f, Vec3d.ZERO, seed(p));
	}

	/** Estampida hasta {@code end}; suena en el tick 3. */
	public static void stampede(ServerPlayerEntity p, Vec3d end) {
		FxEvents.on(p, FxIds.TAMER_STAMPEDE, color(p), 1f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.TAMER_STAMPEDE, () -> play(p, ModSounds.TAMER_STAMPEDE, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.TAMER);
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
