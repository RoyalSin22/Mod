package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.ArrayList;
import java.util.List;

/** Instantánea S2C de las barras de campeón visibles para un jugador. */
public record BossHudPayload(List<Entry> entries, int total) implements CustomPayload {
	public static final int MAX_ENTRIES = 4;
	public static final BossHudPayload EMPTY = new BossHudPayload(List.of(), 0);
	public static final Id<BossHudPayload> ID = new Id<>(Aptitudes.id("boss_hud"));
	public static final PacketCodec<RegistryByteBuf, BossHudPayload> CODEC = PacketCodec.of(
			BossHudPayload::write, BossHudPayload::read);

	public record Entry(Text name, float health, int phase) {
		public Entry {
			health = Float.isFinite(health) ? Math.clamp(health, 0f, 1f) : 0f;
			phase = Math.clamp(phase, 0, 3);
		}
	}

	public BossHudPayload {
		if (entries.size() > MAX_ENTRIES || total < entries.size()) {
			throw new IllegalArgumentException("Cantidad de jefes inválida");
		}
		entries = List.copyOf(entries);
	}

	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(total);
		buf.writeVarInt(entries.size());
		for (Entry entry : entries) {
			TextCodecs.REGISTRY_PACKET_CODEC.encode(buf, entry.name());
			buf.writeFloat(entry.health());
			buf.writeByte(entry.phase());
		}
	}

	private static BossHudPayload read(RegistryByteBuf buf) {
		int total = buf.readVarInt();
		int count = buf.readVarInt();
		if (count < 0 || count > MAX_ENTRIES || total < count) {
			throw new IllegalArgumentException("Cantidad de jefes inválida");
		}
		List<Entry> entries = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			entries.add(new Entry(TextCodecs.REGISTRY_PACKET_CODEC.decode(buf), buf.readFloat(), buf.readUnsignedByte()));
		}
		return new BossHudPayload(entries, total);
	}

	@Override
	public Id<BossHudPayload> getId() {
		return ID;
	}
}
