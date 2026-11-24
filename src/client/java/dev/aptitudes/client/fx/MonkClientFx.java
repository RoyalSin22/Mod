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
 * V88: coreografía visual del Monje. La patada deja una estela naranja de ki con una onda de choque al impactar; los cien puños
 * lanzan un destello y anillos de choque por golpe, cada vez más cerrados.
 */
final class MonkClientFx {
	private static final int ORANGE = 0xE67E22;
	private static final int PALE = 0xFFE0B2;

	private MonkClientFx() {
	}

	private static ParticleEffect ki(float size) {
		return ParticleFx.transition(PALE, ORANGE, size);
	}

	static void kick(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.MONK_KICK);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MONK_KICK);
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d travel = new Vec3d(p.dx(), p.dy(), p.dz());
		boolean hit = p.power() >= 0.5f;
		FxTimeline.later(delay, () -> {
			double len = travel.length();
			if (len < 0.5) return;
			Vec3d axis = travel.multiply(1 / len);
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			// Estela de ki a la altura de los pies y el torso.
			for (double d = 0; d < len; d += step) {
				Vec3d c = from.add(axis.multiply(d));
				FxClient.spawn(ki(1.2f), c.x, c.y + 0.9, c.z, 0, 0, 0);
				FxClient.spawn(ParticleTypes.FLAME, c.x, c.y + 0.3, c.z, 0, 0.01, 0);
			}
			Vec3d end = from.add(travel).add(0, 0.9, 0);
			if (hit) {
				FxClient.spawn(ParticleTypes.EXPLOSION, end.x, end.y, end.z, 0, 0, 0);
				int ring = FxClient.n(26);
				for (int i = 0; i < ring; i++) {
					double a = Math.PI * 2 * i / ring;
					FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, Math.cos(a) * 0.3, 0.05, Math.sin(a) * 0.3);
				}
				FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(from), 14);
			if (f > 0) {
				ScreenDynamics.fovPunch(12f * f, 30, 60, 340);
				if (hit) {
					ScreenDynamics.shake(0.4f * f, 220, 24);
					ScreenDynamics.kick(-2.0f * f, 0, 30, 300);
				}
			}
		});
	}

	static void fist(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int index = Math.round(p.power());
		if (index == 0) PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.MONK_FISTS);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		// Cada puñetazo: destello, anillo de choque más estrecho cuanto más avanza la ráfaga y chispas.
		FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, 0, 0, 0);
		int ring = FxClient.n(12);
		double speed = 0.35 - 0.025 * Math.min(index, 7);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			FxClient.spawn(ki(1.0f), c.x, c.y, c.z, Math.cos(a) * speed, 0.02, Math.sin(a) * speed);
		}
		int sparks = FxClient.n(6);
		for (int i = 0; i < sparks; i++) {
			FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.4, (rng.nextDouble() - 0.3) * 0.3, (rng.nextDouble() - 0.5) * 0.4);
		}
		if (index == 7) FxClient.spawn(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 0, 0, 0);
		float f = FxMath.falloff(FxClient.distanceTo(c), 10);
		if (f > 0) {
			ScreenDynamics.shake((index == 7 ? 0.35f : 0.12f) * f, index == 7 ? 220 : 80, 26);
			if (index == 7) ScreenDynamics.kick(-2.0f * f, 0, 30, 300);
		}
	}
}
