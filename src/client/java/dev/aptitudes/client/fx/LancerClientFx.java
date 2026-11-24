package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V87: coreografía visual del Lancero. El salto del dragón arranca con una ráfaga de aire y deja una estela de aliento de dragón; el
 * aterrizaje levanta escombros con física real y una onda violeta; la estocada traza una línea recta de viento y chispas.
 */
final class LancerClientFx {
	private static final int BLUE = 0x2980B9;
	private static final int ICE = 0xB3E5FC;
	private static final int PURPLE = 0xAB47BC;

	private LancerClientFx() {
	}

	private static ParticleEffect wind(float size) {
		return ParticleFx.transition(ICE, BLUE, size);
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	// ------------------------------------------------------------------ Salto del dragón

	static void leap(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.LANCER_LEAP);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.LANCER_LEAP);
		int flight = Math.max(6, Math.round(p.power()) - delay);
		FxTimeline.later(delay, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			// Despegue: anillo de aire que sale por el suelo y nube que se aparta.
			int ring = FxClient.n(30);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(ParticleTypes.CLOUD, c.x, c.y + 0.1, c.z, Math.cos(a) * 0.3, 0.04, Math.sin(a) * 0.3);
			}
			int dust = FxClient.n(18);
			for (int i = 0; i < dust; i++) {
				Vec3d d = randomDirection(rng).multiply(0.12);
				FxClient.chunk(world, FxClient.groundState(world, c.x, c.y, c.z), c.x, c.y + 0.1, c.z, d.x, Math.abs(d.y) + 0.2, d.z, 0.1f, 28, 0.06f, 0.3f);
			}
			// Estela mientras sube y cae.
			FxTimeline.every(flight, 1, i -> {
				Entity now = FxClient.entity(world, p.entityId());
				if (now == null) return;
				Vec3d pos = now.getPos();
				int k = FxClient.n(5);
				for (int j = 0; j < k; j++) {
					FxClient.spawn(ParticleTypes.DRAGON_BREATH, pos.x + (rng.nextDouble() - 0.5) * 0.6, pos.y + rng.nextDouble() * 1.6,
							pos.z + (rng.nextDouble() - 0.5) * 0.6, 0, -0.01, 0);
				}
				FxClient.spawn(wind(1.2f), pos.x, pos.y + 0.9, pos.z, 0, 0, 0);
			});
			if (FxClient.isLocal(p.entityId())) {
				ScreenDynamics.fovPunch(10f, 50, 200, 500);
				ScreenDynamics.kick(2.0f, 0, 50, 300);
			}
		});
	}

	static void slam(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.LANCER_SLAM);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.LANCER_SLAM), () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(2, p.power());
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y + 0.3, c.z, 0, 0, 0);
			// Onda violeta que se abre por el suelo con escombros que saltan en el borde.
			FxTimeline.every(6, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 6, radius);
				int pts = FxClient.n(Math.max(14, (int) (r * 8)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + rng.nextDouble() * 0.2;
					FxClient.spawn(ParticleTypes.DRAGON_BREATH, c.x + Math.cos(a) * r, c.y + 0.15, c.z + Math.sin(a) * r, 0, 0.03, 0);
					if (j % 3 == 0) FxClient.spawn(ParticleTypes.CRIT, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, Math.cos(a) * 0.1, 0.1, Math.sin(a) * 0.1);
				}
			});
			int debris = FxClient.n(26);
			for (int i = 0; i < debris; i++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double s = 0.12 + rng.nextDouble() * 0.2;
				FxClient.chunk(world, FxClient.groundState(world, c.x, c.y, c.z), c.x, c.y + 0.1, c.z, Math.cos(a) * s, 0.25 + rng.nextDouble() * 0.25,
						Math.sin(a) * s, 0.12f, 36, 0.08f, 0.35f);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 18);
			if (f > 0) {
				ScreenDynamics.shake(0.55f * f, 320, 24);
				ScreenDynamics.fovPunch(-6f * f, 40, 30, 320);
				ScreenDynamics.kick(-3f * f, 0, 40, 380);
				FxClient.flash(PURPLE, 0.25f * f);
			}
		});
	}

	// ------------------------------------------------------------------ Estocada perforante

	static void thrust(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.LANCER_THRUST);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.LANCER_THRUST);
		FxTimeline.later(delay, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d eye = e != null ? e.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = e != null ? FxClient.handPos(e, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len < 0.5) return;
			Vec3d axis = end.subtract(start).normalize();
			double step = 0.35 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			// Línea recta de viento con chispas y destellos de punta que se van abriendo.
			for (double d = 0; d < len; d += step) {
				Vec3d c = start.add(axis.multiply(d));
				FxClient.spawn(wind(1.0f), c.x, c.y, c.z, axis.x * 0.04, axis.y * 0.04, axis.z * 0.04);
				if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.CRIT, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.1, (rng.nextDouble() - 0.5) * 0.1, (rng.nextDouble() - 0.5) * 0.1);
				if (((int) (d / step)) % 5 == 0) FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
			}
			FxClient.spawn(ParticleTypes.FLASH, end.x, end.y, end.z, 0, 0, 0);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
			if (f > 0) {
				ScreenDynamics.kick(-1.6f * f, 0, 30, 260);
				ScreenDynamics.fovPunch(5f * f, 30, 20, 260);
				ScreenDynamics.shake(0.2f * f, 140, 22);
			}
		});
	}
}
