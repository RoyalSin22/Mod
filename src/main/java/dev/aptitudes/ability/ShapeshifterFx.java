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
 * V89: efectos de batalla de Abalanzarse y el Aullido de la bestia del Cambiaformas. Dibujados por
 * {@code client/fx/ShapeshifterClientFx}.
 */
public final class ShapeshifterFx {
	private static final Set<String> HANDLED = Set.of("pounce", "beast_howl");

	private ShapeshifterFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Abalanzarse: el salto con zarpazo; {@code travel} es el desplazamiento hasta el objetivo. */
	public static void pounce(ServerPlayerEntity p, Vec3d travel) {
		FxEvents.on(p, FxIds.SHAPE_POUNCE, color(p), 1f, travel, seed(p));
		atImpact(p, PlayerAnimIds.SHAPE_POUNCE, () -> play(p, ModSounds.SHAPESHIFTER_POUNCE, p.getPos(), 1.0f, jitter(p, 1.0f, 0.08f)));
	}

	/** Aullido: la onda sale en el tick 5. */
	public static void howl(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.SHAPE_HOWL, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.SHAPE_HOWL, () -> play(p, ModSounds.SHAPESHIFTER_HOWL, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.SHAPESHIFTER);
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
