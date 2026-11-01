package dev.aptitudes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aptitudes.unique.UniqueManager;
import dev.aptitudes.unique.UniqueSkill;
import dev.aptitudes.unique.UniqueState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * <pre>
 * /unique                                   lista de habilidades únicas y sus dueños
 * /unique admin give &lt;jugador&gt; &lt;habilidad&gt;   (OP) asigna (quita la anterior de ambos)
 * /unique admin revoke &lt;habilidad&gt;           (OP) la deja libre
 * </pre>
 */
public final class UniqueCommand {
	private static final DynamicCommandExceptionType UNKNOWN =
			new DynamicCommandExceptionType(v -> Text.translatable("aptitudes.cmd.unknown_unique", v));
	private static final SuggestionProvider<ServerCommandSource> SKILLS = (ctx, builder) ->
			CommandSource.suggestMatching(Arrays.stream(UniqueSkill.values()).map(UniqueSkill::key), builder);

	private UniqueCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("unique")
				.executes(ctx -> {
					UniqueState state = UniqueState.get(ctx.getSource().getServer());
					ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.unique_header")
							.formatted(Formatting.LIGHT_PURPLE), false);
					for (UniqueSkill skill : UniqueSkill.values()) {
						UniqueState.Holder h = state.holder(skill);
						Text owner = h == null ? Text.translatable("aptitudes.unique.free").formatted(Formatting.GREEN)
								: Text.literal(h.name()).formatted(Formatting.GOLD);
						ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.unique_line",
								Text.translatable(skill.translationKey()).withColor(skill.color()), owner), false);
					}
					return 1;
				})
				.then(literal("admin").requires(src -> src.hasPermissionLevel(2))
						.then(literal("give").then(argument("player", EntityArgumentType.player())
								.then(argument("skill", word()).suggests(SKILLS).executes(ctx -> {
									ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
									UniqueSkill skill = parse(getString(ctx, "skill"));
									UniqueManager.give(player, skill);
									return 1;
								}))))
						.then(literal("revoke").then(argument("skill", word()).suggests(SKILLS).executes(ctx -> {
							UniqueSkill skill = parse(getString(ctx, "skill"));
							UniqueManager.revoke(ctx.getSource().getServer(), skill);
							ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.unique_revoked",
									Text.translatable(skill.translationKey())), true);
							return 1;
						})))));
	}

	private static UniqueSkill parse(String key) throws CommandSyntaxException {
		return UniqueSkill.byKey(key).orElseThrow(() -> UNKNOWN.create(key));
	}
}
