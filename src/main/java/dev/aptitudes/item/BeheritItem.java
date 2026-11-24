package dev.aptitudes.item;

import dev.aptitudes.event.BeheritRules;
import dev.aptitudes.event.EclipseManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

/**
 * Behelit (Resonador del Umbral): se sacrifica en la Hoguera para iniciar el Eclipse.
 * Solo se consume si el Eclipse empieza. Hay una espera de {@code beheritCooldownDays} días (3 por defecto) por mundo,
 * contada desde el fin del Eclipse anterior ({@link BeheritRules}); si no se puede usar, el jugador recibe un aviso
 * en la barra de acción y el objeto se conserva.
 */
public class BeheritItem extends Item {
	public BeheritItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (world.isClient || !(world.getBlockState(context.getBlockPos()).getBlock() instanceof dev.aptitudes.block.BonfireBlock)) {
			return ActionResult.PASS;
		}
		PlayerEntity player = context.getPlayer();
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
		ServerWorld overworld = serverPlayer.getServerWorld();
		// El Eclipse es del Mundo Normal: en otra dimensión el ritual no hace nada (y no se consume).
		if (overworld.getRegistryKey() != World.OVERWORLD) return ActionResult.PASS;

		BeheritRules.Result result = EclipseManager.beheritStatus(overworld);
		if (result == BeheritRules.Result.READY && EclipseManager.startByBeherit(overworld)) {
			context.getStack().decrement(1);
			return ActionResult.SUCCESS;
		}
		Text message = switch (result) {
			case COOLDOWN -> Text.translatable("eclipse.aptitudes.behelit.cooldown", EclipseManager.beheritHoursLeft(overworld));
			case BUSY -> Text.translatable("eclipse.aptitudes.behelit.busy");
			case DISABLED -> Text.translatable("eclipse.aptitudes.behelit.disabled");
			case READY -> Text.translatable("eclipse.aptitudes.behelit.busy"); // empezar falló en la segunda comprobación
		};
		serverPlayer.sendMessage(message.copy().formatted(Formatting.GRAY, Formatting.ITALIC), true);
		return ActionResult.SUCCESS; // evita que el clic derecho además haga descansar en la Hoguera
	}
}
