package dev.aptitudes.forge;

import dev.aptitudes.item.weapon.RoleBladeItem;
import dev.aptitudes.item.weapon.RoleFocusItem;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** El afijo se guarda en el arma y viaja con ella. */
public final class ForgeWeapons {
	private static final String KEY = "aptitudes_forge";
	private static final String PROFILE = "profile";
	private static final String RANK = "temper";

	private ForgeWeapons() {
	}

	public static boolean isWeapon(ItemStack stack) {
		return stack.getItem() instanceof RoleBladeItem || stack.getItem() instanceof RoleFocusItem;
	}

	public static boolean canUpgrade(ServerPlayerEntity player, ItemStack stack) {
		var roles = RoleManager.get(player);
		if (stack.getItem() instanceof RoleBladeItem blade) {
			return roles.isActive(blade.role()) && roles.tier(blade.role()) >= blade.tier();
		}
		if (stack.getItem() instanceof RoleFocusItem focus) {
			return roles.isActive(focus.role()) && roles.tier(focus.role()) >= focus.tier();
		}
		return false;
	}

	@Nullable
	public static ForgeRules.Affix read(ItemStack stack) {
		if (!isWeapon(stack)) return null;
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component == null || !component.contains(KEY)) return null;
		return readAffix(component.copyNbt());
	}

	@Nullable
	static ForgeRules.Affix readAffix(NbtCompound root) {
		if (!root.contains(KEY, NbtElement.COMPOUND_TYPE)) return null;
		NbtCompound stored = root.getCompound(KEY);
		if (!stored.contains(PROFILE, NbtElement.STRING_TYPE) || !stored.contains(RANK, NbtElement.INT_TYPE)) return null;
		return ForgeRules.read(stored.getString(PROFILE), stored.getInt(RANK));
	}

	static ItemStack upgraded(ItemStack original, ForgeRules.Affix affix) {
		ItemStack result = original.copy();
		NbtCompound root = original.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		writeAffix(root, affix);
		result.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(root));
		return result;
	}

	static void writeAffix(NbtCompound root, ForgeRules.Affix affix) {
		NbtCompound stored = new NbtCompound();
		stored.putString(PROFILE, affix.profile().key());
		stored.putInt(RANK, affix.rank());
		root.put(KEY, stored);
	}

	public static double energyCost(ServerPlayerEntity player, Role role, double cost) {
		ItemStack stack = player.getMainHandStack();
		if ((stack.getItem() instanceof RoleBladeItem blade && blade.role() == role
				|| stack.getItem() instanceof RoleFocusItem focus && focus.role() == role)
				&& canUpgrade(player, stack)) {
			return ForgeRules.energyCost(cost, read(stack));
		}
		return cost;
	}

	public static float impactGain(ItemStack stack, ServerPlayerEntity player, boolean hostileImpact) {
		if (!hostileImpact || player.isCreative() || player.isSpectator() || !canUpgrade(player, stack)) return 3;
		return ForgeRules.impactGain(3, read(stack));
	}

	public static Text describe(@Nullable ForgeRules.Affix affix) {
		if (affix == null) return Text.translatable("forge.aptitudes.none");
		String rank = switch (affix.rank()) {
			case 1 -> "I";
			case 2 -> "II";
			default -> "0";
		};
		return Text.translatable("forge.aptitudes.affix", profileName(affix.profile()), rank);
	}

	public static Text profileName(ForgeRules.Profile profile) {
		return Text.translatable("forge.aptitudes.profile." + profile.key());
	}

	public static Text effect(ForgeRules.Affix affix) {
		return Text.translatable("forge.aptitudes.effect." + affix.profile().key(), affix.percent());
	}

	public static Text hint(ForgeRules.Profile profile) {
		return Text.translatable("forge.aptitudes.hint." + profile.key());
	}

	public static void appendTooltip(ItemStack stack, List<Text> lines) {
		ForgeRules.Affix affix = read(stack);
		if (affix == null) return;
		lines.add(describe(affix).copy().formatted(Formatting.GOLD));
		lines.add(effect(affix).copy().formatted(Formatting.GRAY));
		lines.add(hint(affix.profile()).copy().formatted(Formatting.GRAY));
	}
}
