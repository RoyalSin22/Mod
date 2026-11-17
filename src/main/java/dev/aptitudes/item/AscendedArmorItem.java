package dev.aptitudes.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Pieza de la Armadura Ascendida: el tooltip explica el bonus común y el aspecto que depende del rol principal. */
public class AscendedArmorItem extends ArmorItem {
	public AscendedArmorItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
		super(material, type, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.ascended.set_header").formatted(Formatting.GOLD));
		tooltip.add(Text.literal(" ").append(Text.translatable("item.aptitudes.ascended.common")).formatted(Formatting.YELLOW));
		tooltip.add(Text.literal(" ").append(Text.translatable("item.aptitudes.ascended.aspect")).formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.aptitudes.ascended.requires").formatted(Formatting.DARK_GRAY));
	}
}
