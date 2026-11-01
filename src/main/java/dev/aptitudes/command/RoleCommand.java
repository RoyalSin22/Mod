package dev.aptitudes.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.ability.AbilityManager;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.RoleData;
import dev.aptitudes.role.RoleManager;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleRules;
import dev.aptitudes.role.RoleTree;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Locale;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * <pre>
 * /roles                                         resumen propio
 * /roles primary|secondary &lt;rol&gt;
 * /roles learn &lt;nodo&gt;                           sube un rango
 * /roles use &lt;1-4&gt;                              usa una habilidad (útil para macros)
 * /roles evolve &lt;rol&gt;                           evoluciona a Maestro / Definitivo
 * /roles respec &lt;rol&gt;                           si allowSelfRespec = true
 * /roles admin setlevel|addxp &lt;jugador&gt; &lt;rol&gt; &lt;n&gt;
 * /roles admin settier &lt;jugador&gt; &lt;rol&gt; &lt;0-2&gt;
 * /roles admin reset|clearcooldown &lt;jugador&gt;
 * </pre>
 */
public final class RoleCommand {
	private static final DynamicCommandExceptionType UNKNOWN_ROLE =
			new DynamicCommandExceptionType(v -> Text.translatable("aptitudes.cmd.unknown_role", v));

	private static final SuggestionProvider<ServerCommandSource> ROLES = (ctx, builder) ->
			CommandSource.suggestMatching(Arrays.stream(Role.values()).map(Role::key), builder);
	private static final SuggestionProvider<ServerCommandSource> NODES = (ctx, builder) ->
			CommandSource.suggestMatching(RoleTree.all().stream().map(RoleNode::id), builder);

	private RoleCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("roles")
				.executes(ctx -> show(ctx.getSource(), ctx.getSource().getPlayerOrThrow()))
				.then(literal("primary").then(argument("role", word()).suggests(ROLES)
						.executes(ctx -> setRole(ctx, true))))
				.then(literal("secondary").then(argument("role", word()).suggests(ROLES)
						.executes(ctx -> setRole(ctx, false))))
				.then(literal("learn").then(argument("node", word()).suggests(NODES)
						.executes(RoleCommand::learn)))
				.then(literal("use").then(argument("slot", integer(1, AbilityManager.SLOTS))
						.executes(ctx -> {
							AbilityManager.use(ctx.getSource().getPlayerOrThrow(), getInteger(ctx, "slot") - 1);
							return 1;
						})))
				.then(literal("evolve").then(argument("role", word()).suggests(ROLES)
						.executes(ctx -> {
							ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
							Role role = parseRole(getString(ctx, "role"));
							RoleManager.EvolveResult result = RoleManager.evolve(player, role);
							if (result != RoleManager.EvolveResult.OK) {
								ctx.getSource().sendError(Text.translatable("aptitudes.evolve."
										+ result.name().toLowerCase(Locale.ROOT)));
								return 0;
							}
							return 1;
						})))
				.then(literal("respec").then(argument("role", word()).suggests(ROLES)
						.executes(RoleCommand::selfRespec)))
				.then(literal("admin").requires(src -> src.hasPermissionLevel(2))
						.then(literal("setlevel").then(argument("player", EntityArgumentType.player())
								.then(argument("role", word()).suggests(ROLES)
										.then(argument("level", integer(0)).executes(RoleCommand::adminSetLevel)))))
						.then(literal("addxp").then(argument("player", EntityArgumentType.player())
								.then(argument("role", word()).suggests(ROLES)
										.then(argument("amount", integer(1)).executes(RoleCommand::adminAddXp)))))
						.then(literal("reset").then(argument("player", EntityArgumentType.player())
								.executes(ctx -> {
									ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
									RoleManager.reset(p);
									ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.reset",
											p.getDisplayName()), true);
									return 1;
								})))
						.then(literal("settier").then(argument("player", EntityArgumentType.player())
								.then(argument("role", word()).suggests(ROLES)
										.then(argument("tier", integer(0, 2)).executes(ctx -> {
											ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
											Role role = parseRole(getString(ctx, "role"));
											int tier = getInteger(ctx, "tier");
											RoleManager.get(p).setTier(role, tier);
											RoleManager.refresh(p);
											ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.settier",
													Text.translatable(role.translationKey()), p.getDisplayName(),
													Text.translatable(dev.aptitudes.role.Evolution.titleKey(role, tier))), true);
											return 1;
										})))))
						.then(literal("clearcooldown").then(argument("player", EntityArgumentType.player())
								.executes(ctx -> {
									ServerPlayerEntity p = EntityArgumentType.getPlayer(ctx, "player");
									RoleManager.clearChangeCooldown(p);
									ctx.getSource().sendFeedback(() -> Text.translatable(
											"aptitudes.cmd.cooldown_cleared", p.getDisplayName()), true);
									return 1;
								})))));
	}

	private static int show(ServerCommandSource source, ServerPlayerEntity player) {
		RoleData data = RoleManager.get(player);
		RoleRules rules = RoleManager.rules();
		source.sendFeedback(() -> Text.translatable("aptitudes.cmd.roles_header", player.getDisplayName())
				.formatted(Formatting.LIGHT_PURPLE), false);
		for (Role role : Role.values()) {
			int level = data.level(role);
			Text marker = role == data.primary() ? Text.translatable("aptitudes.role.primary_short")
					: role == data.secondary() ? Text.translatable("aptitudes.role.secondary_short") : Text.empty();
			source.sendFeedback(() -> Text.translatable("aptitudes.cmd.role_line",
					Text.translatable(role.translationKey()), level, data.xp(role), rules.xpToNext(level),
					RoleManager.availablePoints(data, role), marker)
					.formatted(data.isActive(role) ? Formatting.WHITE : Formatting.GRAY), false);
		}
		return 1;
	}

	private static int setRole(CommandContext<ServerCommandSource> ctx, boolean primary) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		Role role = parseRole(getString(ctx, "role"));
		switch (RoleManager.setRole(player, role, primary)) {
			case OK -> ctx.getSource().sendFeedback(() -> Text.translatable(
					primary ? "aptitudes.cmd.primary_set" : "aptitudes.cmd.secondary_set",
					Text.translatable(role.translationKey())), false);
			case SAME -> ctx.getSource().sendError(Text.translatable("aptitudes.cmd.role_same"));
			case ON_COOLDOWN -> {
				int minutes = (RoleManager.changeCooldownRemaining(player, RoleManager.get(player)) + 1199) / 1200;
				ctx.getSource().sendError(Text.translatable("aptitudes.role.change_cooldown", minutes));
				return 0;
			}
		}
		return 1;
	}

	private static int learn(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		String nodeId = getString(ctx, "node").toLowerCase(Locale.ROOT);
		RoleManager.UpgradeResult result = RoleManager.upgrade(player, nodeId);
		if (result != RoleManager.UpgradeResult.OK) {
			ctx.getSource().sendError(Text.translatable("aptitudes.role.upgrade." + result.name().toLowerCase(Locale.ROOT)));
			return 0;
		}
		RoleNode node = RoleTree.get(nodeId);
		int rank = RoleManager.get(player).rank(nodeId);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.learned",
				Text.translatable(node.translationKey()), rank), false);
		return 1;
	}

	private static int selfRespec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
		if (!Aptitudes.config().allowSelfRespec && !ctx.getSource().hasPermissionLevel(2)) {
			ctx.getSource().sendError(Text.translatable("aptitudes.cmd.respec_disabled"));
			return 0;
		}
		Role role = parseRole(getString(ctx, "role"));
		RoleManager.respecTree(player, role);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.respec_done"), false);
		return 1;
	}

	private static int adminSetLevel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		Role role = parseRole(getString(ctx, "role"));
		int level = Math.min(getInteger(ctx, "level"), Aptitudes.config().maxRoleLevel);
		RoleManager.setLevel(player, role, level);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.setlevel",
				Text.translatable(role.translationKey()), player.getDisplayName(), level), true);
		return 1;
	}

	private static int adminAddXp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
		Role role = parseRole(getString(ctx, "role"));
		int amount = getInteger(ctx, "amount");
		RoleManager.addRoleXp(player, role, amount, true);
		ctx.getSource().sendFeedback(() -> Text.translatable("aptitudes.cmd.addxp",
				amount, Text.translatable(role.translationKey()), player.getDisplayName()), true);
		return 1;
	}

	private static Role parseRole(String key) throws CommandSyntaxException {
		return Role.byKey(key).orElseThrow(() -> UNKNOWN_ROLE.create(key));
	}
}
