package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente atacante: número de daño flotante en la posición indicada. */
public record DamageNumberPayload(double x, double y, double z, float amount, int color) implements CustomPayload {
	public static final Id<DamageNumberPayload> ID = new Id<>(Aptitudes.id("damage_number"));
	public static final PacketCodec<RegistryByteBuf, DamageNumberPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.DOUBLE, DamageNumberPayload::x,
			PacketCodecs.DOUBLE, DamageNumberPayload::y,
			PacketCodecs.DOUBLE, DamageNumberPayload::z,
			PacketCodecs.FLOAT, DamageNumberPayload::amount,
			PacketCodecs.INTEGER, DamageNumberPayload::color,
			DamageNumberPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
