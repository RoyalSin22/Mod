package dev.aptitudes.alchemy;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

/** Cuatro pociones de 1.21.1; las transformaciones arrojadizas y persistentes siguen siendo vanilla. */
public final class AlchemyPotions {
	public static final RegistryEntry<Potion> MANA = register("mana",
			new StatusEffectInstance(AlchemyEffects.MANA, AlchemyRules.MANA_TICKS));
	public static final RegistryEntry<Potion> FRENZY = register("frenzy",
			new StatusEffectInstance(AlchemyEffects.FRENZY, AlchemyRules.FRENZY_TICKS));
	public static final RegistryEntry<Potion> ECLIPSE_VEIL = register("eclipse_veil",
			new StatusEffectInstance(AlchemyEffects.ECLIPSE_VEIL, AlchemyRules.ECLIPSE_VEIL_TICKS));
	public static final RegistryEntry<Potion> FOREST_VIGOR = register("forest_vigor",
			new StatusEffectInstance(StatusEffects.REGENERATION, AlchemyRules.FOREST_VIGOR_TICKS),
			new StatusEffectInstance(StatusEffects.SATURATION, 1));

	private AlchemyPotions() {
	}

	private static RegistryEntry<Potion> register(String name, StatusEffectInstance... effects) {
		return Registry.registerReference(Registries.POTION, Aptitudes.id(name), new Potion("aptitudes_" + name, effects));
	}

	public static List<RegistryEntry<Potion>> all() {
		return List.of(MANA, FRENZY, ECLIPSE_VEIL, FOREST_VIGOR);
	}

	public static void init() {
		FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
			builder.registerPotionRecipe(Potions.AWKWARD, ModItems.HERB_PETALS, MANA);
			builder.registerPotionRecipe(Potions.AWKWARD, ModItems.ELITE_ESSENCE, FRENZY);
			builder.registerPotionRecipe(Potions.AWKWARD, ModItems.ECLIPSE_FRAGMENT, ECLIPSE_VEIL);
			builder.registerPotionRecipe(MANA, ModItems.HERB_PETALS, FOREST_VIGOR);
		});
	}
}
