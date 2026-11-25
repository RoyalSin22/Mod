package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

/**
 * C7 (S5), cliente → servidor: escribir una nota en {@code pos} (el bloque de aire sobre el suelo). El servidor lo
 * valida todo: tiza en la mano, distancia, suelo, catálogo y límites.
 */
public record GroundMessageWritePayload(BlockPos pos, String template, String word) implements CustomPayload {
	public static final Id<GroundMessageWritePayload> ID = new Id<>(Aptitudes.id("ground_message_write"));
	public static final PacketCodec<PacketByteBuf, GroundMessageWritePayload> CODEC = PacketCodec.of(
			(p, buf) -> {
				buf.writeBlockPos(p.pos());
				buf.writeString(p.template(), 32);
				buf.writeString(p.word(), 32);
			},
			buf -> new GroundMessageWritePayload(buf.readBlockPos(), buf.readString(32), buf.readString(32)));

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
