package dev.aptitudes.client.fx;

import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.logic.CastAnimationRules;
import dev.aptitudes.logic.CastAnimationRules.Kind;
import dev.aptitudes.network.CastAnimationPayload;
import dev.aptitudes.role.RoleCategory;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/** Remates 3D anclados al jugador, con profundidad y sin partículas adicionales del servidor. */
public final class CastAnimations {
	private static final RoleCategory[] CATEGORIES = RoleCategory.values();
	private static final Deque<Cast> ACTIVE = new ArrayDeque<>();
	private static final double TURN = Math.PI * 2;

	private record Cast(PlayerEntity caster, RoleCategory category, Kind kind, int color, float yaw, long start) {}

	private CastAnimations() {}

	public static void receive(MinecraftClient client, CastAnimationPayload payload) {
		if (!ClientConfig.get().castAnimations || client.world == null || !Float.isFinite(payload.yaw())) return;
		int category = payload.category();
		Kind kind = Kind.byIndex(payload.kind());
		if (category < 0 || category >= CATEGORIES.length || kind == null) return;
		if (!(client.world.getEntityById(payload.entityId()) instanceof PlayerEntity caster)
				|| !caster.getUuid().equals(payload.caster()) || !caster.isAlive() || caster.isSpectator()) return;
		if (ACTIVE.size() >= CastAnimationRules.MAX_VISIBLE) ACTIVE.removeFirst();
		ACTIVE.addLast(new Cast(caster, CATEGORIES[category], kind, payload.color() & 0xFFFFFF,
				payload.yaw() % 360f, Util.getMeasuringTimeMs()));
	}

	public static void clear() {
		ACTIVE.clear();
	}

	public static void render(WorldRenderContext context) {
		if (ACTIVE.isEmpty()) return;
		if (!ClientConfig.get().castAnimations) {
			clear();
			return;
		}
		MatrixStack matrices = context.matrixStack();
		VertexConsumerProvider consumers = context.consumers();
		if (matrices == null || consumers == null) return;
		RenderLayer layer = RenderLayer.getDebugQuads();
		VertexConsumer vertices = null;
		PlayerEntity viewer = MinecraftClient.getInstance().player;
		Vec3d camera = context.camera().getPos();
		float tickDelta = context.tickCounter().getTickDelta(false);
		long now = Util.getMeasuringTimeMs();
		for (Iterator<Cast> it = ACTIVE.iterator(); it.hasNext();) {
			Cast cast = it.next();
			var frame = CastAnimationRules.frame(cast.kind(), now - cast.start());
			PlayerEntity caster = cast.caster();
			if (frame.progress() >= 1 || caster.getWorld() != context.world() || !caster.isAlive() || caster.isRemoved()
					|| caster.isSpectator()) {
				it.remove();
				continue;
			}
			Vec3d offset = caster.getLerpedPos(tickDelta).subtract(camera);
			if (frame.opacity() < 0.02f || offset.lengthSquared() > CastAnimationRules.RANGE_SQUARED
					|| (viewer != null && viewer != caster && caster.isInvisibleTo(viewer))) continue;
			if (vertices == null) vertices = consumers.getBuffer(layer);
			matrices.push();
			matrices.translate(offset.x, offset.y, offset.z);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cast.yaw()));
			matrices.scale(frame.scale(), frame.scale(), frame.scale());
			draw(new Mesh(vertices, matrices.peek().getPositionMatrix(), cast.color(), frame.opacity()),
					cast.category(), frame.progress());
			matrices.pop();
		}
		if (vertices != null && consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw(layer);
	}

	private static void draw(Mesh mesh, RoleCategory category, float progress) {
		switch (category) {
			case MARTIAL -> slash(mesh, progress);
			case ARCANE -> orbit(mesh, progress);
			case SHADOW -> shards(mesh, progress);
			case GUARDIAN -> shields(mesh, progress);
			case CRAFT -> gear(mesh, progress);
		}
	}

	/** Hoja curva delante del jugador, inclinada y afilada en los extremos. */
	private static void slash(Mesh mesh, float progress) {
		double start = -1.62 + progress * 1.8;
		int segments = 16;
		for (int i = 0; i < segments; i++) {
			double a = start + 1.64 * i / segments;
			double b = start + 1.64 * (i + 1) / segments;
			double aw = 0.05 + 0.41 * Math.sin(Math.PI * i / segments);
			double bw = 0.05 + 0.41 * Math.sin(Math.PI * (i + 1) / segments);
			double ay = 0.91 + 0.21 * a;
			double by = 0.91 + 0.21 * b;
			Vec3d edgeA = polar(a, 1.62, ay);
			Vec3d edgeB = polar(b, 1.62, by);
			Vec3d bevelA = polar(a, 1.59 - aw * 0.22, ay + 0.07);
			Vec3d bevelB = polar(b, 1.59 - bw * 0.22, by + 0.07);
			mesh.quad(edgeA, edgeB, bevelB, bevelA, 0.65f, 0.92f);
			mesh.quad(bevelA, bevelB, polar(b, 1.62 - bw, by + 0.03), polar(a, 1.62 - aw, ay + 0.03),
					0.18f, 0.5f);
		}
	}

	private static void orbit(Mesh mesh, float progress) {
		for (int i = 0; i < 3; i++) {
			double angle = TURN * (progress * 0.8 + i / 3.0);
			Vec3d center = polar(angle, 0.93, 0.63 + progress * 0.88 + Math.sin(angle) * 0.12);
			crystal(mesh, center, 0.19, 0.29, angle + progress * TURN, 0.82f);
		}
	}

	private static void shards(Mesh mesh, float progress) {
		for (int i = -1; i <= 1; i++) {
			double angle = Math.PI + i * (0.55 + progress * 0.48);
			Vec3d center = polar(angle, 0.94 + progress * 0.73, 0.68 + 0.22 * (1 - Math.abs(i)) + progress * 0.27);
			crystal(mesh, center, 0.13, 0.42 - 0.05 * Math.abs(i), angle + progress * 1.1, 0.67f);
		}
	}

	private static void crystal(Mesh mesh, Vec3d center, double radius, double height, double angle, float opacity) {
		Vec3d top = center.add(0, height, 0);
		Vec3d bottom = center.add(0, -height, 0);
		for (int i = 0; i < 4; i++) {
			Vec3d a = center.add(polar(angle + i * TURN / 4, radius, 0));
			Vec3d b = center.add(polar(angle + (i + 1) * TURN / 4, radius, 0));
			float light = 0.32f + 0.15f * i;
			mesh.triangle(top, a, b, light, opacity);
			mesh.triangle(bottom, b, a, light * 0.38f, opacity * 0.8f);
		}
	}

	/** Placas facetadas que se abren alrededor del jugador. */
	private static void shields(Mesh mesh, float progress) {
		double radius = 0.85 + 0.5 * progress;
		for (int i = 0; i < 6; i++) {
			double angle = TURN * i / 6 + progress * 0.45;
			Vec3d top = polar(angle, radius, 1.30);
			Vec3d left = polar(angle - 0.26, radius, 1.10);
			Vec3d right = polar(angle + 0.26, radius, 1.10);
			Vec3d bottom = polar(angle, radius + 0.13, 0.52);
			Vec3d center = polar(angle, radius + 0.19, 0.99);
			mesh.triangle(top, left, center, 0.66f, 0.64f);
			mesh.triangle(top, center, right, 0.82f, 0.66f);
			mesh.triangle(bottom, center, left, 0.17f, 0.58f);
			mesh.triangle(bottom, right, center, 0.36f, 0.62f);
		}
	}

	private static void gear(Mesh mesh, float progress) {
		double rotation = progress * TURN * 0.7;
		double y = 0.26 + progress * 0.64;
		for (int i = 0; i < 32; i++) {
			double a = rotation + TURN * i / 32;
			double b = rotation + TURN * (i + 1) / 32;
			Vec3d outerA = polar(a, gearRadius(i), y);
			Vec3d outerB = polar(b, gearRadius(i + 1), y);
			mesh.quad(polar(a, 0.79, y + 0.06), outerA, outerB, polar(b, 0.79, y + 0.06), 0.47f, 0.72f);
			mesh.quad(outerA, outerB, outerB.add(0, -0.085, 0), outerA.add(0, -0.085, 0), 0.05f, 0.68f);
		}
	}

	private static double gearRadius(int step) {
		int tooth = step % 4;
		return tooth == 1 || tooth == 2 ? 1.18 : 0.98;
	}

	private static Vec3d polar(double angle, double radius, double y) {
		return new Vec3d(Math.sin(angle) * radius, y, Math.cos(angle) * radius);
	}

	private record Mesh(VertexConsumer vertices, Matrix4f matrix, int color, float opacity) {
		void quad(Vec3d a, Vec3d b, Vec3d c, Vec3d d, float light, float faceOpacity) {
			int shade = shade(light, faceOpacity);
			vertex(a, shade);
			vertex(b, shade);
			vertex(c, shade);
			vertex(d, shade);
		}

		// El formato de la capa es QUADS. Repetir el último vértice forma una cara triangular.
		void triangle(Vec3d a, Vec3d b, Vec3d c, float light, float faceOpacity) {
			quad(a, b, c, c, light, faceOpacity);
		}

		private int shade(float light, float faceOpacity) {
			int alpha = (int) (187 * opacity * faceOpacity);
			int r = tint(color >> 16 & 255, light);
			int g = tint(color >> 8 & 255, light);
			int b = tint(color & 255, light);
			return alpha << 24 | r << 16 | g << 8 | b;
		}

		private static int tint(int channel, float light) {
			return channel + (int) ((255 - channel) * light);
		}

		private void vertex(Vec3d point, int shade) {
			vertices.vertex(matrix, (float) point.x, (float) point.y, (float) point.z).color(shade);
		}
	}
}
