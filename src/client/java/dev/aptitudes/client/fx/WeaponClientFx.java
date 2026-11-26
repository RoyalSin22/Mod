package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.ability.WeaponFx;
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
 * V89: coreografía visual del Invocador de armas. Las espadas espectrales atraviesan el aire con estela azul y se clavan soltando
 * esquirlas de acero; el espadón traza un arco grande de luz; la prisión cierra un anillo de filos sobre el objetivo.
 */
final class WeaponClientFx {
	private static final int STEEL = 0x90A4AE;
	private static final int ICE = 0xB3E5FC;

	private WeaponClientFx() {
	}

	private static ParticleEffect ghost(float size) {
		return ParticleFx.transition(ICE, STEEL, size);
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

	static void blade(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int power = Math.round(p.power());
		if ((power & WeaponFx.ANIMATE) != 0) PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WEAPON_SUMMON);
		Vec3d start = new Vec3d(p.x(), p.y(), p.z());
		Vec3d delta = new Vec3d(p.dx(), p.dy(), p.dz());
		double len = delta.length();
		if (len < 0.3) return;
		Vec3d axis = delta.multiply(1 / len);
		Vec3d end = start.add(delta);
		// Destello de invocación en el origen y estela hasta el impacto.
		FxClient.spawn(ParticleTypes.ENCHANTED_HIT, start.x, start.y, start.z, 0, 0, 0);
		double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
		for (double d = 0; d < len; d += step) {
			Vec3d c = start.add(axis.multiply(d));
			FxClient.spawn(ghost(1.0f), c.x, c.y, c.z, 0, 0, 0);
			if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.ENCHANTED_HIT, c.x, c.y, c.z, 0, 0, 0);
		}
		FxClient.spawn(ParticleTypes.SWEEP_ATTACK, end.x, end.y, end.z, 0, 0, 0);
		int sparks = FxClient.n(10);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.15);
			FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, d.x, d.y, d.z);
		}
		int shards = FxClient.n(4);
		for (int i = 0; i < shards; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.12);
			FxClient.spawn(shard(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.06, d.z);
		}
		float f = FxMath.falloff(FxClient.distanceTo(end), 12);
		if (f > 0) ScreenDynamics.shake(0.1f * f, 80, 24);
	}

	static void greatsword(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WEAPON_GREATSWORD);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WEAPON_GREATSWORD);
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			Vec3d delta = end.subtract(eye);
			double len = delta.length();
			if (len < 0.5) return;
			Vec3d axis = delta.multiply(1 / len);
			Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
			side = side.lengthSquared() < 1e-6 ? new Vec3d(1, 0, 0) : side.normalize();
			// Arco enorme: una hoja de luz que barre de arriba abajo a lo largo del recorrido, con el filo más denso.
			for (double d = 0.5; d < len; d += 0.5) {
				for (double h = -1.4; h <= 1.4; h += 0.35) {
					Vec3d c = eye.add(axis.multiply(d)).add(side.multiply(h * 0.9)).add(0, 0.4 - Math.abs(h) * 0.15, 0);
					Particle pt = FxClient.spawn(ghost(1.4f), c.x, c.y, c.z, axis.x * 0.05, 0, axis.z * 0.05);
					if (pt != null) pt.setMaxAge(8 + (int) (d));
				}
				Vec3d tip = eye.add(axis.multiply(d));
				FxClient.spawn(ParticleTypes.SWEEP_ATTACK, tip.x, tip.y, tip.z, 0, 0, 0);
			}
			int sparks = FxClient.n(24);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.15 + rng.nextDouble() * 0.2);
				FxClient.spawn(ParticleTypes.CRIT, end.x, end.y, end.z, d.x, d.y, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(eye), 14);
			if (f > 0) {
				ScreenDynamics.shake(0.4f * f, 240, 24);
				ScreenDynamics.kick(-2.2f * f, 0, 30, 320);
				FxClient.flash(ICE, 0.2f * f);
			}
		});
	}

	static void prison(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.WEAPON_SUMMON);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.WEAPON_SUMMON);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.later(delay, () -> {
			// Cinco espadas hincadas en círculo que se cierran, y un anillo que gira durante los treinta ticks de la prisión.
			FxTimeline.every(30, 2, i -> {
				double r = Math.max(0.5, 1.6 - 0.07 * i);
				int blades = 5;
				for (int b = 0; b < blades; b++) {
					double a = Math.PI * 2 * b / blades + i * 0.2;
					for (double h = -0.9; h <= 0.9; h += 0.3) {
						Particle pt = FxClient.spawn(ghost(1.0f), c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r, 0, 0, 0);
						if (pt != null) pt.setMaxAge(6);
					}
				}
				FxClient.spawn(ParticleTypes.ENCHANTED_HIT, c.x + (rng.nextDouble() - 0.5) * 1.2, c.y + (rng.nextDouble() - 0.5) * 1.2, c.z + (rng.nextDouble() - 0.5) * 1.2, 0, 0, 0);
			});
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 14);
			if (f > 0) ScreenDynamics.shake(0.2f * f, 160, 20);
		});
	}
}
