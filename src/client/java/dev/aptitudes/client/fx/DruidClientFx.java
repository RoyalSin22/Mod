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
 * V88: coreografía visual del Druida. La manada llega con una ola de hojas y motas de luz alrededor de cada lobo al aparecer; la
 * tormenta de espinas hace brotar zarzas en espiral, con astillas verdes y un viñeteado musgo.
 */
final class DruidClientFx {
	private static final int MOSS = 0x6E8B3D;
	private static final int LIGHT = 0xC5E1A5;

	private DruidClientFx() {
	}

	private static ParticleEffect leaf(float size) {
		return ParticleFx.transition(LIGHT, MOSS, size);
	}

	// ------------------------------------------------------------------ Manada

	static void howl(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.DRUID_WOLVES);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.DRUID_WOLVES);
		FxTimeline.later(delay, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			// Ola de hojas que sale en anillo y notas de luz que suben con el aullido.
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, 4.5);
				int pts = FxClient.n(Math.max(14, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + i * 0.2;
					FxClient.spawn(leaf(1.2f), c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r, 0, 0.05, 0);
				}
			});
			int motes = FxClient.n(24);
			for (int i = 0; i < motes; i++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = rng.nextDouble() * 2.5;
				FxClient.spawn(ParticleTypes.END_ROD, c.x + Math.cos(a) * r, c.y + 0.3, c.z + Math.sin(a) * r, 0, 0.06 + rng.nextDouble() * 0.05, 0);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), 16);
			if (f > 0) {
				ScreenDynamics.vignette(MOSS, 0.28f * f, 100, 200, 600, 0f);
				ScreenDynamics.fovPunch(5f * f, 80, 100, 500);
				ScreenDynamics.shake(0.1f * f, 200, 12);
			}
		});
	}

	/** Un lobo aparece: anillo de hojas bajo sus patas y un destello de luz. */
	static void wolf(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.DRUID_WOLVES);
		Vec3d c = new Vec3d(p.x(), p.y(), p.z());
		FxTimeline.later(delay, () -> {
			FxClient.spawn(ParticleTypes.FLASH, c.x, c.y + 0.6, c.z, 0, 0, 0);
			int ring = FxClient.n(18);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(leaf(1.1f), c.x + Math.cos(a) * 0.4, c.y + 0.1, c.z + Math.sin(a) * 0.4, Math.cos(a) * 0.1, 0.06, Math.sin(a) * 0.1);
			}
			int sparks = FxClient.n(10);
			for (int i = 0; i < sparks; i++) {
				FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x + (rng.nextDouble() - 0.5) * 0.6, c.y + rng.nextDouble() * 0.8, c.z + (rng.nextDouble() - 0.5) * 0.6, 0, 0.03, 0);
			}
		});
	}

	// ------------------------------------------------------------------ Tormenta de espinas

	static void thorns(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.DRUID_THORNS);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.DRUID_THORNS);
		FxTimeline.later(delay, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(2, p.power());
			// Zarzas que brotan en dos espirales opuestas hasta el radio, subiendo en altura.
			FxTimeline.every(10, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 10, radius);
				for (int arm = 0; arm < 3; arm++) {
					double a = i * 0.55 + arm * Math.PI * 2 / 3;
					for (double h = 0; h < 1.8; h += 0.4) {
						Particle pt = FxClient.spawn(leaf(1.0f), c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r, 0, 0.0, 0);
						if (pt != null) pt.setMaxAge(14);
					}
					FxClient.spawn(ParticleTypes.CRIT, c.x + Math.cos(a) * r, c.y + 0.4 + rng.nextDouble(), c.z + Math.sin(a) * r, 0, 0.05, 0);
				}
			});
			int ring = FxClient.n(30);
			for (int i = 0; i < ring; i++) {
				double a = Math.PI * 2 * i / ring;
				FxClient.spawn(ParticleTypes.COMPOSTER, c.x + Math.cos(a) * radius * 0.7, c.y + 0.2, c.z + Math.sin(a) * radius * 0.7, 0, 0.08, 0);
			}
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 3);
			if (f > 0) {
				ScreenDynamics.shake(0.25f * f, 240, 20);
				ScreenDynamics.vignette(MOSS, 0.3f * f, 60, 200, 500, 0f);
			}
		});
	}
}
