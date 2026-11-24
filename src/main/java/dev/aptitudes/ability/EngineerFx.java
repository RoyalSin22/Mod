package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Role;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * V86: efectos de batalla del Garfio y la Granada de pulso del Ingeniero. Misma estructura que {@link MageFx}: un evento por efecto,
 * dibujado por {@code client/fx/EngineerClientFx}, con los sonidos en el fotograma de impacto de la animación.
 */
public final class EngineerFx {
	private static final Set<String> HANDLED = Set.of("grapple", "pulse_grenade");
	/** Ticks de la espoleta de la granada (los mismos que su temporizador en {@code AbilitiesExtra}). */
	public static final int FUSE_TICKS = 20;

	private EngineerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.ENGINEER);
	}

	private static float jitter(ServerPlayerEntity p, float base, float spread) {
		return base + (p.getRandom().nextFloat() - 0.5f) * spread;
	}

	private static void play(ServerPlayerEntity p, SoundEvent sound, Vec3d at, float volume, float pitch) {
		p.getWorld().playSound(null, at.x, at.y, at.z, sound, SoundCategory.PLAYERS, volume, pitch);
	}

	/** Garfio: el cable sale en el tick 3 hasta el anclaje {@code hit}. */
	public static void grapple(ServerPlayerEntity p, Vec3d hit) {
		Vec3d eye = p.getEyePos();
		FxEvents.on(p, FxIds.ENGINEER_GRAPPLE, color(p), 0f, hit.subtract(eye), p.getRandom().nextInt());
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ENGINEER_GRAPPLE), () -> {
			if (p.isAlive()) play(p, ModSounds.ENGINEER_GRAPPLE, p.getPos(), 1.0f, jitter(p, 1.0f, 0.06f));
		});
	}

	/** Granada: sale de la mano en el tick 3 y estalla {@link #FUSE_TICKS} ticks después del lanzamiento. */
	public static void grenade(ServerPlayerEntity p, Vec3d point) {
		Vec3d eye = p.getEyePos();
		FxEvents.on(p, FxIds.ENGINEER_GRENADE, color(p), FUSE_TICKS, point.subtract(eye), p.getRandom().nextInt());
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ENGINEER_GRENADE), () -> {
			if (p.isAlive()) play(p, ModSounds.ENGINEER_THROW, p.getPos(), 0.8f, jitter(p, 1.0f, 0.06f));
		});
	}

	/** Sonido de la explosión de la granada en el punto de impacto. */
	public static void blast(ServerPlayerEntity p, Vec3d point) {
		play(p, ModSounds.ENGINEER_BLAST, point, 1.2f, jitter(p, 1.0f, 0.08f));
	}
}
