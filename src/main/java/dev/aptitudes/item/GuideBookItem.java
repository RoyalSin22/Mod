package dev.aptitudes.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * Grimorio de Aptitudes: guía del mod dentro del juego. Se abre en el cliente (la pantalla
 * la registra el cliente en {@link #opener}); el servidor no hace nada al usarlo.
 */
public class GuideBookItem extends Item {
	/** Lo asigna el cliente al iniciar; en un servidor dedicado se queda vacío. */
	public static Runnable opener = () -> { };

	public GuideBookItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (world.isClient()) opener.run();
		return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.aptitudes.guide_book.tooltip").formatted(Formatting.GRAY));
	}
}
