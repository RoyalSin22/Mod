package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import java.util.ArrayList;
import java.util.List;

public record ContractViewPayload(String rank, int reputation, List<Row> offers, List<Row> active) implements CustomPayload {
	public record Row(int index,int kind,int difficulty,int target,int goal,int progress,int emeralds,int fragments,int roleXp,boolean voucher,boolean emblem) {}
	public static final Id<ContractViewPayload> ID=new Id<>(Aptitudes.id("contract_view"));
	public static final PacketCodec<PacketByteBuf,ContractViewPayload> CODEC=PacketCodec.of((p,b)->{b.writeString(p.rank,16);b.writeVarInt(p.reputation);write(b,p.offers);write(b,p.active);},ContractViewPayload::read);
	private static void write(PacketByteBuf b,List<Row> rows){b.writeVarInt(Math.min(3,rows.size()));for(Row r:rows){b.writeVarInt(r.index);b.writeVarInt(r.kind);b.writeVarInt(r.difficulty);b.writeVarInt(r.target);b.writeVarInt(r.goal);b.writeVarInt(r.progress);b.writeVarInt(r.emeralds);b.writeVarInt(r.fragments);b.writeVarInt(r.roleXp);b.writeBoolean(r.voucher);b.writeBoolean(r.emblem);}}
	private static List<Row> readRows(PacketByteBuf b){int n=Math.max(0,Math.min(3,b.readVarInt()));List<Row> rows=new ArrayList<>(n);for(int i=0;i<n;i++)rows.add(new Row(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean()));return rows;}
	private static ContractViewPayload read(PacketByteBuf b){return new ContractViewPayload(b.readString(16),b.readVarInt(),readRows(b),readRows(b));}
	@Override public Id<? extends CustomPayload> getId(){return ID;}
}
