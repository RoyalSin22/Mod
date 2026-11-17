package dev.aptitudes.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.SynergyMask;
import dev.aptitudes.ability.SynergyTracker;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/** v65 C5: {@code /aptitudes synergy status}. Solo informa al ejecutor. */
public final class SynergyCommand {
	private SynergyCommand() {
	}

	public static LiteralArgumentBuilder<ServerCommandSource> branch() {
		return literal("synergy").executes(SynergyCommand::status)
				.then(literal("status").executes(SynergyCommand::status));
	}

	private static int status(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		if (!Aptitudes.config().enableSynergies) {
			context.getSource().sendFeedback(() -> Text.translatable("synergy.aptitudes.disabled"), false);
			return 0;
		}
		int mask = SynergyTracker.mask(player);
		context.getSource().sendFeedback(() -> Text.translatable("synergy.aptitudes.status",
				SynergyMask.count(mask), SynergyMask.COUNT), false);
		for (int i = 0; i < SynergyMask.COUNT; i++) {
			int index = i;
			boolean on = SynergyMask.has(mask, i);
			context.getSource().sendFeedback(() -> Text.translatable("synergy.aptitudes.line", "Y" + (index + 1),
					Text.translatable("synergy.aptitudes.name." + (index + 1)),
					Text.translatable(on ? "synergy.aptitudes.seen" : "synergy.aptitudes.unseen")), false);
		}
		return 1;
	}
}
