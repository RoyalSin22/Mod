package dev.aptitudes.temple;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** Llave de un templo concreto (v64): la suelta su guardián y solo abre el relicario de ese templo. */
public class TempleKeyItem extends Item {
	private static final String TAG = "aptitudes_temple";

	public TempleKeyItem(Settings settings) {
		super(settings);
	}

	public static ItemStack forTemple(UUID temple) {
		ItemStack stack = new ItemStack(Temples.TEMPLE_KEY);
		NbtCompound nbt = new NbtCompound();
		nbt.putUuid(TAG, temple);
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		return stack;
	}

	@Nullable
	public static UUID templeOf(ItemStack stack) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null) return null;
		NbtCompound nbt = data.copyNbt();
		return nbt.containsUuid(TAG) ? nbt.getUuid(TAG) : null;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable(templeOf(stack) == null ? "item.aptitudes.temple_key.blank" : "item.aptitudes.temple_key.tooltip")
				.formatted(Formatting.GRAY));
	}
}
