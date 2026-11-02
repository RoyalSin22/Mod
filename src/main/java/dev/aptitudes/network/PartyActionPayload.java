package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.UUID;

/** Petición de grupo; su emisor y objetivo se vuelven a validar en el servidor. */
public record PartyActionPayload(int action, UUID target, int value) implements CustomPayload {
	public static final int REQUEST_SYNC = 0;
	public static final int INVITE = 1;
	public static final int LEAVE = 2;
	public static final int KICK = 3;
	public static final int TOGGLE_FRIENDLY_FIRE = 4;
	public static final int SET_SHARE = 5;
	public static final Id<PartyActionPayload> ID = new Id<>(Aptitudes.id("party_action"));
	public static final PacketCodec<PacketByteBuf, PartyActionPayload> CODEC =
			PacketCodec.of(PartyActionPayload::write, PartyActionPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(action);
		buf.writeBoolean(target != null);
		if (target != null) buf.writeUuid(target);
		buf.writeVarInt(value);
	}

	private static PartyActionPayload read(PacketByteBuf buf) {
		int action = buf.readVarInt();
		UUID target = buf.readBoolean() ? buf.readUuid() : null;
		int value = buf.readVarInt();
		return new PartyActionPayload(action, target, value);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
