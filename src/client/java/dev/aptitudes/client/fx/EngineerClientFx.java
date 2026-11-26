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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V86: coreografía visual del Ingeniero. El garfio dispara un cable de chispas que se tensa y se clava con un estallido de metal; la
 * granada de pulso describe un arco con una luz que parpadea cada vez más rápido y estalla en un anillo eléctrico con esquirlas de hierro.
 */
final class EngineerClientFx {
	private static final int ORANGE = 0xFF9F43;
	private static final int CYAN = 0x7FE7FF;

	private EngineerClientFx() {
	}

	private static ParticleEffect shard() {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.IRON_NUGGET));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	// ------------------------------------------------------------------ Garfio

	static void grapple(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ENGINEER_GRAPPLE);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ENGINEER_GRAPPLE);
		// Preparación: chispas que se reúnen en la muñeca mientras el muelle se carga.
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d hand = FxClient.handPos(now, true);
			FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, hand.x, hand.y, hand.z, (rng.nextDouble() - 0.5) * 0.1, 0.02, (rng.nextDouble() - 0.5) * 0.1);
		});
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len < 0.5) return;
			Vec3d axis = end.subtract(start).normalize();
			int travel = MathHelper.clamp((int) Math.ceil(len / 6.0), 1, 4);
			// El cable avanza por tramos: línea de chispas con la punta del garfio brillando.
			FxTimeline.every(travel, 1, i -> {
				double d0 = len * i / travel;
				double d1 = len * (i + 1) / travel;
				double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
				for (double d = d0; d < d1; d += step) {
					Vec3d c = start.add(axis.multiply(d));
					FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 0, 0, 0);
					FxClient.spawn(ParticleFx.transition(ORANGE, CYAN, 0.8f), c.x, c.y, c.z, 0, 0, 0);
				}
				Vec3d head = start.add(axis.multiply(d1));
				FxClient.spawn(ParticleTypes.FLAME, head.x, head.y, head.z, 0, 0, 0);
			});
			FxTimeline.later(travel, () -> {
				// Anclaje: lluvia de chispas y esquirlas de hierro que caen.
				int sparks = FxClient.n(26);
				for (int i = 0; i < sparks; i++) {
					Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.25);
					FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, d.x, d.y, d.z);
				}
				int shards = FxClient.n(8);
				for (int i = 0; i < shards; i++) {
					Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.15);
					FxClient.spawn(shard(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.06, d.z);
				}
				FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
				float f = FxMath.falloff(FxClient.distanceTo(end), 10);
				if (f > 0) ScreenDynamics.shake(0.15f * f, 120, 22);
			});
			// El que lanza nota el tirón: el campo de visión se estira y la cámara se echa atrás.
			if (FxClient.isLocal(p.entityId())) {
				ScreenDynamics.fovPunch(10f, 40, 120, 450);
				ScreenDynamics.kick(-1.2f, 0, 40, 300);
				ScreenDynamics.vignette(CYAN, 0.2f, 40, 100, 400, 0f);
			}
		});
	}

	// ------------------------------------------------------------------ Granada de pulso

	static void grenade(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ENGINEER_GRENADE);
		int release = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ENGINEER_GRENADE);
		int fuse = Math.max(8, Math.round(p.power()));
		FxTimeline.later(release, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			int flight = MathHelper.clamp((int) Math.ceil(len / 2.5), 2, Math.max(2, fuse - 4));
			// Vuelo en arco con un destello que parpadea más rápido a medida que se acaba la espoleta.
			FxTimeline.every(flight, 1, i -> {
				double t = (i + 1) / (double) flight;
				Vec3d c = start.lerp(end, t).add(0, Math.sin(Math.PI * t) * Math.min(2.5, len * 0.22), 0);
				FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y, c.z, 0, 0.01, 0);
				FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 0, 0, 0);
			});
			// Ya en el suelo: parpadeo cada vez más rápido hasta el estallido.
			int rest = fuse - release - flight;
			for (int k = 0, at = 0; at < rest; k++) {
				int when = at;
				FxTimeline.later(flight + when, () -> FxClient.spawn(ParticleTypes.FLAME, end.x, end.y + 0.2, end.z, 0, 0.01, 0));
				at += Math.max(2, 7 - k);
			}
		});
		// El estallido cae cuando el servidor aplica el daño, a {@code fuse} ticks del lanzamiento.
		FxTimeline.later(fuse, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d c = eye.add(p.dx(), p.dy(), p.dz());
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0);
			FxClient.spawn(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 0, 0, 0);
			// Anillo eléctrico que se abre por el suelo y una esfera de chispas.
			FxTimeline.every(6, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 6, 3.5);
				int pts = FxClient.n(Math.max(12, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + i * 0.3;
					FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.03, 0);
				}
			});
			int sparks = FxClient.n(40);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.15 + rng.nextDouble() * 0.3);
				FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + 0.5, c.z, d.x, Math.abs(d.y), d.z);
			}
			int shards = FxClient.n(10);
			for (int i = 0; i < shards; i++) {
				Vec3d d = randomDirection(rng).multiply(0.2 + rng.nextDouble() * 0.25);
				FxClient.spawn(shard(), c.x, c.y + 0.3, c.z, d.x, Math.abs(d.y) + 0.12, d.z);
			}
			float f = FxMath.falloff(FxClient.distanceTo(c), 16);
			if (f > 0) {
				ScreenDynamics.shake(0.45f * f, 260, 24);
				ScreenDynamics.fovPunch(-5f * f, 40, 20, 300);
				FxClient.flash(CYAN, 0.25f * f);
			}
		});
	}
}
