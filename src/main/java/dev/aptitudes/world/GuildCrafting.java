package dev.aptitudes.world;

import dev.aptitudes.Aptitudes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

/** Registro de la fabricación en la Mesa del gremio (v27): tipo de receta, serializador y pantalla. */
public final class GuildCrafting {
	public static final RecipeType<GuildShapedRecipe> RECIPE_TYPE = Registry.register(Registries.RECIPE_TYPE,
			Aptitudes.id("guild_crafting"), new RecipeType<GuildShapedRecipe>() {
				@Override
				public String toString() {
					return Aptitudes.MOD_ID + ":guild_crafting";
				}
			});

	public static final RecipeSerializer<GuildShapedRecipe> SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
			Aptitudes.id("guild_crafting"), new GuildShapedRecipe.Serializer());

	public static final ScreenHandlerType<GuildDeskScreenHandler> SCREEN = Registry.register(Registries.SCREEN_HANDLER,
			Aptitudes.id("guild_desk"), new ScreenHandlerType<>(GuildDeskScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	private GuildCrafting() {
	}

	/** Fuerza la carga de la clase (y los registros) durante la inicialización. */
	public static void init() {
	}
}
