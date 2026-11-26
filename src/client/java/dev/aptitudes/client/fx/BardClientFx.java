package dev.aptitudes.client.fx;

import dev.aptitudes.ability.BardFx;
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
 * V90: coreografía visual del Bardo. Las canciones son ondas de notas que se abren en espiral con el color de cada una (dorado para el
 * valor, azul para el escudo, violeta para el réquiem); la disonancia es una onda de notas rotas y chispas rojas que sacude la cámara.
 */
final class BardClientFx {
	private static final int PINK = 0xEC407A;
	private static final int GOLD = 0xFFD54F;
	private static final int BLUE = 0x4FC3F7;
	private static final int VIOLET = 0x8E44AD;

	private BardClientFx() {
	}

	static void song(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int variant = Math.round(p.dx());
		int tone = variant == BardFx.SHIELD ? BLUE : variant == BardFx.REQUIEM ? VIOLET : GOLD;
		ParticleEffect note = ParticleFx.transition(PINK, tone, 1.2f);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BARD_SONG);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BARD_SONG), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(3, p.power());
			// Tres espirales de notas que suben y se abren, y un anillo de luz en el suelo.
			FxTimeline.every(14, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 14, radius);
				for (int arm = 0; arm < 3; arm++) {
					double a = i * 0.5 + arm * Math.PI * 2 / 3;
					Particle pt = FxClient.spawn(ParticleTypes.NOTE, c.x + Math.cos(a) * r * 0.6, c.y + 0.3 + i * 0.12, c.z + Math.sin(a) * r * 0.6, (rng.nextDouble()), 0, 0);
					if (pt != null) pt.setMaxAge(16);
				}
				int pts = FxClient.n(Math.max(14, (int) (r * 6)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts;
					FxClient.spawn(note, c.x + Math.cos(a) * r, c.y + 0.15, c.z + Math.sin(a) * r, 0, 0.03, 0);
				}
			});
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 2);
			if (f > 0) {
				ScreenDynamics.vignette(tone, 0.22f * f, 150, 300, 700, 0.3f);
				ScreenDynamics.fovPunch(3f * f, 100, 200, 600);
			}
		});
	}

	static void dissonance(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BARD_DISSONANCE);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BARD_DISSONANCE), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(3, p.power());
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, radius);
				int pts = FxClient.n(Math.max(16, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + rng.nextDouble() * 0.1;
					Particle pt = FxClient.spawn(ParticleTypes.NOTE, c.x + Math.cos(a) * r, c.y + 0.5 + rng.nextDouble() * 0.8, c.z + Math.sin(a) * r, rng.nextDouble(), 0, 0);
					if (pt != null) pt.setMaxAge(10);
					if (j % 3 == 0) FxClient.spawn(ParticleTypes.CRIT, c.x + Math.cos(a) * r, c.y + 0.4, c.z + Math.sin(a) * r, 0, 0.05, 0);
				}
			});
			FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y + 1.2, c.z, 0, 0, 0);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 2.5);
			if (f > 0) {
				ScreenDynamics.shake(0.45f * f, 300, 26);
				ScreenDynamics.fovPunch(-6f * f, 40, 40, 360);
				ScreenDynamics.vignette(0xC62828, 0.25f * f, 40, 80, 400, 0f);
			}
		});
	}
}
