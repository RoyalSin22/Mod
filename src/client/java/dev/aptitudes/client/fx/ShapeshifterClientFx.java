package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V89: coreografía visual del Cambiaformas. Abalanzarse deja una estela de violeta con zarpazos; el aullido abre una onda sónica en
 * anillos con motas violeta y un temblor de cámara.
 */
final class ShapeshifterClientFx {
	private static final int PURPLE = 0x9B59B6;
	private static final int LILAC = 0xE1BEE7;

	private ShapeshifterClientFx() {
	}

	private static ParticleEffect aura(float size) {
		return ParticleFx.transition(LILAC, PURPLE, size);
	}

	static void pounce(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.SHAPE_POUNCE);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.SHAPE_POUNCE);
		Vec3d from = FxClient.entityPos(world, p);
		Vec3d travel = new Vec3d(p.dx(), p.dy(), p.dz());
		FxTimeline.later(delay, () -> {
			double len = travel.length();
			if (len < 0.5) return;
			Vec3d axis = travel.multiply(1 / len);
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			for (double d = 0; d < len; d += step) {
				Vec3d c = from.add(axis.multiply(d));
				FxClient.spawn(aura(1.3f), c.x, c.y + 0.9, c.z, 0, 0, 0);
				if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y + 0.5, c.z, 0, 0.01, 0);
			}
			Vec3d end = from.add(travel).add(0, 1.0, 0);
			// Tres zarpazos paralelos al llegar.
			for (int k = -1; k <= 1; k++) {
				FxClient.spawn(ParticleTypes.SWEEP_ATTACK, end.x + k * 0.35, end.y + k * 0.2, end.z, 0, 0, 0);
			}
			int sparks = FxClient.n(14);
			for (int i = 0; i < sparks; i++) {
				FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, (rng.nextDouble() - 0.5) * 0.4, (rng.nextDouble() - 0.3) * 0.3, (rng.nextDouble() - 0.5) * 0.4);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(from), 14);
			if (f > 0) {
				ScreenDynamics.fovPunch(10f * f, 30, 40, 320);
				ScreenDynamics.shake(0.3f * f, 180, 24);
			}
		});
	}

	static void howl(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.SHAPE_HOWL);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.SHAPE_HOWL);
		FxTimeline.later(delay, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(2, p.power());
			// Tres anillos sónicos escalonados que se abren hasta el radio, con motas violeta que suben.
			for (int wave = 0; wave < 3; wave++) {
				int offset = wave * 3;
				FxTimeline.later(offset, () -> FxTimeline.every(8, 1, i -> {
					double r = FxMath.ringRadius(i + 1, 8, radius);
					int pts = FxClient.n(Math.max(16, (int) (r * 8)));
					for (int j = 0; j < pts; j++) {
						double a = Math.PI * 2 * j / pts;
						Particle pt = FxClient.spawn(aura(1.2f), c.x + Math.cos(a) * r, c.y + 0.3, c.z + Math.sin(a) * r, 0, 0.02, 0);
						if (pt != null) pt.setMaxAge(10);
					}
				}));
			}
			FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y + 1.2, c.z, 0, 0, 0);
			int motes = FxClient.n(24);
			for (int i = 0; i < motes; i++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = rng.nextDouble() * radius * 0.6;
				FxClient.spawn(aura(1.0f), c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.07, 0);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 3);
			if (f > 0) {
				ScreenDynamics.shake(0.5f * f, 380, 18);
				ScreenDynamics.fovPunch(-7f * f, 50, 80, 480);
				ScreenDynamics.vignette(PURPLE, 0.3f * f, 80, 200, 600, 0f);
			}
		});
	}
}
