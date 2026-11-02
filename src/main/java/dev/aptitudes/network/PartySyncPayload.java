package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Vista del grupo y de jugadores conectados; nunca contiene autoridad de mutación. */
public record PartySyncPayload(boolean grouped, boolean leader, UUID leaderId, boolean friendlyFire,
							   int sharePercent, List<Member> members, List<OnlinePlayer> onlinePlayers) implements CustomPayload {
	public static final Id<PartySyncPayload> ID = new Id<>(Aptitudes.id("party_sync"));
	public static final PacketCodec<PacketByteBuf, PartySyncPayload> CODEC =
			PacketCodec.of(PartySyncPayload::write, PartySyncPayload::read);

	public record Member(UUID id, String name, int role, float health, boolean online, boolean sameDimension,
						 boolean leader) {}
	public record OnlinePlayer(UUID id, String name, boolean grouped) {}

	private void write(PacketByteBuf buf) {
		buf.writeBoolean(grouped);
		buf.writeBoolean(leader);
		buf.writeBoolean(leaderId != null);
		if (leaderId != null) buf.writeUuid(leaderId);
		buf.writeBoolean(friendlyFire);
		buf.writeVarInt(sharePercent);
		buf.writeVarInt(Math.min(5, members.size()));
		for (Member member : members.stream().limit(5).toList()) {
			buf.writeUuid(member.id());
			buf.writeString(member.name(), 16);
			buf.writeVarInt(member.role());
			float health = Float.isFinite(member.health()) ? Math.max(0.0f, Math.min(1.0f, member.health())) : 0.0f;
			buf.writeFloat(health);
			buf.writeBoolean(member.online());
			buf.writeBoolean(member.sameDimension());
			buf.writeBoolean(member.leader());
		}
		buf.writeVarInt(Math.min(128, onlinePlayers.size()));
		for (OnlinePlayer player : onlinePlayers.stream().limit(128).toList()) {
			buf.writeUuid(player.id());
			buf.writeString(player.name(), 16);
			buf.writeBoolean(player.grouped());
		}
	}

	private static PartySyncPayload read(PacketByteBuf buf) {
		boolean grouped = buf.readBoolean();
		boolean leader = buf.readBoolean();
		UUID leaderId = buf.readBoolean() ? buf.readUuid() : null;
		boolean friendlyFire = buf.readBoolean();
		int sharePercent = Math.max(0, Math.min(50, buf.readVarInt()));
		int memberCount = buf.readVarInt();
		if (memberCount < 0 || memberCount > 5) throw new IllegalArgumentException("Miembros inválidos: " + memberCount);
		List<Member> members = new ArrayList<>(memberCount);
		for (int i = 0; i < memberCount; i++) {
			UUID id = buf.readUuid();
			String name = buf.readString(16);
			int role = buf.readVarInt();
			float rawHealth = buf.readFloat();
			float health = Float.isFinite(rawHealth) ? Math.max(0.0f, Math.min(1.0f, rawHealth)) : 0.0f;
			members.add(new Member(id, name, role, health, buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));
		}
		int onlineCount = buf.readVarInt();
		if (onlineCount < 0 || onlineCount > 128) throw new IllegalArgumentException("Lista de jugadores inválida: " + onlineCount);
		List<OnlinePlayer> online = new ArrayList<>(onlineCount);
		for (int i = 0; i < onlineCount; i++) online.add(new OnlinePlayer(buf.readUuid(), buf.readString(16), buf.readBoolean()));
		return new PartySyncPayload(grouped, leader, leaderId, friendlyFire, sharePercent, members, online);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
