package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ForgeActionPayload(int syncId, int action, long revision) implements CustomPayload {
	public static final int OPEN_TEMPER = 0;
	public static final int SELECT_RESERVE = 1;
	public static final int SELECT_IMPETUS = 2;
	public static final int CONFIRM = 3;
	public static final int OPEN_FLASKS = 4;
	public static final int CONFIRM_FLASKS = 5;
	public static final Id<ForgeActionPayload> ID = new Id<>(Aptitudes.id("forge_action"));
	public static final PacketCodec<PacketByteBuf, ForgeActionPayload> CODEC =
			PacketCodec.of(ForgeActionPayload::write, ForgeActionPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(syncId);
		buf.writeVarInt(action);
		buf.writeVarLong(revision);
	}

	private static ForgeActionPayload read(PacketByteBuf buf) {
		return new ForgeActionPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarLong());
	}

	@Override
	public Id<ForgeActionPayload> getId() {
		return ID;
	}
}
