package dev.aptitudes.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.aptitudes.tutorial.TutorialManager;
import dev.aptitudes.tutorial.TutorialRules;
import dev.aptitudes.tutorial.TutorialView;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/** Los subcomandos solo afectan al ejecutor; no admiten un jugador objetivo ni reclaman premios. */
public final class TutorialCommand {
	private TutorialCommand() {
	}

	public static LiteralArgumentBuilder<ServerCommandSource> branch() {
		return literal("tutorial").executes(TutorialCommand::status)
				.then(literal("status").executes(TutorialCommand::status))
				.then(literal("skip").executes(TutorialCommand::skip));
	}

	private static int status(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		TutorialView view = TutorialView.from(TutorialManager.data(player).state());
		String state = view.skipped() ? "skipped" : view.nextStep() < 0 ? "completed" : "active";
		int total = TutorialRules.ORDER.size();
		context.getSource().sendFeedback(() -> Text.translatable("tutorial.aptitudes.status",
				Text.translatable("tutorial.aptitudes.state." + state), view.completed(), total, view.rewarded(), total), false);
		if (view.nextStep() >= 0) {
			String step = TutorialRules.ORDER.get(view.nextStep()).key();
			context.getSource().sendFeedback(() -> Text.translatable("tutorial.aptitudes.next",
					view.nextStep() + 1, total, Text.translatable("tutorial.aptitudes.step." + step)), false);
		}
		if (view.pendingRewards() > 0) {
			context.getSource().sendFeedback(() -> Text.translatable("tutorial.aptitudes.pending", view.pendingRewards()), false);
		}
		TutorialManager.sync(player, true);
		return 1;
	}

	private static int skip(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		boolean changed = TutorialManager.skip(context.getSource().getPlayerOrThrow());
		context.getSource().sendFeedback(() -> Text.translatable(changed
				? "tutorial.aptitudes.skipped" : "tutorial.aptitudes.already_skipped"), false);
		return changed ? 1 : 0;
	}
}
