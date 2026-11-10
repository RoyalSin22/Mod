package dev.aptitudes.client.render;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.mob.CustodioEntity;
import dev.aptitudes.mob.ErranteEntity;
import dev.aptitudes.mob.NewMobs;
import dev.aptitudes.mob.OsarioEntities;
import dev.aptitudes.mob.OsarioEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Renderizadores de los enemigos nuevos (v65), con modelo GeckoLib. */
public final class MobRenderers {
	private MobRenderers() {}

	public static void register() {
		EntityRendererRegistry.register(NewMobs.ESCORIFICADOR, ctx -> {
			GeoEntityRenderer<dev.aptitudes.mob.EscorificadorEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("escorificador_hierro")));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(NewMobs.ERRANTE, ctx -> {
			GeoEntityRenderer<ErranteEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("errante_velado")));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(NewMobs.CUSTODIO, ctx -> {
			GeoEntityRenderer<CustodioEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("custodio_cuatro_llaves")));
			renderer.withScale(1.0f);
			return renderer;
		});
		EntityRendererRegistry.register(OsarioEntities.OSARIO, ctx -> {
			GeoEntityRenderer<OsarioEntity> renderer = new GeoEntityRenderer<>(ctx,
					new DefaultedEntityGeoModel<>(Aptitudes.id("osario_quebrado")));
			renderer.withScale(1.0f);
			return renderer;
		});
	}
}
