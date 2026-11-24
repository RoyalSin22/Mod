package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V87: coreografía visual del Espadachín. El iaido deja una estela blanca de acero con un tajo que se abre al final; Mil cortes cae como
 * una ráfaga de tajos diagonales con chispas y un destello por golpe.
 */
final class SwordsmanClientFx {
	private static final int STEEL = 0xECEFF1;
	private static final int PALE = 0x90A4AE;

	private SwordsmanClientFx() {
	}

	private static ParticleEffect steel(float size) {
		return ParticleFx.transition(STEEL, PALE, size);
	}

	// ------------------------------------------------------------------ Iaido

	static void iaido(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.SWORDSMAN_IAIDO);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.SWORDSMAN_IAIDO);
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d slash = new Vec3d(p.dx(), p.dy(), p.dz());
		// Preparación: el filo brilla en la cadera.
		FxTimeline.every(delay, 1, i -> {
			var now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d h = FxClient.handPos(now, true);
			FxClient.spawn(ParticleTypes.END_ROD, h.x, h.y - 0.2, h.z, 0, 0, 0);
		});
		FxTimeline.later(delay, () -> {
			double len = slash.length();
			if (len < 0.5) return;
			Vec3d axis = slash.multiply(1 / len);
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			// Estela: línea blanca a la altura del pecho con un abanico de tajos cada tres bloques.
			for (double d = 0; d < len; d += step) {
				Vec3d c = from.add(axis.multiply(d)).add(0, 1.1, 0);
				FxClient.spawn(steel(1.0f), c.x, c.y, c.z, 0, 0, 0);
				if (rng.nextDouble() < 0.4) FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + (rng.nextDouble() - 0.5) * 0.6, c.z, 0, 0, 0);
			}
			for (double d = 1.5; d < len; d += 2.0) {
				Vec3d c = from.add(axis.multiply(d)).add(0, 1.2, 0);
				FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
			}
			Vec3d end = from.add(slash).add(0, 1.1, 0);
			FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(from), 14);
			if (f > 0) {
				ScreenDynamics.fovPunch(14f * f, 30, 30, 340);
				ScreenDynamics.vignette(STEEL, 0.3f * f, 30, 60, 320, 0f);
			}
		});
	}

	// ------------------------------------------------------------------ Mil cortes

	static void cut(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int index = Math.round(p.power());
		if (index == 0) PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.SWORDSMAN_CUTS);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		// Cada corte: arco de tajo, un abanico de chispas en la dirección del golpe y un destello breve.
		double tilt = index % 2 == 0 ? 1 : -1;
		FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
		int sparks = FxClient.n(10);
		for (int i = 0; i < sparks; i++) {
			double s = 0.1 + rng.nextDouble() * 0.2;
			FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, tilt * s, (rng.nextDouble() - 0.4) * s, (rng.nextDouble() - 0.5) * s);
		}
		for (int i = 0; i < 6; i++) {
			double t = i / 5.0 - 0.5;
			FxClient.spawn(steel(0.9f), c.x + tilt * t * 1.2, c.y - t * 1.0, c.z, 0, 0, 0);
		}
		float f = FxMath.falloff(FxClient.distanceTo(c), 12);
		if (f > 0) {
			ScreenDynamics.shake(0.12f * f, 90, 24);
			if (index == 4) {
				ScreenDynamics.kick(-1.8f * f, 0, 30, 300);
				FxClient.flash(STEEL, 0.2f * f);
			}
		}
	}
}
