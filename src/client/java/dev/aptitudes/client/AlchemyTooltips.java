package dev.aptitudes.client;

import dev.aptitudes.alchemy.AlchemyBrewing;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Evita mostrar dos duraciones y dos bonificaciones para un mismo efecto prolongado. */
public final class AlchemyTooltips {
	private AlchemyTooltips() {
	}

	public static void adjust(ItemStack stack, Item.TooltipContext context, List<Text> lines) {
		if (!stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).contains(AlchemyBrewing.BOOSTED_KEY)) return;
		PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
		if (contents == null) return;
		float scale = stack.isOf(Items.LINGERING_POTION) ? 0.25f : stack.isOf(Items.TIPPED_ARROW) ? 0.125f : 1;
		List<Text> original = new ArrayList<>();
		contents.buildTooltip(original::add, scale, context.getUpdateTickRate());
		int start = Collections.indexOfSubList(lines, original);
		if (start < 0 || original.isEmpty()) return; // respeta tooltips ocultos o modificados por otro mod
		Map<RegistryEntry<StatusEffect>, StatusEffectInstance> effective = new LinkedHashMap<>();
		for (StatusEffectInstance effect : contents.getEffects()) {
			StatusEffectInstance old = effective.get(effect.getEffectType());
			if (old == null || effect.getAmplifier() > old.getAmplifier()
					|| (effect.getAmplifier() == old.getAmplifier() && effect.getDuration() > old.getDuration())) {
				effective.put(effect.getEffectType(), effect);
			}
		}
		List<Text> replacement = new ArrayList<>();
		PotionContentsComponent.buildTooltip(effective.values(), replacement::add, scale, context.getUpdateTickRate());
		lines.subList(start, start + original.size()).clear();
		lines.addAll(start, replacement);
	}
}
