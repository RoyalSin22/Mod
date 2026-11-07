package dev.aptitudes.world;

import dev.aptitudes.Aptitudes;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

/** Registro de los dos formatos de receta y la pantalla de la Mesa del gremio. */
public final class GuildCrafting {
	public static final RecipeType<CraftingRecipe> RECIPE_TYPE = Registry.register(Registries.RECIPE_TYPE,
			Aptitudes.id("guild_crafting"), new RecipeType<CraftingRecipe>() {
				@Override
				public String toString() {
					return Aptitudes.MOD_ID + ":guild_crafting";
				}
			});

	public static final RecipeSerializer<GuildShapedRecipe> SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
			Aptitudes.id("guild_crafting"), new GuildShapedRecipe.Serializer());

	public static final RecipeSerializer<GuildShapelessRecipe> SHAPELESS_SERIALIZER = Registry.register(Registries.RECIPE_SERIALIZER,
			Aptitudes.id("guild_crafting_shapeless"), new GuildShapelessRecipe.Serializer());

	public static final ScreenHandlerType<GuildDeskScreenHandler> SCREEN = Registry.register(Registries.SCREEN_HANDLER,
			Aptitudes.id("guild_desk"), new ScreenHandlerType<>(GuildDeskScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

	private GuildCrafting() {
	}

	/** Fuerza la carga de la clase (y los registros) durante la inicialización. */
	public static void init() {
	}

	/** Resultado retirado de una receta válida en la Mesa del gremio, con ingredientes ya consumidos. */
	public static void onCrafted(net.minecraft.server.network.ServerPlayerEntity player) {
		dev.aptitudes.tutorial.TutorialManager.onStep(player, dev.aptitudes.tutorial.TutorialRules.Step.GUILD_CRAFT);
	}
}
