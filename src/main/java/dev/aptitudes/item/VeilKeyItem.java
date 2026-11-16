package dev.aptitudes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * V69: Llave del Velo. Se fabrica en la Mesa del gremio con el Cráneo del Gran Osario, Jirón de Velo Espectral y
 * fragmentos de alma, y abre una sola vez la trampilla de la Cripta de la Niebla (se consume). Sin función fuera de ahí.
 */
public class VeilKeyItem extends Item {
	public VeilKeyItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.llave_del_velo.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC));
	}
}
