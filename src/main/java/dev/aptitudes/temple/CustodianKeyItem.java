package dev.aptitudes.temple;

import dev.aptitudes.temple.TempleKeyRules.Metal;
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

/**
 * Llave del Custodio (T4): hay cuatro, una por metal (cobre, hierro, oro y diamante). Cada una abre la cámara de su metal
 * en el Templo del Custodio del que procede (el identificador del templo viaja en el propio objeto).
 */
public class CustodianKeyItem extends Item {
	private static final String TAG = "aptitudes_custodian_key_temple";

	private final Metal metal;

	public CustodianKeyItem(Metal metal, Settings settings) {
		super(settings);
		this.metal = metal;
	}

	public Metal metal() {
		return metal;
	}

	/** Llave de ese metal ligada a un templo. */
	public static ItemStack forTemple(Metal metal, UUID temple) {
		ItemStack stack = new ItemStack(Temples.keyItem(metal));
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
		tooltip.add(Text.translatable(templeOf(stack) == null ? "item.aptitudes.custodian_key.blank" : "item.aptitudes.custodian_key.tooltip")
				.formatted(Formatting.GRAY));
	}
}
