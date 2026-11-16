package dev.aptitudes.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * v60 (C3): Reglas para convocar el Eclipse desde la Hoguera con el Astrolabio del Ocaso.
 *
 * <p>Puro (sin Minecraft): verifica permisos y autorización.</p>
 */
public final class EclipseSummoningRules {
	private EclipseSummoningRules() {
	}

	/** ¿Puede este jugador convocar el Eclipse? Solo operadores (nivel de permiso 2+). */
	public static boolean canSummon(ServerPlayerEntity player) {
		return player.hasPermissionLevel(2); // Operador
	}

	/** Mensaje de rechazo si no tiene permisos. */
	public static Text deniedMessage() {
		return Text.literal("Only operators can summon events.")
				.formatted(Formatting.RED);
	}

	/** Mensaje de confirmación al convocar. */
	public static Text summoned() {
		return Text.literal("The Eclipse has been summoned!")
				.formatted(Formatting.GOLD);
	}
}
