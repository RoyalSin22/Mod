package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor → cliente (v46.1): estado de las piedras angulares activas del jugador, para el indicador del HUD.
 * {@code template} = {@code KeystoneRules.Template#ordinal()}; {@code progress} 0–1 (1 = lista); {@code armed} =
 * efecto preparado (Espectro tras esquivar, Sobrecarga en la habilidad gratuita).
 */
public record KeystonePayload(List<Entry> entries) implements CustomPayload {
	public static final Id<KeystonePayload> ID = new Id<>(Aptitudes.id("keystones"));
	/** Como mucho una piedra angular por rol activo (principal y secundario). */
	private static final int MAX_ENTRIES = 4;
	public static final PacketCodec<PacketByteBuf, KeystonePayload> CODEC = PacketCodec.of(KeystonePayload::write,
			KeystonePayload::read);

	public record Entry(int template, float progress, boolean armed) {}

	private void write(PacketByteBuf buf) {
		int n = Math.min(MAX_ENTRIES, entries.size());
		buf.writeVarInt(n);
		for (int i = 0; i < n; i++) {
			Entry e = entries.get(i);
			buf.writeVarInt(e.template());
			buf.writeFloat(e.progress());
			buf.writeBoolean(e.armed());
		}
	}

	private static KeystonePayload read(PacketByteBuf buf) {
		int n = Math.max(0, Math.min(MAX_ENTRIES, buf.readVarInt()));
		List<Entry> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) list.add(new Entry(buf.readVarInt(), buf.readFloat(), buf.readBoolean()));
		return new KeystonePayload(List.copyOf(list));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
