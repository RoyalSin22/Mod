package dev.aptitudes.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.aptitudes.social.GroundMessageState;
import dev.aptitudes.social.GroundMessages;
import dev.aptitudes.social.MessageCatalog;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * C7 (S5): {@code /aptitudes messages near [radio]} y {@code /aptitudes messages clear <radio>|player <jugador>}.
 * Solo operadores (nivel 2). Actúan en la dimensión de quien ejecuta.
 */
public final class GroundMessageCommand {
	private static final int DEFAULT_RADIUS = 32;
	private static final int MAX_LISTED = 20;

	private GroundMessageCommand() {
	}

	public static LiteralArgumentBuilder<ServerCommandSource> branch() {
		return literal("messages").requires(src -> src.hasPermissionLevel(2))
				.then(literal("near").executes(ctx -> near(ctx, DEFAULT_RADIUS))
						.then(argument("radius", integer(1, 512)).executes(ctx -> near(ctx, getInteger(ctx, "radius")))))
				.then(literal("clear")
						.then(argument("radius", integer(1, 512)).executes(ctx -> clearRadius(ctx, getInteger(ctx, "radius"))))
						.then(literal("player").then(argument("player", EntityArgumentType.player())
								.executes(GroundMessageCommand::clearPlayer))));
	}

	private static int near(CommandContext<ServerCommandSource> ctx, int radius) {
		ServerCommandSource src = ctx.getSource();
		List<GroundMessageState.Note> notes = GroundMessages.near(src.getWorld(), src.getPosition(), radius);
		src.sendFeedback(() -> Text.translatable("aptitudes.cmd.messages_near", notes.size(), radius), false);
		for (int i = 0; i < Math.min(MAX_LISTED, notes.size()); i++) {
			GroundMessageState.Note n = notes.get(i);
			Text text = Text.translatable(MessageCatalog.templateKey(n.template), Text.translatable(MessageCatalog.wordKey(n.word)));
			src.sendFeedback(() -> Text.translatable("aptitudes.cmd.messages_entry", n.pos.toShortString(), n.authorName,
					text, n.ratings()), false);
		}
		return notes.size();
	}

	private static int clearRadius(CommandContext<ServerCommandSource> ctx, int radius) {
		ServerCommandSource src = ctx.getSource();
		int removed = GroundMessages.clear(src.getWorld(), src.getPosition(), radius, null);
		src.sendFeedback(() -> Text.translatable("aptitudes.cmd.messages_cleared", removed), true);
		return removed;
	}

	private static int clearPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerCommandSource src = ctx.getSource();
		ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
		int removed = GroundMessages.clear(src.getWorld(), src.getPosition(), 0, target.getUuid());
		src.sendFeedback(() -> Text.translatable("aptitudes.cmd.messages_cleared", removed), true);
		return removed;
	}
}
