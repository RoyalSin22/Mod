package dev.aptitudes.client.render;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.mob.OsarioEntity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class OsarioRenderer extends GeoEntityRenderer<OsarioEntity> {
	private static final String CORE = "chest_core";
	private static VertexConsumerProvider worldBuffers;
	private static VertexConsumerProvider.Immediate glowBuffers;
	private static BufferAllocator glowAllocator;

	private final Set<GeoBone> coreChain = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<GeoBone, List<Face>> facesByBone = new IdentityHashMap<>();
	private final Vector4f transformedVertex = new Vector4f();
	private final Vector3f transformedNormal = new Vector3f();
	private BakedGeoModel cachedModel;
	private boolean emitting;

	OsarioRenderer(EntityRendererFactory.Context context) {
		super(context, new DefaultedEntityGeoModel<>(Aptitudes.id("osario_quebrado")));
		addRenderLayer(new AutoGlowingGeoLayer<OsarioEntity>(this) {
			@Override
			public void render(MatrixStack matrices, OsarioEntity entity, BakedGeoModel model, RenderLayer layer,
							   VertexConsumerProvider buffers, VertexConsumer vertices, float tickDelta, int light, int overlay) {
				emitting = true;
				try {
					var glow = buffers == worldBuffers && !entity.isInvisible() && !MinecraftClient.getInstance().hasOutline(entity)
							? worldGlowBuffers() : buffers;
					super.render(matrices, entity, model, layer, glow, vertices, tickDelta, light, overlay);
				} finally {
					emitting = false;
				}
			}
		});
	}

	static void registerWorldBatch() {
		WorldRenderEvents.BEFORE_ENTITIES.register(context -> worldBuffers = context.consumers());
		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			worldBuffers = null;
			if (glowBuffers != null) glowBuffers.draw();
		});
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			if (glowAllocator != null) glowAllocator.close();
		});
	}

	private static VertexConsumerProvider.Immediate worldGlowBuffers() {
		if (glowBuffers == null) {
			glowAllocator = new BufferAllocator(32 * 1024);
			glowBuffers = VertexConsumerProvider.immediate(glowAllocator);
		}
		return glowBuffers;
	}

	@Override
	protected float getDeathMaxRotation(OsarioEntity entity) {
		return 0;
	}

	@Override
	public void preRender(MatrixStack matrices, OsarioEntity entity, BakedGeoModel model,
						  VertexConsumerProvider buffers, VertexConsumer vertices, boolean isReRender,
						  float tickDelta, int light, int overlay, int colour) {
		if (model != cachedModel) {
			cachedModel = model;
			facesByBone.clear();
			coreChain.clear();
			for (GeoBone bone = model.getBone(CORE).orElse(null); bone != null; bone = bone.getParent()) {
				coreChain.add(bone);
			}
		}
		super.preRender(matrices, entity, model, buffers, vertices, isReRender, tickDelta, light, overlay, colour);
	}

	@Override
	public void renderRecursively(MatrixStack matrices, OsarioEntity entity, GeoBone bone, RenderLayer layer,
								  VertexConsumerProvider buffers, VertexConsumer vertices, boolean isReRender,
								  float tickDelta, int light, int overlay, int colour) {
		// La máscara solo contiene texeles de chest_core. Sus ancestros aportan su transformación.
		if (emitting && !coreChain.contains(bone)) return;
		super.renderRecursively(matrices, entity, bone, layer, buffers, vertices, isReRender, tickDelta, light, overlay, colour);
	}

	@Override
	public void renderCubesOfBone(MatrixStack matrices, GeoBone bone, VertexConsumer vertices, int light, int overlay, int colour) {
		if (bone.isHidden() || (emitting && !CORE.equals(bone.getName()))) return;
		var transform = matrices.peek();
		for (Face face : facesByBone.computeIfAbsent(bone, OsarioRenderer::bakeFaces)) {
			transform.getNormalMatrix().transform(transformedNormal.set(face.normal));
			if (face.flatCube != null) RenderUtil.fixInvertedFlatCube(face.flatCube, transformedNormal);
			for (Point point : face.points) {
				transform.getPositionMatrix().transform(transformedVertex.set(point.x, point.y, point.z, 1));
				vertices.vertex(transformedVertex.x, transformedVertex.y, transformedVertex.z, colour, point.u,
						point.v, overlay, light, transformedNormal.x, transformedNormal.y, transformedNormal.z);
			}
		}
	}

	// Los cubos tienen transformaciones locales fijas. La matriz animada del hueso se aplica en cada frame.
	private static List<Face> bakeFaces(GeoBone bone) {
		var faces = new ArrayList<Face>();
		var local = new MatrixStack();
		var vertex = new Vector4f();
		for (GeoCube cube : bone.getCubes()) {
			local.push();
			RenderUtil.translateToPivotPoint(local, cube);
			RenderUtil.rotateMatrixAroundCube(local, cube);
			RenderUtil.translateAwayFromPivotPoint(local, cube);
			var transform = local.peek();
			boolean flat = cube.size().x == 0 || cube.size().y == 0 || cube.size().z == 0;
			for (var quad : cube.quads()) {
				if (quad == null) continue;
				var normal = transform.getNormalMatrix().transform(new Vector3f(quad.normal()));
				var source = quad.vertices();
				var points = new Point[source.length];
				for (int i = 0; i < source.length; i++) {
					var position = source[i].position();
					transform.getPositionMatrix().transform(vertex.set(position.x(), position.y(), position.z(), 1));
					points[i] = new Point(vertex.x, vertex.y, vertex.z, source[i].texU(), source[i].texV());
				}
				faces.add(new Face(normal, points, flat ? cube : null));
			}
			local.pop();
		}
		return faces;
	}

	private record Point(float x, float y, float z, float u, float v) {}

	private record Face(Vector3f normal, Point[] points, GeoCube flatCube) {}
}
