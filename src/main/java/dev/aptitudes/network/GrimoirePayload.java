package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.guide.BestiaryRules;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.Map;
import java.util.TreeMap;

/**
 * v61, solo servidor → cliente: bestiario del jugador y recuento de logros para el grimorio.
 * El cliente lo muestra; nunca lo devuelve ni lo modifica.
 */
public record GrimoirePayload(Map<String, Integer> kills, int achievementsDone, int achievementsTotal) implements CustomPayload {
	public static final Id<GrimoirePayload> ID = new Id<>(Aptitudes.id("grimoire"));
	public static final PacketCodec<PacketByteBuf, GrimoirePayload> CODEC = PacketCodec.of(GrimoirePayload::write, GrimoirePayload::read);

	public GrimoirePayload {
		kills = Map.copyOf(BestiaryRules.sanitize(kills));
		achievementsTotal = Math.max(0, Math.min(1024, achievementsTotal));
		achievementsDone = Math.max(0, Math.min(achievementsTotal, achievementsDone));
	}

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(kills.size());
		kills.forEach((id, count) -> {
			buf.writeString(id, 64);
			buf.writeVarInt(count);
		});
		buf.writeVarInt(achievementsDone);
		buf.writeVarInt(achievementsTotal);
	}

	private static GrimoirePayload read(PacketByteBuf buf) {
		int size = buf.readVarInt();
		if (size < 0 || size > BestiaryRules.MAX_ENTRIES) throw new IllegalArgumentException("Bestiario demasiado grande: " + size);
		Map<String, Integer> kills = new TreeMap<>();
		for (int i = 0; i < size; i++) kills.put(buf.readString(64), buf.readVarInt());
		return new GrimoirePayload(kills, buf.readVarInt(), buf.readVarInt());
	}

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }
}
