package dev.aptitudes.item;

import dev.aptitudes.role.Role;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Pieza de un set de rol: muestra en el tooltip el rol y la bonificación de set completo. */
public class RoleArmorItem extends ArmorItem {
	private final Role role;

	public RoleArmorItem(Role role, RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
		super(material, type, settings);
		this.role = role;
	}

	public Role role() {
		return role;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.armor.role", Text.translatable(role.translationKey()))
				.withColor(role.color()));
		tooltip.add(Text.translatable("item.aptitudes.armor.set_header").formatted(Formatting.GOLD));
		tooltip.add(Text.literal(" ").append(Text.translatable("setbonus.aptitudes." + role.key()))
				.formatted(Formatting.YELLOW));
		tooltip.add(Text.literal(" ").append(Text.translatable("setbonus.aptitudes." + role.key() + ".desc"))
				.formatted(Formatting.GRAY));
		tooltip.add(Text.translatable("item.aptitudes.armor.requires").formatted(Formatting.DARK_GRAY));
	}
}
