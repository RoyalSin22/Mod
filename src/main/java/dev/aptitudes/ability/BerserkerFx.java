package dev.aptitudes.ability;

import dev.aptitudes.logic.FxIds;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.role.Role;
import dev.aptitudes.sound.ModSounds;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Set;

/**
 * V77: efectos de batalla de las cuatro activas base del Bersérker (sonido, partículas con física, cámara y pantalla).
 * El servidor solo decide cuándo y dónde; cada cliente dibuja el efecto ({@code client/fx/BerserkerFx}).
 */
public final class BerserkerFx {
	/** Activas que sustituyen al efecto genérico de lanzamiento ({@link CastFx}). */
	private static final Set<String> HANDLED = Set.of("rage", "war_cry", "leap_slam", "whirlwind");

	private BerserkerFx() {
	}

	public static boolean handles(String abilityId) {
		return abilityId != null && HANDLED.contains(abilityId);
	}

	private static int color(ServerPlayerEntity p) {
		return CastFx.colorOf(p, Role.BERSERKER);
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

	/** Furia: rugido, latidos y aura de brasas durante toda su duración. */
	public static void rage(ServerPlayerEntity p, int durationTicks) {
		sound(p, ModSounds.BERSERKER_ROAR, 1.0f, jitter(p, 1.0f, 0.1f));
		sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.35f, 0.7f);
		AbilityState.schedule(p.getServer(), 16, () -> {
			if (p.isAlive()) sound(p, ModSounds.BERSERKER_HEARTBEAT, 0.6f, 1.0f);
		});
		AbilityState.schedule(p.getServer(), 34, () -> {
			if (p.isAlive()) sound(p, ModSounds.BERSERKER_HEARTBEAT, 0.5f, 1.05f);
		});
		FxEvents.on(p, FxIds.BERSERKER_RAGE, color(p), durationTicks / 20f, Targeting.horizontalFacing(p), seed(p));
	}

	/** Grito de guerra: onda expansiva con escombros y sacudida proporcional a la cercanía. */
	public static void warCry(ServerPlayerEntity p, double radius) {
		sound(p, ModSounds.BERSERKER_WAR_CRY, 1.5f, jitter(p, 1.0f, 0.08f));
		sound(p, SoundEvents.ENTITY_RAVAGER_ROAR, 0.5f, 0.6f);
		FxEvents.on(p, FxIds.BERSERKER_WAR_CRY, color(p), (float) radius, Targeting.horizontalFacing(p), seed(p));
	}

	/** Despegue del salto demoledor: polvo, gruñido y estela mientras dura el vuelo. */
	public static void leapTakeoff(ServerPlayerEntity p, int flightTicks) {
		sound(p, ModSounds.BERSERKER_LEAP, 1.0f, jitter(p, 1.0f, 0.1f));
		sound(p, ModSounds.BERSERKER_WHOOSH, 0.6f, 0.8f);
		Vec3d facing = Targeting.horizontalFacing(p);
		int seed = seed(p);
		FxEvents.on(p, FxIds.BERSERKER_LEAP_TAKEOFF, color(p), 1f, facing, seed);
		FxEvents.on(p, FxIds.BERSERKER_LEAP_TRAIL, color(p), flightTicks, facing, seed);
	}

	/** Aterrizaje del salto: el golpe más pesado del kit. */
	public static void slam(ServerPlayerEntity p, double radius) {
		Vec3d ground = groundBelow(p);
		// Suena en el fotograma de impacto de la animación (PlayerAnimIds.IMPACT_TICKS), no al empezar el gesto.
		AbilityState.schedule(p.getServer(), PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BERSERKER_SLAM), () -> {
			sound(p, ModSounds.BERSERKER_SLAM, 1.8f, jitter(p, 1.0f, 0.1f));
			sound(p, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.5f, 0.55f);
		});
		FxEvents.onAt(p, ground, FxIds.BERSERKER_SLAM, color(p), (float) radius, Vec3d.ZERO, seed(p));
	}

	/** Un giro del Torbellino. {@code spin} = 0, 1, 2. */
	public static void whirl(ServerPlayerEntity p, int spin, double radius) {
		sound(p, ModSounds.BERSERKER_WHOOSH, 1.0f, 0.85f + 0.12f * spin);
		FxEvents.on(p, FxIds.BERSERKER_WHIRL, color(p), (float) radius, Targeting.horizontalFacing(p), spin);
	}

	/** Punto del suelo bajo el jugador (hasta 12 bloques); si no hay suelo, sus pies. */
	static Vec3d groundBelow(ServerPlayerEntity p) {
		Vec3d from = p.getPos().add(0, 0.5, 0);
		BlockHitResult hit = p.getWorld().raycast(new RaycastContext(from, from.add(0, -12, 0),
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
		return hit.getType() == HitResult.Type.BLOCK ? hit.getPos() : p.getPos();
	}
}
