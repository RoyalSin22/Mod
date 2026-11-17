package dev.aptitudes.client.fx;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.aptitudes.logic.PlayerAnimIds;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

/**
 * V78: comando de cliente para ver las animaciones del cuerpo sin lanzar la habilidad: {@code /aptfx anim <nombre>} las
 * reproduce sobre tu personaje (mira tu cuerpo con F5) y {@code /aptfx stop} las corta. Solo afecta a tu pantalla.
 */
public final class FxDebugCommand {
	private FxDebugCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("aptfx")
						.then(ClientCommandManager.literal("anim")
								.then(ClientCommandManager.argument("nombre", StringArgumentType.word())
										.suggests((ctx, builder) -> CommandSource.suggestMatching(PlayerAnimIds.ALL, builder))
										.executes(ctx -> {
											String name = StringArgumentType.getString(ctx, "nombre");
											if (!PlayerAnimIds.ALL.contains(name)) {
												ctx.getSource().sendError(Text.literal("Animación desconocida: " + name));
												return 0;
											}
											PlayerAnims.play(ctx.getSource().getPlayer(), name);
											ctx.getSource().sendFeedback(Text.literal("Reproduciendo " + name + " (F5 para ver tu cuerpo)"));
											return 1;
										})))
						.then(ClientCommandManager.literal("stop")
								.executes(ctx -> {
									PlayerAnims.stop(ctx.getSource().getPlayer());
									return 1;
								}))));
	}
}
