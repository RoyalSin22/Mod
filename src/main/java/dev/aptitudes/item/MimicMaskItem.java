package dev.aptitudes.item;

import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/** Máscara de mil caras (Cambiaformas): clic derecho = "Transformarse" (igual que su tecla). */
public class MimicMaskItem extends Item {
	public MimicMaskItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient() && user instanceof ServerPlayerEntity player) {
			RoleData data = RoleManager.get(player);
			if (data.primary() == Role.SHAPESHIFTER) AbilityManager.use(player, 0);
			else if (data.secondary() == Role.SHAPESHIFTER) AbilityManager.use(player, 2);
			else player.sendMessage(Text.translatable("item.aptitudes.mimic_mask.not_shapeshifter").formatted(Formatting.RED), true);
			player.getItemCooldownManager().set(this, 20);
		}
		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.mimic_mask.tooltip").formatted(Formatting.GRAY));
	}
}
