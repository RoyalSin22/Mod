package dev.aptitudes.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;

/**
 * Receta con forma que SOLO se fabrica en la Mesa del gremio (v27): mismo JSON que una receta con forma
 * vanilla, pero con {@code "type": "aptitudes:guild_crafting"}. Como su tipo no es {@code CRAFTING}, la mesa
 * de trabajo no la reconoce. Hereda de {@link ShapedRecipe} para reutilizar la comprobación de la forma y
 * para que el grimorio la dibuje en su cuadrícula.
 */
public class GuildShapedRecipe extends ShapedRecipe {
	private final RawShapedRecipe pattern;
	private final ItemStack output;

	public GuildShapedRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe pattern, ItemStack output,
							 boolean showNotification) {
		super(group, category, pattern, output, showNotification);
		this.pattern = pattern;
		this.output = output;
	}

	@Override
	public RecipeType<?> getType() {
		return GuildCrafting.RECIPE_TYPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return GuildCrafting.SERIALIZER;
	}

	/** Fuera del libro de recetas vanilla (evita avisos de «categoría desconocida» en el cliente). */
	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

	public static final class Serializer implements RecipeSerializer<GuildShapedRecipe> {
		private static final MapCodec<GuildShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(GuildShapedRecipe::getGroup),
				CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.EQUIPMENT)
						.forGetter(GuildShapedRecipe::getCategory),
				RawShapedRecipe.CODEC.forGetter(r -> r.pattern),
				ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(r -> r.output),
				Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(GuildShapedRecipe::showNotification)
		).apply(i, GuildShapedRecipe::new));

		private static final PacketCodec<RegistryByteBuf, GuildShapedRecipe> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.STRING, GuildShapedRecipe::getGroup,
				CraftingRecipeCategory.PACKET_CODEC, GuildShapedRecipe::getCategory,
				RawShapedRecipe.PACKET_CODEC, r -> r.pattern,
				ItemStack.PACKET_CODEC, r -> r.output,
				PacketCodecs.BOOL, GuildShapedRecipe::showNotification,
				GuildShapedRecipe::new);

		@Override
		public MapCodec<GuildShapedRecipe> codec() {
			return CODEC;
		}

		@Override
		public PacketCodec<RegistryByteBuf, GuildShapedRecipe> packetCodec() {
			return PACKET_CODEC;
		}
	}
}
