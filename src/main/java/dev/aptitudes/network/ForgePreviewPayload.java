package dev.aptitudes.network;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.forge.ForgeRules.Affix;
import dev.aptitudes.forge.ForgeRules.Profile;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.Nullable;

public record ForgePreviewPayload(int syncId, long revision, Preview preview) implements CustomPayload {
	public enum Status { NO_WEAPON, WRONG_ROLE, SAME_AFFIX, NEEDS_REFORGE, MAX_RANK, MISSING, READY }

	public record Preview(Profile selected, @Nullable Affix current, @Nullable Affix result,
						  int trophies, int shards, Status status) {}

	public static final Id<ForgePreviewPayload> ID = new Id<>(Aptitudes.id("forge_preview"));
	public static final PacketCodec<PacketByteBuf, ForgePreviewPayload> CODEC =
			PacketCodec.of(ForgePreviewPayload::write, ForgePreviewPayload::read);

	private void write(PacketByteBuf buf) {
		buf.writeVarInt(syncId);
		buf.writeVarLong(revision);
		buf.writeEnumConstant(preview.selected());
		writeAffix(buf, preview.current());
		writeAffix(buf, preview.result());
		buf.writeVarInt(preview.trophies());
		buf.writeVarInt(preview.shards());
		buf.writeEnumConstant(preview.status());
	}

	private static ForgePreviewPayload read(PacketByteBuf buf) {
		int syncId = buf.readVarInt();
		long revision = buf.readVarLong();
		return new ForgePreviewPayload(syncId, revision, new Preview(buf.readEnumConstant(Profile.class),
				readAffix(buf), readAffix(buf), buf.readVarInt(), buf.readVarInt(), buf.readEnumConstant(Status.class)));
	}

	private static void writeAffix(PacketByteBuf buf, @Nullable Affix affix) {
		buf.writeBoolean(affix != null);
		if (affix != null) {
			buf.writeEnumConstant(affix.profile());
			buf.writeVarInt(affix.rank());
		}
	}

	@Nullable
	private static Affix readAffix(PacketByteBuf buf) {
		return buf.readBoolean() ? new Affix(buf.readEnumConstant(Profile.class), buf.readVarInt()) : null;
	}

	@Override
	public Id<ForgePreviewPayload> getId() {
		return ID;
	}
}
