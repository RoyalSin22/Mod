package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
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
 * V87: coreografía visual del Paladín. La consagración abre un círculo de luz dorada con pilares que suben mientras dura; el martillo de
 * la justicia traza un rayo dorado y cae con un estallido, un anillo vertical y chispas sagradas.
 */
final class PaladinClientFx {
	private static final int GOLD = 0xF1C40F;
	private static final int WHITE = 0xFFF8E1;

	private PaladinClientFx() {
	}

	private static ParticleEffect holy(float size) {
		return ParticleFx.transition(GOLD, WHITE, size);
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	// ------------------------------------------------------------------ Consagración

	static void consecrate(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.PALADIN_CONSECRATE);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.PALADIN_CONSECRATE);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		double radius = Math.max(2, p.power());
		int ticks = Math.max(20, Math.round(p.dx()));
		// Preparación: la luz baja hacia las manos alzadas.
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d pos = now.getPos();
			int k = FxClient.n(4);
			for (int j = 0; j < k; j++) {
				FxClient.spawn(ParticleTypes.END_ROD, pos.x + (rng.nextDouble() - 0.5) * 0.8, pos.y + 3.0, pos.z + (rng.nextDouble() - 0.5) * 0.8, 0, -0.12, 0);
			}
		});
		FxTimeline.later(delay, () -> {
			// El círculo se abre desde el centro hasta el radio y deja pilares de luz en ocho puntos.
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, radius);
				int pts = FxClient.n(Math.max(16, (int) (r * 8)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts;
					FxClient.spawn(holy(1.2f), c.x + Math.cos(a) * r, c.y + 0.12, c.z + Math.sin(a) * r, 0, 0.02, 0);
				}
			});
			for (int k = 0; k < 8; k++) {
				double a = Math.PI * 2 * k / 8;
				double px = c.x + Math.cos(a) * radius * 0.85;
				double pz = c.z + Math.sin(a) * radius * 0.85;
				FxTimeline.every(10, 1, i -> FxClient.spawn(ParticleTypes.END_ROD, px, c.y + i * 0.35, pz, 0, 0.03, 0));
			}
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y + 0.5, c.z, 0, 0, 0);
			// Mientras dura: un pulso dorado por segundo (los de la curación y el daño) y motas de luz que suben.
			FxTimeline.every(ticks / 20, 20, i -> {
				int pts = FxClient.n(Math.max(20, (int) (radius * 9)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts;
					Particle pt = FxClient.spawn(holy(1.0f), c.x + Math.cos(a) * radius, c.y + 0.12, c.z + Math.sin(a) * radius, 0, 0.0, 0);
					if (pt != null) pt.setMaxAge(14);
				}
				int motes = FxClient.n(10);
				for (int j = 0; j < motes; j++) {
					double a = rng.nextDouble() * Math.PI * 2;
					double r = Math.sqrt(rng.nextDouble()) * radius;
					FxClient.spawn(ParticleTypes.END_ROD, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.04, 0);
				}
			});
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 3);
			if (f > 0) {
				ScreenDynamics.vignette(GOLD, 0.25f * f, 200, ticks * 40L, 800, 0.25f);
				ScreenDynamics.fovPunch(4f * f, 100, 400, 600);
			}
		});
	}

	// ------------------------------------------------------------------ Martillo de la justicia

	static void hammer(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.PALADIN_HAMMER);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.PALADIN_HAMMER);
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d h = FxClient.handPos(now, true).add(0, 0.5, 0);
			FxClient.spawn(ParticleTypes.END_ROD, h.x, h.y, h.z, 0, 0.02, 0);
		});
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len > 0.5) {
				Vec3d axis = end.subtract(start).normalize();
				double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
				for (double d = 0; d < len; d += step) {
					Vec3d c = start.add(axis.multiply(d));
					FxClient.spawn(holy(1.2f), c.x, c.y, c.z, 0, 0, 0);
				}
			}
			// Impacto: columna de luz, anillo que se abre, chispas sagradas y un temblor fuerte.
			FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
			for (int i = 0; i < 12; i++) FxClient.spawn(ParticleTypes.END_ROD, end.x, end.y - 1.0 + i * 0.3, end.z, 0, 0.04, 0);
			int ring = FxClient.n(28);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(ParticleTypes.CRIT, end.x, end.y - 0.6, end.z, Math.cos(a) * 0.3, 0.02, Math.sin(a) * 0.3);
			}
			int sparks = FxClient.n(30);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.25);
				FxClient.spawn(holy(1.3f), end.x, end.y, end.z, d.x, d.y, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(end), 16);
			if (f > 0) {
				ScreenDynamics.shake(0.5f * f, 300, 24);
				ScreenDynamics.kick(-2.4f * f, 0, 30, 340);
				ScreenDynamics.fovPunch(-4f * f, 30, 20, 280);
				FxClient.flash(GOLD, 0.3f * f);
			}
		});
	}
}
