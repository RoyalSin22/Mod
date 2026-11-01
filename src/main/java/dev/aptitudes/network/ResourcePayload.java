package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: recurso de rol del principal y del secundario (0-100). */
public record ResourcePayload(float primary, float secondary) implements CustomPayload {
	public static final Id<ResourcePayload> ID = new Id<>(Aptitudes.id("resources"));
	public static final PacketCodec<RegistryByteBuf, ResourcePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.FLOAT, ResourcePayload::primary,
			PacketCodecs.FLOAT, ResourcePayload::secondary,
			ResourcePayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
