package dev.aptitudes.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/** Usa las mismas reglas y formato de Minecraft para recetas sin forma, en el tipo del gremio. */
public record GuildShapelessRecipe(ShapelessRecipe vanilla) implements CraftingRecipe {
	@Override
	public RecipeType<?> getType() {
		return GuildCrafting.RECIPE_TYPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GuildCrafting.SHAPELESS_SERIALIZER;
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

	@Override
	public String getGroup() {
		return vanilla.getGroup();
	}

	@Override
	public CraftingRecipeCategory getCategory() {
		return vanilla.getCategory();
	}

	@Override
	public boolean matches(CraftingRecipeInput input, World world) {
		return vanilla.matches(input, world);
	}

	@Override
	public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries) {
		return vanilla.craft(input, registries);
	}

	@Override
	public boolean fits(int width, int height) {
		return vanilla.fits(width, height);
	}

	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registries) {
		return vanilla.getResult(registries);
	}

	@Override
	public DefaultedList<Ingredient> getIngredients() {
		return vanilla.getIngredients();
	}

	public static final class Serializer implements RecipeSerializer<GuildShapelessRecipe> {
		private static final MapCodec<GuildShapelessRecipe> CODEC =
				RecipeSerializer.SHAPELESS.codec().xmap(GuildShapelessRecipe::new, GuildShapelessRecipe::vanilla);
		private static final PacketCodec<RegistryByteBuf, GuildShapelessRecipe> PACKET_CODEC =
				RecipeSerializer.SHAPELESS.packetCodec().xmap(GuildShapelessRecipe::new, GuildShapelessRecipe::vanilla);

		@Override
		public MapCodec<GuildShapelessRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, GuildShapelessRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
