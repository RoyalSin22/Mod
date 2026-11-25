package dev.aptitudes.item;

import dev.aptitudes.social.GroundMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.function.Consumer;

/**
 * C7 (S5): Tiza de ceniza. Clic derecho sobre la cara superior de un bloque abre (en el cliente) la pantalla para
 * escribir una nota en el hueco de encima; el servidor valida y guarda al recibir la nota. Mayús + clic derecho borra
 * una nota propia de ese hueco (en el servidor). Cada nota escrita gasta una tiza.
 */
public class AshChalkItem extends Item {
	/** Lo asigna el cliente al iniciar: abre la pantalla de escritura para el hueco dado. En servidor no hace nada. */
	public static Consumer<BlockPos> opener = pos -> { };

	public AshChalkItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getSide() != Direction.UP) return ActionResult.PASS;
		PlayerEntity player = context.getPlayer();
		if (player == null) return ActionResult.PASS;
		BlockPos spot = context.getBlockPos().up();
		if (context.getWorld().isClient()) {
			if (!player.isSneaking()) opener.accept(spot);
		} else if (player.isSneaking() && player instanceof ServerPlayerEntity sp) {
			GroundMessages.eraseAt(sp, spot);
		}
		return ActionResult.success(context.getWorld().isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.ash_chalk.tooltip").formatted(Formatting.GRAY));
	}
}
