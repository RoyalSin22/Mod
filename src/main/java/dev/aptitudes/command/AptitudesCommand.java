package dev.aptitudes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.data.PlayerProgress;
import dev.aptitudes.data.ProgressRules;
import dev.aptitudes.data.Skill;
import dev.aptitudes.logic.ProgressManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * <pre>
 * /aptitudes                                   (alias /skills) resumen propio
 * /aptitudes allocate &lt;atributo&gt; [cantidad]
 * /aptitudes respec                            si allowSelfRespec = true
 * /aptitudes admin show|reset|respec &lt;jugador&gt;   (nivel de permiso 2)
 * /aptitudes admin setlevel &lt;jugador&gt; &lt;habilidad&gt; &lt;nivel&gt;
 * /aptitudes admin addxp &lt;jugador&gt; &lt;habilidad&gt; &lt;cantidad&gt;
 * /aptitudes admin addpoints &lt;jugador&gt; &lt;cantidad&gt;
 * /aptitudes admin reload
 * </pre>
 */
public final class AptitudesCommand {
	private static final DynamicCommandExceptionType UNKNOWN_SKILL =
			new DynamicCommandExceptionType(v -> Text.translatable("aptitudes.cmd.unknown_skill", v));
	private static final DynamicCommandExceptionType UNKNOWN_ATTRIBUTE =
			new DynamicCommandExceptionType(v -> Text.translatable("aptitudes.cmd.unknown_attribute", v));

	private static final SuggestionProvider<ServerCommandSource> SKILLS = (ctx, builder) ->
			CommandSource.suggestMatching(Arrays.stream(Skill.values()).map(Skill::key), builder);
	private static final SuggestionProvider<ServerCommandSource> ATTRIBUTES = (ctx, builder) ->
			CommandSource.suggestMatching(Arrays.stream(PlayerAttribute.values()).map(PlayerAttribute::key), builder);

	private AptitudesCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> root = dispatcher.register(literal("aptitudes")
				.executes(ctx -> show(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
				.then(literal("allocate")
						.then(argument("attribute", word()).suggests(ATTRIBUTES)
								.executes(ctx -> allocate(ctx, 1))
								.then(argument("amount", integer(1, 100))
										.executes(ctx -> allocate(ctx, getInteger(ctx, "amount"))))))
				.then(literal("respec").executes(AptitudesCommand::selfRespec))
				.then(literal("admin").requires(src -> src.hasPermissionLevel(2))
						.then(literal("show").then(argument("player", EntityArgumentType.player())
								.executes(ctx -> show(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))))
						.then(literal("reset").then(argument("player", EntityArgumentType.player())
								.executes(AptitudesCommand::adminReset)))
						.then(literal("respec").then(argument("player", EntityArgumentType.player())
								.executes(AptitudesCommand::adminRespec)))
						.then(literal("setlevel").then(argument("player", EntityArgumentType.player())
								.then(argument("skill", word()).suggests(SKILLS)
										.then(argument("level", integer(0))
												.executes(AptitudesCommand::adminSetLevel)))))
						.then(literal("addxp").then(argument("player", EntityArgumentType.player())
								.then(argument("skill", word()).suggests(SKILLS)
										.then(argument("amount", integer(1))
												.executes(AptitudesCommand::adminAddXp)))))
						.then(literal("addpoints").then(argument("player", EntityArgumentType.player())
								.then(argument("amount", integer())
										.executes(AptitudesCommand::adminAddPoints))))
						.then(literal("reload").executes(AptitudesCommand::adminReload))));

		// Alias. Brigadier no ejecuta "executes" de un nodo redirigido sin argumentos,
		// por eso el alias declara su propio executes.
		dispatcher.register(literal("skills")
				.executes(ctx -> show(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
				.redirect(root));
	}

	// ------------------------------------------------------------- Jugador

	private static int show(ServerCommandSource source, ServerPlayerEntity target) {
		PlayerProgress progress = ProgressManager.get(target);
		ProgressRules rules = ProgressManager.rules();
		source.sendFeedback(() -> Text.translatable("aptitudes.cmd.header", target.getDisplayName())
				.formatted(Formatting.GOLD), false);
		for (Skill s : Skill.values()) {
			int level = progress.getLevel(s);
			source.sendFeedback(() -> Text.translatable("aptitudes.cmd.skill_line",
					Text.translatable(s.translationKey()), level, progress.getXp(s), rules.xpToNext(level)), false);
		}
		for (PlayerAttribute a : PlayerAttribute.values()) {
			source.sendFeedback(() -> Text.translatable("aptitudes.cmd.attribute_line",
					Text.translatable(a.translationKey()), progress.getAllocated(a), rules.maxPointsPerAttribute())
					.formatted(Formatting.AQUA), false);
		}
		source.sendFeedback(() -> Text.translatable("aptitudes.cmd.points",
				ProgressManager.availablePoints(progress)).formatted(Formatting.GREEN), false);
		return 1;
	}

	private static int allocate(CommandContext<ServerCommandSource> ctx, int amount) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		PlayerAttribute attribute = parseAttribute(getString(ctx, "attribute"));
		int assigned = ProgressManager.allocate(player, attribute, amount);
		if (assigned <= 0) {
			ctx.getSource().sendError(Text.translatable("aptitudes.cmd.allocate_fail"));
			return 0;
		}
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.allocated",
				assigned, Text.translatable(attribute.translationKey())), false);
		return assigned;
	}

	private static int selfRespec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		if (!Aptitudes.config().allowSelfRespec && !ctx.getSource().hasPermissionLevel(2)) {
			ctx.getSource().sendError(Text.translatable("aptitudes.cmd.respec_disabled"));
			return 0;
		}
		ProgressManager.respec(player);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.respec_done"), false);
		return 1;
	}

	// --------------------------------------------------------------- Admin

	private static int adminReset(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		ProgressManager.reset(player);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.reset", player.getDisplayName()), true);
		return 1;
	}

	private static int adminRespec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		ProgressManager.respec(player);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.respec_done"), true);
		return 1;
	}

	private static int adminSetLevel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		Skill skill = parseSkill(getString(ctx, "skill"));
		int level = Math.min(getInteger(ctx, "level"), Aptitudes.config().maxSkillLevel);
		ProgressManager.setLevel(player, skill, level);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.setlevel",
				Text.translatable(skill.translationKey()), player.getDisplayName(), level), true);
		return 1;
	}

	private static int adminAddXp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		Skill skill = parseSkill(getString(ctx, "skill"));
		int amount = getInteger(ctx, "amount");
		ProgressManager.addXp(player, skill, amount, true); // ignora el filtro de creativo
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.addxp",
				amount, Text.translatable(skill.translationKey()), player.getDisplayName()), true);
		return 1;
	}

	private static int adminAddPoints(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		int amount = getInteger(ctx, "amount");
		ProgressManager.addBonusPoints(player, amount);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.addpoints",
				amount, player.getDisplayName()), true);
		return 1;
	}

	private static int adminReload(CommandContext<ServerCommandSource> ctx) {
		Aptitudes.reloadConfig();
		ProgressManager.refreshAll(ctx.getSource().getServer());
		ctx.getSource().getServer().getPlayerManager().getPlayerList().forEach(dev.aptitudes.role.RoleManager::refresh);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.reload"), true);
		return 1;
	}

	// ------------------------------------------------------------- Parsing

	private static Skill parseSkill(String key) throws CommandSyntaxException {
		return Skill.byKey(key).orElseThrow(() -> UNKNOWN_SKILL.create(key));
	}

	private static PlayerAttribute parseAttribute(String key) throws CommandSyntaxException {
		return PlayerAttribute.byKey(key).orElseThrow(() -> UNKNOWN_ATTRIBUTE.create(key));
	}
}
