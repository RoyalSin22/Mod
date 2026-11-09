package dev.aptitudes.souls;

import dev.aptitudes.boss.BossCoreItem;
import dev.aptitudes.item.SoulFlaskItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

/** La mejora viaja con el objeto. La durabilidad sigue contando usos gastados. */
public final class FlaskUpgrades {
	private static final String RANK = "aptitudes_flask_rank";

	private FlaskUpgrades() {}

	public static boolean isFlask(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof SoulFlaskItem;
	}

	public static boolean isCore(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof BossCoreItem;
	}

	public static int rank(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component == null || !component.contains(RANK)) return 0;
		NbtCompound nbt = component.copyNbt();
		return nbt.contains(RANK, NbtElement.INT_TYPE) ? FlaskRules.rank(nbt.getInt(RANK)) : 0;
	}

	static ItemStack upgraded(ItemStack original, FlaskRules.Upgrade upgrade) {
		ItemStack result = original.copy();
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, result, nbt -> nbt.putInt(RANK, upgrade.rank()));
		int previousCapacity = FlaskRules.capacity(rank(original));
		result.set(DataComponentTypes.MAX_DAMAGE, FlaskRules.capacity(upgrade.rank()));
		result.setDamage(Math.clamp(original.getDamage(), 0, previousCapacity));
		return result;
	}
}
