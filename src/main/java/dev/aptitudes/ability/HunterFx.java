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
 * V90: efectos de batalla de la Marca, la Trampa de oso, la Lluvia de flechas y el Golpe de halcón del Cazador. Dibujados por
 * {@code client/fx/HunterClientFx}.
 */
public final class HunterFx {
	private static final Set<String> HANDLED = Set.of("hunters_mark", "bear_trap", "arrow_volley", "falcon_strike");
	public static final int ANIMATE = 16;

	private HunterFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Marca sobre la presa: el sello cae en el tick 3. */
	public static void mark(ServerPlayerEntity p, LivingEntity target) {
		Vec3d at = bodyOf(target);
		FxEvents.onAt(p, at, FxIds.HUNTER_MARK, color(p), 0f, at.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.HUNTER_MARK, () -> play(p, ModSounds.HUNTER_SHOT, at, 0.7f, jitter(p, 1.3f, 0.1f)));
	}

	/** Trampa colocada ({@code sprung} falso, tick 3) o cerrada ({@code sprung} verdadero). */
	public static void trap(ServerPlayerEntity p, Vec3d spot, boolean sprung) {
		FxEvents.onAt(p, spot, FxIds.HUNTER_TRAP, color(p), sprung ? 1f : 0f, Vec3d.ZERO, seed(p));
		if (sprung) play(p, ModSounds.HUNTER_TRAP, spot, 1.0f, jitter(p, 1.0f, 0.08f));
		else atImpact(p, PlayerAnimIds.HUNTER_TRAP, () -> play(p, ModSounds.HUNTER_TRAP, spot, 0.6f, jitter(p, 0.7f, 0.06f)));
	}

	/** Una flecha de la lluvia; la primera ({@code index} 0) inicia la animación. */
	public static void arrow(ServerPlayerEntity p, Vec3d start, Vec3d end, int index) {
		FxEvents.onAt(p, start, FxIds.HUNTER_ARROW, color(p), index == 0 ? ANIMATE : 0, end.subtract(start), seed(p));
		play(p, ModSounds.HUNTER_SHOT, end, 0.5f, jitter(p, 1.1f, 0.3f));
	}

	/** Un picotazo del halcón sobre la presa; el primero inicia la animación. */
	public static void falcon(ServerPlayerEntity p, LivingEntity target, int index) {
		Vec3d at = bodyOf(target);
		FxEvents.onAt(p, at, FxIds.HUNTER_FALCON, color(p), index, Vec3d.ZERO, seed(p));
		play(p, ModSounds.HUNTER_FALCON, at, 0.8f, jitter(p, 1.0f - 0.04f * index, 0.1f));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.HUNTER);
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
