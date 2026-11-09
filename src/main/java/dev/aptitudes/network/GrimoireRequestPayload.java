package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** v61, cliente → servidor: pide el estado del grimorio al abrirlo. Sin datos; el servidor limita la frecuencia. */
public record GrimoireRequestPayload() implements CustomPayload {
	public static final Id<GrimoireRequestPayload> ID = new Id<>(Aptitudes.id("grimoire_request"));
	public static final PacketCodec<PacketByteBuf, GrimoireRequestPayload> CODEC =
			PacketCodec.of((payload, buf) -> { }, buf -> new GrimoireRequestPayload());

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
