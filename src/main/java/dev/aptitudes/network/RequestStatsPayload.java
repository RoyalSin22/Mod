package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Cliente → servidor: la pantalla de atributos pide estadísticas actualizadas (al abrirse y cada segundo). */
public record RequestStatsPayload() implements CustomPayload {
	public static final Id<RequestStatsPayload> ID = new Id<>(Aptitudes.id("request_stats"));
	public static final PacketCodec<RegistryByteBuf, RequestStatsPayload> CODEC = PacketCodec.unit(new RequestStatsPayload());

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
