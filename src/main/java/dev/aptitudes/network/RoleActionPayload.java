package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Cliente → servidor: acciones de la pantalla de roles.
 * {@code action}: 0 = fijar principal, 1 = fijar secundario, 2 = subir rango de {@code nodeId},
 * 3 = dominar el elemento {@code roleIndex}, 4 = cambiar al siguiente elemento, 5 = evolucionar el rol,
 * 6 = elegir la forma {@code roleIndex} del Cambiaformas.
 */
public record RoleActionPayload(int action, int roleIndex, String nodeId) implements CustomPayload {
	public static final int SET_PRIMARY = 0;
	public static final int SET_SECONDARY = 1;
	public static final int UPGRADE_NODE = 2;
	public static final int CHOOSE_ELEMENT = 3;
	public static final int CYCLE_ELEMENT = 4;
	public static final int EVOLVE = 5;
	public static final int SELECT_FORM = 6;
	/** Equipar la activa {@code nodeId} en la ranura 1 / 2 de su rol. */
	public static final int EQUIP_1 = 7;
	public static final int EQUIP_2 = 8;

	public static final Id<RoleActionPayload> ID = new Id<>(Aptitudes.id("role_action"));
	public static final PacketCodec<RegistryByteBuf, RoleActionPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, RoleActionPayload::action,
			PacketCodecs.VAR_INT, RoleActionPayload::roleIndex,
			PacketCodecs.string(64), RoleActionPayload::nodeId,
			RoleActionPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
