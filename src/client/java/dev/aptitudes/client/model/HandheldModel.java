package dev.aptitudes.client.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Modelo doble: el icono plano en la interfaz y el volumen 3D en cualquier otro contexto (mano, cabeza, suelo y
 * marcos). Las transformaciones también se combinan: las de la interfaz salen del icono y el resto del modelo 3D.
 *
 * <p>Si el renderizador no admite la API de Fabric (p. ej. un motor sin compatibilidad), {@link #getQuads} devuelve
 * el icono: se ve plano, pero nunca falta el objeto.</p>
 */
final class HandheldModel implements BakedModel, FabricBakedModel {
	private final BakedModel icon;
	private final BakedModel hand;
	private final ModelTransformation transformation;

	HandheldModel(BakedModel icon, BakedModel hand) {
		this.icon = icon;
		this.hand = hand;
		ModelTransformation h = hand.getTransformation();
		ModelTransformation i = icon.getTransformation();
		this.transformation = new ModelTransformation(h.thirdPersonLeftHand, h.thirdPersonRightHand,
				h.firstPersonLeftHand, h.firstPersonRightHand, h.head, i.gui, h.ground, h.fixed);
	}

	private static boolean flat(ModelTransformationMode mode) {
		return mode == ModelTransformationMode.GUI || mode == ModelTransformationMode.NONE;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		BakedModel target = flat(context.itemTransformationMode()) ? icon : hand;
		target.emitItemQuads(stack, randomSupplier, context);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return icon.getQuads(state, face, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return icon.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return hand.hasDepth();
	}

	/** Solo se consulta al dibujar en la interfaz: la iluminación es la del icono plano. */
	@Override
	public boolean isSideLit() {
		return icon.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public Sprite getParticleSprite() {
		return icon.getParticleSprite();
	}

	@Override
	public ModelTransformation getTransformation() {
		return transformation;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}
}
