package dev.aptitudes.client.render;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.mob.OsarioEntities;
import dev.aptitudes.mob.OsarioEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Renderizadores de los enemigos nuevos (v65), con modelo GeckoLib. */
public final class MobRenderers {
	private MobRenderers() {}

	public static void register() {
		EntityRendererRegistry.register(OsarioEntities.OSARIO, ctx -> {
			GeoEntityRenderer<OsarioEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("osario_quebrado")));
			renderer.withScale(1.0f);
			return renderer;
		});
	}
}
