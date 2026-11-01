package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * Servidor → todos los clientes: el jugador {@code player} se muestra como la
 * entidad {@code typeId} ("" = forma normal). Así los demás le ven transformado.
 */
public record MorphPayload(UUID player, String typeId) implements CustomPayload {
	public static final Id<MorphPayload> ID = new Id<>(Aptitudes.id("morph"));
	public static final PacketCodec<RegistryByteBuf, MorphPayload> CODEC = PacketCodec.tuple(
			Uuids.PACKET_CODEC, MorphPayload::player,
			PacketCodecs.string(128), MorphPayload::typeId,
			MorphPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
