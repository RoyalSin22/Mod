package dev.aptitudes.client.model;

import dev.aptitudes.Aptitudes;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * v62 (K4, A1): armas con modelo 3D en la mano y su icono 2D en el inventario.
 *
 * <p>Por cada {@code models/item/<arma>_3d.json} (generados por {@code tools/models/gen_weapon_models.py}) se carga
 * ese modelo extra y el modelo normal del objeto se envuelve en {@link HandheldModel}. Descubrir los archivos en
 * cada recarga permite que un paquete de recursos añada o quite modelos sin tocar código.</p>
 */
public final class HandheldModels {
	private static final String SUFFIX = "_3d";

	private HandheldModels() {
	}

	public static void register() {
		PreparableModelLoadingPlugin.register(HandheldModels::discover, (names, context) -> {
			for (String name : names) context.addModels(Aptitudes.id("item/" + name + SUFFIX));
			context.modifyModelAfterBake().register((model, ctx) -> {
				ModelIdentifier top = ctx.topLevelId();
				if (model == null || top == null || !"inventory".equals(top.variant())
						|| !Aptitudes.MOD_ID.equals(top.id().getNamespace()) || !names.contains(top.id().getPath())) {
					return model;
				}
				BakedModel hand = ctx.baker().bake(Aptitudes.id("item/" + top.id().getPath() + SUFFIX), ctx.settings());
				return hand == null ? model : new HandheldModel(model, hand);
			});
		});
	}

	/** Nombres de objeto con modelo 3D, leídos de los recursos en el hilo de carga. */
	private static CompletableFuture<Set<String>> discover(ResourceManager manager, java.util.concurrent.Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Set<String> names = new HashSet<>();
			manager.findResources("models/item", id -> id.getNamespace().equals(Aptitudes.MOD_ID)
					&& id.getPath().endsWith(SUFFIX + ".json")).keySet().forEach((Identifier id) -> {
				String file = id.getPath().substring("models/item/".length());
				names.add(file.substring(0, file.length() - (SUFFIX + ".json").length()));
			});
			return Set.copyOf(names);
		}, executor);
	}
}
