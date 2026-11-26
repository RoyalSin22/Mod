package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import dev.aptitudes.role.EvolvedTree.Template;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V96: encuadre visual de las activas de Maestro y Definitivo, una por plantilla (las diez que comparten los 20 roles). El gesto del
 * cuerpo es el mismo y breve; cada plantilla añade su capa de partículas con el color del rol. El Definitivo (dx = 1) multiplica la
 * cantidad y mueve más la cámara. Las partículas del mundo con el área real las sigue dibujando el servidor.
 */
final class EvolvedClientFx {
	private EvolvedClientFx() {
	}

	static void cast(ClientWorld world, FxEventPayload p) {
		Template[] all = Template.values();
		int idx = Math.round(p.power());
		if (idx < 0 || idx >= all.length) return;
		Entity e = FxClient.entity(world, p.entityId());
		PlayerAnims.play(e, PlayerAnimIds.EVOLVED_CAST);
		int impact = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.EVOLVED_CAST);
		FxTimeline.later(impact, () -> release(world, p, all[idx]));
	}

	private static void ring(ParticleEffect fx, Vec3d c, double speed, int base, double y) {
		int n = FxClient.n(base);
		for (int i = 0; i < n; i++) {
			double a = Math.PI * 2 * i / n;
			FxClient.spawn(fx, c.x, c.y + y, c.z, Math.cos(a) * speed, 0.02, Math.sin(a) * speed);
		}
	}

	private static void release(ClientWorld world, FxEventPayload p, Template template) {
		Random rng = new Random(p.seed());
		boolean ult = p.dx() >= 1f;
		float k = ult ? 1.6f : 1f;
		Entity e = FxClient.entity(world, p.entityId());
		Vec3d pos = new Vec3d(p.x(), p.y(), p.z());
		Vec3d base = e != null && e.isAlive() ? e.getPos() : pos;
		Vec3d look = e != null ? e.getRotationVector() : new Vec3d(0, 0, 1);
		Vec3d flat = new Vec3d(look.x, 0, look.z);
		flat = flat.lengthSquared() < 1e-4 ? new Vec3d(0, 0, 1) : flat.normalize();
		Vec3d hand = e != null ? FxClient.handPos(e, true) : base.add(0, 1.2, 0);
		ParticleEffect tint = ParticleFx.transition(p.color(), 0xFFFFFF, 1.0f + 0.4f * k);
		boolean local = FxClient.isLocal(p.entityId());
		float shake = 0f;
		switch (template) {
			case NOVA -> {
				FxClient.spawn(ParticleTypes.FLASH, base.x, base.y + 1.0, base.z, 0, 0, 0);
				ring(tint, base, 0.4 * k, 30, 0.2);
				ring(ParticleTypes.CRIT, base, 0.25 * k, 20, 0.5);
				shake = 0.4f;
			}
			case BEAM -> {
				double len = 24;
				int n = FxClient.n((int) (28 * k));
				for (int i = 0; i < n; i++) {
					double d = len * i / n;
					Vec3d q = hand.add(look.multiply(d));
					FxClient.spawn(tint, q.x, q.y, q.z, look.x * 0.15, look.y * 0.15, look.z * 0.15);
					if (rng.nextDouble() < 0.4) FxClient.spawn(ParticleTypes.END_ROD, q.x, q.y, q.z, 0, 0, 0);
				}
				shake = 0.25f;
			}
			case CONE -> {
				int n = FxClient.n((int) (26 * k));
				for (int i = 0; i < n; i++) {
					double a = Math.toRadians((rng.nextDouble() - 0.5) * 70);
					double d = 1 + rng.nextDouble() * 5;
					Vec3d dir = flat.rotateY((float) a);
					FxClient.spawn(tint, base.x + dir.x * d, base.y + 1.0, base.z + dir.z * d, dir.x * 0.18, 0.01, dir.z * 0.18);
				}
				shake = 0.2f;
			}
			case DASH -> {
				int n = FxClient.n((int) (20 * k));
				for (int i = 0; i < n; i++) {
					Vec3d q = base.add(flat.multiply(-i * 0.35));
					FxClient.spawn(ParticleTypes.CLOUD, q.x, q.y + 0.2, q.z, -flat.x * 0.05, 0.02, -flat.z * 0.05);
					FxClient.spawn(tint, q.x, q.y + 1.0, q.z, 0, 0.01, 0);
				}
				if (local) ScreenDynamics.fovPunch(6f * k, 80, 120, 300);
			}
			case ZONE -> {
				ring(tint, pos, 0.2 * k, 24, 0.15);
				int n = FxClient.n((int) (14 * k));
				for (int i = 0; i < n; i++) {
					double a = rng.nextDouble() * Math.PI * 2, r = rng.nextDouble() * 4;
					FxClient.spawn(tint, pos.x + Math.cos(a) * r, pos.y + 5, pos.z + Math.sin(a) * r, 0, -0.35, 0);
				}
				shake = 0.2f;
			}
			case VORTEX -> {
				int n = FxClient.n((int) (36 * k));
				for (int i = 0; i < n; i++) {
					double a = i * 0.7, r = 3.5 - 3.0 * i / n;
					FxClient.spawn(tint, pos.x + Math.cos(a) * 3.5, pos.y + 0.3 + i * 0.05, pos.z + Math.sin(a) * 3.5, -Math.cos(a) * 0.12, 0.03, -Math.sin(a) * 0.12);
				}
			}
			case AURA -> {
				ring(tint, base, 0.55, 40, 0.2);
				int n = FxClient.n((int) (20 * k));
				for (int i = 0; i < n; i++) {
					double a = rng.nextDouble() * Math.PI * 2, r = rng.nextDouble() * 8;
					FxClient.spawn(tint, base.x + Math.cos(a) * r, base.y + 0.1, base.z + Math.sin(a) * r, 0, 0.08, 0);
				}
				if (local) ScreenDynamics.vignette(p.color(), 0.22f, 40, 100, 400, 0f);
			}
			case HEAL -> {
				ring(ParticleTypes.HAPPY_VILLAGER, base, 0.3, 24, 0.15);
				int n = FxClient.n((int) (14 * k));
				for (int i = 0; i < n; i++) {
					double a = rng.nextDouble() * Math.PI * 2, r = rng.nextDouble() * 5;
					FxClient.spawn(ParticleTypes.GLOW, base.x + Math.cos(a) * r, base.y + 0.1, base.z + Math.sin(a) * r, 0, 0.07, 0);
				}
			}
			case SHIELD -> {
				ring(ParticleTypes.WAX_ON, base, 0.14, 24, 1.0);
				ring(tint, base, 0.2, 20, 0.3);
				FxClient.spawn(ParticleTypes.FLASH, base.x, base.y + 1.0, base.z, 0, 0, 0);
				if (local) ScreenDynamics.fovPunch(-3f, 50, 40, 300);
			}
			case SUMMON -> {
				ring(ParticleTypes.SOUL_FIRE_FLAME, base, 0.15, 20, 0.1);
				int n = FxClient.n((int) (16 * k));
				for (int i = 0; i < n; i++) {
					double a = rng.nextDouble() * Math.PI * 2, r = 1 + rng.nextDouble() * 2.5;
					FxClient.spawn(ParticleTypes.SOUL, base.x + Math.cos(a) * r, base.y + 0.1, base.z + Math.sin(a) * r, 0, 0.08, 0);
				}
			}
		}
		float f = local ? 1f : FxMath.falloff(FxClient.distanceTo(base), 14);
		if (shake > 0 && f > 0) ScreenDynamics.shake(shake * k * f, 220, 18f);
	}
}
