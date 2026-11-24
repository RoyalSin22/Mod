package dev.aptitudes.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.message.GroundMessageRules;
import dev.aptitudes.message.GroundMessages;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** C7 · S5: {@code /aptitudes message <frase> | list | remove}. Cualquier jugador; retirar notas ajenas pide permiso 2. */
public final class GroundMessageCommand {
	private GroundMessageCommand() {}

	public static LiteralArgumentBuilder<ServerCommandSource> branch() {
		return literal("message")
				.then(literal("list").executes(GroundMessageCommand::list))
				.then(literal("remove").executes(GroundMessageCommand::remove))
				.then(argument("phrase", StringArgumentType.word())
						.suggests((c, b) -> CommandSource.suggestMatching(GroundMessageRules.PHRASES, b))
						.executes(GroundMessageCommand::place));
	}

	private static int place(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		GroundMessages.PlaceResult result = GroundMessages.place(player, StringArgumentType.getString(context, "phrase"));
		var config = Aptitudes.config();
		Text text = switch (result) {
			case OK -> Text.translatable("message.aptitudes.placed").formatted(Formatting.GREEN);
			case DISABLED -> Text.translatable("message.aptitudes.disabled").formatted(Formatting.RED);
			case BAD_PHRASE -> Text.translatable("message.aptitudes.bad_phrase").formatted(Formatting.RED);
			case LIMIT -> Text.translatable("message.aptitudes.limit", config.groundMessagesPerPlayer).formatted(Formatting.RED);
			case COOLDOWN -> Text.translatable("message.aptitudes.cooldown", config.groundMessageCooldownSeconds).formatted(Formatting.RED);
			case FAILED -> Text.translatable("message.aptitudes.failed").formatted(Formatting.RED);
		};
		context.getSource().sendFeedback(() -> text, false);
		return result == GroundMessages.PlaceResult.OK ? 1 : 0;
	}

	private static int list(CommandContext<ServerCommandSource> context) {
		context.getSource().sendFeedback(() -> Text.translatable("message.aptitudes.list_header").formatted(Formatting.GOLD), false);
		for (String id : GroundMessageRules.PHRASES) {
			context.getSource().sendFeedback(() -> Text.literal(id + " — ").formatted(Formatting.GRAY)
					.append(Text.translatable("message.aptitudes.phrase." + id)), false);
		}
		return GroundMessageRules.PHRASES.size();
	}

	private static int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		boolean removed = GroundMessages.removeNearest(player, context.getSource().hasPermissionLevel(2));
		context.getSource().sendFeedback(() -> Text.translatable(removed ? "message.aptitudes.removed" : "message.aptitudes.none_nearby")
				.formatted(removed ? Formatting.GREEN : Formatting.RED), false);
		return removed ? 1 : 0;
	}
}
