package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente: energía actual y máxima (para el HUD). Se envía solo si cambia. */
public record EnergyPayload(float energy, float maxEnergy) implements CustomPayload {
	public static final Id<EnergyPayload> ID = new Id<>(Aptitudes.id("energy"));
	public static final PacketCodec<RegistryByteBuf, EnergyPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.FLOAT, EnergyPayload::energy,
			PacketCodecs.FLOAT, EnergyPayload::maxEnergy,
			EnergyPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
