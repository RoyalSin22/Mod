package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ContractActionPayload(int action, int offer) implements CustomPayload {
	public static final int ACCEPT=0, ABANDON=1, DELIVER=2, CLAIM=3;
	public static final Id<ContractActionPayload> ID = new Id<>(Aptitudes.id("contract_action"));
	public static final PacketCodec<PacketByteBuf, ContractActionPayload> CODEC = PacketCodec.of((p,b)->{b.writeVarInt(p.action);b.writeVarInt(p.offer);}, b->new ContractActionPayload(b.readVarInt(),b.readVarInt()));
	@Override public Id<? extends CustomPayload> getId() { return ID; }
}
