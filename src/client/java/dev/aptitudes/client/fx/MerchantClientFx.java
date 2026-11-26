package dev.aptitudes.client.fx;

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
 * V88: coreografía visual del Mercader táctico. La moneda es un destello dorado con estela y monedas que saltan al impactar; la
 * guardia dorada levanta un domo de motas y un anillo de monedas que giran.
 */
final class MerchantClientFx {
	private static final int GOLD = 0xFFD54F;
	private static final int EMERALD = 0x50E080;

	private MerchantClientFx() {
	}

	private static ParticleEffect coinDust(float size) {
		return ParticleFx.transition(GOLD, EMERALD, size);
	}

	private static ParticleEffect coin() {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.GOLD_NUGGET));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void coin(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.MERCHANT_COIN);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MERCHANT_COIN);
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len < 0.5) return;
			Vec3d axis = end.subtract(start).normalize();
			int travel = Math.max(1, Math.min(4, (int) Math.ceil(len / 6.0)));
			FxTimeline.every(travel, 1, i -> {
				double d0 = len * i / travel;
				double d1 = len * (i + 1) / travel;
				double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
				for (double d = d0; d < d1; d += step) {
					Vec3d c = start.add(axis.multiply(d));
					FxClient.spawn(coinDust(0.9f), c.x, c.y, c.z, 0, 0, 0);
					if (rng.nextDouble() < 0.4) FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y, c.z, 0, 0, 0);
				}
				Vec3d head = start.add(axis.multiply(d1));
				FxClient.spawn(ParticleTypes.CRIT, head.x, head.y, head.z, 0, 0, 0);
			});
			FxTimeline.later(travel, () -> {
				FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
				int coins = FxClient.n(10);
				for (int i = 0; i < coins; i++) {
					Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.2);
					FxClient.spawn(coin(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.1, d.z);
				}
				int sparks = FxClient.n(14);
				for (int i = 0; i < sparks; i++) {
					Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.15);
					FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, d.x, d.y, d.z);
				}
				float f = FxMath.falloff(FxClient.distanceTo(end), 10);
				if (f > 0) ScreenDynamics.shake(0.15f * f, 120, 22);
			});
			if (FxClient.isLocal(p.entityId())) ScreenDynamics.kick(-1.0f, 0, 30, 220);
		});
	}

	static void guard(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.MERCHANT_GUARD);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MERCHANT_GUARD);
		FxTimeline.later(delay, () -> {
			// Domo que se cierra: arcos de motas doradas desde el suelo hasta sobre la cabeza y un anillo de monedas girando.
			FxTimeline.every(10, 1, i -> {
				Entity now = FxClient.entity(world, p.entityId());
				Vec3d c = now != null ? now.getPos() : FxClient.entityPos(world, p);
				for (int arm = 0; arm < 6; arm++) {
					double a = Math.PI * 2 * arm / 6 + i * 0.3;
					double h = i * 0.22;
					double r = 1.0 * Math.cos(Math.min(Math.PI / 2, h / 2.2 * Math.PI / 2));
					Particle pt = FxClient.spawn(coinDust(1.1f), c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r, 0, 0, 0);
					if (pt != null) pt.setMaxAge(16);
				}
				if (i % 2 == 0) {
					double a = i * 0.9;
					FxClient.spawn(coin(), c.x + Math.cos(a) * 0.9, c.y + 1.0, c.z + Math.sin(a) * 0.9, -Math.sin(a) * 0.05, 0.02, Math.cos(a) * 0.05);
				}
			});
			Vec3d c = FxClient.entityPos(world, p);
			int sparks = FxClient.n(20);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.1);
				FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, d.x, d.y, d.z);
			}
			if (FxClient.isLocal(p.entityId())) {
				ScreenDynamics.vignette(GOLD, 0.3f, 80, 160, 600, 0f);
				ScreenDynamics.fovPunch(-3f, 60, 60, 400);
				FxClient.flash(GOLD, 0.2f);
			}
		});
	}
}
