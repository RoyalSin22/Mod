package dev.aptitudes.item;

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

import java.util.List;

/**
 * Vale de armería (aldeano de gremio, contratos y botín): al usarlo se canjea por una Plantilla de ascensión, que sirve
 * para mejorar una pieza de netherita en la mesa de herrería (V80).
 */
public class ArmoryVoucherItem extends Item {
	public ArmoryVoucherItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient() || !(user instanceof ServerPlayerEntity player)) return TypedActionResult.success(stack, true);
		ItemStack reward = new ItemStack(ModItems.ASCENDED_TEMPLATE);
		stack.decrementUnlessCreative(1, player);
		if (!player.getInventory().insertStack(reward)) player.dropItem(reward, false);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE.value(),
				SoundCategory.PLAYERS, 1.0f, 1.0f);
		player.sendMessage(Text.translatable("item.aptitudes.armory_voucher.received", reward.getName())
				.formatted(Formatting.GOLD), true);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.armory_voucher.tooltip").formatted(Formatting.GRAY));
	}
}
