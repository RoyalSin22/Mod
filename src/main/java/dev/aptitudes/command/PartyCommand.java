package dev.aptitudes.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.aptitudes.party.PartyManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** Comandos alternativos a la pantalla de grupos. */
public final class PartyCommand {
	private PartyCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("party")
				.then(literal("invite").then(argument("player", EntityArgumentType.player())
						.executes(ctx -> {
							ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
							PartyManager.invite(player, EntityArgumentType.getPlayer(ctx, "player"));
							return 1;
						})))
				.then(literal("accept").executes(ctx -> {
					PartyManager.accept(ctx.getSource().getPlayerOrThrow());
					return 1;
				}))
				.then(literal("reject").executes(ctx -> {
					PartyManager.reject(ctx.getSource().getPlayerOrThrow());
					return 1;
				}))
				.then(literal("leave").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
					if (!PartyManager.leave(player)) player.sendMessage(Text.translatable("party.error.not_member").formatted(Formatting.RED), false);
					return 1;
				}))
				.then(literal("kick").then(argument("player", EntityArgumentType.player())
						.executes(ctx -> {
							ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
							if (!PartyManager.kick(player, EntityArgumentType.getPlayer(ctx, "player").getUuid())) {
								player.sendMessage(Text.translatable("party.error.cannot_kick").formatted(Formatting.RED), false);
							}
							return 1;
						})))
				.then(literal("list").executes(ctx -> {
					PartyManager.sendList(ctx.getSource().getPlayerOrThrow());
					return 1;
				}))
				.then(literal("pvp").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
					if (!PartyManager.toggleFriendlyFire(player)) player.sendMessage(
							Text.translatable("party.error.leader_only").formatted(Formatting.RED), false);
					return 1;
				}))
				.then(literal("share").then(argument("percent", integer(0, 50)).executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
					if (!PartyManager.setShare(player, getInteger(ctx, "percent"))) player.sendMessage(
							Text.translatable("party.error.leader_only").formatted(Formatting.RED), false);
					return 1;
				})))
				.executes(ctx -> {
					PartyManager.sendList(ctx.getSource().getPlayerOrThrow());
					return 1;
				}));
	}
}
