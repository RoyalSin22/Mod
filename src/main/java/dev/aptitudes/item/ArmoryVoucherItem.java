package dev.aptitudes.item;

import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Vale de armería (del aldeano de gremio): al usarlo te da una pieza del set de
 * armadura de tu rol PRINCIPAL; primero las que aún no tienes.
 */
public class ArmoryVoucherItem extends Item {
	public ArmoryVoucherItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient() || !(user instanceof ServerPlayerEntity player)) return TypedActionResult.success(stack, true);
		Role role = RoleManager.get(player).primary();
		if (role == null) {
			player.sendMessage(Text.translatable("item.aptitudes.armory_voucher.no_role").formatted(Formatting.RED), true);
			return TypedActionResult.fail(stack);
		}
		List<Item> pieces = RoleArmor.pieces(role);
		List<Item> missing = new ArrayList<>();
		for (Item piece : pieces) {
			boolean has = player.getInventory().count(piece) > 0;
			for (ItemStack worn : player.getArmorItems()) if (worn.isOf(piece)) has = true;
			if (!has) missing.add(piece);
		}
		List<Item> pool = missing.isEmpty() ? pieces : missing;
		Item reward = pool.get(world.random.nextInt(pool.size()));
		stack.decrementUnlessCreative(1, player);
		if (!player.getInventory().insertStack(new ItemStack(reward))) player.dropItem(new ItemStack(reward), false);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
				SoundCategory.PLAYERS, 1.0f, 1.0f);
		player.sendMessage(Text.translatable("item.aptitudes.armory_voucher.received", new ItemStack(reward).getName())
				.formatted(Formatting.GOLD), true);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.armory_voucher.tooltip").formatted(Formatting.GRAY));
	}
}
