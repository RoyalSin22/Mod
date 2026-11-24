package dev.aptitudes.client.fx;

import dev.aptitudes.ability.GunslingerFx;
import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V89: coreografía visual del Mago pistolero. Cada disparo es un rayo cian con chispas y fogonazo en la boca del arma; los rebotes
 * dibujan un rayo dorado que salta de objetivo en objetivo; el Ojo de halcón fija una mira que se cierra y dispara certeros.
 */
final class GunslingerClientFx {
	private static final int CYAN = 0x26C6DA;
	private static final int WHITE = 0xE0F7FA;
	private static final int GOLD = 0xFFE082;

	private GunslingerClientFx() {
	}

	private static ParticleEffect beam(int variant, float size) {
		return variant == GunslingerFx.DEADEYE ? ParticleFx.transition(GOLD, WHITE, size) : ParticleFx.transition(CYAN, WHITE, size);
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void shot(ClientWorld world, FxEventPayload p) {
		int power = Math.round(p.power());
		int variant = power & 3;
		boolean animate = (power & GunslingerFx.ANIMATE) != 0;
		if (animate && variant != GunslingerFx.DEADEYE) PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.GUNSLINGER_SHOT);
		// El primer disparo suelto espera al retroceso de la animación; los escalonados salen en cuanto llega el evento.
		int delay = variant == GunslingerFx.ARCANE ? PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.GUNSLINGER_SHOT) : 0;
		FxTimeline.later(delay, () -> draw(world, p, variant));
	}

	private static void draw(ClientWorld world, FxEventPayload p, int variant) {
		Random rng = new Random(p.seed());
		Vec3d start = new Vec3d(p.x(), p.y(), p.z());
		Vec3d delta = new Vec3d(p.dx(), p.dy(), p.dz());
		double len = delta.length();
		if (len < 0.3) return;
		Vec3d axis = delta.multiply(1 / len);
		Vec3d end = start.add(delta);
		// Fogonazo.
		FxClient.spawn(ParticleTypes.FLASH, start.x, start.y, start.z, 0, 0, 0);
		for (int i = 0; i < FxClient.n(6); i++) {
			Vec3d d = randomDirection(rng).multiply(0.08).add(axis.multiply(0.15));
			FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, start.x, start.y, start.z, d.x, d.y, d.z);
		}
		// Rayo.
		double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
		for (double d = 0; d < len; d += step) {
			Vec3d c = start.add(axis.multiply(d));
			FxClient.spawn(beam(variant, 1.0f), c.x, c.y, c.z, 0, 0, 0);
			if (rng.nextDouble() < 0.25) FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y, c.z, 0, 0, 0);
		}
		// Impacto.
		int sparks = FxClient.n(variant == GunslingerFx.FAN ? 6 : 14);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.CRIT, end.x, end.y, end.z, d.x, d.y, d.z);
		}
		if (variant != GunslingerFx.FAN) FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
		if (f > 0) {
			float k = variant == GunslingerFx.DEADEYE ? 2.4f : variant == GunslingerFx.FAN ? 0.8f : 1.4f;
			ScreenDynamics.kick(-k * f, 0, 25, 240);
			if (variant == GunslingerFx.DEADEYE) {
				ScreenDynamics.shake(0.3f * f, 160, 24);
				FxClient.flash(GOLD, 0.2f * f);
			}
		}
	}

	/** Mira que se cierra sobre el objetivo marcado: cuatro marcas que convergen y un destello. */
	static void mark(ClientWorld world, FxEventPayload p) {
		if ((Math.round(p.power()) & GunslingerFx.ANIMATE) != 0) {
			PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.GUNSLINGER_DEADEYE);
			if (FxClient.isLocal(p.entityId())) {
				ScreenDynamics.vignette(GOLD, 0.35f, 150, 500, 500, 0.3f);
				ScreenDynamics.fovPunch(-8f, 200, 400, 500);
			}
		}
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.every(6, 1, i -> {
			double r = 1.2 - 0.18 * i;
			for (int k = 0; k < 4; k++) {
				double a = Math.PI / 2 * k + Math.PI / 4;
				FxClient.spawn(ParticleTypes.END_ROD, c.x + Math.cos(a) * r, c.y + Math.sin(a) * r, c.z, 0, 0, 0);
				FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + Math.sin(a) * r, c.z + Math.cos(a) * r, 0, 0, 0);
			}
		});
		FxTimeline.later(6, () -> FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0));
	}
}
