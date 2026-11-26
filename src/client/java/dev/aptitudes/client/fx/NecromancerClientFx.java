package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V88: coreografía visual del Nigromante. La lanza de hueso es una línea de polvo óseo con astillas de hueso que caen y un lamento de
 * almas al clavarse; el sifón abre un pulso verde-violeta y tira de hilos de alma de cada enemigo hacia el nigromante.
 */
final class NecromancerClientFx {
	private static final int BONE = 0xE0E0D0;
	private static final int VIOLET = 0x6C3483;
	private static final int SOUL = 0x9CFFB0;

	private NecromancerClientFx() {
	}

	private static ParticleEffect marrow(float size) {
		return ParticleFx.transition(BONE, VIOLET, size);
	}

	private static ParticleEffect shard() {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.BONE_MEAL));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	// ------------------------------------------------------------------ Lanza de hueso

	static void spear(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.NECRO_SPEAR);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.NECRO_SPEAR);
		// Preparación: el hueso se forma en la mano entre susurros de alma.
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d h = FxClient.handPos(now, true);
			FxClient.spawn(ParticleTypes.SOUL, h.x, h.y, h.z, 0, 0.03, 0);
			FxClient.spawn(marrow(0.9f), h.x, h.y, h.z, 0, 0, 0);
		});
		FxTimeline.later(delay, () -> {
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
				FxClient.spawn(marrow(1.2f), c.x, c.y, c.z, 0, 0, 0);
				if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SOUL, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.04, 0.02, (rng.nextDouble() - 0.5) * 0.04);
				if (rng.nextDouble() < 0.2) FxClient.spawn(shard(), c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.03, 0.0, (rng.nextDouble() - 0.5) * 0.03);
			}
			int burst = FxClient.n(24);
			for (int i = 0; i < burst; i++) {
				Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
				FxClient.spawn(i % 2 == 0 ? ParticleTypes.SOUL : ParticleTypes.SOUL_FIRE_FLAME, end.x, end.y, end.z, d.x, d.y, d.z);
			}
			int shards = FxClient.n(10);
			for (int i = 0; i < shards; i++) {
				Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.15);
				FxClient.spawn(shard(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.08, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
			if (f > 0) {
				ScreenDynamics.kick(-1.4f * f, 0, 30, 260);
				ScreenDynamics.vignette(VIOLET, 0.25f * f, 30, 80, 380, 0f);
			}
		});
	}

	// ------------------------------------------------------------------ Sifón de almas

	static void siphon(ClientWorld world, FxEventPayload p) {
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.NECRO_SIPHON);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.NECRO_SIPHON), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(2, p.power());
			// Pulso que se abre por el suelo y vuelve hacia dentro, como una respiración.
			FxTimeline.every(10, 1, i -> {
				double t = i < 5 ? (i + 1) / 5.0 : (10 - i) / 5.0;
				double r = radius * t;
				int pts = FxClient.n(Math.max(14, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + i * 0.25;
					FxClient.spawn(i < 5 ? ParticleTypes.SOUL : ParticleTypes.SOUL_FIRE_FLAME, c.x + Math.cos(a) * r, c.y + 0.15, c.z + Math.sin(a) * r, 0, 0.03, 0);
				}
			});
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 2.5);
			if (f > 0) {
				ScreenDynamics.vignette(SOUL, 0.28f * f, 100, 200, 600, 0.4f);
				ScreenDynamics.fovPunch(-4f * f, 80, 120, 500);
			}
		});
	}

	/** Un hilo de alma que viaja del enemigo (punto del evento) al nigromante. */
	static void link(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.NECRO_SIPHON);
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d to = from.add(p.dx(), p.dy(), p.dz());
		FxTimeline.later(delay, () -> {
			int steps = 8;
			FxTimeline.every(steps, 1, i -> {
				double t = (i + 1) / (double) steps;
				Entity e = FxClient.entity(world, p.entityId());
				Vec3d dest = e != null ? e.getEyePos() : to;
				double wob = Math.sin(t * Math.PI * 3) * 0.25;
				Vec3d c = from.lerp(dest, t).add(wob, Math.sin(t * Math.PI) * 0.6, -wob);
				FxClient.spawn(ParticleTypes.SOUL, c.x, c.y, c.z, 0, 0.0, 0);
				FxClient.spawn(ParticleFx.transition(SOUL, VIOLET, 1.0f), c.x, c.y, c.z, 0, 0, 0);
			});
			FxClient.spawn(ParticleTypes.SOUL_FIRE_FLAME, from.x, from.y, from.z, 0, 0.1, 0);
		});
	}
}
