package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Role;
import dev.aptitudes.role.datapack.RoleTalentCatalog;
import dev.aptitudes.role.datapack.TalentProgressRules;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.util.HashMap;
import java.util.Map;

public record TalentCatalogPayload(long revision, Map<String, Entry> entries) implements CustomPayload {
	public static final TalentCatalogPayload EMPTY = new TalentCatalogPayload(0, Map.of());
	public static final Id<TalentCatalogPayload> ID = new Id<>(Aptitudes.id("talent_catalog"));
	public static final PacketCodec<PacketByteBuf, TalentCatalogPayload> CODEC =
			PacketCodec.of(TalentCatalogPayload::write, TalentCatalogPayload::read);

	public record Entry(Role role, String prerequisite, int level, int ranks, int rank) {
		public int levelForNextRank() {
			return TalentProgressRules.levelForRank(level, Math.min(rank + 1, ranks));
		}
	}

	public TalentCatalogPayload {
		entries = Map.copyOf(entries);
	}

	public int spent(Role role) {
		return entries.values().stream().filter(entry -> entry.role() == role).mapToInt(Entry::rank).sum();
	}

	private void write(PacketByteBuf buf) {
		buf.writeVarLong(revision);
		buf.writeVarInt(entries.size());
		entries.forEach((id, entry) -> {
			buf.writeString(id, RoleTalentCatalog.MAX_ID_LENGTH);
			buf.writeVarInt(entry.role().ordinal());
			buf.writeString(entry.prerequisite(), RoleTalentCatalog.MAX_ID_LENGTH);
			buf.writeVarInt(entry.level());
			buf.writeVarInt(entry.ranks());
			buf.writeVarInt(entry.rank());
		});
	}

	private static TalentCatalogPayload read(PacketByteBuf buf) {
		long revision = buf.readVarLong();
		int count = buf.readVarInt();
		if (count < 0 || count > RoleTalentCatalog.MAX_NODES) throw new IllegalArgumentException("Invalid talent count");
		Map<String, Entry> entries = new HashMap<>();
		for (int i = 0; i < count; i++) {
			String id = buf.readString(RoleTalentCatalog.MAX_ID_LENGTH);
			Role role = Role.byIndex(buf.readVarInt());
			String prerequisite = buf.readString(RoleTalentCatalog.MAX_ID_LENGTH);
			int level = buf.readVarInt();
			int ranks = buf.readVarInt();
			int rank = buf.readVarInt();
			if (role == null || level < 1 || level > 50 || ranks < 1
					|| ranks > dev.aptitudes.role.RoleNode.MAX_RANK || rank < 0 || rank > ranks) {
				throw new IllegalArgumentException("Invalid talent entry");
			}
			entries.put(id, new Entry(role, prerequisite, level, ranks, rank));
		}
		return new TalentCatalogPayload(revision, entries);
	}

	@Override
	public Id<TalentCatalogPayload> getId() {
		return ID;
	}
}
