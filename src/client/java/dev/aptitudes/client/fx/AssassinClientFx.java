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
 * V84: coreografía visual del Asesino. Humo negro que estalla y se queda flotando, un paso que deja estela de sombra y remata con un
 * tajo, cuchillas y shuriken que giran por el aire con su estela y se clavan soltando esquirlas de hierro con gravedad real. Todo sale
 * de la semilla del evento (todos ven lo mismo) y cae en el fotograma de impacto de la animación del cuerpo.
 */
final class AssassinClientFx {
	private static final int SHADOW = 0x2C3E50;
	private static final int INK = 0x0B0F14;
	private static final Vec3d UP = new Vec3d(0, 1, 0);

	private AssassinClientFx() {
	}

	private static ParticleEffect shadowDust(float size) {
		return ParticleFx.transition(SHADOW, INK, size);
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

	private static Vec3d[] basis(Vec3d axis) {
		Vec3d u = axis.crossProduct(UP);
		if (u.lengthSquared() < 1e-6) u = new Vec3d(1, 0, 0);
		u = u.normalize();
		return new Vec3d[] {u, axis.crossProduct(u).normalize()};
	}

	private static Vec3d eyeOf(Entity e, FxEventPayload p) {
		return e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
	}

	// ------------------------------------------------------------------ Desaparecer

	static void vanish(ClientWorld world, FxEventPayload p) {
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ASSASSIN_VANISH);
		// Preparación: el humo se recoge alrededor del cuerpo mientras se cruzan los brazos.
		int charge = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_VANISH);
		Random rng = new Random(p.seed());
		FxTimeline.every(charge, 1, i -> {
			Vec3d c = FxClient.entityPos(world, p);
			int k = FxClient.n(5);
			for (int j = 0; j < k; j++) {
				Vec3d d = randomDirection(rng).multiply(1.1 - 0.25 * i);
				FxClient.spawn(ParticleTypes.SMOKE, c.x + d.x, c.y + 1.0 + d.y * 0.7, c.z + d.z, -d.x * 0.12, -d.y * 0.06, -d.z * 0.12);
			}
		});
		FxTimeline.later(charge, () -> vanishBurst(world, p));
	}

	private static void vanishBurst(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed() * 31L + 7);
		Vec3d c = FxClient.entityPos(world, p);
		// Esfera de humo grueso que sale despedida y un anillo bajo que se abre por el suelo.
		int big = FxClient.n(34);
		for (int i = 0; i < big; i++) {
			Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.22);
			Particle pt = FxClient.spawn(ParticleTypes.LARGE_SMOKE, c.x, c.y + 1.0, c.z, d.x, d.y * 0.6 + 0.02, d.z);
			if (pt != null) pt.setMaxAge(30 + rng.nextInt(24));
		}
		int ink = FxClient.n(20);
		for (int i = 0; i < ink; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(ParticleTypes.SQUID_INK, c.x, c.y + 1.0, c.z, d.x, d.y * 0.5, d.z);
		}
		int dust = FxClient.n(26);
		for (int i = 0; i < dust; i++) {
			Vec3d d = randomDirection(rng).multiply(0.08 + rng.nextDouble() * 0.2);
			FxClient.spawn(shadowDust(1.4f), c.x, c.y + 1.0, c.z, d.x, d.y * 0.5, d.z);
		}
		int ring = FxClient.n(28);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y + 0.1, c.z, Math.cos(a) * 0.3, 0.015, Math.sin(a) * 0.3);
		}
		// Después del estallido, jirones que suben despacio mientras dura la invisibilidad.
		FxTimeline.every(10, 2, i -> {
			Vec3d now = FxClient.entityPos(world, p);
			int k = FxClient.n(3);
			for (int j = 0; j < k; j++) {
				Particle pt = FxClient.spawn(ParticleTypes.CAMPFIRE_COSY_SMOKE, now.x + (rng.nextDouble() - 0.5) * 0.8, now.y + 0.2,
						now.z + (rng.nextDouble() - 0.5) * 0.8, 0, 0.02, 0);
				if (pt != null) pt.setMaxAge(36 + rng.nextInt(20));
			}
		});
		// Cámara: la visión se cierra en túnel oscuro («te ocultas») y vuelve poco a poco.
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 12);
		if (f > 0) {
			ScreenDynamics.vignette(INK, 0.55f * f, 60, 500, 900, 0.5f);
			ScreenDynamics.fovPunch(-7f * f, 80, 300, 800);
			ScreenDynamics.kick(1.2f * f, 0, 60, 300);
		}
	}

	// ------------------------------------------------------------------ Paso de sombra

	static void step(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d delta = new Vec3d(p.dx(), p.dy(), p.dz());
		Vec3d to = from.add(delta);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ASSASSIN_STEP);
		// Origen: nube que se queda donde estabas, como una silueta que se deshace.
		int cloud = FxClient.n(26);
		for (int i = 0; i < cloud; i++) {
			Vec3d d = randomDirection(rng).multiply(0.06 + rng.nextDouble() * 0.12);
			Particle pt = FxClient.spawn(i % 2 == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SQUID_INK, from.x, from.y + 0.9, from.z, d.x, d.y * 0.5, d.z);
			if (pt != null) pt.setMaxAge(24 + rng.nextInt(16));
		}
		// Estela de posimágenes: figuras de humo oscuro (cabeza, torso y piernas) a intervalos del recorrido.
		double len = delta.length();
		int images = MathHelper.clamp((int) (len / 1.4), 2, 9);
		for (int k = 0; k < images; k++) {
			double t = (k + 0.5) / images;
			Vec3d c = from.lerp(to, t);
			for (double h = 0.1; h <= 1.75; h += 0.35) {
				Particle pt = FxClient.spawn(shadowDust(1.5f), c.x, c.y + h, c.z, 0, 0.004, 0);
				if (pt != null) pt.setMaxAge(14 + rng.nextInt(8) + k);
			}
		}
		int pts = FxClient.n(Math.max(10, (int) (len * 4)));
		for (int i = 0; i < pts; i++) {
			Vec3d c = from.lerp(to, (i + rng.nextDouble()) / pts);
			FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y + 0.4 + rng.nextDouble() * 1.2, c.z, 0, 0.01, 0);
		}
		// Estocada al reaparecer (tick 3): arco de tajo y chispas en la dirección del avance.
		Vec3d dir = len > 1e-3 ? delta.multiply(1 / len) : UP;
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_STEP), () -> {
			Vec3d spot = to.add(0, 1.1, 0).add(dir.multiply(0.8));
			FxClient.spawn(ParticleTypes.SWEEP_ATTACK, spot.x, spot.y, spot.z, 0, 0, 0);
			FxClient.spawn(ParticleTypes.FLASH, spot.x, spot.y, spot.z, 0, 0, 0);
			int sparks = FxClient.n(22);
			for (int i = 0; i < sparks; i++) {
				Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.18).add(dir.multiply(0.25));
				FxClient.spawn(ParticleTypes.CRIT, spot.x, spot.y, spot.z, d.x, d.y, d.z);
			}
			int smoke = FxClient.n(14);
			for (int i = 0; i < smoke; i++) {
				Vec3d d = randomDirection(rng).multiply(0.1);
				FxClient.spawn(ParticleTypes.LARGE_SMOKE, to.x, to.y + 0.9, to.z, d.x, d.y, d.z);
			}
		});
		// Cámara: estirón de campo de visión por la velocidad y un golpe seco al llegar.
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(from), 16);
		if (f > 0) {
			ScreenDynamics.fovPunch(15f * f, 30, 20, 320);
			ScreenDynamics.vignette(INK, 0.4f * f, 30, 60, 320, 0f);
			FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_STEP), () -> {
				ScreenDynamics.kick(-2.0f * f, 0, 30, 260);
				ScreenDynamics.shake(0.25f * f, 160, 20);
			});
		}
	}

	// ------------------------------------------------------------------ Abanico de cuchillas

	static void fan(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ASSASSIN_FAN);
		int charge = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_FAN);
		double radius = Math.max(2, p.power());
		// Preparación: destellos de hierro que se reúnen en el pecho mientras los brazos se cruzan.
		FxTimeline.every(charge, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			Vec3d c = now != null ? now.getEyePos().add(0, -0.45, 0) : FxClient.entityPos(world, p).add(0, 1.2, 0);
			int k = FxClient.n(4);
			for (int j = 0; j < k; j++) {
				Vec3d off = randomDirection(rng).multiply(1.0 - 0.2 * i);
				FxClient.spawn(ParticleTypes.CRIT, c.x + off.x, c.y + off.y * 0.6, c.z + off.z, -off.x * 0.2, -off.y * 0.1, -off.z * 0.2);
			}
		});
		FxTimeline.later(charge, () -> fanBurst(world, p, radius));
	}

	private static void fanBurst(ClientWorld world, FxEventPayload p, double radius) {
		Random rng = new Random(p.seed() * 31L + 3);
		Vec3d c = FxClient.entityPos(world, p).add(0, 1.2, 0);
		// Anillo de tajos que se abre hasta el radio del abanico: ocho cuchillas, cada una con su tajo y su cola de humo.
		int blades = 8;
		for (int b = 0; b < blades; b++) {
			double a = Math.PI * 2 * b / blades + rng.nextDouble() * 0.2;
			Vec3d dir = new Vec3d(Math.cos(a), 0, Math.sin(a));
			FxTimeline.every(4, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 4, radius);
				Vec3d pos = c.add(dir.multiply(r));
				FxClient.spawn(ParticleTypes.CRIT, pos.x, pos.y, pos.z, dir.x * 0.05, 0.0, dir.z * 0.05);
				FxClient.spawn(shadowDust(1.0f), pos.x, pos.y, pos.z, 0, 0, 0);
				if (i % 2 == 0) FxClient.spawn(shard(), pos.x, pos.y, pos.z, dir.x * 0.08, 0.04, dir.z * 0.08);
			});
		}
		int ring = FxClient.n(30);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y - 0.5, c.z, Math.cos(a) * 0.28, 0.01, Math.sin(a) * 0.28);
		}
		FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 14);
		if (f > 0) {
			ScreenDynamics.fovPunch(6f * f, 40, 30, 300);
			ScreenDynamics.shake(0.28f * f, 180, 22);
			ScreenDynamics.vignette(SHADOW, 0.3f * f, 30, 60, 300, 0f);
		}
	}

	/** Una cuchilla del abanico: vuela girando hasta el objetivo y se clava. */
	static void knife(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_FAN);
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = eyeOf(e, p);
			Vec3d start = e != null ? FxClient.handPos(e, p.power() % 2 == 0) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			flight(world, start, end, rng, true, () -> stab(world, end, rng));
		});
	}

	// ------------------------------------------------------------------ Shuriken

	static void shuriken(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ASSASSIN_SHURIKEN);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ASSASSIN_SHURIKEN);
		// Preparación: un brillo de acero en la mano mientras se arma el latigazo.
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d hand = FxClient.handPos(now, true);
			FxClient.spawn(ParticleTypes.CRIT, hand.x, hand.y, hand.z, 0, 0.02, 0);
		});
		boolean hit = p.power() >= 0.5f;
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = eyeOf(e, p);
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			flight(world, start, end, rng, false, () -> {
				if (hit) stab(world, end, rng);
			});
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
			if (f > 0) {
				ScreenDynamics.kick(-1.0f * f, 0, 30, 220);
				ScreenDynamics.fovPunch(-3f * f, 30, 20, 220);
			}
		});
	}

	// ------------------------------------------------------------------ Piezas comunes

	/**
	 * Vuelo de un proyectil: avanza por tramos por la línea, deja estela de sombra y, si es un shuriken, una estrella de cuatro puntas que
	 * gira alrededor del eje; si es cuchilla, esquirlas de hierro con gravedad que se van soltando.
	 */
	private static void flight(ClientWorld world, Vec3d start, Vec3d end, Random rng, boolean knife, Runnable onArrive) {
		double len = start.distanceTo(end);
		if (len < 0.5) {
			onArrive.run();
			return;
		}
		Vec3d axis = end.subtract(start).normalize();
		Vec3d[] uv = basis(axis);
		int travel = MathHelper.clamp((int) Math.ceil(len / 5.0), 1, 5);
		FxTimeline.every(travel, 1, i -> {
			double d0 = len * i / travel;
			double d1 = len * (i + 1) / travel;
			double step = 0.45 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			for (double d = d0; d < d1; d += step) {
				Vec3d c = start.add(axis.multiply(d));
				FxClient.spawn(shadowDust(0.9f), c.x, c.y, c.z, 0, 0, 0);
				if (rng.nextDouble() < 0.4) FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y, c.z, 0, 0.005, 0);
				if (knife && rng.nextDouble() < 0.35) FxClient.spawn(shard(), c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.04, 0.0, (rng.nextDouble() - 0.5) * 0.04);
			}
			// La estrella (o el filo) en la cabeza del vuelo, girando.
			Vec3d head = start.add(axis.multiply(d1));
			double spin = i * 1.2 + rng.nextDouble();
			int arms = knife ? 2 : 4;
			for (int a = 0; a < arms; a++) {
				double ang = spin + Math.PI * 2 * a / arms;
				Vec3d off = uv[0].multiply(Math.cos(ang) * 0.22).add(uv[1].multiply(Math.sin(ang) * 0.22));
				FxClient.spawn(ParticleTypes.CRIT, head.x + off.x, head.y + off.y, head.z + off.z, axis.x * 0.02, axis.y * 0.02, axis.z * 0.02);
			}
		});
		FxTimeline.later(travel, onArrive);
	}

	/** Clavada: tajo, chispas, esquirlas de hierro que caen y un temblor corto si queda cerca. */
	private static void stab(ClientWorld world, Vec3d at, Random rng) {
		FxClient.spawn(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 0, 0, 0);
		int sparks = FxClient.n(18);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(ParticleTypes.CRIT, at.x, at.y, at.z, d.x, d.y, d.z);
		}
		int shards = FxClient.n(8);
		for (int i = 0; i < shards; i++) {
			Vec3d d = randomDirection(rng).multiply(0.12 + rng.nextDouble() * 0.18);
			FxClient.spawn(shard(), at.x, at.y, at.z, d.x, Math.abs(d.y) + 0.08, d.z);
		}
		int smoke = FxClient.n(6);
		for (int i = 0; i < smoke; i++) {
			Vec3d d = randomDirection(rng).multiply(0.05);
			FxClient.spawn(ParticleTypes.SMOKE, at.x, at.y, at.z, d.x, d.y, d.z);
		}
		float f = FxMath.falloff(FxClient.distanceTo(at), 10);
		if (f > 0) ScreenDynamics.shake(0.15f * f, 120, 22);
	}
}
