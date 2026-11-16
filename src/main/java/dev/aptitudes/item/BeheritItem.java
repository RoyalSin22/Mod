package dev.aptitudes.item;

import dev.aptitudes.event.EclipseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

/**
 * Behelit (Resonador del Umbral): se sacrifica en la Hoguera para iniciar el Eclipse.
 * El cooldown es de 3 días por mundo, contados desde el fin del Eclipse.
 */
public class BeheritItem extends Item {
	public BeheritItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (!world.isClient && world.getBlockState(context.getBlockPos()).getBlock() instanceof dev.aptitudes.block.BonfireBlock) {
			PlayerEntity player = context.getPlayer();
			if (player instanceof ServerPlayerEntity serverPlayer) {
				if (EclipseManager.canStartEclipse(serverPlayer.getServerWorld())) {
					EclipseManager.startByBeherit(serverPlayer.getServerWorld());
					context.getStack().decrement(1);
					return ActionResult.SUCCESS;
				}
			}
		}
		return ActionResult.PASS;
	}
}
