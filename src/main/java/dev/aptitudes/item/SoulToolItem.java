package dev.aptitudes.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Marca común de las herramientas de alma (la lógica está en {@code SoulTools}). */
public interface SoulToolItem {
	String tooltipKey();

	static void tooltip(SoulToolItem tool, List<Text> tooltip) {
		tooltip.add(Text.translatable(tool.tooltipKey()).formatted(Formatting.LIGHT_PURPLE));
	}

	final class Pickaxe extends net.minecraft.item.PickaxeItem implements SoulToolItem {
		public Pickaxe(Settings settings) {
			super(net.minecraft.item.ToolMaterials.DIAMOND, settings);
		}

		@Override
		public String tooltipKey() {
			return "item.aptitudes.soul_pickaxe.tooltip";
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			SoulToolItem.tooltip(this, tooltip);
		}
	}

	final class Shovel extends net.minecraft.item.ShovelItem implements SoulToolItem {
		public Shovel(Settings settings) {
			super(net.minecraft.item.ToolMaterials.DIAMOND, settings);
		}

		@Override
		public String tooltipKey() {
			return "item.aptitudes.soul_shovel.tooltip";
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			SoulToolItem.tooltip(this, tooltip);
		}
	}

	final class Axe extends net.minecraft.item.AxeItem implements SoulToolItem {
		public Axe(Settings settings) {
			super(net.minecraft.item.ToolMaterials.DIAMOND, settings);
		}

		@Override
		public String tooltipKey() {
			return "item.aptitudes.soul_axe.tooltip";
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			SoulToolItem.tooltip(this, tooltip);
		}
	}

	final class Hoe extends net.minecraft.item.HoeItem implements SoulToolItem {
		public Hoe(Settings settings) {
			super(net.minecraft.item.ToolMaterials.DIAMOND, settings);
		}

		@Override
		public String tooltipKey() {
			return "item.aptitudes.soul_hoe.tooltip";
		}

		@Override
		public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
			SoulToolItem.tooltip(this, tooltip);
		}
	}
}
