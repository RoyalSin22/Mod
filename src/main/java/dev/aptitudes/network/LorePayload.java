package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.guide.LoreState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sincroniza el estado de lore desbloqueado desde servidor a cliente (v67, T7a).
 *
 * @param loreState estado de desbloqueos de lore
 */
public record LorePayload(LoreState loreState) implements CustomPayload {
	public static final Id<LorePayload> ID = new Id<>(Aptitudes.id("lore_sync"));
	public static final PacketCodec<PacketByteBuf, LorePayload> CODEC = PacketCodec.of(LorePayload::write, LorePayload::read);

	public LorePayload {
		loreState = loreState != null ? loreState : new LoreState();
	}

	private void write(PacketByteBuf buf) {
		Map<String, Boolean> unlocked = loreState.unlocked();
		buf.writeVarInt(unlocked.size());
		unlocked.forEach((id, state) -> {
			buf.writeString(id, 64);
			buf.writeBoolean(state);
		});
	}

	private static LorePayload read(PacketByteBuf buf) {
		int size = buf.readVarInt();
		if (size < 0 || size > 256) throw new IllegalArgumentException("Lore demasiado grande: " + size);
		Map<String, Boolean> unlocked = new TreeMap<>();
		for (int i = 0; i < size; i++) {
			unlocked.put(buf.readString(64), buf.readBoolean());
		}
		return new LorePayload(new LoreState(unlocked));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
