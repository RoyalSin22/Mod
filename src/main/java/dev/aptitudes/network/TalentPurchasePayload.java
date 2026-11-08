package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.datapack.RoleTalentCatalog;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record TalentPurchasePayload(long revision, String nodeId, int expectedRank) implements CustomPayload {
	public static final Id<TalentPurchasePayload> ID = new Id<>(Aptitudes.id("talent_purchase"));
	public static final PacketCodec<RegistryByteBuf, TalentPurchasePayload> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_LONG, TalentPurchasePayload::revision,
			PacketCodecs.string(RoleTalentCatalog.MAX_ID_LENGTH), TalentPurchasePayload::nodeId,
			PacketCodecs.VAR_INT, TalentPurchasePayload::expectedRank,
			TalentPurchasePayload::new);

	@Override
	public Id<TalentPurchasePayload> getId() {
		return ID;
	}
}
