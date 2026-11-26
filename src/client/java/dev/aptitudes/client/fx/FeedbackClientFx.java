package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.logic.ImpactFeedbackRules.Kind;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V95: lenguaje común de feedback de combate, el mismo para los 20 roles: un golpe de habilidad deja unas chispas con el color del rol,
 * el golpe pesado una ráfaga y un anillo, el crítico un destello con estrella, el bloqueo chispas metálicas y la curación efectiva un
 * ascenso de luz verde. Se escala con {@code fxIntensity} y se apaga con la opción de cliente {@code impactFeedback}.
 */
final class FeedbackClientFx {
	private static final int GREEN = 0x7CFFB2;
	private static final int STEEL = 0xCFD8DC;

	private FeedbackClientFx() {
	}

	private static boolean enabled() {
		Boolean on = ClientConfig.get().impactFeedback;
		return on == null || on;
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	static void impact(ClientWorld world, FxEventPayload p) {
		if (!enabled()) return;
		Random rng = new Random(p.seed());
		Kind kind = Kind.byIndex(Math.round(p.power()));
		float weight = Math.max(0.2f, Math.min(1f, p.dx()));
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		ParticleEffect tint = ParticleFx.transition(p.color(), 0xFFFFFF, 0.9f + 0.6f * weight);
		int sparks = FxClient.n(switch (kind) {
			case NORMAL -> 4;
			case HEAVY -> 10;
			case CRITICAL -> 14;
		});
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.08 + rng.nextDouble() * (kind == Kind.NORMAL ? 0.1 : 0.2));
			FxClient.spawn(tint, c.x, c.y, c.z, d.x, d.y, d.z);
		}
		if (kind == Kind.HEAVY) {
			// Anillo de choque corto y un golpe de polvo.
			int ring = FxClient.n(12);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, Math.cos(a) * 0.22, 0.02, Math.sin(a) * 0.22);
			}
			FxClient.spawn(ParticleTypes.POOF, c.x, c.y, c.z, 0, 0.02, 0);
		} else if (kind == Kind.CRITICAL) {
			// Destello y estrella de cuatro puntas.
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0);
			for (int k = 0; k < 4; k++) {
				double a = Math.PI / 2 * k + Math.PI / 4;
				for (int s = 1; s <= 3; s++) {
					FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y, c.z, Math.cos(a) * 0.06 * s, 0.0, Math.sin(a) * 0.06 * s);
				}
			}
		}
	}

	static void block(ClientWorld world, FxEventPayload p) {
		if (!enabled()) return;
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		// Las chispas salen hacia delante del que bloquea (el evento lleva su dirección de mirada).
		Vec3d f = new Vec3d(p.dx(), p.dy(), p.dz());
		Vec3d front = f.lengthSquared() > 1e-4 ? f.normalize() : new Vec3d(0, 0, 1);
		Vec3d at = c.add(front.multiply(0.6));
		FxClient.spawn(ParticleTypes.CRIT, at.x, at.y, at.z, 0, 0, 0);
		int sparks = FxClient.n(10);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.12).add(front.multiply(0.1));
			FxClient.spawn(i % 2 == 0 ? ParticleTypes.ELECTRIC_SPARK : ParticleFx.transition(STEEL, 0xFFFFFF, 0.8f), at.x, at.y, at.z, d.x, d.y, d.z);
		}
		if (FxClient.isLocal(p.entityId())) ScreenDynamics.shake(0.1f, 80, 24);
	}

	static void heal(ClientWorld world, FxEventPayload p) {
		if (!enabled()) return;
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		float amount = Math.max(1f, p.power());
		int pts = FxClient.n(4 + Math.min(10, (int) amount));
		ParticleEffect glow = ParticleFx.transition(GREEN, 0xFFFFFF, 1.0f);
		for (int i = 0; i < pts; i++) {
			double a = Math.PI * 2 * i / pts + rng.nextDouble() * 0.3;
			Particle pt = FxClient.spawn(glow, c.x + Math.cos(a) * 0.45, c.y - 0.4, c.z + Math.sin(a) * 0.45, 0, 0.07, 0);
			if (pt != null) pt.setMaxAge(14 + rng.nextInt(6));
		}
		FxClient.spawn(ParticleTypes.HEART, c.x, c.y + 0.6, c.z, 0, 0.04, 0);
	}
}
