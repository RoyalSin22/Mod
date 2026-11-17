package dev.aptitudes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Plantilla de ascensión (V80): se usa en la mesa de herrería para mejorar una pieza de netherita. */
public class AscendedTemplateItem extends Item {
	public AscendedTemplateItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.ascended_template.tooltip").formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.aptitudes.ascended_template.recipe").formatted(Formatting.DARK_GRAY));
	}
}
