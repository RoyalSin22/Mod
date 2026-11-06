package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.logic.ThreatRules;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Aviso exclusivamente S2C; -1 borra el aviso al desactivarlo o salir del Mundo Normal. */
public record ThreatPayload(int ring) implements CustomPayload {
	public static final Id<ThreatPayload> ID = new Id<>(Aptitudes.id("threat"));
	public static final PacketCodec<PacketByteBuf, ThreatPayload> CODEC = PacketCodec.of(
			(p, buf) -> buf.writeVarInt(p.ring()), buf -> new ThreatPayload(buf.readVarInt()));

	public ThreatPayload { ring = Math.max(-1, Math.min(ThreatRules.MAX_RING, ring)); }

	@Override public Id<? extends CustomPayload> getId() { return ID; }
}
