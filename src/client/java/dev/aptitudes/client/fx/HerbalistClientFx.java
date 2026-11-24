package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V85: coreografía visual del Herbolista. Un bálsamo que florece en ondas de hojas y remolinos sobre cada aliado, una nube de lluvia con
 * gotas que caen y un círculo luminoso en el suelo, y una vaina de esporas que describe un arco y estalla en una niebla verde y violeta.
 */
final class HerbalistClientFx {
	private static final int LEAF = 0x7BD88F;
	private static final int GOLD = 0xF4E285;
	private static final int SPORE_A = 0x9CCC65;
	private static final int SPORE_B = 0x7E57C2;

	private HerbalistClientFx() {
	}

	private static ParticleEffect leaf(float size) {
		return ParticleFx.transition(LEAF, GOLD, size);
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	// ------------------------------------------------------------------ Bálsamo

	static void balm(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HERBALIST_BALM);
		int charge = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HERBALIST_BALM);
		// Preparación: motas verdes suben hacia las manos mientras se recogen.
		FxTimeline.every(charge, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d c = now.getPos();
			int k = FxClient.n(4);
			for (int j = 0; j < k; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = 0.9 - 0.15 * i;
				FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.08, 0);
			}
		});
		FxTimeline.later(charge, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(2, p.power());
			// Onda de hojas que se abre por el suelo hasta el radio y un surtidor de luz en el centro.
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, radius);
				int pts = FxClient.n(Math.max(12, (int) (r * 6)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + i * 0.2;
					FxClient.spawn(leaf(1.1f), c.x + Math.cos(a) * r, c.y + 0.15, c.z + Math.sin(a) * r, 0, 0.04, 0);
					if (j % 3 == 0) FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.05, 0);
				}
			});
			int up = FxClient.n(30);
			for (int i = 0; i < up; i++) {
				Vec3d d = randomDirection(rng).multiply(0.08);
				FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y + 1.0, c.z, d.x, Math.abs(d.y) + 0.06, d.z);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 10);
			if (f > 0) {
				ScreenDynamics.vignette(LEAF, 0.22f * f, 80, 120, 500, 0f);
				ScreenDynamics.fovPunch(3f * f, 60, 20, 400);
			}
		});
	}

	/** Remolino de hojas que sube alrededor del aliado curado. */
	static void balmAlly(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HERBALIST_BALM);
		FxTimeline.later(delay, () -> FxTimeline.every(12, 1, i -> {
			Entity e = FxClient.entity(world, p.entityId());
			if (e == null) return;
			Vec3d c = e.getPos();
			for (int arm = 0; arm < 2; arm++) {
				double a = i * 0.7 + arm * Math.PI;
				double y = i * 0.16;
				FxClient.spawn(leaf(1.0f), c.x + Math.cos(a) * 0.7, c.y + y, c.z + Math.sin(a) * 0.7, 0, 0.02, 0);
			}
			if (i % 3 == 0) FxClient.spawn(ParticleTypes.HEART, c.x + (rng.nextDouble() - 0.5) * 0.6, c.y + 1.8, c.z + (rng.nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
		}));
		if (FxClient.isLocal(p.entityId())) {
			FxTimeline.later(delay, () -> FxClient.flash(LEAF, 0.18f));
		}
	}

	// ------------------------------------------------------------------ Lluvia sanadora

	static void rain(ClientWorld world, FxEventPayload p) {
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HERBALIST_RAIN);
		FxTimeline.later(PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HERBALIST_RAIN), () -> rainStart(world, p));
	}

	private static void rainStart(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		double radius = Math.max(2, p.power());
		int ticks = Math.max(20, Math.round(p.dx()));
		// Nube que se forma sobre el centro: nubes grises con un borde luminoso y relámpagos verdes ocasionales.
		FxTimeline.every(ticks, 2, i -> {
			int cloud = FxClient.n(14);
			for (int j = 0; j < cloud; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = Math.sqrt(rng.nextDouble()) * radius * 0.9;
				Particle pt = FxClient.spawn(ParticleTypes.CLOUD, c.x + Math.cos(a) * r, c.y + 4.2 + rng.nextDouble() * 0.6, c.z + Math.sin(a) * r, 0, 0, 0);
				if (pt != null) pt.setMaxAge(16);
			}
			int drops = FxClient.n(30);
			for (int j = 0; j < drops; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = Math.sqrt(rng.nextDouble()) * radius;
				FxClient.spawn(ParticleTypes.FALLING_WATER, c.x + Math.cos(a) * r, c.y + 4.0, c.z + Math.sin(a) * r, 0, -0.3, 0);
			}
			int glow = FxClient.n(6);
			for (int j = 0; j < glow; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = Math.sqrt(rng.nextDouble()) * radius;
				FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x + Math.cos(a) * r, c.y + 0.3 + rng.nextDouble() * 1.4, c.z + Math.sin(a) * r, 0, 0.02, 0);
			}
		});
		// Círculo luminoso en el suelo que gira despacio mientras dura la lluvia.
		FxTimeline.every(ticks / 4, 4, i -> {
			int pts = FxClient.n(Math.max(20, (int) (radius * 8)));
			for (int j = 0; j < pts; j++) {
				double a = Math.PI * 2 * j / pts + i * 0.12;
				Particle pt = FxClient.spawn(leaf(1.0f), c.x + Math.cos(a) * radius, c.y + 0.12, c.z + Math.sin(a) * radius, 0, 0.0, 0);
				if (pt != null) pt.setMaxAge(12);
			}
		});
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 3);
		if (f > 0) {
			ScreenDynamics.vignette(LEAF, 0.2f * f, 200, ticks * 40L, 700, 0.3f);
			ScreenDynamics.fovPunch(2.5f * f, 120, 600, 600);
		}
	}

	// ------------------------------------------------------------------ Nube de esporas

	static void spore(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.HERBALIST_SPORE);
		int release = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.HERBALIST_SPORE);
		Vec3d center = new Vec3d(p.x(), p.y(), p.z());
		double radius = Math.max(2, p.power());
		FxTimeline.later(release, () -> {
			Entity e = FxClient.entity(world, p.entityId());
			Vec3d start = e != null ? FxClient.handPos(e, true) : center.add(0, 1.5, 0);
			Vec3d end = center.add(0, 0.6, 0);
			double len = start.distanceTo(end);
			int travel = Math.max(2, Math.min(8, (int) Math.ceil(len / 2.5)));
			// La vaina describe un arco: sube a mitad de camino y cae con la gravedad de un tiro por debajo.
			FxTimeline.every(travel, 1, i -> {
				double t = (i + 1) / (double) travel;
				Vec3d c = start.lerp(end, t).add(0, Math.sin(Math.PI * t) * Math.min(2.5, len * 0.25), 0);
				FxClient.spawn(ParticleTypes.SPORE_BLOSSOM_AIR, c.x, c.y, c.z, 0, 0, 0);
				FxClient.spawn(ParticleFx.transition(SPORE_A, SPORE_B, 1.0f), c.x, c.y, c.z, 0, 0, 0);
			});
			FxTimeline.later(travel, () -> sporeBurst(world, p, end, radius, rng));
		});
	}

	private static void sporeBurst(ClientWorld world, FxEventPayload p, Vec3d c, double radius, Random rng) {
		ParticleEffect mist = ParticleFx.transition(SPORE_A, SPORE_B, 1.6f);
		int burst = FxClient.n(40);
		for (int i = 0; i < burst; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.22);
			FxClient.spawn(mist, c.x, c.y + 0.4, c.z, d.x, d.y * 0.4, d.z);
		}
		FxClient.spawn(ParticleTypes.POOF, c.x, c.y + 0.4, c.z, 0, 0.02, 0);
		// Niebla que permanece durante seis segundos, tan ancha como el radio, con esporas que suben y se apagan.
		FxTimeline.every(60, 2, i -> {
			int k = FxClient.n(10);
			for (int j = 0; j < k; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = Math.sqrt(rng.nextDouble()) * radius;
				Particle pt = FxClient.spawn(mist, c.x + Math.cos(a) * r, c.y + rng.nextDouble() * 1.6, c.z + Math.sin(a) * r, 0, 0.01, 0);
				if (pt != null) pt.setMaxAge(22 + rng.nextInt(14));
				if (j % 2 == 0) FxClient.spawn(ParticleTypes.SPORE_BLOSSOM_AIR, c.x + Math.cos(a) * r, c.y + 1.2 + rng.nextDouble(), c.z + Math.sin(a) * r, 0, -0.01, 0);
			}
		});
		float f = FxMath.falloff(FxClient.distanceTo(c), 12);
		if (f > 0) {
			ScreenDynamics.shake(0.18f * f, 140, 20);
			ScreenDynamics.vignette(SPORE_B, 0.25f * f, 60, 240, 500, 0f);
		}
	}
}
