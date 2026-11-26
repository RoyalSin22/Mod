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
 * V83: efectos de batalla de las cuatro activas base del Mago (sonido, partículas, cámara y pantalla). El servidor decide cuándo y
 * dónde y manda un evento por efecto; cada cliente lo dibuja ({@code client/fx/MageClientFx}). Los sonidos caen en el fotograma de
 * impacto de la animación del cuerpo ({@link PlayerAnimIds#IMPACT_TICKS}).
 */
public final class MageFx {
	/** Activas que sustituyen al efecto genérico de lanzamiento ({@link CastFx}). */
	private static final Set<String> HANDLED = Set.of("fireball", "blink", "time_warp", "arcane_missiles");

	private MageFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.MAGE);
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

	/** Proyectil arcano: carga en la mano y, en el tick de lanzamiento, rayo helicoidal hasta {@code end} con estallido de cristal. */
	public static void bolt(ServerPlayerEntity p, Vec3d end) {
		sound(p, ModSounds.MAGE_CHARGE, 0.8f, jitter(p, 1.0f, 0.08f));
		Vec3d eye = p.getEyePos();
		FxEvents.on(p, FxIds.MAGE_BOLT, color(p), (float) eye.distanceTo(end), end.subtract(eye), seed(p));
		atImpact(p, PlayerAnimIds.MAGE_BOLT, () -> {
			sound(p, ModSounds.MAGE_BOLT, 1.1f, jitter(p, 1.0f, 0.08f));
			// El estallido suena cuando llega el rayo (un par de ticks después del lanzamiento).
			AbilityState.schedule(p.getServer(), 2, () -> p.getWorld().playSound(null, end.x, end.y, end.z, ModSounds.MAGE_IMPACT,
					SoundCategory.PLAYERS, 1.0f, jitter(p, 1.0f, 0.1f)));
		});
	}

	/** Salto arcano: implosión en el origen, estela hasta el destino y estallido al reaparecer. */
	public static void blink(ServerPlayerEntity p, Vec3d from, Vec3d to) {
		sound(p, ModSounds.MAGE_BLINK, 1.0f, jitter(p, 1.0f, 0.06f));
		FxEvents.onAt(p, from, FxIds.MAGE_BLINK, color(p), 0f, to.subtract(from), seed(p));
	}

	/** Distorsión temporal: la onda y los relojes aparecen en el golpe de palmas (tick 6). */
	public static void timeWarp(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.MAGE_TIME_WARP, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.MAGE_TIME_WARP, () -> sound(p, ModSounds.MAGE_TIME, 1.2f, 1.0f));
	}

	/** Un misil arcano. {@code index} alterna el brazo que dispara. */
	public static void missile(ServerPlayerEntity p, Vec3d target, int index) {
		Vec3d eye = p.getEyePos();
		FxEvents.on(p, FxIds.MAGE_MISSILE, color(p), index, target.subtract(eye), seed(p));
		atImpact(p, PlayerAnimIds.MAGE_MISSILE_RIGHT, () -> sound(p, ModSounds.MAGE_MISSILE, 0.8f, 0.9f + 0.05f * (index % 4)));
	}
}
