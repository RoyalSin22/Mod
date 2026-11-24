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
 * V90: coreografía visual del Brujo abisal. La maldición es un hilo carmesí que se enrosca hasta la víctima y late; el rayo abismal es
 * una línea violeta con jirones de sombra; los diablillos salen del suelo en un anillo de azufre; la perdición marca un sello que se
 * llena de almas y cae como una sentencia.
 */
final class WarlockClientFx {
	private static final int CRIMSON = 0xAD1457;
	private static final int ABYSS = 0x6A1B9A;
	private static final int SOUL = 0xB39DDB;

	private WarlockClientFx() {
	}

	private static ParticleEffect shadow(float size) {
		return ParticleFx.transition(SOUL, ABYSS, size);
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void curse(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WARLOCK_CURSE);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WARLOCK_CURSE), () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d start = e != null ? FxClient.handPos(e, true) : c.add(0, 1, 0);
			double len = start.distanceTo(c);
			Vec3d axis = len > 0.1 ? c.subtract(start).multiply(1 / len) : new Vec3d(0, 1, 0);
			Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
			side = side.lengthSquared() < 1e-6 ? new Vec3d(1, 0, 0) : side.normalize();
			for (double d = 0; d < len; d += 0.3) {
				double wob = Math.sin(d * 3) * 0.2;
				Vec3d pt = start.add(axis.multiply(d)).add(side.multiply(wob));
				FxClient.spawn(ParticleTypes.CRIMSON_SPORE, pt.x, pt.y, pt.z, 0, 0, 0);
				if (rng.nextDouble() < 0.4) FxClient.spawn(ParticleFx.dust(CRIMSON, 1.0f), pt.x, pt.y, pt.z, 0, 0, 0);
			}
			for (int i = 0; i < FxClient.n(16); i++) {
				Vec3d d = randomDirection(rng).multiply(0.1);
				FxClient.spawn(ParticleTypes.CRIMSON_SPORE, c.x, c.y, c.z, d.x, d.y, d.z);
			}
			if (FxClient.isLocal(p.entityId())) ScreenDynamics.vignette(CRIMSON, 0.22f, 80, 120, 500, 0f);
		});
	}

	static void pulse(ClientWorld world, FxEventPayload p) {
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		int ring = FxClient.n(10);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			FxClient.spawn(ParticleTypes.CRIMSON_SPORE, c.x + Math.cos(a) * 0.5, c.y, c.z + Math.sin(a) * 0.5, 0, 0.04, 0);
		}
	}

	static void bolt(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WARLOCK_BOLT);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WARLOCK_BOLT), () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len < 0.5) return;
			Vec3d axis = end.subtract(start).normalize();
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			for (double d = 0; d < len; d += step) {
				Vec3d c = start.add(axis.multiply(d));
				FxClient.spawn(shadow(1.3f), c.x, c.y, c.z, 0, 0, 0);
				if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.04, 0.02, (rng.nextDouble() - 0.5) * 0.04);
			}
			for (int i = 0; i < FxClient.n(20); i++) {
				Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
				FxClient.spawn(i % 2 == 0 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMOKE, end.x, end.y, end.z, d.x, d.y, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
			if (f > 0) {
				ScreenDynamics.kick(-1.8f * f, 0, 30, 280);
				ScreenDynamics.vignette(ABYSS, 0.3f * f, 30, 80, 380, 0f);
			}
		});
	}

	static void imps(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WARLOCK_IMPS);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WARLOCK_IMPS), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			FxTimeline.every(8, 1, i -> {
				double r = 0.4 + i * 0.25;
				int pts = FxClient.n(16);
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + i * 0.3;
					FxClient.spawn(ParticleTypes.CRIMSON_SPORE, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 0, 0.08, 0);
					if (j % 4 == 0) FxClient.spawn(ParticleTypes.FLAME, c.x + Math.cos(a) * r, c.y + 0.1, c.z + Math.sin(a) * r, 0, 0.05, 0);
				}
			});
			for (int i = 0; i < FxClient.n(18); i++) {
				Vec3d d = randomDirection(rng).multiply(0.12);
				FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y + 0.5, c.z, d.x, Math.abs(d.y) + 0.05, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 14);
			if (f > 0) {
				ScreenDynamics.shake(0.2f * f, 200, 20);
				ScreenDynamics.vignette(CRIMSON, 0.25f * f, 100, 160, 500, 0f);
			}
		});
	}

	static void doom(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		if (p.power() < 0.5f) {
			PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WARLOCK_DOOM);
			FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WARLOCK_DOOM), () -> {
				// Sello sobre la víctima: anillo horizontal que gira y almas que suben durante cinco segundos.
				FxTimeline.every(25, 4, i -> {
					int pts = FxClient.n(16);
					for (int j = 0; j < pts; j++) {
						double a = Math.PI * 2 * j / pts + i * 0.25;
						Particle pt = FxClient.spawn(shadow(1.1f), c.x + Math.cos(a) * 0.9, c.y + 1.0 + 0.04 * i, c.z + Math.sin(a) * 0.9, 0, 0, 0);
						if (pt != null) pt.setMaxAge(6);
					}
					FxClient.spawn(ParticleTypes.SCULK_SOUL, c.x + (rng.nextDouble() - 0.5) * 0.6, c.y + 0.3, c.z + (rng.nextDouble() - 0.5) * 0.6, 0, 0.06, 0);
				});
			});
			return;
		}
		// Sentencia: columna de almas, onda y estruendo.
		FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0);
		for (int i = 0; i < 14; i++) FxClient.spawn(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y - 0.5 + i * 0.3, c.z, 0, 0.06, 0);
		for (int i = 0; i < FxClient.n(30); i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(ParticleTypes.SCULK_SOUL, c.x, c.y, c.z, d.x, d.y, d.z);
		}
		FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y + 0.5, c.z, 0, 0, 0);
		float f = FxMath.falloff(FxClient.distanceTo(c), 16);
		if (f > 0) {
			ScreenDynamics.shake(0.5f * f, 320, 22);
			FxClient.flash(ABYSS, 0.3f * f);
		}
	}
}
