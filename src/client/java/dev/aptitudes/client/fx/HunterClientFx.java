package dev.aptitudes.client.fx;

import dev.aptitudes.ability.HunterFx;
import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.particle.Particle;
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
 * V90: coreografía visual del Cazador. La marca fija un sello verde sobre la presa; la trampa deja un disco de dientes que se cierra
 * con astillas de hierro; la lluvia dibuja flechas que caen en diagonal y se clavan; el halcón pica en ráfagas de plumas.
 */
final class HunterClientFx {
	private static final int GREEN = 0x7CB342;
	private static final int PALE = 0xDCEDC8;
	private static final int BROWN = 0x8D6E63;

	private HunterClientFx() {
	}

	private static ParticleEffect leaf(float size) {
		return ParticleFx.transition(PALE, GREEN, size);
	}

	private static ParticleEffect shard(boolean feather) {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(feather ? Items.FEATHER : Items.IRON_NUGGET));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void mark(ClientWorld world, FxEventPayload p) {
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HUNTER_MARK);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HUNTER_MARK), () -> {
			// Sello: un anillo vertical con cruz que se cierra sobre la presa.
			FxTimeline.every(8, 1, i -> {
				double r = 1.0 - 0.07 * i;
				int pts = FxClient.n(14);
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts;
					FxClient.spawn(leaf(1.0f), c.x + Math.cos(a) * r, c.y + Math.sin(a) * r, c.z, 0, 0, 0);
				}
			});
			for (double d = -0.9; d <= 0.9; d += 0.3) {
				FxClient.spawn(ParticleTypes.END_ROD, c.x + d, c.y, c.z, 0, 0, 0);
				FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + d, c.z, 0, 0, 0);
			}
			FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, 0, 0.1, 0);
			if (FxClient.isLocal(p.entityId())) ScreenDynamics.kick(-0.8f, 0, 30, 200);
		});
	}

	static void trap(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		boolean sprung = p.power() >= 0.5f;
		if (!sprung) {
			PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HUNTER_TRAP);
			FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HUNTER_TRAP), () -> {
				// Disco de dientes que se asienta con polvo.
				int ring = FxClient.n(16);
				for (int i = 0; i < ring; i++) {
					double a = Math.PI * 2 * i / ring;
					FxClient.spawn(ParticleFx.dust(BROWN, 1.0f), c.x + Math.cos(a) * 0.6, c.y + 0.1, c.z + Math.sin(a) * 0.6, 0, 0.02, 0);
				}
				int dust = FxClient.n(10);
				for (int i = 0; i < dust; i++) FxClient.spawn(ParticleTypes.SMOKE, c.x + (rng.nextDouble() - 0.5), c.y + 0.1, c.z + (rng.nextDouble() - 0.5), 0, 0.02, 0);
			});
			return;
		}
		// Se cierra: los dientes saltan hacia dentro con chispas y astillas.
		for (int i = 0; i < FxClient.n(18); i++) {
			double a = Math.PI * 2 * i / 18;
			FxClient.spawn(ParticleTypes.CRIT, c.x + Math.cos(a) * 0.7, c.y + 0.15, c.z + Math.sin(a) * 0.7, -Math.cos(a) * 0.2, 0.15, -Math.sin(a) * 0.2);
		}
		for (int i = 0; i < FxClient.n(8); i++) {
			Vec3d d = randomDirection(rng).multiply(0.15);
			FxClient.spawn(shard(false), c.x, c.y + 0.2, c.z, d.x, Math.abs(d.y) + 0.12, d.z);
		}
		FxClient.spawn(ParticleTypes.EXPLOSION, c.x, c.y + 0.3, c.z, 0, 0, 0);
		float f = FxMath.falloff(FxClient.distanceTo(c), 12);
		if (f > 0) ScreenDynamics.shake(0.3f * f, 160, 24);
	}

	static void arrow(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		if ((Math.round(p.power()) & HunterFx.ANIMATE) != 0) {
			PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HUNTER_VOLLEY);
			if (FxClient.isLocal(p.entityId())) {
				ScreenDynamics.fovPunch(5f, 60, 100, 400);
				ScreenDynamics.kick(1.4f, 0, 60, 300);
			}
		}
		Vec3d start = new Vec3d(p.x(), p.y(), p.z());
		Vec3d delta = new Vec3d(p.dx(), p.dy(), p.dz());
		Vec3d end = start.add(delta);
		double len = delta.length();
		if (len < 0.3) return;
		Vec3d axis = delta.multiply(1 / len);
		double step = 0.5 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
		for (double d = 0; d < len; d += step) {
			Vec3d c = start.add(axis.multiply(d));
			FxClient.spawn(leaf(0.9f), c.x, c.y, c.z, 0, 0, 0);
		}
		FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, 0, 0.1, 0);
		for (int i = 0; i < FxClient.n(5); i++) {
			Vec3d d = randomDirection(rng).multiply(0.1);
			FxClient.spawn(shard(false), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.05, d.z);
		}
	}

	static void falcon(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int index = Math.round(p.power());
		if (index == 0) PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HUNTER_FALCON);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		// El halcón baja en picado: una estela de nubes desde arriba, plumas y un destello de garras.
		for (int k = 0; k < 6; k++) {
			double t = k / 5.0;
			Particle pt = FxClient.spawn(ParticleTypes.CLOUD, c.x + 1.5 * (1 - t), c.y + 3.5 * (1 - t), c.z, 0, 0, 0);
			if (pt != null) pt.setMaxAge(8);
		}
		FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
		for (int i = 0; i < FxClient.n(6); i++) {
			Vec3d d = randomDirection(rng).multiply(0.12);
			FxClient.spawn(shard(true), c.x, c.y, c.z, d.x, Math.abs(d.y) + 0.05, d.z);
		}
		float f = FxMath.falloff(FxClient.distanceTo(c), 12);
		if (f > 0) ScreenDynamics.shake(0.12f * f, 90, 24);
	}
}
