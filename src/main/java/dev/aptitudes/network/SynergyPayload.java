package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Servidor → cliente (v65 C5): una sinergia (0 = Y1 … 4 = Y5) acaba de activarse; el HUD la ilumina. */
public record SynergyPayload(int index) implements CustomPayload {
	public static final Id<SynergyPayload> ID = new Id<>(Aptitudes.id("synergy"));
	public static final PacketCodec<PacketByteBuf, SynergyPayload> CODEC = PacketCodec.of(SynergyPayload::write,
			SynergyPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(index);
	}

	private static SynergyPayload read(PacketByteBuf buf) {
		return new SynergyPayload(Math.max(0, Math.min(4, buf.readVarInt())));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
