package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente (v40): abrir la pantalla de configuración (comando {@code /aptitudes config}). */
public record OpenConfigPayload() implements CustomPayload {
	public static final Id<OpenConfigPayload> ID = new Id<>(Aptitudes.id("open_config"));
	public static final PacketCodec<RegistryByteBuf, OpenConfigPayload> CODEC = PacketCodec.unit(new OpenConfigPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
