package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor → cliente (F4): pedidos visibles para el jugador. El cliente solo muestra estos datos y decide qué botón
 * activar; el servidor vuelve a validar cada acción.
 */
public record OrderViewPayload(List<Row> rows) implements CustomPayload {
	/** Un pedido. {@code mine}: lo publicó este jugador. {@code joined}: este jugador ya ha entregado en él. */
	public record Row(long id, String item, int count, int tier, int delivered, int claimed, boolean closed,
					  boolean mine, boolean joined) {}

	public static final int MAX_ROWS = 12;
	public static final Id<OrderViewPayload> ID = new Id<>(Aptitudes.id("order_view"));
	public static final PacketCodec<PacketByteBuf, OrderViewPayload> CODEC = PacketCodec.of(OrderViewPayload::write,
			OrderViewPayload::read);

	private static void write(OrderViewPayload p, PacketByteBuf b) {
		int n = Math.min(MAX_ROWS, p.rows.size());
		b.writeVarInt(n);
		for (int i = 0; i < n; i++) {
			Row r = p.rows.get(i);
			b.writeVarLong(r.id);
			b.writeString(r.item, 128);
			b.writeVarInt(r.count);
			b.writeVarInt(r.tier);
			b.writeVarInt(r.delivered);
			b.writeVarInt(r.claimed);
			b.writeBoolean(r.closed);
			b.writeBoolean(r.mine);
			b.writeBoolean(r.joined);
		}
	}

	private static OrderViewPayload read(PacketByteBuf b) {
		int n = Math.min(MAX_ROWS, b.readVarInt());
		List<Row> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Row(b.readVarLong(), b.readString(128), b.readVarInt(), b.readVarInt(), b.readVarInt(),
					b.readVarInt(), b.readBoolean(), b.readBoolean(), b.readBoolean()));
		}
		return new OrderViewPayload(rows);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
