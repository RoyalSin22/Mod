package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Cliente → servidor: el jugador pulsa la tecla de accesorios. */
public record OpenAccessoriesPayload() implements CustomPayload {
	public static final Id<OpenAccessoriesPayload> ID = new Id<>(Aptitudes.id("open_accessories"));
	public static final PacketCodec<RegistryByteBuf, OpenAccessoriesPayload> CODEC = PacketCodec.unit(new OpenAccessoriesPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
