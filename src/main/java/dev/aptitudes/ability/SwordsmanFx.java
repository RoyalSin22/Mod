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
 * V87: efectos de batalla del Iaido y Mil cortes del Espadachín. Un evento por efecto (un evento por tajo en Mil cortes), dibujado por
 * {@code client/fx/SwordsmanClientFx}.
 */
public final class SwordsmanFx {
	private static final Set<String> HANDLED = Set.of("iaido", "thousand_cuts");

	private SwordsmanFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Iaido: el tajo y la estela recorren {@code slash} desde el origen; suena en el tick 3. */
	public static void iaido(ServerPlayerEntity p, Vec3d slash) {
		FxEvents.on(p, FxIds.SWORDSMAN_IAIDO, color(p), 0f, slash, seed(p));
		atImpact(p, PlayerAnimIds.SWORDSMAN_IAIDO, () -> play(p, ModSounds.SWORDSMAN_DRAW, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	/** Un tajo de Mil cortes sobre {@code target}; el primero ({@code index} 0) inicia la animación. */
	public static void cut(ServerPlayerEntity p, LivingEntity target, int index) {
		Vec3d at = new Vec3d(target.getX(), target.getBodyY(0.5), target.getZ());
		FxEvents.onAt(p, at, FxIds.SWORDSMAN_CUT, color(p), index, Vec3d.ZERO, seed(p));
		play(p, ModSounds.SWORDSMAN_CUT, at, 0.9f, 0.9f + 0.08f * index);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.SWORDSMAN);
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
