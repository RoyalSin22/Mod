package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

/** Servidor 	 cliente: remate cosmético de un lanzamiento confirmado. */
public record CastAnimationPayload(int entityId, UUID caster, int category, int kind, int color, float yaw)
		implements CustomPayload {
	public static final Id<CastAnimationPayload> ID = new Id<>(Aptitudes.id("cast_animation"));
	public static final PacketCodec<RegistryByteBuf, CastAnimationPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, CastAnimationPayload::entityId,
			Uuids.PACKET_CODEC, CastAnimationPayload::caster,
			PacketCodecs.VAR_INT, CastAnimationPayload::category,
			PacketCodecs.VAR_INT, CastAnimationPayload::kind,
			PacketCodecs.INTEGER, CastAnimationPayload::color,
			PacketCodecs.FLOAT, CastAnimationPayload::yaw,
			CastAnimationPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
