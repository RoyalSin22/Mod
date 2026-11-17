package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V77: coreografía visual del Bersérker. Brasas rojas que se apagan, escombros con física real (gravedad, rebote y giro),
 * grietas que se abren desde el impacto y cámara que reacciona según la distancia. Todo sale de la semilla del evento, así
 * que todos los jugadores ven el mismo desorden.
 */
final class BerserkerClientFx {
	private static final int EMBER_HOT = 0xFF6A1F;
	private static final int EMBER_COLD = 0x2A0A05;

	private BerserkerClientFx() {
	}

	private static ParticleEffect ember(float size) {
		return ParticleFx.transition(EMBER_HOT, EMBER_COLD, size);
	}

	private static double yawDegrees(FxEventPayload p) {
		return Math.toDegrees(Math.atan2(-p.dx(), p.dz()));
	}

	// ------------------------------------------------------------------ Furia

	static void rage(ClientWorld world, FxEventPayload p) {
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BERSERKER_RAGE), () -> rageImpact(world, p));
	}

	private static void rageImpact(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d at = FxClient.entityPos(world, p);
		int durationTicks = Math.round(p.power() * 20);
		ParticleEffect hot = ember(1.3f);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BERSERKER_RAGE);

		// Anillo de brasas que sale de los pies.
		int ring = FxClient.n(40);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring + rng.nextDouble() * 0.15;
			double speed = 0.09 + rng.nextDouble() * 0.13;
			FxClient.spawn(i % 4 == 0 ? ParticleTypes.FLAME : hot, at.x + Math.cos(a) * 0.4, at.y + 0.1, at.z + Math.sin(a) * 0.4,
					Math.cos(a) * speed, 0.02 + rng.nextDouble() * 0.07, Math.sin(a) * speed);
		}
		// El suelo se resquebraja bajo el rugido.
		BlockState ground = FxClient.groundState(world, at.x, at.y, at.z);
		int chunks = FxClient.n(16);
		for (int i = 0; i < chunks; i++) {
			double[] d = FxMath.coneDirection(rng, rng.nextDouble() * 360, 180, 35, 75);
			double speed = 0.22 + rng.nextDouble() * 0.3;
			FxClient.chunk(world, ground, at.x + d[0] * 0.5, at.y + 0.15, at.z + d[2] * 0.5, d[0] * speed, d[1] * speed, d[2] * speed,
					0.11f + rng.nextFloat() * 0.1f, 30 + rng.nextInt(25), 1.0f, 0.35f);
		}
		// Columna de brasas en espiral que sube por el cuerpo.
		FxTimeline.every(10, 1, i -> {
			Vec3d c = FxClient.entityPos(world, p);
			int k = FxClient.n(5);
			for (int j = 0; j < k; j++) {
				double a = i * 0.9 + j * Math.PI * 2 / k;
				double r = 0.9 - i * 0.03;
				FxClient.spawn(hot, c.x + Math.cos(a) * r, c.y + 0.1 + i * 0.12, c.z + Math.sin(a) * r,
						-Math.cos(a) * 0.01, 0.12 + rng.nextDouble() * 0.18, -Math.sin(a) * 0.01);
			}
		});
		// Aura de brasas mientras dura la Furia.
		int pulses = Math.min(durationTicks / 3, 200);
		FxTimeline.every(pulses, 3, i -> {
			Entity e = FxClient.entity(world, p.entityId());
			if (e == null || !e.isAlive()) return;
			Vec3d c = e.getPos();
			int k = FxClient.n(2);
			for (int j = 0; j < k; j++) {
				FxClient.spawn(hot, c.x + (rng.nextDouble() - 0.5) * 0.9, c.y + rng.nextDouble() * 1.7, c.z + (rng.nextDouble() - 0.5) * 0.9,
						0, 0.03 + rng.nextDouble() * 0.05, 0);
			}
			if (i % 4 == 0) FxClient.spawn(ParticleTypes.FLAME, c.x + (rng.nextDouble() - 0.5) * 0.6, c.y + 0.2, c.z + (rng.nextDouble() - 0.5) * 0.6,
					0, 0.05, 0);
		});

		// Cámara: el jugador siente el rugido; los cercanos, un temblor suave.
		boolean local = FxClient.isLocal(p.entityId());
		float f = local ? 1f : FxMath.falloff(FxClient.distanceTo(at), 18);
		if (f > 0) {
			ScreenDynamics.fovPunch(7f * f, 70, 40, 650);
			ScreenDynamics.kick(-2.4f * f, 0, 60, 520);
			ScreenDynamics.shake(0.9f * f, 700, 22);
		}
		if (local) ScreenDynamics.vignette(p.color(), 0.5f, 140, Math.max(0, durationTicks * 50L - 900), 760, 1.25f);
	}

	// ------------------------------------------------------------------ Grito de guerra

	static void warCry(ClientWorld world, FxEventPayload p) {
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BERSERKER_WAR_CRY), () -> warCryImpact(world, p));
	}

	private static void warCryImpact(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d at = FxClient.entityPos(world, p);
		double radius = Math.max(2, p.power());
		double yaw = yawDegrees(p);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BERSERKER_WAR_CRY);

		FxClient.spawn(ParticleTypes.SONIC_BOOM, at.x, at.y + 1.2, at.z, 0, 0, 0);
		// Onda de choque que se expande y levanta escombros del suelo.
		int total = 11;
		FxTimeline.every(total, 1, i -> {
			double r = FxMath.ringRadius(i + 1, total, radius * 1.15);
			int points = FxClient.n(Math.max(10, (int) (r * 6)));
			for (int j = 0; j < points; j++) {
				double a = Math.PI * 2 * j / points + i * 0.3;
				double cos = Math.cos(a);
				double sin = Math.sin(a);
				double px = at.x + cos * r;
				double pz = at.z + sin * r;
				FxClient.spawn(ParticleTypes.POOF, px, at.y + 0.1, pz, cos * 0.12, 0.02, sin * 0.12);
				if (j % 3 == 0) {
					FxClient.chunk(world, FxClient.groundState(world, px, at.y, pz), px, at.y + 0.1, pz,
							cos * 0.1 + (rng.nextDouble() - 0.5) * 0.05, 0.2 + rng.nextDouble() * 0.2, sin * 0.1 + (rng.nextDouble() - 0.5) * 0.05,
							0.12f + rng.nextFloat() * 0.12f, 25 + rng.nextInt(20), 1.0f, 0.3f);
				}
			}
		});
		// Chispas de la voz, en un cono frontal.
		int sparks = FxClient.n(30);
		for (int i = 0; i < sparks; i++) {
			double[] d = FxMath.coneDirection(rng, yaw, 28, 3, 22);
			double speed = 0.5 + rng.nextDouble() * 0.5;
			FxClient.spawn(ParticleTypes.CRIT, at.x - Math.sin(Math.toRadians(yaw)) * 0.8, at.y + 1.2, at.z + Math.cos(Math.toRadians(yaw)) * 0.8,
					d[0] * speed, d[1] * speed, d[2] * speed);
		}

		float f = FxMath.falloff(FxClient.distanceTo(at), radius * 3 + 12);
		if (f > 0) {
			ScreenDynamics.fovPunch(8f * f, 45, 20, 480);
			ScreenDynamics.kick(-3.2f * f, 0, 50, 450);
			ScreenDynamics.shake(1.15f * f, 560, 24);
			FxClient.flash(0xFFFFFF, 0.28f * f);
		}
	}

	// ------------------------------------------------------------------ Salto demoledor

	static void leapTakeoff(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d at = FxClient.entityPos(world, p);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BERSERKER_LEAP);
		BlockState ground = FxClient.groundState(world, at.x, at.y, at.z);
		int ring = FxClient.n(26);
		for (int i = 0; i < ring; i++) {
			double a = Math.PI * 2 * i / ring;
			double speed = 0.10 + rng.nextDouble() * 0.08;
			FxClient.spawn(ParticleTypes.POOF, at.x + Math.cos(a) * 0.3, at.y + 0.1, at.z + Math.sin(a) * 0.3,
					Math.cos(a) * speed, 0.03, Math.sin(a) * speed);
		}
		double backYaw = yawDegrees(p) + 180;
		int chunks = FxClient.n(12);
		for (int i = 0; i < chunks; i++) {
			double[] d = FxMath.coneDirection(rng, backYaw, 35, 15, 55);
			double speed = 0.28 + rng.nextDouble() * 0.22;
			FxClient.chunk(world, ground, at.x, at.y + 0.1, at.z, d[0] * speed, d[1] * speed, d[2] * speed,
					0.1f + rng.nextFloat() * 0.1f, 28 + rng.nextInt(20), 1.0f, 0.3f);
		}
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(at), 20);
		if (f > 0) {
			ScreenDynamics.fovPunch(6f * f, 110, 0, 520);
			ScreenDynamics.kick(-1.2f * f, 0, 60, 380);
		}
	}

	static void leapTrail(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed() ^ 0x5DEECE6);
		int ticks = Math.max(1, Math.round(p.power()));
		ParticleEffect hot = ember(1.0f);
		FxTimeline.every(ticks, 1, i -> {
			Entity e = FxClient.entity(world, p.entityId());
			if (e == null || !e.isAlive()) return;
			Vec3d c = e.getPos().add(0, e.getHeight() * 0.4, 0);
			Vec3d v = e.getVelocity();
			int k = FxClient.n(3);
			for (int j = 0; j < k; j++) {
				FxClient.spawn(hot, c.x + (rng.nextDouble() - 0.5) * 0.5, c.y + (rng.nextDouble() - 0.5) * 0.6, c.z + (rng.nextDouble() - 0.5) * 0.5,
						-v.x * 0.3 + (rng.nextDouble() - 0.5) * 0.05, -v.y * 0.1, -v.z * 0.3 + (rng.nextDouble() - 0.5) * 0.05);
			}
			FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, -v.x * 0.5, -v.y * 0.2, -v.z * 0.5);
		});
		if (FxClient.isLocal(p.entityId())) {
			long ms = ticks * 50L;
			ScreenDynamics.fovPunch(5f, 200, Math.max(0, ms - 400), 350);
			ScreenDynamics.vignette(0xFFFFFF, 0.10f, 90, Math.max(0, ms - 250), 250, 0f);
		}
	}

	static void slam(ClientWorld world, FxEventPayload p) {
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.BERSERKER_SLAM), () -> slamImpact(world, p));
	}

	private static void slamImpact(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d at = new Vec3d(p.x(), p.y(), p.z());
		double radius = Math.max(2, p.power());
		PlayerAnims.playWithFade(FxClient.entity(world, p.entityId()), PlayerAnimIds.BERSERKER_SLAM);
		BlockState ground = FxClient.groundState(world, at.x, at.y, at.z);

		FxClient.spawn(ParticleTypes.EXPLOSION, at.x, at.y + 0.2, at.z, 0, 0, 0);
		// Cúpula de polvo.
		int dust = FxClient.n(50);
		for (int i = 0; i < dust; i++) {
			double[] d = FxMath.coneDirection(rng, rng.nextDouble() * 360, 180, 5, 40);
			double speed = 0.25 + rng.nextDouble() * 0.35;
			FxClient.spawn(ParticleTypes.POOF, at.x, at.y + 0.2, at.z, d[0] * speed, d[1] * speed, d[2] * speed);
		}
		// Escombros que salen despedidos y rebotan.
		int chunks = FxClient.n(70);
		double power = 0.8 + radius / 8;
		for (int i = 0; i < chunks; i++) {
			double[] d = FxMath.coneDirection(rng, rng.nextDouble() * 360, 180, 30, 85);
			double speed = (0.3 + rng.nextDouble() * 0.55) * power;
			FxClient.chunk(world, ground, at.x + d[0] * 0.4, at.y + 0.2, at.z + d[2] * 0.4, d[0] * speed, d[1] * speed, d[2] * speed,
					0.12f + rng.nextFloat() * 0.16f, 40 + rng.nextInt(40), 1.0f, 0.35f);
		}
		// Grietas que se abren en abanico desde el impacto, con brasas en el fondo.
		int spokes = 9;
		double base = rng.nextDouble() * Math.PI * 2;
		int steps = 9;
		FxTimeline.every(steps, 1, i -> {
			double r = radius * 1.3 * (i + 1) / steps;
			for (int s = 0; s < spokes; s++) {
				double a = base + Math.PI * 2 * s / spokes + (rng.nextDouble() - 0.5) * 0.12;
				double px = at.x + Math.cos(a) * r;
				double pz = at.z + Math.sin(a) * r;
				FxClient.chunk(world, FxClient.groundState(world, px, at.y, pz), px, at.y + 0.1, pz, (rng.nextDouble() - 0.5) * 0.08,
						0.15 + rng.nextDouble() * 0.15, (rng.nextDouble() - 0.5) * 0.08, 0.1f, 22 + rng.nextInt(14), 1.0f, 0.25f);
				FxClient.spawn(i % 2 == 0 ? ParticleTypes.LAVA : ParticleTypes.SMALL_FLAME, px, at.y + 0.1, pz, 0, 0.05, 0);
			}
		});
		// Onda de polvo que se aleja.
		int waveSteps = 8;
		FxTimeline.every(waveSteps, 1, i -> {
			double r = FxMath.ringRadius(i + 1, waveSteps, radius * 1.5);
			int points = FxClient.n(Math.max(10, (int) (r * 7)));
			for (int j = 0; j < points; j++) {
				double a = Math.PI * 2 * j / points;
				FxClient.spawn(ParticleTypes.POOF, at.x + Math.cos(a) * r, at.y + 0.1, at.z + Math.sin(a) * r,
						Math.cos(a) * 0.15, 0.02, Math.sin(a) * 0.15);
			}
		});
		// Brasas y salpicaduras de lava.
		int flames = FxClient.n(18);
		for (int i = 0; i < flames; i++) {
			FxClient.spawn(ParticleTypes.FLAME, at.x + (rng.nextDouble() - 0.5) * radius, at.y + 0.2, at.z + (rng.nextDouble() - 0.5) * radius,
					(rng.nextDouble() - 0.5) * 0.1, 0.25 + rng.nextDouble() * 0.25, (rng.nextDouble() - 0.5) * 0.1);
		}
		int lava = FxClient.n(12);
		for (int i = 0; i < lava; i++) {
			FxClient.spawn(ParticleTypes.LAVA, at.x + (rng.nextDouble() - 0.5) * radius * 0.6, at.y + 0.2, at.z + (rng.nextDouble() - 0.5) * radius * 0.6,
					0, 0, 0);
		}

		float f = FxMath.falloff(FxClient.distanceTo(at), radius * 4 + 14);
		if (f > 0) {
			ScreenDynamics.kick(4.2f * f, 0, 40, 520);
			ScreenDynamics.fovPunch(-6f * f, 40, 30, 450);
			ScreenDynamics.shake(1.6f * f, 800, 16);
			FxClient.flash(0xFFFFFF, 0.3f * f);
			ScreenDynamics.vignette(0x401008, 0.35f * f, 40, 60, 500, 0f);
		}
	}

	// ------------------------------------------------------------------ Torbellino

	static void whirl(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed() * 31L + 7);
		double radius = Math.max(2, p.power());
		double start = Math.toRadians(yawDegrees(p)) + p.seed() * 0.7;
		int steps = 10;
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.BERSERKER_WHIRL);
		FxTimeline.every(steps, 1, i -> {
			Vec3d c = FxClient.entityPos(world, p);
			double head = start + Math.PI * 2 * (i + 1) / steps;
			double r = radius * 0.78;
			int trail = Math.max(1, FxClient.n(4));
			for (int t = 0; t < trail; t++) {
				double a = head - t * 0.22;
				double px = c.x + Math.cos(a) * r;
				double pz = c.z + Math.sin(a) * r;
				if (t == 0) FxClient.spawn(ParticleTypes.SWEEP_ATTACK, px, c.y + 1.0, pz, 0, 0, 0);
				FxClient.spawn(ParticleTypes.CRIT, px, c.y + 1.0, pz, -Math.sin(a) * 0.25, 0.02, Math.cos(a) * 0.25);
			}
			// Polvo y escombros arrastrados por el giro a la altura de los pies.
			double a = head + rng.nextDouble();
			double fr = radius * 0.9;
			double fx = c.x + Math.cos(a) * fr;
			double fz = c.z + Math.sin(a) * fr;
			FxClient.spawn(ParticleTypes.POOF, fx, c.y + 0.1, fz, -Math.sin(a) * 0.12, 0.03, Math.cos(a) * 0.12);
			if (i % 2 == 0) {
				FxClient.chunk(world, FxClient.groundState(world, fx, c.y, fz), fx, c.y + 0.1, fz, -Math.sin(a) * 0.14,
						0.18 + rng.nextDouble() * 0.12, Math.cos(a) * 0.14, 0.1f, 22 + rng.nextInt(14), 1.0f, 0.3f);
			}
		});
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(FxClient.entityPos(world, p)), 14);
		if (f > 0) {
			ScreenDynamics.shake(0.6f * f, 300, 20);
			ScreenDynamics.fovPunch(3.2f * f, 60, 0, 280);
		}
	}
}
