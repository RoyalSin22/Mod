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
 * V89: efectos de batalla de la Andanada de espadas, Lluvia de espadas, Espadón espectral y Prisión de espadas del Invocador de armas.
 * Dibujados por {@code client/fx/WeaponClientFx}. {@code power}: bit 4 (16) = inicia la animación.
 */
public final class WeaponFx {
	private static final Set<String> HANDLED = Set.of("blade_volley", "sword_rain", "spectral_greatsword", "blade_prison");
	public static final int VOLLEY = 0;
	public static final int RAIN = 1;
	public static final int ANIMATE = 16;

	private WeaponFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	/** Una espada espectral que va de {@code start} a {@code end}. */
	public static void blade(ServerPlayerEntity p, Vec3d start, Vec3d end, int kind, boolean animate, int index) {
		FxEvents.onAt(p, start, FxIds.WEAPON_BLADE, color(p), kind | (animate ? ANIMATE : 0), end.subtract(start), seed(p));
		float pitch = kind == RAIN ? 1.2f : 1.0f + 0.05f * index;
		play(p, ModSounds.SWORDSMAN_CUT, end, 0.8f, pitch);
	}

	/** Espadón espectral: el tajo cae en el tick 4 a lo largo de {@code end}. */
	public static void greatsword(ServerPlayerEntity p, Vec3d end) {
		FxEvents.on(p, FxIds.WEAPON_GREATSWORD, color(p), 1f, end.subtract(p.getEyePos()), seed(p));
		atImpact(p, PlayerAnimIds.WEAPON_GREATSWORD, () -> play(p, ModSounds.WEAPON_GREATSWORD, p.getPos(), 1.1f, jitter(p, 1.0f, 0.06f)));
	}

	/** Prisión: las espadas se cierran sobre {@code target}. */
	public static void prison(ServerPlayerEntity p, Vec3d target) {
		FxEvents.onAt(p, target, FxIds.WEAPON_PRISON, color(p), ANIMATE, Vec3d.ZERO, seed(p));
		atImpact(p, PlayerAnimIds.WEAPON_SUMMON, () -> play(p, ModSounds.WEAPON_PRISON, target, 1.0f, jitter(p, 1.0f, 0.06f)));
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.WEAPON_SUMMONER);
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
