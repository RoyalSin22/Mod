package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente propio: muertes por forma, formas desbloqueadas y forma elegida (pantalla de roles). */
public record FormSyncPayload(int[] kills, int unlockedMask, int selected) implements CustomPayload {
	public static final Id<FormSyncPayload> ID = new Id<>(Aptitudes.id("form_sync"));
	public static final PacketCodec<PacketByteBuf, FormSyncPayload> CODEC = PacketCodec.of(FormSyncPayload::write, FormSyncPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(kills.length);
		for (int k : kills) buf.writeVarInt(k);
		buf.writeVarInt(unlockedMask);
		buf.writeVarInt(selected);
	}

	private static FormSyncPayload read(PacketByteBuf buf) {
		int n = buf.readVarInt();
		if (n < 0 || n > 64) throw new IllegalArgumentException("Longitud inválida: " + n);
		int[] kills = new int[n];
		for (int i = 0; i < n; i++) kills[i] = buf.readVarInt();
		return new FormSyncPayload(kills, buf.readVarInt(), buf.readVarInt());
	}

	public boolean unlocked(int index) {
		return (unlockedMask & (1 << index)) != 0;
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
