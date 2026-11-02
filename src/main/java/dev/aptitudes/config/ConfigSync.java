package dev.aptitudes.config;

import com.google.gson.Gson;
import dev.aptitudes.Aptitudes;
import dev.aptitudes.network.ConfigEditPayload;
import dev.aptitudes.network.ConfigSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;

/**
 * v40: sincroniza la configuración del servidor con los clientes y aplica los cambios que pide un operador desde
 * la pantalla de configuración. Autoridad del servidor: el cliente solo propone; aquí se comprueba el permiso,
 * se interpreta el valor ({@link ConfigFieldRules}), se valida ({@link AptitudesConfig#validated}), se guarda en
 * disco y se reenvía a todos.
 */
public final class ConfigSync {
	private static final Gson GSON = new Gson();
	private static final int PERMISSION = 2;

	private ConfigSync() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
		ServerPlayNetworking.registerGlobalReceiver(ConfigEditPayload.ID, (payload, context) -> edit(context.player(), payload));
	}

	public static void sync(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, ConfigSyncPayload.ID)) return;
		String json = GSON.toJson(Aptitudes.config());
		if (json.length() > ConfigSyncPayload.MAX_JSON) {
			Aptitudes.LOGGER.warn("[Aptitudes] Configuración demasiado grande para sincronizar ({} caracteres)", json.length());
			return;
		}
		ServerPlayNetworking.send(player, new ConfigSyncPayload(json, player.hasPermissionLevel(PERMISSION)));
	}

	public static void syncAll(MinecraftServer server) {
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) sync(p);
	}

	private static void edit(ServerPlayerEntity player, ConfigEditPayload payload) {
		if (!player.hasPermissionLevel(PERMISSION)) {
			player.sendMessage(Text.translatable("aptitudes.config.no_permission").formatted(Formatting.RED), true);
			return;
		}
		Field field = find(payload.field());
		if (field == null) {
			player.sendMessage(Text.translatable("aptitudes.config.unknown", payload.field()).formatted(Formatting.RED), true);
			return;
		}
		AptitudesConfig copy = GSON.fromJson(GSON.toJson(Aptitudes.config()), AptitudesConfig.class);
		try {
			field.set(copy, ConfigFieldRules.parse(field.getType(), payload.value()));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			player.sendMessage(Text.translatable("aptitudes.config.invalid", payload.field(), payload.value()).formatted(Formatting.RED), true);
			return;
		}
		AptitudesConfig applied = AptitudesConfig.validated(copy);
		Aptitudes.applyConfig(applied);
		AptitudesConfig.save(applied);
		MinecraftServer server = player.getServer();
		if (server != null) {
			dev.aptitudes.logic.ProgressManager.refreshAll(server);
			server.getPlayerManager().getPlayerList().forEach(dev.aptitudes.role.RoleManager::refresh);
			syncAll(server);
		}
		Object finalValue;
		try {
			finalValue = field.get(applied);
		} catch (IllegalAccessException e) {
			finalValue = payload.value();
		}
		Aptitudes.LOGGER.info("[Aptitudes] {} cambió la configuración: {} = {}", player.getName().getString(), field.getName(), finalValue);
		player.sendMessage(Text.translatable("aptitudes.config.changed", field.getName(), ConfigFieldRules.format(finalValue))
				.formatted(Formatting.GOLD), true);
	}

	private static Field find(String name) {
		for (Field f : ConfigFieldRules.editableFields(AptitudesConfig.class)) {
			if (f.getName().equals(name)) return f;
		}
		return null;
	}
}
