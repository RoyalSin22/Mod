package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Cliente → servidor (F4): acción sobre los pedidos. {@code tier} solo se usa al publicar. */
public record OrderActionPayload(int action, long id, int tier) implements CustomPayload {
	public static final int PUBLISH = 0, DELIVER = 1, CLAIM = 2, CANCEL = 3, REQUEST = 4;
	public static final Id<OrderActionPayload> ID = new Id<>(Aptitudes.id("order_action"));
	public static final PacketCodec<PacketByteBuf, OrderActionPayload> CODEC = PacketCodec.of(
			(p, b) -> {
				b.writeVarInt(p.action);
				b.writeVarLong(p.id);
				b.writeVarInt(p.tier);
			},
			b -> new OrderActionPayload(b.readVarInt(), b.readVarLong(), b.readVarInt()));

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
