package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: el jugador ha descansado en una Hoguera; abre la pantalla de subida de nivel. */
public record OpenBonfirePayload() implements CustomPayload {
	public static final Id<OpenBonfirePayload> ID = new Id<>(Aptitudes.id("open_bonfire"));
	public static final PacketCodec<RegistryByteBuf, OpenBonfirePayload> CODEC = PacketCodec.unit(new OpenBonfirePayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
