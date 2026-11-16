package dev.aptitudes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * V68: objeto de material narrativo con una línea de descripción (no tiene uso propio). Lo usa el Cráneo del Gran
 * Osario, que se guarda para la receta futura de la mazmorra del Custodio.
 */
public final class TrophyItem extends Item {
	private final String tooltipKey;

	public TrophyItem(String tooltipKey, Settings settings) {
		super(settings);
		this.tooltipKey = tooltipKey;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(tooltipKey).formatted(Formatting.GRAY));
	}
}
