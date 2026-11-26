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
 * V89: efectos de batalla del Disparo arcano, Disparo en abanico, Rebote y Ojo de halcón del Mago pistolero. Un evento por disparo,
 * dibujado por {@code client/fx/GunslingerClientFx}. {@code power} de los eventos: bits 0-1 = variante; bit 4 (16) = inicia la animación.
 */
public final class GunslingerFx {
	private static final Set<String> HANDLED = Set.of("arcane_shot", "fan_the_hammer", "ricochet", "deadeye");
	public static final int ARCANE = 0;
	public static final int RICOCHET = 1;
	public static final int DEADEYE = 2;
	public static final int FAN = 3;
	public static final int ANIMATE = 16;

	private GunslingerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/**
	 * Un disparo de {@code start} a {@code end}. El Disparo arcano espera al fotograma de impacto de su animación; los demás suenan en
	 * cuanto el servidor los dispara, porque ya van escalonados.
	 */
	public static void shot(ServerPlayerEntity p, Vec3d start, Vec3d end, int variant, boolean animate) {
		FxEvents.onAt(p, start, FxIds.GUNSLINGER_SHOT, color(p), variant | (animate ? ANIMATE : 0), end.subtract(start), seed(p));
		if (variant == ARCANE) {
			atImpact(p, PlayerAnimIds.GUNSLINGER_SHOT, () -> play(p, ModSounds.GUNSLINGER_SHOT, start, 1.0f, jitter(p, 1.0f, 0.08f)));
		} else {
			play(p, ModSounds.GUNSLINGER_SHOT, start, variant == RICOCHET ? 0.7f : 0.8f, jitter(p, variant == FAN ? 1.25f : 1.1f, 0.12f));
		}
	}

	/** Ojo de halcón: la marca se fija sobre {@code target} y la animación de apuntar arranca. */
	public static void mark(ServerPlayerEntity p, Vec3d target, boolean animate) {
		FxEvents.onAt(p, target, FxIds.GUNSLINGER_MARK, color(p), animate ? ANIMATE : 0, Vec3d.ZERO, seed(p));
		play(p, ModSounds.GUNSLINGER_MARK, target, 0.6f, jitter(p, 1.0f, 0.1f));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.GUNSLINGER);
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
