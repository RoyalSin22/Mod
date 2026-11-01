package dev.aptitudes.accessory;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Anillo o amuleto. Se equipa en la pantalla de accesorios (tecla Y). */
public class AccessoryItem extends Item {
	private final Accessory accessory;

	public AccessoryItem(Accessory accessory, Settings settings) {
		super(settings.maxCount(1));
		this.accessory = accessory;
	}

	public Accessory accessory() {
		return accessory;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(accessory.ring() ? "item.aptitudes.accessory.ring" : "item.aptitudes.accessory.amulet")
				.formatted(Formatting.GOLD));
		tooltip.add(Text.translatable(accessory.descriptionKey()).withColor(accessory.color()));
		tooltip.add(Text.translatable("item.aptitudes.accessory.hint").formatted(Formatting.DARK_GRAY));
	}
}
