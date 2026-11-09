package dev.aptitudes.client.render;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.boss.BossEntities;
import dev.aptitudes.boss.FacelessColossusEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * v63 (C4): renderizadores GeckoLib de los jefes animados. Cada jefe lee
 * {@code geo/entity/<id>.geo.json}, {@code animations/entity/<id>.animation.json} y {@code textures/entity/<id>.png}.
 */
public final class BossRenderers {
	private BossRenderers() {
	}

	public static void register() {
		EntityRendererRegistry.register(BossEntities.FACELESS_COLOSSUS, ctx -> {
			GeoEntityRenderer<FacelessColossusEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("faceless_colossus"), true));
			renderer.withScale(1.0f);
			return renderer;
		});
	}
}
