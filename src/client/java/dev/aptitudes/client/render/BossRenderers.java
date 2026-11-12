package dev.aptitudes.client.render;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.AnimatedBossEntity;
import dev.aptitudes.boss.AutomatonEntity;
import dev.aptitudes.boss.AutomatonOrbEntity;
import dev.aptitudes.boss.BossEntities;
import dev.aptitudes.boss.FacelessColossusEntity;
import dev.aptitudes.boss.GuardianEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * v63 (C4): renderizadores de los jefes animados (GeckoLib). Cada jefe lee
 * {@code geo/entity/<id>.geo.json}, {@code animations/entity/<id>.animation.json} y {@code textures/entity/<id>.png}.
 * La esfera del Autómata no tiene modelo: se ve por sus partículas.
 */
public final class BossRenderers {
	private BossRenderers() {
	}

	public static void register() {
		EntityRendererRegistry.register(BossEntities.FACELESS_COLOSSUS, ctx -> {
			GeoEntityRenderer<FacelessColossusEntity> renderer = new GeoEntityRenderer<>(ctx, new FacelessColossusModel());
			renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(BossEntities.DISMANTLED_GUARDIAN, ctx -> {
			GeoEntityRenderer<GuardianEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("dismantled_guardian"), true));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(BossEntities.ANCIENT_AUTOMATON, ctx -> {
			GeoEntityRenderer<AutomatonEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("ancient_automaton"), true));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(BossEntities.AUTOMATON_ORB,
				ctx -> new FlyingItemEntityRenderer<AutomatonOrbEntity>(ctx, 1.4f, true));
	}

	private static final class FacelessColossusModel extends DefaultedEntityGeoModel<FacelessColossusEntity> {
		private static final Identifier CRACKED_TEXTURE = Aptitudes.id("textures/entity/faceless_colossus_phase2.png");

		private FacelessColossusModel() {
			super(Aptitudes.id("faceless_colossus"), true);
		}

		@Override
		public Identifier getTextureResource(FacelessColossusEntity colossus) {
			return colossus.hasCrackedArmor() ? CRACKED_TEXTURE : super.getTextureResource(colossus);
		}

		@Override
		public void setCustomAnimations(FacelessColossusEntity colossus, long instanceId,
				AnimationState<FacelessColossusEntity> state) {
			var action = colossus.getAnimatableInstanceCache().getManagerForId(instanceId)
					.getAnimationControllers().get(AnimatedBossEntity.ACTION_CONTROLLER);
			if (action == null || !action.isPlayingTriggeredAnimation()) {
				super.setCustomAnimations(colossus, instanceId, state);
			}
		}
	}
}
