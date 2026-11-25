package dev.aptitudes.item;

import dev.aptitudes.event.EclipseManager;
import dev.aptitudes.event.EclipseSummoningRules;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

/**
 * Behelit (Embrión del Umbral): se sacrifica en la Hoguera para iniciar el Eclipse.
 * La espera (3 días de juego por mundo, desde el fin del Eclipse) la comparte con el Astrolabio del Ocaso
 * ({@link EclipseSummoningRules}).
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
				EclipseSummoningRules.Verdict verdict = EclipseManager.summonVerdict(serverPlayer, false);
				if (verdict != EclipseSummoningRules.Verdict.ALLOWED) {
					EclipseManager.sendDenied(serverPlayer, verdict);
					return ActionResult.FAIL;
				}
				context.getStack().decrementUnlessCreative(1, serverPlayer);
				EclipseManager.startBySummon(serverPlayer);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}
}
