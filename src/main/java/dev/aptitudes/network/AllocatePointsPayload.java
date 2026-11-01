package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Cliente → servidor: petición de asignar puntos. El servidor la trata como
 * NO confiable y valida índice, cantidad, puntos disponibles y máximo.
 */
public record AllocatePointsPayload(int attributeIndex, int amount) implements CustomPayload {
	public static final Id<AllocatePointsPayload> ID = new Id<>(Aptitudes.id("allocate_points"));
	public static final PacketCodec<RegistryByteBuf, AllocatePointsPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, AllocatePointsPayload::attributeIndex,
			PacketCodecs.VAR_INT, AllocatePointsPayload::amount,
			AllocatePointsPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
