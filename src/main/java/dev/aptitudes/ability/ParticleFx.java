package dev.aptitudes.ability;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Partículas con forma (en el servidor, visibles para todos): círculos, círculos
 * rúnicos con estrella, espirales, columnas y ondas expansivas. Cada punto es una
 * partícula sin dispersión, así las figuras se ven nítidas.
 */
public final class ParticleFx {
	private ParticleFx() {
	}

	/** Polvo de color (RGB 0xRRGGBB). */
	public static DustParticleEffect dust(int rgb, float size) {
		return new DustParticleEffect(new Vector3f((rgb >> 16 & 0xFF) / 255f, (rgb >> 8 & 0xFF) / 255f, (rgb & 0xFF) / 255f),
				size);
	}

	private static void point(ServerWorld world, ParticleEffect particle, double x, double y, double z) {
		world.spawnParticles(particle, x, y, z, 1, 0, 0, 0, 0);
	}

	public static void ring(ServerWorld world, Vec3d center, double radius, ParticleEffect particle, int points) {
		for (int i = 0; i < points; i++) {
			double a = 2 * Math.PI * i / points;
			point(world, particle, center.x + Math.cos(a) * radius, center.y, center.z + Math.sin(a) * radius);
		}
	}

	/** Dos círculos concéntricos y una estrella de 5 puntas: el "círculo rúnico" de los lanzamientos. */
	public static void runeCircle(ServerWorld world, Vec3d center, double radius, ParticleEffect particle) {
		Vec3d c = center.add(0, 0.05, 0);
		int points = (int) Math.max(16, radius * 18);
		ring(world, c, radius, particle, points);
		ring(world, c, radius * 0.75, particle, (int) (points * 0.75));
		double inner = radius * 0.75;
		for (int i = 0; i < 5; i++) {
			double a1 = 2 * Math.PI * i / 5 - Math.PI / 2;
			double a2 = 2 * Math.PI * ((i + 2) % 5) / 5 - Math.PI / 2;
			Vec3d p1 = c.add(Math.cos(a1) * inner, 0, Math.sin(a1) * inner);
			Vec3d p2 = c.add(Math.cos(a2) * inner, 0, Math.sin(a2) * inner);
			line(world, particle, p1, p2, (int) Math.max(6, inner * 8));
		}
	}

	public static void spiral(ServerWorld world, Vec3d base, double radius, double height, ParticleEffect particle,
							  int points, double turns) {
		for (int i = 0; i < points; i++) {
			double t = i / (double) points;
			double a = 2 * Math.PI * turns * t;
			point(world, particle, base.x + Math.cos(a) * radius, base.y + t * height, base.z + Math.sin(a) * radius);
		}
	}

	public static void column(ServerWorld world, Vec3d base, double height, ParticleEffect particle, int points) {
		for (int i = 0; i < points; i++) point(world, particle, base.x, base.y + height * i / points, base.z);
	}

	public static void line(ServerWorld world, ParticleEffect particle, Vec3d from, Vec3d to, int points) {
		Vec3d delta = to.subtract(from);
		for (int i = 0; i <= points; i++) {
			Vec3d p = from.add(delta.multiply(i / (double) Math.max(1, points)));
			point(world, particle, p.x, p.y, p.z);
		}
	}

	/** Onda expansiva: anillos que crecen hasta {@code maxRadius} durante {@code steps} ticks. */
	public static void shockwave(MinecraftServer server, ServerWorld world, Vec3d center, double maxRadius,
								 ParticleEffect particle, int steps) {
		AbilityState.repeat(server, steps, 1, i -> {
			double r = maxRadius * (i + 1) / steps;
			ring(world, center.add(0, 0.15, 0), r, particle, (int) Math.max(12, r * 10));
		});
	}
}
