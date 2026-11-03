package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor → cliente (v30): vista del tablón de contratos. {@code offers}: las 3 ofertas del día ({@code index} =
 * número de oferta, {@code taken} = ya aceptada hoy). {@code active}: contratos en curso ({@code index} = ranura).
 * El cliente solo muestra estos datos; no calcula progreso ni recompensas.
 */
public record ContractViewPayload(int rank, int reputation, int activeLimit, List<Row> offers, List<Row> active)
		implements CustomPayload {
	public record Row(int index, int kind, int difficulty, int target, int goal, int progress, int emeralds,
					  int fragments, int roleXp, int reputation, boolean voucher, boolean emblem, boolean taken) {}

	private static final int MAX_ROWS = 3;
	public static final Id<ContractViewPayload> ID = new Id<>(Aptitudes.id("contract_view"));
	public static final PacketCodec<PacketByteBuf, ContractViewPayload> CODEC = PacketCodec.of(ContractViewPayload::write,
			ContractViewPayload::read);

	private static void write(ContractViewPayload p, PacketByteBuf b) {
		b.writeVarInt(p.rank);
		b.writeVarInt(p.reputation);
		b.writeVarInt(p.activeLimit);
		writeRows(b, p.offers);
		writeRows(b, p.active);
	}

	private static void writeRows(PacketByteBuf b, List<Row> rows) {
		int n = Math.min(MAX_ROWS, rows.size());
		b.writeVarInt(n);
		for (int i = 0; i < n; i++) {
			Row r = rows.get(i);
			b.writeVarInt(r.index);
			b.writeVarInt(r.kind);
			b.writeVarInt(r.difficulty);
			b.writeVarInt(r.target);
			b.writeVarInt(r.goal);
			b.writeVarInt(r.progress);
			b.writeVarInt(r.emeralds);
			b.writeVarInt(r.fragments);
			b.writeVarInt(r.roleXp);
			b.writeVarInt(r.reputation);
			b.writeBoolean(r.voucher);
			b.writeBoolean(r.emblem);
			b.writeBoolean(r.taken);
		}
	}

	private static List<Row> readRows(PacketByteBuf b) {
		int n = Math.max(0, Math.min(MAX_ROWS, b.readVarInt()));
		List<Row> rows = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			rows.add(new Row(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(),
					b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean(), b.readBoolean()));
		}
		return rows;
	}

	private static ContractViewPayload read(PacketByteBuf b) {
		return new ContractViewPayload(b.readVarInt(), b.readVarInt(), b.readVarInt(), readRows(b), readRows(b));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
