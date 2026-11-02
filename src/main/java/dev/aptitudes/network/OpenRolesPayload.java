package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: ritual de renacimiento completado; abre la pantalla de roles. */
public record OpenRolesPayload() implements CustomPayload {
	public static final Id<OpenRolesPayload> ID = new Id<>(Aptitudes.id("open_roles"));
	public static final PacketCodec<RegistryByteBuf, OpenRolesPayload> CODEC = PacketCodec.unit(new OpenRolesPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
