package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.ability.TamerFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V90: coreografía visual del Domador. Las órdenes abren un anillo de polvo que se vuelve rojo al azuzar, verde al vincular y dorado
 * en el rugido del alfa; cada mascota recibe su propia aureola; la estampida arrasa en línea recta con una nube de polvo y pezuñas.
 */
final class TamerClientFx {
	private static final int BROWN = 0xA1887F;
	private static final int RED = 0xE53935;
	private static final int GREEN = 0x66BB6A;
	private static final int GOLD = 0xFFCA28;

	private TamerClientFx() {
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void command(ClientWorld world, FxEventPayload p) {
		int variant = Math.round(p.power());
		int tone = variant == TamerFx.BOND ? GREEN : variant == TamerFx.ALPHA ? GOLD : RED;
		ParticleEffect dust = ParticleFx.transition(BROWN, tone, 1.3f);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.TAMER_COMMAND);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.TAMER_COMMAND), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(3, p.dx());
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, radius);
				int pts = FxClient.n(Math.max(16, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts;
					Particle pt = FxClient.spawn(dust, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.04, 0);
					if (pt != null) pt.setMaxAge(12);
				}
			});
			if (variant == TamerFx.ALPHA) FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y + 1.2, c.z, 0, 0, 0);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 14);
			if (f > 0) {
				ScreenDynamics.vignette(tone, 0.22f * f, 80, 160, 500, 0f);
				if (variant == TamerFx.ALPHA) ScreenDynamics.shake(0.4f * f, 320, 18);
			}
		});
	}

	static void pet(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		boolean bond = p.power() >= 0.5f;
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.TAMER_COMMAND), () -> {
			// Aureola que sube alrededor de la mascota.
			FxTimeline.every(8, 1, i -> {
				for (int arm = 0; arm < 2; arm++) {
					double a = i * 0.8 + arm * Math.PI;
					FxClient.spawn(bond ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER, c.x + Math.cos(a) * 0.6, c.y - 0.5 + i * 0.14, c.z + Math.sin(a) * 0.6, 0, 0.02, 0);
				}
			});
			if (bond) for (int i = 0; i < 3; i++) FxClient.spawn(ParticleTypes.HEART, c.x + (rng.nextDouble() - 0.5) * 0.5, c.y + 0.6, c.z + (rng.nextDouble() - 0.5) * 0.5, 0, 0.04, 0);
		});
	}

	static void stampede(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.TAMER_STAMPEDE);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.TAMER_STAMPEDE), () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			Vec3d start = new Vec3d(eye.x, (e != null ? e.getY() : p.y()) + 0.2, eye.z);
			Vec3d delta = new Vec3d(end.x - start.x, 0, end.z - start.z);
			double len = delta.length();
			if (len < 0.5) return;
			Vec3d axis = delta.multiply(1 / len);
			Vec3d side = new Vec3d(-axis.z, 0, axis.x);
			for (double d = 0; d < len; d += 0.4) {
				for (int k = -1; k <= 1; k++) {
					Vec3d c = start.add(axis.multiply(d)).add(side.multiply(k * 0.8));
					Particle pt = FxClient.spawn(ParticleTypes.CLOUD, c.x, c.y, c.z, axis.x * 0.1, 0.03, axis.z * 0.1);
					if (pt != null) pt.setMaxAge(14);
					if (rng.nextDouble() < 0.25) FxClient.chunk(world, FxClient.groundState(world, c.x, c.y, c.z), c.x, c.y, c.z, axis.x * 0.2, 0.2, axis.z * 0.2, 0.1f, 24, 0.08f, 0.3f);
				}
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 14);
			if (f > 0) {
				ScreenDynamics.fovPunch(9f * f, 40, 80, 360);
				ScreenDynamics.shake(0.45f * f, 280, 24);
			}
		});
	}
}
