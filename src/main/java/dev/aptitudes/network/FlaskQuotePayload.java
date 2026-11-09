package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.souls.FlaskRules;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Oferta de un solo uso. El servidor calcula sus requisitos con las ranuras reales. */
public record FlaskQuotePayload(int syncId, long revision, int limit) implements CustomPayload {
	public static final Id<FlaskQuotePayload> ID = new Id<>(Aptitudes.id("flask_quote"));
	public static final PacketCodec<RegistryByteBuf, FlaskQuotePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, FlaskQuotePayload::syncId,
			PacketCodecs.VAR_LONG, FlaskQuotePayload::revision,
			PacketCodecs.VAR_INT, FlaskQuotePayload::limit,
			FlaskQuotePayload::new);

	public FlaskQuotePayload {
		limit = FlaskRules.rank(limit);
	}

	@Override
	public Id<FlaskQuotePayload> getId() {
		return ID;
	}
}
