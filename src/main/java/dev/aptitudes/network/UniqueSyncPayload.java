package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Servidor → cliente: dueño de cada habilidad única ("" = libre), progreso propio
 * en cada gesta y la habilidad que posee el jugador (-1 = ninguna).
 */
public record UniqueSyncPayload(String[] holders, int[] progress, int ownSkill) implements CustomPayload {
	public static final Id<UniqueSyncPayload> ID = new Id<>(Aptitudes.id("unique_sync"));
	public static final PacketCodec<PacketByteBuf, UniqueSyncPayload> CODEC =
			PacketCodec.of(UniqueSyncPayload::write, UniqueSyncPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(holders.length);
		for (String h : holders) buf.writeString(h, 64);
		buf.writeVarInt(progress.length);
		for (int p : progress) buf.writeVarInt(p);
		buf.writeVarInt(ownSkill);
	}

	private static UniqueSyncPayload read(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > 64) throw new IllegalArgumentException("Longitud inválida: " + n);
		String[] holders = new String[n];
		for (int i = 0; i < n; i++) holders[i] = buf.readString(64);
		int m = buf.readVarInt();
		if (m < 0 || m > 64) throw new IllegalArgumentException("Longitud inválida: " + m);
		int[] progress = new int[m];
		for (int i = 0; i < m; i++) progress[i] = buf.readVarInt();
		return new UniqueSyncPayload(holders, progress, buf.readVarInt());
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
