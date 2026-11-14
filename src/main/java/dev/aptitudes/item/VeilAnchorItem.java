package dev.aptitudes.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Ancla del Velo (T2 de la cadena narrativa): se fabrica en la Mesa del gremio con los restos del Errante Velado y del
 * Osario Quebrado. Todavía no hace nada por sí sola: la puerta de la sala del Custodio que la consume llega en T3.
 */
public class VeilAnchorItem extends Item {
	public VeilAnchorItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.ancla_del_velo.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC));
	}
}
