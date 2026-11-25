package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/** C7 (S5), cliente → servidor: marcar como útil la nota {@code id}. El servidor valida distancia, autor y repetición. */
public record GroundMessageRatePayload(UUID id) implements CustomPayload {
	public static final Id<GroundMessageRatePayload> ID = new Id<>(Aptitudes.id("ground_message_rate"));
	public static final PacketCodec<PacketByteBuf, GroundMessageRatePayload> CODEC = PacketCodec.of(
			(p, buf) -> buf.writeUuid(p.id()), buf -> new GroundMessageRatePayload(buf.readUuid()));

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
