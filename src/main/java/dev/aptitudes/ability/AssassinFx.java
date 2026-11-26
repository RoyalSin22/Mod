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

import java.util.List;
import java.util.Set;

/**
 * V84: efectos de batalla de las cuatro activas base del Asesino (sonido, partículas, cámara y pantalla). El servidor decide cuándo y
 * dónde y manda un evento por efecto; cada cliente lo dibuja ({@code client/fx/AssassinClientFx}). Los sonidos caen en el fotograma de
 * impacto de la animación del cuerpo ({@link PlayerAnimIds#IMPACT_TICKS}).
 */
public final class AssassinFx {
	/** Activas que sustituyen al efecto genérico de lanzamiento ({@link CastFx}). */
	private static final Set<String> HANDLED = Set.of("vanish", "shadow_step", "fan_of_knives", "shuriken");

	private AssassinFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.ASSASSIN);
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

	/** Desaparecer: la bomba de humo estalla en el barrido de capa (tick 3). */
	public static void vanish(ServerPlayerEntity p) {
		FxEvents.on(p, FxIds.ASSASSIN_VANISH, color(p), 0f, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.ASSASSIN_VANISH, () -> sound(p, ModSounds.ASSASSIN_VANISH, 1.0f, jitter(p, 1.0f, 0.08f)));
	}

	/** Paso de sombra: estela desde el origen y estocada al reaparecer a la espalda del objetivo (tick 3). */
	public static void step(ServerPlayerEntity p, Vec3d from, Vec3d to) {
		FxEvents.onAt(p, from, FxIds.ASSASSIN_STEP, color(p), 0f, to.subtract(from), seed(p));
		sound(p, ModSounds.ASSASSIN_STEP, 0.9f, jitter(p, 1.0f, 0.08f));
		// El tajo de la estocada suena en el fotograma de impacto.
		atImpact(p, PlayerAnimIds.ASSASSIN_STEP, () -> sound(p, ModSounds.ASSASSIN_SLASH, 0.8f, jitter(p, 1.0f, 0.1f)));
	}

	/** Abanico de cuchillas: una cuchilla por objetivo, lanzada en el tick 4; cada impacto suena al llegar. */
	public static void fan(ServerPlayerEntity p, double radius, List<LivingEntity> targets) {
		FxEvents.on(p, FxIds.ASSASSIN_FAN, color(p), (float) radius, Vec3d.ZERO, seed(p));
		Vec3d eye = p.getEyePos();
		int index = 0;
		for (LivingEntity target : targets) {
			Vec3d dir = target.getEyePos().subtract(eye);
			FxEvents.on(p, FxIds.ASSASSIN_KNIFE, color(p), index++, dir, seed(p));
		}
		atImpact(p, PlayerAnimIds.ASSASSIN_FAN, () -> {
			sound(p, ModSounds.ASSASSIN_THROW, 1.0f, jitter(p, 0.9f, 0.08f));
			for (LivingEntity target : targets) {
				p.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.ASSASSIN_IMPACT,
						SoundCategory.PLAYERS, 0.7f, jitter(p, 1.0f, 0.2f));
			}
		});
	}

	/** Shuriken: se lanza con el latigazo (tick 3); el impacto llega un par de ticks después. */
	public static void shuriken(ServerPlayerEntity p, Vec3d end, boolean hit) {
		Vec3d eye = p.getEyePos();
		FxEvents.on(p, FxIds.ASSASSIN_SHURIKEN, color(p), hit ? 1f : 0f, end.subtract(eye), seed(p));
		atImpact(p, PlayerAnimIds.ASSASSIN_SHURIKEN, () -> {
			sound(p, ModSounds.ASSASSIN_THROW, 0.9f, jitter(p, 1.15f, 0.08f));
			if (hit) {
				AbilityState.schedule(p.getServer(), 2, () -> p.getWorld().playSound(null, end.x, end.y, end.z, ModSounds.ASSASSIN_IMPACT,
						SoundCategory.PLAYERS, 1.0f, jitter(p, 1.0f, 0.1f)));
			}
		});
	}
}
