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
 * V90: efectos de batalla de las canciones del Bardo (Valor, Escudo y Réquiem) y su Disonancia. Dibujados por
 * {@code client/fx/BardClientFx}.
 */
public final class BardFx {
	private static final Set<String> HANDLED = Set.of("song_of_valor", "ballad_of_shielding", "requiem", "dissonance");
	public static final int VALOR = 0;
	public static final int SHIELD = 1;
	public static final int REQUIEM = 2;

	private BardFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Canción de {@code variant} con alcance {@code radius}; suena en el tick 4. */
	public static void song(ServerPlayerEntity p, int variant, double radius) {
		FxEvents.on(p, FxIds.BARD_SONG, color(p), (float) radius, new Vec3d(variant, 0, 0), seed(p));
		atImpact(p, PlayerAnimIds.BARD_SONG, () -> play(p, ModSounds.BARD_SONG, p.getPos(), 1.0f, variant == REQUIEM ? 0.75f : variant == SHIELD ? 1.1f : 1.0f));
	}

	/** Disonancia: la onda sale en el tick 3. */
	public static void dissonance(ServerPlayerEntity p, double radius) {
		FxEvents.on(p, FxIds.BARD_DISSONANCE, color(p), (float) radius, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.BARD_DISSONANCE, () -> play(p, ModSounds.BARD_DISSONANCE, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.BARD);
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
