package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * C7 (S5), servidor → cliente: notas en el suelo cercanas al jugador (lista completa; sustituye a la anterior). Solo
 * datos: plantilla y palabra van como claves del catálogo y el cliente las traduce.
 */
public record GroundMessageSyncPayload(List<Entry> notes) implements CustomPayload {
	/** Máximo de notas por paquete (con los topes de la configuración no se llega). */
	public static final int MAX_NOTES = 512;
	public static final Id<GroundMessageSyncPayload> ID = new Id<>(Aptitudes.id("ground_message_sync"));
	public static final PacketCodec<PacketByteBuf, GroundMessageSyncPayload> CODEC =
			PacketCodec.of(GroundMessageSyncPayload::write, GroundMessageSyncPayload::read);

	/** Una nota vista por el jugador: {@code author} vacío si el servidor oculta los nombres. */
	public record Entry(UUID id, BlockPos pos, String template, String word, String author, int ratings, boolean mine,
						boolean rated) {}

	private void write(PacketByteBuf buf) {
		int n = Math.min(MAX_NOTES, notes.size());
		buf.writeVarInt(n);
		for (int i = 0; i < n; i++) {
			Entry e = notes.get(i);
			buf.writeUuid(e.id());
			buf.writeBlockPos(e.pos());
			buf.writeString(e.template(), 32);
			buf.writeString(e.word(), 32);
			buf.writeString(e.author(), 32);
			buf.writeVarInt(Math.max(0, e.ratings()));
			buf.writeBoolean(e.mine());
			buf.writeBoolean(e.rated());
		}
	}

	private static GroundMessageSyncPayload read(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > MAX_NOTES) throw new IllegalArgumentException("Demasiadas notas: " + n);
		List<Entry> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			out.add(new Entry(buf.readUuid(), buf.readBlockPos(), buf.readString(32), buf.readString(32),
					buf.readString(32), buf.readVarInt(), buf.readBoolean(), buf.readBoolean()));
		}
		return new GroundMessageSyncPayload(List.copyOf(out));
	}

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
