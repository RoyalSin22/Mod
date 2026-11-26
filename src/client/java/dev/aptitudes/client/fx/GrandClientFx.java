package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import dev.aptitudes.role.Role;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V96: encuadre visual de las firmas y definitivas de los roles piloto. Cada rol tiene su gesto de partículas (rabia que sube,
 * lluvia de elementos, mutación, halo sagrado, descarga mecánica); la definitiva añade un pilar, un anillo mayor y más cámara.
 * Las partículas dependen solo de la semilla, así que todos ven lo mismo.
 */
final class GrandClientFx {
	private GrandClientFx() {
	}

	static void signature(ClientWorld world, FxEventPayload p) {
		run(world, p, false);
	}

	static void ultimate(ClientWorld world, FxEventPayload p) {
		run(world, p, true);
	}

	private static void run(ClientWorld world, FxEventPayload p, boolean ultimate) {
		Entity e = FxClient.entity(world, p.entityId());
		PlayerAnims.play(e, ultimate ? PlayerAnimIds.GRAND_ULTIMATE : PlayerAnimIds.GRAND_SIGNATURE);
		Role[] roles = Role.values();
		int idx = Math.round(p.power());
		if (idx < 0 || idx >= roles.length) return;
		Role role = roles[idx];
		Random rng = new Random(p.seed());
		int impact = PlayerAnimIds.IMPACT_TICKS.get(ultimate ? PlayerAnimIds.GRAND_ULTIMATE : PlayerAnimIds.GRAND_SIGNATURE);
		ParticleEffect tint = ParticleFx.transition(p.color(), 0xFFFFFF, ultimate ? 1.5f : 1.1f);
		ParticleEffect motif = motif(role);

		// Carga: la energía se recoge hacia el cuerpo hasta el tick de impacto.
		FxTimeline.every(impact, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			Vec3d c = now != null && now.isAlive() ? now.getPos().add(0, 1.0, 0) : new Vec3d(p.x(), p.y() + 1.0, p.z());
			int k = FxClient.n(ultimate ? 7 : 4);
			for (int j = 0; j < k; j++) {
				double a = rng.nextDouble() * Math.PI * 2;
				double r = (ultimate ? 2.6 : 1.8) - 0.15 * i;
				double h = rng.nextDouble() * 1.8 - 0.8;
				FxClient.spawn(tint, c.x + Math.cos(a) * r, c.y + h, c.z + Math.sin(a) * r, -Math.cos(a) * 0.16, 0.0, -Math.sin(a) * 0.16);
			}
		});

		FxTimeline.later(impact, () -> release(world, p, role, motif, tint, ultimate));
	}

	private static ParticleEffect motif(Role role) {
		return switch (role) {
			case BERSERKER -> ParticleTypes.LAVA;
			case ELEMENTALIST, MAGE -> ParticleTypes.END_ROD;
			case SHAPESHIFTER -> ParticleTypes.SPORE_BLOSSOM_AIR;
			case PALADIN, MONK -> ParticleTypes.WAX_ON;
			case ASSASSIN, WARLOCK -> ParticleTypes.SMOKE;
			case HERBALIST, DRUID -> ParticleTypes.HAPPY_VILLAGER;
			case LANCER, TAMER -> ParticleTypes.CLOUD;
			case SWORDSMAN, WEAPON_SUMMONER -> ParticleTypes.SWEEP_ATTACK;
			case NECROMANCER -> ParticleTypes.SOUL_FIRE_FLAME;
			case MERCHANT -> ParticleTypes.GLOW;
			case GUNSLINGER -> ParticleTypes.CRIT;
			case HUNTER -> ParticleTypes.FIREWORK;
			case BARD -> ParticleTypes.NOTE;
			default -> ParticleTypes.ELECTRIC_SPARK;
		};
	}

	private static void release(ClientWorld world, FxEventPayload p, Role role, ParticleEffect motif, ParticleEffect tint, boolean ultimate) {
		Random rng = new Random(p.seed() * 31L + 7);
		Entity e = FxClient.entity(world, p.entityId());
		Vec3d base = e != null ? e.getPos() : new Vec3d(p.x(), p.y(), p.z());
		double rMax = ultimate ? 7.0 : 4.5;
		FxClient.spawn(ParticleTypes.FLASH, base.x, base.y + 1.0, base.z, 0, 0, 0);

		// Anillos de choque en el suelo (uno en la firma, tres escalonados en la definitiva).
		int rings = ultimate ? 3 : 1;
		for (int k = 0; k < rings; k++) {
			final double scale = (k + 1) / (double) rings;
			FxTimeline.later(k * 3, () -> {
				int n = FxClient.n((int) (28 + 16 * scale));
				for (int i = 0; i < n; i++) {
					double a = Math.PI * 2 * i / n;
					FxClient.spawn(tint, base.x, base.y + 0.15, base.z, Math.cos(a) * 0.45 * scale + 0.1 * Math.cos(a),
							0.02, Math.sin(a) * 0.45 * scale + 0.1 * Math.sin(a));
				}
			});
		}

		// Gesto propio del rol.
		int n = FxClient.n(ultimate ? 70 : 36);
		for (int i = 0; i < n; i++) {
			double a = rng.nextDouble() * Math.PI * 2;
			double r = rng.nextDouble() * rMax * 0.8;
			double x = base.x + Math.cos(a) * r;
			double z = base.z + Math.sin(a) * r;
			switch (role) {
				case BERSERKER -> FxClient.spawn(motif, x, base.y + 0.1, z, 0, 0.05 + rng.nextDouble() * 0.12, 0);
				case ELEMENTALIST -> FxClient.spawn(motif, x, base.y + 4.5 - rng.nextDouble(), z, 0, -0.18 - rng.nextDouble() * 0.12, 0);
				case SHAPESHIFTER -> FxClient.spawn(motif, base.x + (rng.nextDouble() - 0.5) * 1.6, base.y + rng.nextDouble() * 1.9,
						base.z + (rng.nextDouble() - 0.5) * 1.6, (rng.nextDouble() - 0.5) * 0.2, 0.04, (rng.nextDouble() - 0.5) * 0.2);
				case PALADIN, MONK, HERBALIST, DRUID, MERCHANT, BARD -> FxClient.spawn(motif, x, base.y + 0.1, z, 0, 0.1 + rng.nextDouble() * 0.1, 0);
				case MAGE, WEAPON_SUMMONER, HUNTER -> FxClient.spawn(motif, x, base.y + 4.0 - rng.nextDouble(), z, 0, -0.16 - rng.nextDouble() * 0.1, 0);
				case ASSASSIN, WARLOCK, NECROMANCER -> FxClient.spawn(motif, base.x + (rng.nextDouble() - 0.5) * 3.0, base.y + rng.nextDouble() * 0.6,
						base.z + (rng.nextDouble() - 0.5) * 3.0, (rng.nextDouble() - 0.5) * 0.05, 0.06 + rng.nextDouble() * 0.06, (rng.nextDouble() - 0.5) * 0.05);
				case LANCER, SWORDSMAN, GUNSLINGER, TAMER -> {
					double a2 = rng.nextDouble() * Math.PI * 2;
					FxClient.spawn(motif, base.x, base.y + 0.9, base.z, Math.cos(a2) * (0.25 + rng.nextDouble() * 0.3), (rng.nextDouble() - 0.4) * 0.1, Math.sin(a2) * (0.25 + rng.nextDouble() * 0.3));
				}
				default -> {
					FxClient.spawn(motif, x, base.y + rng.nextDouble() * 2.5, z, (rng.nextDouble() - 0.5) * 0.4, rng.nextDouble() * 0.2, (rng.nextDouble() - 0.5) * 0.4);
				}
			}
		}
		if (ultimate) {
			// Pilar de luz que sube desde el suelo.
			int h = FxClient.n(40);
			for (int i = 0; i < h; i++) {
				FxClient.spawn(tint, base.x + (rng.nextDouble() - 0.5) * 0.6, base.y + i * 0.3, base.z + (rng.nextDouble() - 0.5) * 0.6, 0, 0.12, 0);
			}
		}

		// Cámara: más fuerte y larga en la definitiva; la local recibe el viñeteado del color del rol.
		float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(base), ultimate ? 24 : 14);
		if (f > 0) {
			ScreenDynamics.shake((ultimate ? 0.9f : 0.45f) * f, ultimate ? 700 : 350, 18f);
			ScreenDynamics.fovPunch((ultimate ? 7f : 4f) * f, 60, ultimate ? 160 : 60, 360);
			ScreenDynamics.kick(-(ultimate ? 2.4f : 1.2f) * f, 0, 50, 380);
			if (FxClient.isLocal(p.entityId())) ScreenDynamics.vignette(p.color(), ultimate ? 0.45f : 0.3f, 40, ultimate ? 400 : 150, 500, 0f);
		}
	}
}
