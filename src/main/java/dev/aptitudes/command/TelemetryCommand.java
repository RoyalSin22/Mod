package dev.aptitudes.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.aptitudes.ability.CombatTelemetry;
import dev.aptitudes.logic.CombatTelemetryRules.Sample;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * V93: {@code /aptitudes telemetry}. {@code me} lo puede usar cualquiera; el resto (resumen por rol, etiqueta de escenario y reinicio)
 * exige permiso de operador. Es una herramienta de equilibrio: los textos están en español y no se traducen.
 */
public final class TelemetryCommand {
	private TelemetryCommand() {
	}

	public static LiteralArgumentBuilder<ServerCommandSource> branch() {
		return literal("telemetry")
				.executes(TelemetryCommand::me)
				.then(literal("me").executes(TelemetryCommand::me))
				.then(literal("roles").requires(src -> src.hasPermissionLevel(2)).executes(TelemetryCommand::roles))
				.then(literal("label").requires(src -> src.hasPermissionLevel(2))
						.then(argument("name", StringArgumentType.word()).executes(TelemetryCommand::label)))
				.then(literal("reset").requires(src -> src.hasPermissionLevel(2)).executes(TelemetryCommand::reset));
	}

	private static String line(String name, Sample s) {
		return String.format(Locale.ROOT,
				"%s: %.1f dps · %.0f %% habilidad (%.0f %% derivada) · %.2f dmg/energía · %d bajas · %.1f s por baja · %.1f vida/s · PvP %.0f · %d usos",
				name, s.dps(), s.abilityShare() * 100, s.derivedShare() * 100, s.damagePerEnergy(), s.kills,
				s.averageTimeToKillSeconds(), s.hpKilledPerSecond(), s.pvpDamage, s.casts);
	}

	private static int me(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		Sample sample = CombatTelemetry.get().ofPlayer(player.getUuid());
		String label = CombatTelemetry.get().label();
		context.getSource().sendFeedback(() -> Text.literal("Telemetría [" + label + "] " + line("tú", sample)), false);
		return 1;
	}

	private static int roles(CommandContext<ServerCommandSource> context) {
		Map<String, Sample> groups = CombatTelemetry.get().byGroup();
		String label = CombatTelemetry.get().label();
		double mspt = context.getSource().getServer().getAverageTickTime();
		context.getSource().sendFeedback(() -> Text.literal(String.format(Locale.ROOT,
				"Telemetría [%s] · %d grupos · MSPT %.1f (VANILLA = jugadores sin rol: línea base)", label, groups.size(), mspt)), false);
		for (Map.Entry<String, Sample> entry : groups.entrySet()) {
			context.getSource().sendFeedback(() -> Text.literal(line(entry.getKey(), entry.getValue())), false);
		}
		return groups.size();
	}

	private static int label(CommandContext<ServerCommandSource> context) {
		CombatTelemetry.get().setLabel(StringArgumentType.getString(context, "name"));
		String label = CombatTelemetry.get().label();
		context.getSource().sendFeedback(() -> Text.literal("Escenario de telemetría: " + label), true);
		return 1;
	}

	private static int reset(CommandContext<ServerCommandSource> context) {
		CombatTelemetry.get().reset();
		context.getSource().sendFeedback(() -> Text.literal("Telemetría reiniciada."), true);
		return 1;
	}
}
