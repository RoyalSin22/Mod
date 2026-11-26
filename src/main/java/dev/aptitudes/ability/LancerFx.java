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
 * V87: efectos de batalla del Salto del dragón y la Estocada perforante del Lancero. Un evento por efecto, dibujado por
 * {@code client/fx/LancerClientFx}, con los sonidos en el fotograma de impacto de cada animación.
 */
public final class LancerFx {
	private static final Set<String> HANDLED = Set.of("dragon_leap", "piercing_thrust");
	/** Ticks entre el salto y el aterrizaje (los de {@code AbilitiesExtra}). */
	public static final int FLIGHT_TICKS = 24;

	private LancerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Salto: despegue en el tick 3 y, al llegar al suelo, el evento del aterrizaje adelantado para que su golpe caiga con el daño. */
	public static void leap(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.LANCER_LEAP, color(p), FLIGHT_TICKS, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.LANCER_LEAP, () -> play(p, ModSounds.LANCER_LEAP, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
		int lead = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.LANCER_SLAM);
		AbilityState.schedule(p.getServer(), FLIGHT_TICKS - lead, () -> {
			if (!p.isAlive()) return;
			FxEvents.on(p, FxIds.LANCER_SLAM, color(p), (float) radius, Vec3d.ZERO, seed(p));
			AbilityState.schedule(p.getServer(), lead, () -> play(p, ModSounds.LANCER_SLAM, p.getPos(), 1.2f, jitter(p, 1.0f, 0.06f)));
		});
	}

	/** Estocada: el trazo recorre la línea en el tick 3. */
	public static void thrust(ServerPlayerEntity p, Vec3d end, boolean hit) {
		FxEvents.on(p, FxIds.LANCER_THRUST, color(p), hit ? 1f : 0f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.LANCER_THRUST, () -> play(p, ModSounds.LANCER_THRUST, p.getPos(), 0.9f, jitter(p, 1.0f, 0.08f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.LANCER);
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
