package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente (v34): fase del Eclipse ({@code EclipseRules.Phase#ordinal()}), intensidad visual 0–1 y
 * progreso del evento 0–1.
 * Fase 0 = sin eclipse (también se envía a quien está fuera del Mundo Normal).
 */
public record EclipsePayload(int phase, float intensity, float progress) implements CustomPayload {
	public static final Id<EclipsePayload> ID = new Id<>(Aptitudes.id("eclipse"));
	public static final PacketCodec<RegistryByteBuf, EclipsePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, EclipsePayload::phase,
			PacketCodecs.FLOAT, EclipsePayload::intensity,
			PacketCodecs.FLOAT, EclipsePayload::progress,
			EclipsePayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
