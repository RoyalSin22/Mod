package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor → cliente: estado de la mecánica de los roles activos para el HUD.
 * Cada entrada: nodo de la mecánica, valor, máximo (0 = estado con nombre) y
 * {@code label} (clave de texto opcional, p. ej. la postura del Monje).
 */
public record MechanicPayload(List<Entry> entries) implements CustomPayload {
	public record Entry(String node, int value, int max, String label) {}

	public static final Id<MechanicPayload> ID = new Id<>(Aptitudes.id("mechanic"));
	public static final PacketCodec<PacketByteBuf, MechanicPayload> CODEC =
			PacketCodec.of(MechanicPayload::write, MechanicPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(entries.size());
		for (Entry e : entries) {
			buf.writeString(e.node(), 64);
			buf.writeVarInt(e.value());
			buf.writeVarInt(e.max());
			buf.writeString(e.label(), 96);
		}
	}

	private static MechanicPayload read(PacketByteBuf buf) {
		int count = buf.readVarInt();
		if (count < 0 || count > 4) throw new IllegalArgumentException("Entradas inválidas: " + count);
		List<Entry> out = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			out.add(new Entry(buf.readString(64), buf.readVarInt(), buf.readVarInt(), buf.readString(96)));
		}
		return new MechanicPayload(out);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
