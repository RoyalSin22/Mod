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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V83: coreografía visual del Mago. Magia que se concentra en la mano y sale en un rayo helicoidal, fragmentos de amatista con
 * gravedad real, un salto con estela y estallido, relojes que giran al frenar el tiempo y misiles que describen una curva. Todo sale de
 * la semilla del evento (todos ven lo mismo) y cae en el fotograma de impacto de la animación del cuerpo.
 */
final class MageClientFx {
	private static final int VIOLET_LIGHT = 0xD1B3FF;
	private static final int CYAN = 0x80DEEA;
	private static final Vec3d UP = new Vec3d(0, 1, 0);

	private MageClientFx() {
	}

	private static ParticleEffect magic(float size) {
		return ParticleFx.transition(VIOLET_LIGHT, CYAN, size);
	}

	private static ParticleEffect shard() {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.AMETHYST_SHARD));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	private static Vec3d eyeOf(Entity e, FxEventPayload p) {
		return e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
	}

	/** Dos vectores perpendiculares a {@code axis}, para dibujar una hélice alrededor de una línea. */
	private static Vec3d[] basis(Vec3d axis) {
		Vec3d u = axis.crossProduct(UP);
		if (u.lengthSquared() < 1e-6) u = new Vec3d(1, 0, 0);
		u = u.normalize();
		return new Vec3d[] {u, axis.crossProduct(u).normalize()};
	}

	// ------------------------------------------------------------------ Proyectil arcano

	static void bolt(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Entity e = FxClient.entity(world, p.entityId());
		PlayerAnims.play(e, PlayerAnimIds.MAGE_BOLT);
		int charge = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MAGE_BOLT);
		// La magia se concentra en la mano derecha durante la preparación.
		FxTimeline.every(charge, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			Vec3d hand = now != null && now.isAlive() ? FxClient.handPos(now, true) : eyeOf(null, p);
			int k = FxClient.n(4);
			for (int j = 0; j < k; j++) {
				Vec3d off = randomDirection(rng).multiply(0.95 - 0.18 * i);
				FxClient.spawn(ParticleTypes.ENCHANT, hand.x + off.x, hand.y + off.y, hand.z + off.z, -off.x * 0.28, -off.y * 0.28, -off.z * 0.28);
			}
			FxClient.spawn(ParticleTypes.END_ROD, hand.x, hand.y, hand.z, 0, 0.01, 0);
		});
		FxTimeline.later(charge, () -> boltRelease(world, p));
	}

	private static void boltRelease(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed() ^ 0x9E3779B9L);
		Entity e = FxClient.entity(world, p.entityId());
		Vec3d eye = eyeOf(e, p);
		Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
		Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
		double len = start.distanceTo(end);
		if (len < 0.5) return;
		Vec3d axis = end.subtract(start).normalize();
		Vec3d[] uv = basis(axis);
		int travel = MathHelper.clamp((int) Math.ceil(len / 8.0), 1, 4);
		ParticleEffect trail = magic(1.1f);
		// El rayo avanza por tramos, dibujando una hélice alrededor de su eje.
		FxTimeline.every(travel, 1, i -> {
			double d0 = len * i / travel;
			double d1 = len * (i + 1) / travel;
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			for (double d = d0; d < d1; d += step) {
				double phase = d * 2.4;
				Vec3d c = start.add(axis.multiply(d)).add(uv[0].multiply(Math.cos(phase) * 0.16)).add(uv[1].multiply(Math.sin(phase) * 0.16));
				FxClient.spawn(trail, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.02, (rng.nextDouble() - 0.5) * 0.02, (rng.nextDouble() - 0.5) * 0.02);
				if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y, c.z, axis.x * 0.02, axis.y * 0.02, axis.z * 0.02);
			}
		});
		FxTimeline.later(travel, () -> boltImpact(world, p, end, axis));
		// Cámara: retroceso del lanzamiento (solo quien lanza o está cerca).
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
		if (f > 0) {
			ScreenDynamics.kick(-1.4f * f, 0, 40, 300);
			ScreenDynamics.fovPunch(-4f * f, 40, 20, 260);
			if (FxClient.isLocal(p.entityId())) ScreenDynamics.vignette(p.color(), 0.25f, 20, 40, 260, 0f);
		}
	}

	private static void boltImpact(ClientWorld world, FxEventPayload p, Vec3d end, Vec3d axis) {
		Random rng = new Random(p.seed() * 31L + 5);
		FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
		// Anillo de runas que se abre y esfera de destellos.
		int ring = FxClient.n(28);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			FxClient.spawn(ParticleTypes.ENCHANT, end.x, end.y, end.z, Math.cos(a) * 0.22, 0.02, Math.sin(a) * 0.22);
		}
		int sparks = FxClient.n(36);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.15 + rng.nextDouble() * 0.25);
			FxClient.spawn(ParticleTypes.END_ROD, end.x, end.y, end.z, d.x, d.y, d.z);
		}
		int dust = FxClient.n(24);
		for (int i = 0; i < dust; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(magic(1.3f), end.x, end.y, end.z, d.x, d.y, d.z);
		}
		// Fragmentos de amatista: partículas de objeto, con la gravedad del juego, que salen despedidas y caen.
		int shards = FxClient.n(14);
		for (int i = 0; i < shards; i++) {
			Vec3d d = randomDirection(rng).multiply(0.2 + rng.nextDouble() * 0.3);
			FxClient.spawn(shard(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.12, d.z);
		}
		float f = FxMath.falloff(FxClient.distanceTo(end), 14);
		if (f > 0) {
			ScreenDynamics.shake(0.3f * f, 200, 20);
			FxClient.flash(p.color(), 0.18f * f);
		}
	}

	// ------------------------------------------------------------------ Salto arcano

	static void blink(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d delta = new Vec3d(p.dx(), p.dy(), p.dz());
		Vec3d to = from.add(delta);
		Entity e = FxClient.entity(world, p.entityId());
		PlayerAnims.play(e, PlayerAnimIds.MAGE_BLINK);
		// Implosión en el origen.
		int in = FxClient.n(30);
		for (int i = 0; i < in; i++) {
			Vec3d d = randomDirection(rng);
			double r = 1.2 + rng.nextDouble() * 0.8;
			FxClient.spawn(i % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT, from.x + d.x * r, from.y + 0.9 + d.y * r, from.z + d.z * r,
					-d.x * 0.28, -d.y * 0.28, -d.z * 0.28);
		}
		FxClient.spawn(ParticleTypes.FLASH, from.x, from.y + 0.9, from.z, 0, 0, 0);
		// Estela entre el origen y el destino.
		double len = delta.length();
		int pts = FxClient.n(Math.max(8, (int) (len * 3)));
		for (int i = 0; i < pts; i++) {
			double t = (i + rng.nextDouble()) / pts;
			Vec3d c = from.lerp(to, t);
			FxClient.spawn(magic(1.0f), c.x, c.y + 0.9, c.z, (rng.nextDouble() - 0.5) * 0.04, (rng.nextDouble() - 0.5) * 0.04, (rng.nextDouble() - 0.5) * 0.04);
			if (i % 3 == 0) FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + 0.9, c.z, 0, 0.01, 0);
		}
		// Reaparición: estallido en el destino justo cuando los brazos se abren.
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MAGE_BLINK), () -> {
			int out = FxClient.n(30);
			for (int i = 0; i < out; i++) {
				Vec3d d = randomDirection(rng).multiply(0.3);
				FxClient.spawn(ParticleTypes.REVERSE_PORTAL, to.x, to.y + 0.9, to.z, d.x, d.y, d.z);
			}
			int ring = FxClient.n(24);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(ParticleTypes.END_ROD, to.x, to.y + 0.1, to.z, Math.cos(a) * 0.25, 0.01, Math.sin(a) * 0.25);
				FxClient.spawn(ParticleTypes.ENCHANT, to.x, to.y + 0.1, to.z, Math.cos(a) * 0.15, 0.05, Math.sin(a) * 0.15);
			}
			FxClient.spawn(ParticleTypes.FLASH, to.x, to.y + 0.9, to.z, 0, 0, 0);
		});
		// Cámara: estiramiento del campo de visión («velocidad de salto») y un destello.
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(from), 16);
		if (f > 0) {
			ScreenDynamics.fovPunch(16f * f, 30, 20, 380);
			ScreenDynamics.vignette(p.color(), 0.45f * f, 30, 60, 380, 0f);
			FxClient.flash(p.color(), 0.3f * f);
		}
	}

	// ------------------------------------------------------------------ Distorsión temporal

	static void timeWarp(ClientWorld world, FxEventPayload p) {
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.MAGE_TIME_WARP);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MAGE_TIME_WARP), () -> timeWarpImpact(world, p));
	}

	private static void timeWarpImpact(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d c = FxClient.entityPos(world, p);
		double radius = Math.max(2, p.power());
		ParticleEffect clock = ParticleFx.transition(CYAN, 0x8E44AD, 1.3f);
		// Dos anillos de runas que giran en sentidos contrarios mientras se expanden; los exteriores se quedan flotando, como el tiempo parado.
		int total = 14;
		FxTimeline.every(total, 1, i -> {
			double r = FxMath.ringRadius(i + 1, total, radius);
			for (int ring = 0; ring < 2; ring++) {
				double sign = ring == 0 ? 1 : -1;
				int pts = FxClient.n(Math.max(10, (int) (r * 5)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + sign * i * 0.18;
					double cos = Math.cos(a);
					double sin = Math.sin(a);
					Particle pt = FxClient.spawn(ring == 0 ? ParticleTypes.ENCHANT : clock, c.x + cos * r, c.y + 0.15 + ring * 0.9, c.z + sin * r,
							-sin * sign * 0.03, 0.0, cos * sign * 0.03);
					if (pt != null && ring == 1) pt.setMaxAge(40 + rng.nextInt(30));
				}
			}
		});
		// Manecilla de reloj que barre el círculo.
		FxTimeline.every(20, 1, i -> {
			double a = i * 0.5;
			for (double d = 0.5; d < radius; d += 0.6) {
				Particle pt = FxClient.spawn(ParticleTypes.END_ROD, c.x + Math.cos(a) * d, c.y + 0.2, c.z + Math.sin(a) * d, 0, 0, 0);
				if (pt != null) pt.setMaxAge(7);
			}
		});
		// Motas de luz congeladas en el aire.
		int motes = FxClient.n(50);
		for (int i = 0; i < motes; i++) {
			double a = rng.nextDouble() * Math.PI * 2;
			double r = Math.sqrt(rng.nextDouble()) * radius;
			Particle pt = FxClient.spawn(ParticleTypes.END_ROD, c.x + Math.cos(a) * r, c.y + 0.2 + rng.nextDouble() * 2.2, c.z + Math.sin(a) * r, 0, 0, 0);
			if (pt != null) {
				pt.setMaxAge(60 + rng.nextInt(60));
				pt.setVelocity(0, 0, 0);
			}
		}
		// Cámara: viñeteado turquesa que late despacio y un campo de visión algo más cerrado (calma).
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 2.5);
		if (f > 0) {
			ScreenDynamics.vignette(CYAN, 0.4f * f, 200, 1200, 700, 0.8f);
			ScreenDynamics.fovPunch(-5f * f, 200, 900, 700);
			FxClient.flash(CYAN, 0.22f * f);
		}
	}

	// ------------------------------------------------------------------ Misiles arcanos

	static void missile(ClientWorld world, FxEventPayload p) {
		int index = Math.round(p.power());
		boolean right = index % 2 == 0;
		PlayerAnims.play(FxClient.entity(world, p.entityId()), right ? PlayerAnimIds.MAGE_MISSILE_RIGHT : PlayerAnimIds.MAGE_MISSILE_LEFT);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.MAGE_MISSILE_RIGHT), () -> missileFlight(world, p, right));
	}

	private static void missileFlight(ClientWorld world, FxEventPayload p, boolean right) {
		Random rng = new Random(p.seed());
		Entity e = FxClient.entity(world, p.entityId());
		Vec3d eye = eyeOf(e, p);
		Vec3d start = e != null ? FxClient.handPos(e, right) : eye;
		Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
		double len = start.distanceTo(end);
		if (len < 0.5) return;
		// Curva de Bézier con un punto de control desviado: el misil dibuja un arco antes de dar en el blanco.
		Vec3d axis = end.subtract(start).normalize();
		Vec3d[] uv = basis(axis);
		double bend = len * 0.22 * (rng.nextBoolean() ? 1 : -1);
		double ang = rng.nextDouble() * Math.PI;
		Vec3d control = start.add(end).multiply(0.5).add(uv[0].multiply(Math.cos(ang) * bend)).add(uv[1].multiply(Math.sin(ang) * bend)).add(0, len * 0.06, 0);
		int steps = MathHelper.clamp((int) Math.round(len / 2.0), 3, 10);
		ParticleEffect trail = magic(0.8f);
		FxTimeline.every(steps, 1, i -> {
			double t = (i + 1.0) / steps;
			double s = 1 - t;
			Vec3d b = start.multiply(s * s).add(control.multiply(2 * s * t)).add(end.multiply(t * t));
			FxClient.spawn(ParticleTypes.END_ROD, b.x, b.y, b.z, 0, 0, 0);
			FxClient.spawn(trail, b.x, b.y, b.z, (rng.nextDouble() - 0.5) * 0.03, (rng.nextDouble() - 0.5) * 0.03, (rng.nextDouble() - 0.5) * 0.03);
			if (FxClient.n(1) > 0) FxClient.spawn(ParticleTypes.ENCHANT, b.x, b.y, b.z, 0, 0.02, 0);
		});
		FxTimeline.later(steps, () -> {
			int sparks = FxClient.n(10);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.18);
				FxClient.spawn(ParticleTypes.END_ROD, end.x, end.y, end.z, d.x, d.y, d.z);
			}
			int shards = FxClient.n(3);
			for (int i = 0; i < shards; i++) {
				Vec3d d = randomDirection(rng).multiply(0.15 + rng.nextDouble() * 0.2);
				FxClient.spawn(shard(), end.x, end.y, end.z, d.x, Math.abs(d.y) + 0.08, d.z);
			}
			FxClient.spawn(ParticleTypes.ENCHANT, end.x, end.y, end.z, 0, 0.05, 0);
		});
		if (FxClient.isLocal(p.entityId())) ScreenDynamics.kick(-0.45f, right ? 0.2f : -0.2f, 30, 180);
	}
}
