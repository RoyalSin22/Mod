package dev.aptitudes.client.fx;

import dev.aptitudes.ability.ParticleFx;
import dev.aptitudes.logic.FxMath;
import dev.aptitudes.logic.PlayerAnimIds;
import dev.aptitudes.network.FxEventPayload;
import dev.aptitudes.role.Element;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * V91: coreografía visual del Elementalista, con un estilo propio por elemento (14). Cada elemento define su estela, su impacto y su
 * firma de estallido; el color sale del propio elemento. Los proyectiles dibujan la línea tras el gesto, el rayo en cadena traza zigzags
 * y el estallido abre un anillo con la firma del elemento y un golpe de cámara.
 */
final class ElementClientFx {
	private static final Vec3d UP = new Vec3d(0, 1, 0);

	private ElementClientFx() {
	}

	private static Element elementOf(FxEventPayload p) {
		return Element.byIndex(Math.round(p.power()));
	}

	private static Vec3d randomDirection(Random rng) {
		double z = rng.nextDouble() * 2 - 1;
		double a = rng.nextDouble() * Math.PI * 2;
		double r = Math.sqrt(1 - z * z);
		return new Vec3d(r * Math.cos(a), z, r * Math.sin(a));
	}

	private static ParticleEffect tint(Element e, float size) {
		return ParticleFx.transition(e.color(), 0xFFFFFF, size);
	}

	private static ParticleEffect item(net.minecraft.item.Item item) {
		return new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(item));
	}

	/** Partículas propias del elemento en un punto de la estela. */
	private static void trailPoint(ClientWorld world, Element e, Vec3d c, Vec3d axis, Random rng) {
		FxClient.spawn(tint(e, 1.0f), c.x, c.y, c.z, 0, 0, 0);
		switch (e) {
			case FIRE -> { FxClient.spawn(ParticleTypes.FLAME, c.x, c.y, c.z, 0, 0.01, 0); if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y, c.z, 0, 0.02, 0); }
			case MAGMA -> { FxClient.spawn(ParticleTypes.LAVA, c.x, c.y, c.z, 0, 0, 0); FxClient.spawn(ParticleTypes.FLAME, c.x, c.y, c.z, 0, 0, 0); }
			case ICE -> FxClient.spawn(ParticleTypes.SNOWFLAKE, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.04, -0.01, (rng.nextDouble() - 0.5) * 0.04);
			case LIGHTNING -> FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.2, (rng.nextDouble() - 0.5) * 0.2, (rng.nextDouble() - 0.5) * 0.2);
			case EARTH -> { if (rng.nextDouble() < 0.4) FxClient.chunk(world, FxClient.groundState(world, c.x, c.y, c.z), c.x, c.y, c.z, axis.x * 0.1, 0.1, axis.z * 0.1, 0.1f, 20, 0.07f, 0.3f); }
			case WIND -> FxClient.spawn(ParticleTypes.CLOUD, c.x, c.y, c.z, axis.x * 0.05, 0, axis.z * 0.05);
			case WATER -> { FxClient.spawn(ParticleTypes.SPLASH, c.x, c.y, c.z, 0, 0, 0); FxClient.spawn(ParticleTypes.FALLING_WATER, c.x, c.y, c.z, 0, 0, 0); }
			case PLANT -> { FxClient.spawn(ParticleTypes.HAPPY_VILLAGER, c.x, c.y, c.z, 0, 0, 0); if (rng.nextDouble() < 0.3) FxClient.spawn(item(Items.OAK_LEAVES), c.x, c.y, c.z, 0, 0, 0); }
			case SHADOW -> { FxClient.spawn(ParticleTypes.SQUID_INK, c.x, c.y, c.z, 0, 0, 0); FxClient.spawn(ParticleTypes.SMOKE, c.x, c.y, c.z, 0, 0.01, 0); }
			case LIGHT -> FxClient.spawn(ParticleTypes.END_ROD, c.x, c.y, c.z, axis.x * 0.02, axis.y * 0.02, axis.z * 0.02);
			case VOID -> { FxClient.spawn(ParticleTypes.REVERSE_PORTAL, c.x, c.y, c.z, 0, 0, 0); FxClient.spawn(ParticleTypes.PORTAL, c.x, c.y, c.z, 0, 0, 0); }
			case POISON -> { FxClient.spawn(ParticleTypes.ITEM_SLIME, c.x, c.y, c.z, 0, 0, 0); if (rng.nextDouble() < 0.3) FxClient.spawn(ParticleTypes.SNEEZE, c.x, c.y, c.z, 0, 0, 0); }
			case CRYSTAL -> { FxClient.spawn(ParticleTypes.GLOW, c.x, c.y, c.z, 0, 0, 0); if (rng.nextDouble() < 0.3) FxClient.spawn(item(Items.AMETHYST_SHARD), c.x, c.y, c.z, 0, 0, 0); }
			case SOUND -> { if (rng.nextDouble() < 0.5) FxClient.spawn(ParticleTypes.NOTE, c.x, c.y, c.z, rng.nextDouble(), 0, 0); }
		}
	}

	/** Estallido propio del elemento en un punto. */
	private static void impactAt(ClientWorld world, Element e, Vec3d c, Random rng, int scale) {
		int sparks = FxClient.n(10 * scale);
		for (int i = 0; i < sparks; i++) {
			Vec3d d = randomDirection(rng).multiply(0.1 + rng.nextDouble() * 0.2);
			FxClient.spawn(e.particle(), c.x, c.y, c.z, d.x, d.y, d.z);
			FxClient.spawn(tint(e, 1.2f), c.x, c.y, c.z, d.x * 0.7, d.y * 0.7, d.z * 0.7);
		}
		switch (e) {
			case FIRE, MAGMA -> FxClient.spawn(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 0, 0, 0);
			case ICE -> { for (int i = 0; i < FxClient.n(6); i++) { Vec3d d = randomDirection(rng).multiply(0.15); FxClient.spawn(item(Items.ICE), c.x, c.y, c.z, d.x, Math.abs(d.y) + 0.08, d.z); } }
			case LIGHTNING -> { FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0); FxClient.spawn(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 0, 0.2, 0); }
			case EARTH -> { for (int i = 0; i < FxClient.n(8); i++) { Vec3d d = randomDirection(rng).multiply(0.2); FxClient.chunk(world, FxClient.groundState(world, c.x, c.y, c.z), c.x, c.y, c.z, d.x, Math.abs(d.y) + 0.15, d.z, 0.12f, 30, 0.08f, 0.35f); } }
			case WIND -> FxClient.spawn(ParticleTypes.SWEEP_ATTACK, c.x, c.y, c.z, 0, 0, 0);
			case WATER -> FxClient.spawn(ParticleTypes.BUBBLE_POP, c.x, c.y, c.z, 0, 0.05, 0);
			case PLANT -> { for (int i = 0; i < FxClient.n(6); i++) FxClient.spawn(ParticleTypes.COMPOSTER, c.x, c.y, c.z, 0, 0.1, 0); }
			case SHADOW -> FxClient.spawn(ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 0, 0.03, 0);
			case LIGHT -> FxClient.spawn(ParticleTypes.FLASH, c.x, c.y, c.z, 0, 0, 0);
			case VOID -> FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y, c.z, 0, 0, 0);
			case POISON -> { for (int i = 0; i < FxClient.n(6); i++) FxClient.spawn(ParticleTypes.ITEM_SLIME, c.x, c.y, c.z, (rng.nextDouble() - 0.5) * 0.2, 0.1, (rng.nextDouble() - 0.5) * 0.2); }
			case CRYSTAL -> { for (int i = 0; i < FxClient.n(6); i++) { Vec3d d = randomDirection(rng).multiply(0.15); FxClient.spawn(item(Items.AMETHYST_SHARD), c.x, c.y, c.z, d.x, Math.abs(d.y) + 0.08, d.z); } }
			case SOUND -> FxClient.spawn(ParticleTypes.SONIC_BOOM, c.x, c.y, c.z, 0, 0, 0);
		}
	}

	// ------------------------------------------------------------------ Proyectil

	static void bolt(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Element e = elementOf(p);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ELEMENT_BOLT);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ELEMENT_BOLT);
		// Preparación: la energía del elemento se reúne en la palma.
		FxTimeline.every(delay, 1, i -> {
			Entity now = FxClient.entity(world, p.entityId());
			if (now == null) return;
			Vec3d h = FxClient.handPos(now, true);
			FxClient.spawn(e.particle(), h.x, h.y, h.z, 0, 0.01, 0);
			FxClient.spawn(tint(e, 0.9f), h.x, h.y, h.z, 0, 0, 0);
		});
		FxTimeline.later(delay, () -> {
			Entity en = FxClient.entity(world, p.entityId());
			Vec3d eye = en != null ? en.getEyePos() : new Vec3d(p.x(), p.y() + 1.62, p.z());
			Vec3d start = en != null ? FxClient.handPos(en, true) : eye;
			Vec3d end = eye.add(p.dx(), p.dy(), p.dz());
			double len = start.distanceTo(end);
			if (len < 0.5) return;
			Vec3d axis = end.subtract(start).normalize();
			Vec3d u = axis.crossProduct(UP);
			u = u.lengthSquared() < 1e-6 ? new Vec3d(1, 0, 0) : u.normalize();
			Vec3d v = axis.crossProduct(u).normalize();
			double step = 0.3 / Math.max(0.35f, FxClient.intensity() * FxClient.particleFactor());
			for (double d = 0; d < len; d += step) {
				// El rayo y el viento van en espiral; el resto, recto con ruido.
				double wob = e == Element.LIGHTNING ? 0.3 : (e == Element.WIND ? 0.25 : 0.05);
				double ph = e == Element.LIGHTNING ? rng.nextDouble() * 6.28 : d * 3;
				Vec3d c = start.add(axis.multiply(d)).add(u.multiply(Math.cos(ph) * wob)).add(v.multiply(Math.sin(ph) * wob));
				trailPoint(world, e, c, axis, rng);
			}
			impactAt(world, e, end, rng, 2);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(start), 12);
			if (f > 0) {
				ScreenDynamics.kick(-1.4f * f, 0, 30, 260);
				ScreenDynamics.vignette(e.color(), 0.2f * f, 30, 80, 320, 0f);
				if (e == Element.LIGHTNING || e == Element.EARTH) ScreenDynamics.shake(0.25f * f, 160, 24);
			}
		});
	}

	/** Un salto del rayo: línea en zigzag entre dos puntos. */
	static void arc(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Element e = elementOf(p);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ELEMENT_BOLT);
		Vec3d from = new Vec3d(p.x(), p.y(), p.z());
		Vec3d to = from.add(p.dx(), p.dy(), p.dz());
		FxTimeline.later(delay + 1, () -> {
			double len = from.distanceTo(to);
			if (len < 0.3) return;
			Vec3d axis = to.subtract(from).normalize();
			for (double d = 0; d < len; d += 0.25) {
				Vec3d c = from.add(axis.multiply(d)).add((rng.nextDouble() - 0.5) * 0.4, (rng.nextDouble() - 0.5) * 0.4, (rng.nextDouble() - 0.5) * 0.4);
				trailPoint(world, e, c, axis, rng);
			}
			impactAt(world, e, to, rng, 1);
		});
	}

	// ------------------------------------------------------------------ Estallido

	static void burst(ClientWorld world, FxEventPayload p) {
		Random rng = new Random(p.seed());
		Element e = elementOf(p);
		PlayerAnims.play(FxClient.entity(world, p.entityId()), PlayerAnimIds.ELEMENT_BURST);
		int delay = PlayerAnimIds.IMPACT_TICKS.get(PlayerAnimIds.ELEMENT_BURST);
		FxTimeline.later(delay, () -> {
			Vec3d c = FxClient.entityPos(world, p);
			double radius = Math.max(3, p.dx());
			// Anillo que se abre hasta el radio con la partícula del elemento y, en el borde, su firma.
			FxTimeline.every(8, 1, i -> {
				double r = FxMath.ringRadius(i + 1, 8, radius);
				int pts = FxClient.n(Math.max(14, (int) (r * 7)));
				for (int j = 0; j < pts; j++) {
					double a = Math.PI * 2 * j / pts + rng.nextDouble() * 0.1;
					Vec3d pos = new Vec3d(c.x + Math.cos(a) * r, c.y + 0.2, c.z + Math.sin(a) * r);
					Particle pt = FxClient.spawn(e.particle(), pos.x, pos.y, pos.z, Math.cos(a) * 0.03, 0.04, Math.sin(a) * 0.03);
					if (pt != null) pt.setMaxAge(14);
					if (j % 4 == 0) trailPoint(world, e, pos.add(0, 0.2, 0), UP, rng);
				}
			});
			impactAt(world, e, c.add(0, 1.0, 0), rng, 3);
			float f = FxClient.isLocal(p.entityId()) ? 1f : FxMath.falloff(FxClient.distanceTo(c), radius * 3);
			if (f > 0) {
				ScreenDynamics.shake(0.35f * f, 240, 22);
				ScreenDynamics.fovPunch(-5f * f, 40, 40, 340);
				ScreenDynamics.vignette(e.color(), 0.28f * f, 40, 100, 450, 0f);
				FxClient.flash(e.color(), 0.18f * f);
			}
		});
	}
}
